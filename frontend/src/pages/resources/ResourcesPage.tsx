import * as React from "react";
import type { GridColDef } from "@mui/x-data-grid";
import DictionaryPage from "../../components/common/DictionaryPage";
import type { Resource } from "../../types/resource";
import { resourcesApi } from "../../api/resourcesApi";

export default function ResourcesPage() {
    const cols: GridColDef<Resource>[] = React.useMemo(
        () => [
            { field: "id", headerName: "ID", width: 90 },
            { field: "name", headerName: "Name", flex: 1, minWidth: 220 },
            { field: "state", headerName: "State", width: 140 },
        ],
        []
    );

    return (
        <DictionaryPage<Resource>
            mode="RESOURCE"
            title="Resources"
            subtitle="Manage resources"
            columns={cols}
            api={{
                list: resourcesApi.list,
                create: resourcesApi.create,
                update: resourcesApi.update,
                archive: resourcesApi.archive,
                activate: resourcesApi.activate,
                remove: resourcesApi.remove,
            }}
        />
    );
}