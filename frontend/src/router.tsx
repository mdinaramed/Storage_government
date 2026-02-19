import { createBrowserRouter } from "react-router-dom";

import AppLayout from "./components/layout/AppLayout";

import UnitsPage from "./pages/units/UnitsPage";
import ResourcesPage from "./pages/resources/ResourcesPage";
import ClientsPage from "./pages/clients/ClientsPage";
import BalancesPage from "./pages/balances/BalancesPage";
import ReceiptsPage from "./pages/receipts/ReceiptsPage";
import ShipmentsPage from "./pages/shipments/ShipmentsPage";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <AppLayout />,
        children: [
            { index: true, element: <UnitsPage /> },
            { path: "units", element: <UnitsPage /> },
            { path: "resources", element: <ResourcesPage /> },
            { path: "clients", element: <ClientsPage /> },
            { path: "balances", element: <BalancesPage /> },
            { path: "receipts", element: <ReceiptsPage /> },
            { path: "shipments", element: <ShipmentsPage /> },
        ],
    },
    { path: "*", element: <AppLayout /> },
]);