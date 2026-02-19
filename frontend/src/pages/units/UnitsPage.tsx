import * as React from "react";
import type { GridColDef } from "@mui/x-data-grid";
import DictionaryPage from "../../components/common/DictionaryPage";
import type { Unit } from "../../types/unit";
import { unitsApi } from "../../api/unitsApi";

export default function UnitsPage() {
    const cols: GridColDef<Unit>[] = React.useMemo(
        () => [
            { field: "id", headerName: "ID", width: 90 },
            { field: "name", headerName: "Name", flex: 1, minWidth: 240 },
        ],
        []
    );

    return (
        <DictionaryPage<Unit>
            mode="UNIT"
            title="Units"
            subtitle="Manage measurement units"
            columns={cols}
            api={unitsApi}
        />
    );
}