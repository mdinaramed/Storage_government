import * as React from "react";
import { Alert, Snackbar } from "@mui/material";

export type Severity = "success" | "error" | "warning" | "info";

type SnackbarState = {
    open: boolean;
    message: string;
    severity: Severity;
};

type Ctx = {
    notify: (message: string, severity?: Severity) => void;
};

const SnackbarContext = React.createContext<Ctx | null>(null);

export function useAppSnackbar(): Ctx {
    const ctx = React.useContext(SnackbarContext);
    if (!ctx) throw new Error("useAppSnackbar must be used inside AppSnackbarProvider");
    return ctx;
}

export function AppSnackbarProvider({ children }: { children: React.ReactNode }) {
    const [state, setState] = React.useState<SnackbarState>({
        open: false,
        message: "",
        severity: "info",
    });

    const notify = React.useCallback((message: string, severity: Severity = "info") => {
        setState({ open: true, message, severity });
    }, []);

    const close = React.useCallback(() => {
        setState((s) => ({ ...s, open: false }));
    }, []);

    return (
        <SnackbarContext.Provider value={{ notify }}>
            {children}
            <Snackbar
                open={state.open}
                autoHideDuration={3500}
                onClose={close}
                anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
            >
                <Alert onClose={close} severity={state.severity} variant="filled" sx={{ width: "100%" }}>
                    {state.message}
                </Alert>
            </Snackbar>
        </SnackbarContext.Provider>
    );
}