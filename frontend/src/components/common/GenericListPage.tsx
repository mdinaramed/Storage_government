import * as React from "react";
import { Box, Paper, Stack, Typography, Button } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { useAppSnackbar } from "./AppSnackbar";

type Row = { id: number } & Record<string, unknown>;

function toText(v: unknown): string {
    if (v === null || v === undefined) return "";
    if (typeof v === "string") return v;
    if (typeof v === "number" || typeof v === "boolean") return String(v);
    return JSON.stringify(v);
}

export default function GenericListPage(props: {
    title: string;
    subtitle: string;
    load: () => Promise<unknown>;
}) {
    const { notify } = useAppSnackbar();
    const [rows, setRows] = React.useState<Row[]>([]);
    const [loading, setLoading] = React.useState(false);

    const loadData = React.useCallback(async () => {
        setLoading(true);
        try {
            const raw = await props.load();
            const arr = Array.isArray(raw) ? raw : [];
            const normalized: Row[] = arr
                .map((x) => (typeof x === "object" && x !== null ? (x as Record<string, unknown>) : null))
                .filter((x): x is Record<string, unknown> => x !== null)
                .map((x) => {
                    const id = typeof x.id === "number" ? x.id : Number(x.id);
                    return { id, ...x } as Row;
                })
                .filter((r) => Number.isFinite(r.id));

            setRows(normalized);
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Load failed", "error");
        } finally {
            setLoading(false);
        }
    }, [props, notify]);

    React.useEffect(() => {
        void loadData();
    }, [loadData]);

    const cols: GridColDef<Row>[] = React.useMemo(() => {
        const first = rows[0];
        const keys = first ? Object.keys(first).filter((k) => k !== "id") : [];
        const dynamic: GridColDef<Row>[] = keys.slice(0, 8).map((k) => ({
            field: k,
            headerName: k.toUpperCase(),
            flex: 1,
            minWidth: 160,
            valueGetter: (value) => toText(value),
        }));

        return [{ field: "id", headerName: "ID", width: 90 }, ...dynamic];
    }, [rows]);

    return (
        <Box sx={{ p: 3 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="flex-start" sx={{ mb: 2 }}>
                <Box>
                    <Typography variant="h3">{props.title}</Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                        {props.subtitle}
                    </Typography>
                </Box>

                <Button variant="outlined" onClick={() => void loadData()}>
                    Refresh
                </Button>
            </Stack>

            <Paper variant="outlined" sx={{ height: 600, overflow: "hidden" }}>
                <DataGrid
                    rows={rows}
                    columns={cols}
                    loading={loading}
                    disableRowSelectionOnClick
                    pageSizeOptions={[10, 25, 50, 100]}
                    initialState={{ pagination: { paginationModel: { pageSize: 10, page: 0 } } }}
                />
            </Paper>
        </Box>
    );
}