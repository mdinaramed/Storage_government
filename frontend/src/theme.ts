import { createTheme } from "@mui/material/styles";
import "@mui/x-data-grid/themeAugmentation";

const shadow = (y: number, blur: number, a: number) =>
    `0 ${y}px ${blur}px rgba(15, 23, 42, ${a})`;

export const theme = createTheme({
    palette: {
        mode: "light",
        primary: { main: "#6D28D9" },
        secondary: { main: "#0EA5E9" },
        success: { main: "#16A34A" },
        warning: { main: "#F59E0B" },
        error: { main: "#EF4444" },
        background: {
            default: "#F5F7FB",
            paper: "#FFFFFF",
        },
        text: {
            primary: "#0F172A",
            secondary: "rgba(15, 23, 42, 0.65)",
        },
        divider: "rgba(15, 23, 42, 0.10)",
    },

    shape: { borderRadius: 16 },

    typography: {
        fontFamily:
            'Inter, system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", "Apple Color Emoji", "Segoe UI Emoji"',
        h1: { fontWeight: 900, letterSpacing: -1.2 },
        h2: { fontWeight: 900, letterSpacing: -0.9 },
        h3: { fontWeight: 900, letterSpacing: -0.7 },
        h4: { fontWeight: 850, letterSpacing: -0.4 },
        subtitle1: { fontWeight: 700 },
        button: { textTransform: "none", fontWeight: 800, letterSpacing: 0 },
    },

    shadows: [
        "none",
        shadow(1, 2, 0.06),
        shadow(2, 6, 0.07),
        shadow(6, 18, 0.08),
        shadow(10, 28, 0.10),
        shadow(16, 40, 0.12),
        ...Array.from({ length: 19 }, () => shadow(18, 48, 0.12)),
    ] as any,

    components: {
        MuiCssBaseline: {
            styleOverrides: {
                body: {
                    backgroundImage: "none",
                    backgroundColor: "#F5F7FB",
                },
            },
        },

        MuiPaper: {
            defaultProps: { elevation: 0 },
            styleOverrides: {
                root: {
                    borderRadius: 18,
                    border: "1px solid rgba(15, 23, 42, 0.10)",
                    boxShadow: "0 10px 30px rgba(15, 23, 42, 0.06)",
                },
            },
        },

        MuiButton: {
            defaultProps: { disableElevation: true },
            styleOverrides: {
                root: {
                    borderRadius: 14,
                    paddingLeft: 16,
                    paddingRight: 16,
                    paddingTop: 10,
                    paddingBottom: 10,
                },
                containedPrimary: {
                    boxShadow: "0 10px 24px rgba(109, 40, 217, 0.22)",
                },
            },
        },

        MuiTextField: { defaultProps: { size: "small" } },
        MuiFormControl: { defaultProps: { size: "small" } },

        MuiOutlinedInput: {
            styleOverrides: {
                root: {
                    borderRadius: 14,
                    backgroundColor: "#FFFFFF",
                },
            },
        },

        MuiChip: {
            styleOverrides: {
                root: {
                    borderRadius: 999,
                    fontWeight: 800,
                },
            },
        },

        MuiDialog: {
            defaultProps: { fullWidth: true, maxWidth: "sm" },
            styleOverrides: {
                paper: {
                    borderRadius: 18,
                    border: "1px solid rgba(15, 23, 42, 0.10)",
                    boxShadow: "0 18px 60px rgba(15, 23, 42, 0.20)",
                },
            },
        },

        MuiDataGrid: {
            styleOverrides: {
                root: {
                    border: "none",
                    backgroundColor: "#FFFFFF",
                },
                columnHeaders: {
                    backgroundColor: "rgba(15, 23, 42, 0.02)",
                    borderBottom: "1px solid rgba(15, 23, 42, 0.10)",
                },
                row: {
                    borderBottom: "1px solid rgba(15, 23, 42, 0.08)",
                },
                cell: {
                    borderBottom: "1px solid rgba(15, 23, 42, 0.08)",
                },
                footerContainer: {
                    borderTop: "1px solid rgba(15, 23, 42, 0.10)",
                },
            },
        },
    },
});