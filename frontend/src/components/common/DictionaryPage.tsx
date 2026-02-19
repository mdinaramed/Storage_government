import * as React from "react";
import AddIcon from "@mui/icons-material/Add";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import {
    Button,
    Chip,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    IconButton,
    InputLabel,
    Menu,
    MenuItem,
    Select,
    Stack,
    TextField,
} from "@mui/material";
import { DataGrid, type GridColDef, type GridRenderCellParams } from "@mui/x-data-grid";

import PageShell from "./PageShell";
import ToolbarCard from "./ToolbarCard";
import TableCard from "./TableCard";
import ConfirmDialog from "./ConfirmDialog";
import { useAppSnackbar } from "./AppSnackbar";
import type { EntityState, StateFilter } from "../../types/common";
import { toApiState } from "../../types/common";

type BaseRow = { id: number; name: string; state: EntityState };
type Mode = "UNIT" | "RESOURCE" | "CLIENT";
type ClientFields = { address?: string | null };

type Props<T extends BaseRow> = {
    mode: Mode;
    title: string;
    subtitle: string;
    columns: GridColDef<T>[];
    api: {
        list: (q?: string, state?: EntityState) => Promise<T[]>;
        create: (payload: { name: string } & Partial<ClientFields>) => Promise<T>;
        update: (id: number, payload: { name: string } & Partial<ClientFields>) => Promise<T>;
        archive: (id: number) => Promise<T>;
        activate: (id: number) => Promise<T>;
        remove: (id: number) => Promise<void>;
    };
};

function StatePill({ state }: { state: EntityState }) {
    const color = state === "ACTIVE" ? "success" : "default";
    return <Chip label={state} color={color} size="small" />;
}

export default function DictionaryPage<T extends BaseRow>(props: Props<T>) {
    const { notify } = useAppSnackbar();

    const [rows, setRows] = React.useState<T[]>([]);
    const [loading, setLoading] = React.useState(false);

    const [q, setQ] = React.useState("");
    const [stateFilter, setStateFilter] = React.useState<StateFilter>("ALL");
    const apiState = toApiState(stateFilter);

    const [openForm, setOpenForm] = React.useState(false);
    const [editing, setEditing] = React.useState<T | null>(null);

    const [name, setName] = React.useState("");
    const [address, setAddress] = React.useState("");

    const [menuAnchor, setMenuAnchor] = React.useState<HTMLElement | null>(null);
    const [menuRow, setMenuRow] = React.useState<T | null>(null);

    const [confirmOpen, setConfirmOpen] = React.useState(false);
    const [deleteRow, setDeleteRow] = React.useState<T | null>(null);

    const load = React.useCallback(async () => {
        setLoading(true);
        try {
            const data = await props.api.list(q.trim() || undefined, apiState);
            setRows(Array.isArray(data) ? data : []);
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Load failed", "error");
            setRows([]);
        } finally {
            setLoading(false);
        }
    }, [props.api, q, apiState, notify]);

    React.useEffect(() => {
        void load();
    }, [load]);

    const openCreate = () => {
        setEditing(null);
        setName("");
        setAddress("");
        setOpenForm(true);
    };

    const openEdit = (row: T) => {
        setEditing(row);
        setName(row.name ?? "");
        if (props.mode === "CLIENT") {
            const r = row as unknown as BaseRow & ClientFields;
            setAddress(r.address ?? "");
        } else {
            setAddress("");
        }
        setOpenForm(true);
    };

    const save = async () => {
        const n = name.trim();
        if (!n) {
            notify("Name is required", "warning");
            return;
        }

        try {
            const payload =
                props.mode === "CLIENT"
                    ? ({ name: n, address: address.trim() ? address.trim() : null } as { name: string } & ClientFields)
                    : ({ name: n } as { name: string });

            if (editing) {
                await props.api.update(editing.id, payload);
                notify("Updated", "success");
            } else {
                await props.api.create(payload);
                notify("Created", "success");
            }
            setOpenForm(false);
            await load();
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Save failed", "error");
        }
    };

    const openMenu = (evt: React.MouseEvent<HTMLElement>, row: T) => {
        setMenuAnchor(evt.currentTarget);
        setMenuRow(row);
    };

    const closeMenu = () => {
        setMenuAnchor(null);
        setMenuRow(null);
    };

    const doArchiveToggle = async () => {
        if (!menuRow) return;
        try {
            if (menuRow.state === "ACTIVE") {
                await props.api.archive(menuRow.id);
                notify("Archived", "success");
            } else {
                await props.api.activate(menuRow.id);
                notify("Activated", "success");
            }
            closeMenu();
            await load();
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Action failed", "error");
        }
    };

    const askDelete = () => {
        setDeleteRow(menuRow);
        setConfirmOpen(true);
        closeMenu();
    };

    const doDelete = async () => {
        const row = deleteRow;
        setConfirmOpen(false);
        setDeleteRow(null);
        if (!row) return;

        try {
            await props.api.remove(row.id);
            notify("Deleted", "success");
            await load();
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Delete failed", "error");
        }
    };

    const cols: GridColDef<T>[] = React.useMemo(() => {
        return [
            ...props.columns,
            {
                field: "_statePill",
                headerName: "State",
                width: 160,
                sortable: false,
                valueGetter: (_value, row) => row.state,
                renderCell: (p: GridRenderCellParams<T>) => <StatePill state={p.row.state} />,
            },
            {
                field: "_actions",
                headerName: "",
                width: 90,
                sortable: false,
                filterable: false,
                align: "right",
                headerAlign: "right",
                renderCell: (p: GridRenderCellParams<T>) => (
                    <IconButton onClick={(e) => openMenu(e, p.row)} size="small">
                        <MoreVertIcon fontSize="small" />
                    </IconButton>
                ),
            },
        ];
    }, [props.columns]);

    return (
        <PageShell
            title={props.title}
            subtitle={props.subtitle}
            actions={
                <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>
                    New
                </Button>
            }
        >
            <ToolbarCard
                left={
                    <>
                        <TextField
                            label="Search"
                            value={q}
                            onChange={(e) => setQ(e.target.value)}
                            sx={{ width: 360 }}
                        />

                        <FormControl sx={{ width: 220 }}>
                            <InputLabel>State</InputLabel>
                            <Select
                                label="State"
                                value={stateFilter}
                                onChange={(e) => setStateFilter(e.target.value as StateFilter)}
                            >
                                <MenuItem value="ALL">All</MenuItem>
                                <MenuItem value="ACTIVE">ACTIVE</MenuItem>
                                <MenuItem value="ARCHIVED">ARCHIVED</MenuItem>
                            </Select>
                        </FormControl>
                    </>
                }
                right={
                    <>
                        <Button variant="outlined" onClick={() => void load()}>
                            Refresh
                        </Button>
                        <Button variant="contained" onClick={() => void load()}>
                            Apply
                        </Button>
                    </>
                }
            />

            <TableCard height={660}>
                <DataGrid
                    rows={rows}
                    columns={cols}
                    loading={loading}
                    pageSizeOptions={[10, 25, 50, 100]}
                    initialState={{ pagination: { paginationModel: { pageSize: 10, page: 0 } } }}
                    disableRowSelectionOnClick
                />
            </TableCard>

            <Menu anchorEl={menuAnchor} open={!!menuAnchor} onClose={closeMenu}>
                <MenuItem
                    onClick={() => {
                        if (menuRow) openEdit(menuRow);
                        closeMenu();
                    }}
                >
                    Edit
                </MenuItem>
                <MenuItem onClick={() => void doArchiveToggle()}>
                    {menuRow?.state === "ACTIVE" ? "Archive" : "Activate"}
                </MenuItem>
                <MenuItem onClick={askDelete} sx={{ color: "error.main" }}>
                    Delete
                </MenuItem>
            </Menu>

            <ConfirmDialog
                open={confirmOpen}
                title="Delete"
                text="Are you sure you want to delete this item?"
                confirmText="Delete"
                onCancel={() => {
                    setConfirmOpen(false);
                    setDeleteRow(null);
                }}
                onConfirm={() => void doDelete()}
            />

            <Dialog open={openForm} onClose={() => setOpenForm(false)}>
                <DialogTitle sx={{ fontWeight: 900 }}>{editing ? "Edit" : "Create"}</DialogTitle>
                <DialogContent sx={{ pt: 2 }}>
                    <Stack spacing={2} sx={{ mt: 1 }}>
                        <TextField label="Name" value={name} onChange={(e) => setName(e.target.value)} fullWidth />
                        {props.mode === "CLIENT" ? (
                            <TextField
                                label="Address"
                                value={address}
                                onChange={(e) => setAddress(e.target.value)}
                                fullWidth
                            />
                        ) : null}
                    </Stack>
                </DialogContent>
                <DialogActions sx={{ p: 2 }}>
                    <Button onClick={() => setOpenForm(false)} variant="outlined">
                        Cancel
                    </Button>
                    <Button onClick={() => void save()} variant="contained">
                        Save
                    </Button>
                </DialogActions>
            </Dialog>
        </PageShell>
    );
}