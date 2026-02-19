import * as React from "react";
import type { GridColDef } from "@mui/x-data-grid";
import DictionaryPage from "../../components/common/DictionaryPage";
import type { Client } from "../../types/client";
import { clientsApi } from "../../api/clientsApi";

export default function ClientsPage() {
    const cols: GridColDef<Client>[] = React.useMemo(
        () => [
            { field: "id", headerName: "ID", width: 90 },
            { field: "name", headerName: "Name", flex: 1, minWidth: 220 },
            {
                field: "address",
                headerName: "Address",
                flex: 1,
                minWidth: 280,
                valueGetter: (_value, row) => row.address ?? "",
            },
        ],
        []
    );

    return (
        <DictionaryPage<Client>
            mode="CLIENT"
            title="Clients"
            subtitle="Manage clients and addresses"
            columns={cols}
            api={clientsApi}
        />
    );
}