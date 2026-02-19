import { Outlet, NavLink } from "react-router-dom";
import {
    Avatar,
    Box,
    Divider,
    Drawer,
    List,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Stack,
    Typography,
} from "@mui/material";

import ScaleOutlinedIcon from "@mui/icons-material/ScaleOutlined";
import Inventory2OutlinedIcon from "@mui/icons-material/Inventory2Outlined";
import GroupsOutlinedIcon from "@mui/icons-material/GroupsOutlined";
import AccountBalanceWalletOutlinedIcon from "@mui/icons-material/AccountBalanceWalletOutlined";
import ReceiptLongOutlinedIcon from "@mui/icons-material/ReceiptLongOutlined";
import LocalShippingOutlinedIcon from "@mui/icons-material/LocalShippingOutlined";

const drawerWidth = 280;

const navItems = [
    { to: "/units", label: "Units", icon: <ScaleOutlinedIcon /> },
    { to: "/resources", label: "Resources", icon: <Inventory2OutlinedIcon /> },
    { to: "/clients", label: "Clients", icon: <GroupsOutlinedIcon /> },
    { to: "/balances", label: "Balances", icon: <AccountBalanceWalletOutlinedIcon /> },
    { to: "/receipts", label: "Receipts", icon: <ReceiptLongOutlinedIcon /> },
    { to: "/shipments", label: "Shipments", icon: <LocalShippingOutlinedIcon /> },
];

export default function AppLayout() {
    const user = { name: "Admin", role: "ADMIN" };
    const initial = user.name?.slice(0, 1).toUpperCase() || "U";

    return (
        <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "#F4F6FB" }}>
            {/* Sidebar */}
            <Drawer
                variant="permanent"
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    "& .MuiDrawer-paper": {
                        width: drawerWidth,
                        boxSizing: "border-box",
                        borderRadius: 0,
                        height: "100vh",
                        overflow: "hidden",
                        borderRight: "1px solid rgba(17,24,39,0.10)",
                        bgcolor: "#0B1220",
                        color: "rgba(255,255,255,0.86)",
                        px: 2,
                        py: 2,
                    },
                }}
            >
                {/* Brand */}
                <Stack direction="row" alignItems="center" spacing={1.5} sx={{ px: 1, py: 1 }}>
                    <Avatar
                        sx={{
                            width: 40,
                            height: 40,
                            bgcolor: "rgba(139,92,246,0.22)",
                            color: "#C4B5FD",
                            fontWeight: 900,
                        }}
                    >
                        W
                    </Avatar>
                    <Box>
                        <Typography sx={{ fontWeight: 900, lineHeight: 1.1, color: "#fff" }}>
                            Warehouse
                        </Typography>
                        <Typography variant="caption" sx={{ color: "rgba(255,255,255,0.55)" }}>
                            Admin panel
                        </Typography>
                    </Box>
                </Stack>

                {/* Nav */}
                <Box sx={{ mt: 2 }}>
                    <List sx={{ display: "flex", flexDirection: "column", gap: 1 }}>
                        {navItems.map((it) => (
                            <ListItemButton
                                key={it.to}
                                component={NavLink}
                                to={it.to}
                                end
                                sx={{
                                    borderRadius: 999,
                                    px: 2,
                                    py: 1.25,
                                    color: "rgba(255,255,255,0.78)",
                                    "& .MuiListItemIcon-root": {
                                        color: "rgba(255,255,255,0.70)",
                                        minWidth: 42,
                                    },
                                    "&:hover": { bgcolor: "rgba(255,255,255,0.06)" },
                                    "&.active": {
                                        bgcolor: "rgba(139,92,246,0.18)",
                                        color: "#fff",
                                        "& .MuiListItemIcon-root": { color: "#C4B5FD" },
                                    },
                                }}
                            >
                                <ListItemIcon>{it.icon}</ListItemIcon>
                                <ListItemText primary={it.label} primaryTypographyProps={{ fontWeight: 800 }} />
                            </ListItemButton>
                        ))}
                    </List>
                </Box>

                <Box sx={{ flex: 1 }} />

                <Divider sx={{ borderColor: "rgba(255,255,255,0.10)", my: 2 }} />

                {/* User card */}
                <Stack
                    direction="row"
                    alignItems="center"
                    spacing={1.5}
                    sx={{
                        p: 1.25,
                        borderRadius: 2,
                        bgcolor: "rgba(255,255,255,0.04)",
                        border: "1px solid rgba(255,255,255,0.08)",
                    }}
                >
                    <Avatar sx={{ bgcolor: "rgba(255,255,255,0.12)", color: "#fff" }}>{initial}</Avatar>
                    <Box sx={{ minWidth: 0 }}>
                        <Typography sx={{ fontWeight: 900, color: "#fff", lineHeight: 1.1 }} noWrap>
                            {user.name}
                        </Typography>
                        <Typography variant="caption" sx={{ color: "rgba(255,255,255,0.60)" }} noWrap>
                            {user.role}
                        </Typography>
                    </Box>
                </Stack>
            </Drawer>

            {/* main */}
            <Box
                component="main"
                sx={{
                    flex: 1,
                    minWidth: 0,
                    px: 3,
                    py: 3,
                    bgcolor: "#F4F6FB",
                    backgroundImage: "none",
                }}
            >
                <Outlet />
            </Box>
        </Box>
    );
}