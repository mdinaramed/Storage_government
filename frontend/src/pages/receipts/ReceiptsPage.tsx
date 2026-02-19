import * as React from "react";
import AddIcon from "@mui/icons-material/Add";
import MoreVertIcon from "@mui/icons-material/MoreVert";
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

import type { Receipt, ReceiptItem } from "../../types/receipt";
import type { Resource } from "../../types/resource";
import type { Unit } from "../../types/unit";

import { receiptsApi } from "../../api/receiptsApi";
import { resourcesApi } from "../../api/resourcesApi";
import { unitsApi } from "../../api/unitsApi";

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
        quantity: Number(it.quantity),
    }));
}

function safeItemsCount(r: Receipt): number {
    return Array.isArray(r.items) ? r.items.length : 0;
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

    const [rows, setRows] = React.useState<Receipt[]>([]);
    const [loading, setLoading] = React.useState(false);

    const [openForm, setOpenForm] = React.useState(false);
    const [editing, setEditing] = React.useState<Receipt | null>(null);
    const [number, setNumber] = React.useState("");
    const [date, setDate] = React.useState<string>(todayISO);
    const [items, setItems] = React.useState<ItemDraft[]>([]);

    const [menuAnchor, setMenuAnchor] = React.useState<HTMLElement | null>(null);
    const [menuRow, setMenuRow] = React.useState<Receipt | null>(null);
    const [confirmOpen, setConfirmOpen] = React.useState(false);
    const [deleteRow, setDeleteRow] = React.useState<Receipt | null>(null);

    const [from, setFrom] = React.useState<string>("");
    const [to, setTo] = React.useState<string>("");

    const [allNumbers, setAllNumbers] = React.useState<string[]>([]);
    const [selectedNumbers, setSelectedNumbers] = React.useState<string[]>([]);

    const [resources, setResources] = React.useState<Resource[]>([]);
    const [units, setUnits] = React.useState<Unit[]>([]);

    const [selectedResources, setSelectedResources] = React.useState<Resource[]>([]);
    const [selectedUnits, setSelectedUnits] = React.useState<Unit[]>([]);

    const loadLookups = React.useCallback(async () => {
        try {
            const [res, u, all] = await Promise.all([
                resourcesApi.list(undefined, "ACTIVE"),
                unitsApi.list(undefined, "ACTIVE"),
                receiptsApi.list(),
            ]);

            setResources(Array.isArray(res) ? res : []);
            setUnits(Array.isArray(u) ? u : []);

            const nums = (Array.isArray(all) ? all : [])
                .map((x) => (x.number ?? "").trim())
                .filter((x) => x.length > 0);

            setAllNumbers(Array.from(new Set(nums)).sort((a, b) => a.localeCompare(b)));
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
                resourceIds: selectedResources.map((r) => r.id),
                unitIds: selectedUnits.map((u) => u.id),
            });

            setRows(Array.isArray(data) ? data : []);
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
    }, [loadLookups]);

    const cols: GridColDef<Receipt>[] = React.useMemo(
        () => [
            { field: "id", headerName: "ID", width: 90 },
            { field: "number", headerName: "Number", flex: 1, minWidth: 220 },
            { field: "date", headerName: "Date", width: 160 },
            {
                field: "itemsCount",
                headerName: "Items",
                width: 120,
                sortable: false,
                valueGetter: (_value, row) => safeItemsCount(row),
            },
            {
                field: "_actions",
                headerName: "",
                width: 90,
                sortable: false,
                filterable: false,
                align: "right",
                headerAlign: "right",
                renderCell: (p: GridRenderCellParams<Receipt>) => (
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
        []
    );

    const openCreate = () => {
        setEditing(null);
        setNumber("");
        setDate(todayISO());
        setItems([]);
        setOpenForm(true);
    };

    const openEdit = async (r: Receipt) => {
        try {
            const full = await receiptsApi.get(r.id);
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

    const addItem = () => {
        setItems((prev) => [...prev, { _key: mkKey(), resourceId: 0, unitId: 0, quantity: 0 }]);
    };

    const removeItem = (key: string) => {
        setItems((prev) => prev.filter((it) => it._key !== key));
    };

    const updateItem = (key: string, patch: Partial<ReceiptItem>) => {
        setItems((prev) => prev.map((it) => (it._key === key ? { ...it, ...patch } : it)));
    };

    const save = async () => {
        const n = number.trim();
        if (!n) return notify("Number is required", "warning");
        if (!date.trim()) return notify("Date is required", "warning");

        const payload = {
            number: n,
            date: date.trim(),
            items: fromDraft(items).filter(
                (it) =>
                    Number.isFinite(it.resourceId) &&
                    Number.isFinite(it.unitId) &&
                    Number.isFinite(it.quantity) &&
                    it.resourceId > 0 &&
                    it.unitId > 0 &&
                    it.quantity > 0
            ),
        };

        try {
            if (editing) {
                await receiptsApi.update(editing.id, payload);
                notify("Updated", "success");
            } else {
                await receiptsApi.create(payload);
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
                        <Button variant="outlined" onClick={() => void loadReceipts()}>
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
                />
            </TableCard>

            <Menu anchorEl={menuAnchor} open={!!menuAnchor} onClose={closeMenu}>
                <MenuItem
                    onClick={() => {
                        if (menuRow) void openEdit(menuRow);
                        closeMenu();
                    }}
                >
                    Edit
                </MenuItem>
                <MenuItem onClick={askDelete} sx={{ color: "error.main" }}>
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
                <DialogTitle sx={{ fontWeight: 900 }}>
                    {editing ? `Edit receipt #${editing.id}` : "Create receipt"}
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
                            <Typography color="text.secondary">
                                No items yet. (По ТЗ поступление может быть пустым — но проверь бэк!)
                            </Typography>
                        ) : (
                            <Stack spacing={1.5}>
                                {items.map((it) => (
                                    <Card
                                        key={it._key}
                                        sx={{ p: 2, borderRadius: 3, border: "1px solid", borderColor: "divider" }}
                                    >
                                        <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems="center">
                                            <TextField
                                                label="Resource ID"
                                                type="number"
                                                value={it.resourceId}
                                                onChange={(e) =>
                                                    updateItem(it._key, { resourceId: Number(e.target.value) })
                                                }
                                                sx={{ width: { xs: "100%", md: 180 } }}
                                            />
                                            <TextField
                                                label="Unit ID"
                                                type="number"
                                                value={it.unitId}
                                                onChange={(e) =>
                                                    updateItem(it._key, { unitId: Number(e.target.value) })
                                                }
                                                sx={{ width: { xs: "100%", md: 180 } }}
                                            />
                                            <TextField
                                                label="Quantity"
                                                type="number"
                                                value={it.quantity}
                                                onChange={(e) =>
                                                    updateItem(it._key, { quantity: Number(e.target.value) })
                                                }
                                                sx={{ width: { xs: "100%", md: 220 } }}
                                            />

                                            <Box sx={{ flex: 1 }} />

                                            <Button color="error" variant="outlined" onClick={() => removeItem(it._key)}>
                                                Remove
                                            </Button>
                                        </Stack>
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