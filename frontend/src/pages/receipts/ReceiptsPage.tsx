import * as React from "react";
import AddIcon from "@mui/icons-material/Add";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import DeleteIcon from "@mui/icons-material/DeleteOutline";
import EditIcon from "@mui/icons-material/EditOutlined";
import {
    Autocomplete,
    Box,
    Button,
    Card,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Divider,
    IconButton,
    Menu,
    MenuItem,
    Stack,
    TextField,
    Typography,
} from "@mui/material";
import { DataGrid, type GridColDef, type GridRenderCellParams } from "@mui/x-data-grid";

import PageShell from "../../components/common/PageShell";
import ToolbarCard from "../../components/common/ToolbarCard";
import TableCard from "../../components/common/TableCard";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import { useAppSnackbar } from "../../components/common/AppSnackbar";

import type { Resource } from "../../types/resource";
import type { Unit } from "../../types/unit";
import { receiptsApi } from "../../api/receiptsApi";
import { resourcesApi } from "../../api/resourcesApi";
import { unitsApi } from "../../api/unitsApi";

type ReceiptListItem = {
    id: number;
    number: string;
    date: string;
    items: {
        resourceId: number | null;
        resourceName?: string | null;
        unitId: number | null;
        unitName?: string | null;
        quantity: any;
    }[];
};

type ReceiptItem = { resourceId: number; unitId: number; quantity: any };
type ReceiptFull = { id: number; number: string; date: string; items?: ReceiptItem[] };

type ItemDraft = ReceiptItem & { _key: string };

function mkKey() {
    return Math.random().toString(16).slice(2) + Date.now().toString(16);
}

function toDraft(items: ReceiptItem[] | undefined): ItemDraft[] {
    return (items ?? []).map((it) => ({ ...it, _key: mkKey() }));
}

function fromDraft(items: ItemDraft[]): ReceiptItem[] {
    return items.map((it) => ({
        resourceId: Number(it.resourceId),
        unitId: Number(it.unitId),
        quantity: Number(String(it.quantity ?? "").replace(",", ".").trim()),
    }));
}

function todayISO(): string {
    const d = new Date();
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
}

export default function ReceiptsPage() {
    const { notify } = useAppSnackbar();

    const [rows, setRows] = React.useState<ReceiptListItem[]>([]);
    const [loading, setLoading] = React.useState(false);

    const [openForm, setOpenForm] = React.useState(false);
    const [editing, setEditing] = React.useState<ReceiptFull | null>(null);
    const [number, setNumber] = React.useState("");
    const [date, setDate] = React.useState<string>(todayISO());
    const [items, setItems] = React.useState<ItemDraft[]>([]);

    const [menuAnchor, setMenuAnchor] = React.useState<HTMLElement | null>(null);
    const [menuRow, setMenuRow] = React.useState<ReceiptListItem | null>(null);
    const [confirmOpen, setConfirmOpen] = React.useState(false);
    const [deleteRow, setDeleteRow] = React.useState<ReceiptListItem | null>(null);

    const [from, setFrom] = React.useState<string>("");
    const [to, setTo] = React.useState<string>("");

    const [allNumbers, setAllNumbers] = React.useState<string[]>([]);
    const [selectedNumbers, setSelectedNumbers] = React.useState<string[]>([]);

    const [resources, setResources] = React.useState<Resource[]>([]);
    const [units, setUnits] = React.useState<Unit[]>([]);

    const [selectedResources, setSelectedResources] = React.useState<Resource[]>([]);
    const [selectedUnits, setSelectedUnits] = React.useState<Unit[]>([]);

    const nameById = React.useCallback((list: { id: number; name: string }[], id: number | null | undefined) => {
        if (!id) return "—";
        return list.find((x) => x.id === id)?.name ?? `#${id}`;
    }, []);

    const loadLookups = React.useCallback(async () => {
        try {
            const [res, u, all] = await Promise.all([
                resourcesApi.list(undefined, "ACTIVE"),
                unitsApi.list(undefined, "ACTIVE"),
                receiptsApi.list(),
            ]);

            setResources(Array.isArray(res) ? res : []);
            setUnits(Array.isArray(u) ? u : []);

            const arr = Array.isArray(all) ? all : [];
            const nums = Array.from(new Set(arr.map((x: any) => (x.number ?? "").trim()).filter(Boolean))).sort((a, b) =>
                a.localeCompare(b)
            );
            setAllNumbers(nums);
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Failed to load filters", "error");
        }
    }, [notify]);

    const loadReceipts = React.useCallback(async () => {
        setLoading(true);
        try {
            const data = await receiptsApi.list({
                from: from.trim() || undefined,
                to: to.trim() || undefined,
                numbers: selectedNumbers.length ? selectedNumbers : undefined,
                resourceIds: selectedResources.length ? selectedResources.map((r) => r.id) : undefined,
                unitIds: selectedUnits.length ? selectedUnits.map((u) => u.id) : undefined,
            });

            setRows(Array.isArray(data) ? (data as any) : []);
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Failed to load receipts", "error");
            setRows([]);
        } finally {
            setLoading(false);
        }
    }, [notify, from, to, selectedNumbers, selectedResources, selectedUnits]);

    React.useEffect(() => {
        void loadLookups();
        void loadReceipts();
    }, [loadLookups, loadReceipts]);

    const openCreate = () => {
        setEditing(null);
        setNumber("");
        setDate(todayISO());
        setItems([]);
        setOpenForm(true);
    };

    const openEdit = async (r: ReceiptListItem) => {
        try {
            const full = (await receiptsApi.get(r.id)) as any as ReceiptFull;
            setEditing(full);
            setNumber(full.number ?? "");
            setDate(full.date ?? todayISO());
            setItems(toDraft(full.items));
            setOpenForm(true);
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Failed to open receipt", "error");
        }
    };

    const closeForm = () => setOpenForm(false);

    const addItem = () => setItems((prev) => [...prev, { _key: mkKey(), resourceId: 0, unitId: 0, quantity: "" }]);
    const removeItem = (key: string) => setItems((prev) => prev.filter((it) => it._key !== key));
    const updateItem = (key: string, patch: Partial<ReceiptItem>) =>
        setItems((prev) => prev.map((it) => (it._key === key ? { ...it, ...patch } : it)));

    const save = async () => {
        if (!number.trim()) return notify("Number is required", "warning");
        if (!date.trim()) return notify("Date is required", "warning");

        const payload = {
            number: number.trim(),
            date: date.trim(),
            items: fromDraft(items).filter((x) => x.resourceId > 0 && x.unitId > 0 && Number(x.quantity) > 0),
        };

        try {
            if (editing) {
                await receiptsApi.update(editing.id, payload as any);
                notify("Updated", "success");
            } else {
                await receiptsApi.create(payload as any);
                notify("Created", "success");
            }
            setOpenForm(false);
            await loadLookups();
            await loadReceipts();
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Save failed", "error");
        }
    };

    const closeMenu = () => {
        setMenuAnchor(null);
        setMenuRow(null);
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
            await receiptsApi.remove(r.id);
            notify("Deleted", "success");
            await loadLookups();
            await loadReceipts();
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Delete failed", "error");
        }
    };

    const cols: GridColDef<ReceiptListItem>[] = React.useMemo(
        () => [
            { field: "id", headerName: "ID", width: 90 },
            { field: "number", headerName: "Number", flex: 1, minWidth: 160 },
            { field: "date", headerName: "Date", width: 160 },
            {
                field: "content",
                headerName: "Content",
                flex: 2,
                minWidth: 520,
                sortable: false,
                filterable: false,
                renderCell: (p: GridRenderCellParams<ReceiptListItem>) => {
                    const list = Array.isArray(p.row.items) ? p.row.items : [];
                    if (list.length === 0) return <Typography sx={{ opacity: 0.6 }}>—</Typography>;

                    const head = list.slice(0, 3);
                    const rest = list.length - head.length;

                    return (
                        <Stack spacing={0.3} sx={{ py: 0.5, whiteSpace: "normal", lineHeight: 1.2 }}>
                            {head.map((it, idx) => {
                                const rName = (it.resourceName ?? "").trim() || nameById(resources, it.resourceId);
                                const uName = (it.unitName ?? "").trim() || nameById(units, it.unitId);
                                return (
                                    <Typography key={idx} variant="body2">
                                        • {rName} / {uName} - {String(it.quantity)}
                                    </Typography>
                                );
                            })}
                            {rest > 0 && (
                                <Typography variant="body2" sx={{ opacity: 0.7 }}>
                                    +{rest} more…
                                </Typography>
                            )}
                        </Stack>
                    );
                },
            },

            {
                field: "itemsCount",
                headerName: "Items",
                width: 120,
                sortable: false,
                valueGetter: (_v, row) => (Array.isArray(row.items) ? row.items.length : 0),
            },

            {
                field: "_actions",
                headerName: "",
                width: 90,
                sortable: false,
                filterable: false,
                align: "right",
                headerAlign: "right",
                renderCell: (p: GridRenderCellParams<ReceiptListItem>) => (
                    <IconButton
                        size="small"
                        onClick={(e) => {
                            setMenuAnchor(e.currentTarget);
                            setMenuRow(p.row);
                        }}
                    >
                        <MoreVertIcon fontSize="small" />
                    </IconButton>
                ),
            },
        ],
        [nameById, resources, units]
    );

    return (
        <PageShell
            title="Receipts"
            subtitle="Incoming documents (server-side filters)"
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
                            label="Date from"
                            type="date"
                            value={from}
                            onChange={(e) => setFrom(e.target.value)}
                            sx={{ width: 180 }}
                            InputLabelProps={{ shrink: true }}
                        />
                        <TextField
                            label="Date to"
                            type="date"
                            value={to}
                            onChange={(e) => setTo(e.target.value)}
                            sx={{ width: 180 }}
                            InputLabelProps={{ shrink: true }}
                        />

                        <Autocomplete
                            multiple
                            options={allNumbers}
                            value={selectedNumbers}
                            onChange={(_, v) => setSelectedNumbers(v)}
                            limitTags={2}
                            renderInput={(params) => <TextField {...params} label="Numbers" placeholder="All" />}
                            sx={{ minWidth: 320, flex: 1 }}
                        />

                        <Autocomplete
                            multiple
                            options={resources}
                            value={selectedResources}
                            onChange={(_, v) => setSelectedResources(v)}
                            getOptionLabel={(o) => o.name}
                            isOptionEqualToValue={(a, b) => a.id === b.id}
                            limitTags={2}
                            renderInput={(params) => <TextField {...params} label="Resources" placeholder="All" />}
                            sx={{ minWidth: 320, flex: 1 }}
                        />

                        <Autocomplete
                            multiple
                            options={units}
                            value={selectedUnits}
                            onChange={(_, v) => setSelectedUnits(v)}
                            getOptionLabel={(o) => o.name}
                            isOptionEqualToValue={(a, b) => a.id === b.id}
                            limitTags={2}
                            renderInput={(params) => <TextField {...params} label="Units" placeholder="All" />}
                            sx={{ minWidth: 260, flex: 1 }}
                        />
                    </>
                }
                right={
                    <>
                        <Button
                            variant="outlined"
                            onClick={() => {
                                setFrom("");
                                setTo("");
                                setSelectedNumbers([]);
                                setSelectedResources([]);
                                setSelectedUnits([]);
                                void loadReceipts();
                            }}
                        >
                            Refresh
                        </Button>
                        <Button variant="contained" onClick={() => void loadReceipts()}>
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
                    getRowHeight={() => "auto"}
                />
            </TableCard>

            <Menu anchorEl={menuAnchor} open={!!menuAnchor} onClose={closeMenu}>
                <MenuItem
                    onClick={() => {
                        if (menuRow) void openEdit(menuRow);
                        closeMenu();
                    }}
                >
                    <EditIcon fontSize="small" style={{ marginRight: 10 }} />
                    Edit
                </MenuItem>

                <Divider />

                <MenuItem onClick={askDelete} sx={{ color: "error.main" }}>
                    <DeleteIcon fontSize="small" style={{ marginRight: 10 }} />
                    Delete
                </MenuItem>
            </Menu>

            <ConfirmDialog
                open={confirmOpen}
                title="Delete receipt"
                text="Are you sure you want to delete this receipt?"
                confirmText="Delete"
                onCancel={() => {
                    setConfirmOpen(false);
                    setDeleteRow(null);
                }}
                onConfirm={() => void doDelete()}
            />

            <Dialog open={openForm} onClose={closeForm} maxWidth="md" fullWidth>
                <DialogTitle sx={{ fontWeight: 900 }}>{editing ? `Edit receipt #${editing.id}` : "Create receipt"}</DialogTitle>

                <DialogContent sx={{ pt: 2 }}>
                    <Stack spacing={2} sx={{ mt: 1 }}>
                        <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                            <TextField label="Number" value={number} onChange={(e) => setNumber(e.target.value)} fullWidth />
                            <TextField
                                label="Date"
                                type="date"
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                                fullWidth
                                InputLabelProps={{ shrink: true }}
                            />
                        </Stack>

                        <Stack direction="row" alignItems="center" justifyContent="space-between">
                            <Typography sx={{ fontWeight: 900 }}>Items</Typography>
                            <Button variant="outlined" onClick={addItem}>
                                Add item
                            </Button>
                        </Stack>

                        <Divider />

                        {items.length === 0 ? (
                            <Typography color="text.secondary">No items yet</Typography>
                        ) : (
                            <Stack spacing={1.5}>
                                {items.map((it) => (
                                    <Card key={it._key} sx={{ p: 2, borderRadius: 3, border: "1px solid", borderColor: "divider" }}>
                                        <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems="center">
                                            <Autocomplete
                                                options={resources}
                                                value={resources.find((r) => r.id === Number(it.resourceId)) ?? null}
                                                onChange={(_, v) => updateItem(it._key, { resourceId: v?.id ?? 0 })}
                                                getOptionLabel={(o) => o.name}
                                                isOptionEqualToValue={(a, b) => a.id === b.id}
                                                renderInput={(params) => <TextField {...params} label="Resource" />}
                                                sx={{ width: { xs: "100%", md: 260 } }}
                                            />

                                            <Autocomplete
                                                options={units}
                                                value={units.find((u) => u.id === Number(it.unitId)) ?? null}
                                                onChange={(_, v) => updateItem(it._key, { unitId: v?.id ?? 0 })}
                                                getOptionLabel={(o) => o.name}
                                                isOptionEqualToValue={(a, b) => a.id === b.id}
                                                renderInput={(params) => <TextField {...params} label="Unit" />}
                                                sx={{ width: { xs: "100%", md: 220 } }}
                                            />
                                            <TextField
                                                label="Quantity"
                                                value={it.quantity ?? ""}
                                                onChange={(e) => updateItem(it._key, { quantity: e.target.value as any })}
                                                sx={{ width: { xs: "100%", md: 220 } }}
                                            />

                                            <Box sx={{ flex: 1 }} />

                                            <Button color="error" variant="outlined" onClick={() => removeItem(it._key)}>
                                                Remove
                                            </Button>
                                        </Stack>

                                        <Typography variant="body2" sx={{ mt: 1, opacity: 0.75 }}>
                                            {nameById(resources, Number(it.resourceId))} / {nameById(units, Number(it.unitId))}
                                        </Typography>
                                    </Card>
                                ))}
                            </Stack>
                        )}
                    </Stack>
                </DialogContent>

                <DialogActions sx={{ p: 2 }}>
                    <Button onClick={closeForm} variant="outlined">
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