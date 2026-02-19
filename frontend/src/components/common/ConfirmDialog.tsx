import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from "@mui/material";

type Props = {
    open: boolean;
    title: string;
    text: string;
    confirmText?: string;
    cancelText?: string;
    onCancel: () => void;
    onConfirm: () => void;
};

export default function ConfirmDialog({
                                          open,
                                          title,
                                          text,
                                          confirmText = "Confirm",
                                          cancelText = "Cancel",
                                          onCancel,
                                          onConfirm,
                                      }: Props) {
    return (
        <Dialog open={open} onClose={onCancel} maxWidth="xs" fullWidth>
            <DialogTitle sx={{ fontWeight: 900 }}>{title}</DialogTitle>
            <DialogContent>
                <DialogContentText>{text}</DialogContentText>
            </DialogContent>
            <DialogActions sx={{ p: 2 }}>
                <Button variant="outlined" onClick={onCancel}>
                    {cancelText}
                </Button>
                <Button variant="contained" color="error" onClick={onConfirm}>
                    {confirmText}
                </Button>
            </DialogActions>
        </Dialog>
    );
}