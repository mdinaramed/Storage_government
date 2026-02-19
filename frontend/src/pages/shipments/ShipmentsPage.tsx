import * as React from "react";
import AddIcon from "@mui/icons-material/Add";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import DeleteIcon from "@mui/icons-material/DeleteOutline";
import DoneIcon from "@mui/icons-material/TaskAlt";
import UndoIcon from "@mui/icons-material/Undo";
import EditIcon from "@mui/icons-material/EditOutlined";
import {
    Box,
    Button,
    Chip,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Divider,
    FormControl,
    IconButton,
    InputLabel,
    Menu,
    MenuItem,
    Paper,
    Select,
    Stack,
    TextField,
    Typography,
} from "@mui/material";
import { DataGrid, type GridColDef, type GridRenderCellParams } from "@mui/x-data-grid";

import PageShell from "../../components/common/PageShell";
import TableCard from "../../components/common/TableCard";

import ConfirmDialog from "../../components/common/ConfirmDialog";
import { useAppSnackbar } from "../../components/common/AppSnackbar";

import type { Shipment, ShipmentItem, ShipmentState } from "../../types/shipment";
import { shipmentsApi, type ShipmentPayload } from "../../api/shipmentsApi";
import { clientsApi } from "../../api/clientsApi";
import { resourcesApi } from "../../api/resourcesApi";
import { unitsApi } from "../../api/unitsApi";

type Option = { id: number; name: string };

function StateChip({ state }: { state: ShipmentState }) {
    const color = state === "SIGNED" ? "success" : state === "REVOKED" ? "default" : "warning";
    return <Chip label={state} color={color} size="small" />;
}

function emptyItem(): ShipmentItem {
    return { resourceId: 0, unitId: 0, quantity: 0 };
}

function todayISO(): string {
    return new Date().toISOString().slice(0, 10);
}

export default function ShipmentsPage() {
    const { notify } = useAppSnackbar();

    const [rows, setRows] = React.useState<Shipment[]>([]);
    const [loading, setLoading] = React.useState(false);

    const [clients, setClients] = React.useState<Option[]>([]);
    const [resources, setResources] = React.useState<Option[]>([]);
    const [units, setUnits] = React.useState<Option[]>([]);

    const [menuAnchor, setMenuAnchor] = React.useState<HTMLElement | null>(null);
    const [menuRow, setMenuRow] = React.useState<Shipment | null>(null);

    const [confirmOpen, setConfirmOpen] = React.useState(false);
    const [deleteRow, setDeleteRow] = React.useState<Shipment | null>(null);

    const [openForm, setOpenForm] = React.useState(false);
    const [editing, setEditing] = React.useState<Shipment | null>(null);

    const [number, setNumber] = React.useState("");
    const [date, setDate] = React.useState(todayISO());
    const [clientId, setClientId] = React.useState<number>(0);
    const [items, setItems] = React.useState<ShipmentItem[]>([emptyItem()]);

    const load = React.useCallback(async () => {
        setLoading(true);
        try {
            const data = await shipmentsApi.list();
            setRows(Array.isArray(data) ? data : []);
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Load failed", "error");
            setRows([]);
        } finally {
            setLoading(false);
        }
    }, [notify]);

    const loadLookups = React.useCallback(async () => {
        try {
            const [c, r, u] = await Promise.all([
                clientsApi.list(undefined, "ACTIVE"),
                resourcesApi.list(undefined, "ACTIVE"),
                unitsApi.list(undefined, "ACTIVE"),
            ]);
            setClients(c.map((x) => ({ id: x.id, name: x.name })));
            setResources(r.map((x) => ({ id: x.id, name: x.name })));
            setUnits(u.map((x) => ({ id: x.id, name: x.name })));
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Failed to load dropdowns", "error");
        }
    }, [notify]);

    React.useEffect(() => {
        void load();
        void loadLookups();
    }, [load, loadLookups]);

    const openCreate = () => {
        setEditing(null);
        setNumber("");
        setDate(todayISO());
        setClientId(clients[0]?.id ?? 0);
        setItems([emptyItem()]);
        setOpenForm(true);
    };

    const openEdit = (s: Shipment) => {
        setEditing(s);
        setNumber(s.number ?? "");
        setDate(s.date ?? todayISO());
        setClientId(s.clientId ?? 0);
        setItems(s.items && s.items.length > 0 ? s.items : [emptyItem()]);
        setOpenForm(true);
    };

    const validate = (): string | null => {
        if (!number.trim()) return "Number is required";
        if (!date.trim()) return "Date is required";
        if (!clientId || clientId <= 0) return "Client is required";
        if (!items.length) return "At least 1 item is required";

        for (let i = 0; i < items.length; i++) {
            const it = items[i];
            if (!it.resourceId || it.resourceId <= 0) return `Item #${i + 1}: resource is required`;
            if (!it.unitId || it.unitId <= 0) return `Item #${i + 1}: unit is required`;
            if (!(it.quantity > 0)) return `Item #${i + 1}: quantity must be > 0`;
        }
        return null;
    };

    const save = async () => {
        const errMsg = validate();
        if (errMsg) return notify(errMsg, "warning");

        const payload: ShipmentPayload = {
            number: number.trim(),
            date: date.trim(),
            clientId,
            items: items.map((it) => ({
                resourceId: it.resourceId,
                unitId: it.unitId,
                quantity: Number(it.quantity),
            })),
        };

        try {
            if (editing) {
                await shipmentsApi.update(editing.id, payload);
                notify("Updated", "success");
            } else {
                await shipmentsApi.create(payload);
                notify("Created", "success");
            }
            setOpenForm(false);
            await load();
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Save failed", "error");
        }
    };

    const openMenu = (evt: React.MouseEvent<HTMLElement>, row: Shipment) => {
        setMenuAnchor(evt.currentTarget);
        setMenuRow(row);
    };

    const closeMenu = () => {
        setMenuAnchor(null);
        setMenuRow(null);
    };

    const doSign = async () => {
        if (!menuRow) return;
        try {
            await shipmentsApi.sign(menuRow.id);
            notify("Signed", "success");
            closeMenu();
            await load();
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Sign failed", "error");
        }
    };

    const doRevoke = async () => {
        if (!menuRow) return;
        try {
            await shipmentsApi.revoke(menuRow.id);
            notify("Revoked", "success");
            closeMenu();
            await load();
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Revoke failed", "error");
        }
    };

    const askDelete = () => {
        setDeleteRow(menuRow);
        setConfirmOpen(true);
        closeMenu();
    };

    const doDelete = async () => {
        const r = deleteRow;
        setConfirmOpen(false);
        setDeleteRow(null);
        if (!r) return;

        try {
            await shipmentsApi.remove(r.id);
            notify("Deleted", "success");
            await load();
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Delete failed", "error");
        }
    };

    const nameById = React.useCallback((list: Option[], id: number) => {
        return list.find((x) => x.id === id)?.name ?? `#${id}`;
    }, []);

    const cols: GridColDef<Shipment>[] = React.useMemo(
        () => [
            { field: "id", headerName: "ID", width: 90 },
            { field: "number", headerName: "Number", flex: 1, minWidth: 240 },
            { field: "date", headerName: "Date", width: 160 },
            {
                field: "clientId",
                headerName: "Client",
                flex: 1,
                minWidth: 240,
                valueGetter: (_v, row) => nameById(clients, row.clientId),
            },
            {
                field: "state",
                headerName: "State",
                width: 160,
                sortable: false,
                renderCell: (p: GridRenderCellParams<Shipment>) => <StateChip state={p.row.state} />,
            },
            {
                field: "items",
                headerName: "Items",
                width: 120,
                sortable: false,
                valueGetter: (_v, row) => row.items?.length ?? 0,
            },
            {
                field: "_actions",
                headerName: "",
                width: 90,
                sortable: false,
                filterable: false,
                align: "right",
                headerAlign: "right",
                renderCell: (p: GridRenderCellParams<Shipment>) => (
                    <IconButton onClick={(e) => openMenu(e, p.row)} size="small">
                        <MoreVertIcon fontSize="small" />
                    </IconButton>
                ),
            },
        ],
        [clients, nameById]
    );

    const setItem = (idx: number, patch: Partial<ShipmentItem>) => {
        setItems((prev) => prev.map((it, i) => (i === idx ? { ...it, ...patch } : it)));
    };

    const addItem = () => setItems((prev) => [...prev, emptyItem()]);
    const removeItem = (idx: number) => setItems((prev) => prev.filter((_, i) => i !== idx));

    return (
        <PageShell
            title="Shipments"
            subtitle="Outgoing documents"
            actions={
                <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>
                    New
                </Button>
            }
        >
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
                    <EditIcon fontSize="small" style={{ marginRight: 10 }} />
                    Edit
                </MenuItem>

                <MenuItem onClick={() => void doSign()}>
                    <DoneIcon fontSize="small" style={{ marginRight: 10 }} />
                    Sign
                </MenuItem>

                <MenuItem onClick={() => void doRevoke()}>
                    <UndoIcon fontSize="small" style={{ marginRight: 10 }} />
                    Revoke
                </MenuItem>

                <Divider />

                <MenuItem onClick={askDelete} sx={{ color: "error.main" }}>
                    <DeleteIcon fontSize="small" style={{ marginRight: 10 }} />
                    Delete
                </MenuItem>
            </Menu>

            <ConfirmDialog
                open={confirmOpen}
                title="Delete"
                text="Are you sure you want to delete this shipment?"
                confirmText="Delete"
                onCancel={() => {
                    setConfirmOpen(false);
                    setDeleteRow(null);
                }}
                onConfirm={() => void doDelete()}
            />

            <Dialog open={openForm} onClose={() => setOpenForm(false)} maxWidth="md" fullWidth>
                <DialogTitle sx={{ fontWeight: 900 }}>
                    {editing ? `Edit shipment #${editing.id}` : "Create shipment"}
                </DialogTitle>

                <DialogContent sx={{ pt: 2 }}>
                    <Stack spacing={2} sx={{ mt: 1 }}>
                        <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                            <TextField
                                label="Number"
                                value={number}
                                onChange={(e) => setNumber(e.target.value)}
                                fullWidth
                            />
                            <TextField
                                label="Date"
                                type="date"
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                                sx={{ width: { xs: "100%", sm: 240 } }}
                                InputLabelProps={{ shrink: true }}
                            />
                        </Stack>

                        <FormControl fullWidth>
                            <InputLabel>Client</InputLabel>
                            <Select
                                label="Client"
                                value={clientId}
                                onChange={(e) => setClientId(Number(e.target.value))}
                            >
                                {clients.map((c) => (
                                    <MenuItem key={c.id} value={c.id}>
                                        {c.name}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                        <Typography sx={{ fontWeight: 900, mt: 1 }}>Items</Typography>

                        <Paper variant="outlined" sx={{ p: 2, borderRadius: 3 }}>
                            <Stack spacing={2}>
                                {items.map((it, idx) => (
                                    <Stack
                                        key={idx}
                                        direction={{ xs: "column", md: "row" }}
                                        spacing={2}
                                        alignItems="center"
                                    >
                                        <FormControl fullWidth>
                                            <InputLabel>Resource</InputLabel>
                                            <Select
                                                label="Resource"
                                                value={it.resourceId}
                                                onChange={(e) => setItem(idx, { resourceId: Number(e.target.value) })}
                                            >
                                                <MenuItem value={0} disabled>
                                                    Select resource
                                                </MenuItem>
                                                {resources.map((r) => (
                                                    <MenuItem key={r.id} value={r.id}>
                                                        {r.name}
                                                    </MenuItem>
                                                ))}
                                            </Select>
                                        </FormControl>

                                        <FormControl sx={{ width: { xs: "100%", md: 220 } }}>
                                            <InputLabel>Unit</InputLabel>
                                            <Select
                                                label="Unit"
                                                value={it.unitId}
                                                onChange={(e) => setItem(idx, { unitId: Number(e.target.value) })}
                                            >
                                                <MenuItem value={0} disabled>
                                                    Select unit
                                                </MenuItem>
                                                {units.map((u) => (
                                                    <MenuItem key={u.id} value={u.id}>
                                                        {u.name}
                                                    </MenuItem>
                                                ))}
                                            </Select>
                                        </FormControl>

                                        <TextField
                                            label="Quantity"
                                            type="number"
                                            value={it.quantity}
                                            onChange={(e) => setItem(idx, { quantity: Number(e.target.value) })}
                                            sx={{ width: { xs: "100%", md: 220 } }}
                                            inputProps={{ min: 0, step: "0.001" }}
                                        />

                                        <Button
                                            variant="outlined"
                                            color="error"
                                            onClick={() => removeItem(idx)}
                                            disabled={items.length === 1}
                                        >
                                            Remove
                                        </Button>
                                    </Stack>
                                ))}

                                <Box>
                                    <Button variant="outlined" onClick={addItem}>
                                        + Add item
                                    </Button>
                                </Box>
                            </Stack>
                        </Paper>
                    </Stack>
                </DialogContent>

                <DialogActions sx={{ p: 2 }}>
                    <Button variant="outlined" onClick={() => setOpenForm(false)}>
                        Cancel
                    </Button>
                    <Button variant="contained" onClick={() => void save()}>
                        Save
                    </Button>
                </DialogActions>
            </Dialog>
        </PageShell>
    );
}