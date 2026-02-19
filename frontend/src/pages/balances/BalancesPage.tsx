import * as React from "react";
import { Autocomplete, Button, TextField } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";

import PageShell from "../../components/common/PageShell";
import ToolbarCard from "../../components/common/ToolbarCard";
import TableCard from "../../components/common/TableCard";

import { useAppSnackbar } from "../../components/common/AppSnackbar";
import { balancesApi } from "../../api/balancesApi";
import { resourcesApi } from "../../api/resourcesApi";
import { unitsApi } from "../../api/unitsApi";

import type { Balance } from "../../types/balance";
import type { Resource } from "../../types/resource";
import type { Unit } from "../../types/unit";

export default function BalancesPage() {
    const { notify } = useAppSnackbar();

    const [loading, setLoading] = React.useState(false);
    const [rows, setRows] = React.useState<Balance[]>([]);

    const [resources, setResources] = React.useState<Resource[]>([]);
    const [units, setUnits] = React.useState<Unit[]>([]);

    const [selectedResources, setSelectedResources] = React.useState<Resource[]>([]);
    const [selectedUnits, setSelectedUnits] = React.useState<Unit[]>([]);

    const cols: GridColDef<Balance>[] = React.useMemo(
        () => [
            { field: "id", headerName: "ID", width: 90 },
            { field: "resourceName", headerName: "Resource", flex: 1, minWidth: 240 },
            { field: "unitName", headerName: "Unit", width: 160 },
            {
                field: "amount",
                headerName: "Amount",
                width: 180,
                type: "number",
                valueFormatter: (value) => {
                    const num = Number(value ?? 0);
                    return Number.isFinite(num) ? num.toFixed(3) : String(value ?? "");
                },
            },
        ],
        []
    );

    const loadFilters = React.useCallback(async () => {
        try {
            const [res, u] = await Promise.all([
                resourcesApi.list(undefined, "ACTIVE"),
                unitsApi.list(undefined, "ACTIVE"),
            ]);
            setResources(res);
            setUnits(u);
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Failed to load filters", "error");
        }
    }, [notify]);

    const loadBalances = React.useCallback(async () => {
        setLoading(true);
        try {
            const data = await balancesApi.list({
                resourceIds: selectedResources.map((r) => r.id),
                unitIds: selectedUnits.map((u) => u.id),
            });
            setRows(Array.isArray(data) ? data : []);
        } catch (e: unknown) {
            const err = e as { userMessage?: string; message?: string };
            notify(err.userMessage ?? err.message ?? "Failed to load balances", "error");
            setRows([]);
        } finally {
            setLoading(false);
        }
    }, [notify, selectedResources, selectedUnits]);

    React.useEffect(() => {
        void loadFilters();
    }, [loadFilters]);

    React.useEffect(() => {
        void loadBalances();
    }, [loadBalances]);

    return (
        <PageShell
            title="Balances"
            subtitle="Current stock amounts (filter by resources / units)"
            actions={
                <Button variant="outlined" onClick={() => void loadBalances()}>
                    Refresh
                </Button>
            }
        >
            <ToolbarCard
                left={
                    <>
                        <Autocomplete
                            multiple
                            options={resources}
                            value={selectedResources}
                            onChange={(_, v) => setSelectedResources(v)}
                            getOptionLabel={(o) => o.name}
                            isOptionEqualToValue={(a, b) => a.id === b.id}
                            limitTags={2}
                            renderInput={(params) => (
                                <TextField {...params} label="Resources" placeholder="All" />
                            )}
                            sx={{ minWidth: 360, flex: 1 }}
                        />

                        <Autocomplete
                            multiple
                            options={units}
                            value={selectedUnits}
                            onChange={(_, v) => setSelectedUnits(v)}
                            getOptionLabel={(o) => o.name}
                            isOptionEqualToValue={(a, b) => a.id === b.id}
                            limitTags={2}
                            renderInput={(params) => (
                                <TextField {...params} label="Units" placeholder="All" />
                            )}
                            sx={{ minWidth: 300, flex: 1 }}
                        />
                    </>
                }
                right={
                    <>
                        <Button variant="contained" onClick={() => void loadBalances()}>
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
        </PageShell>
    );
}