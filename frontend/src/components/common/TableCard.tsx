import * as React from "react";
import { Box, Paper } from "@mui/material";

type Props = {
    height?: number;
    children: React.ReactNode;
};

export default function TableCard({ height = 640, children }: Props) {
    return (
        <Paper sx={{ overflow: "hidden" }}>
            <Box sx={{ height, width: "100%" }}>{children}</Box>
        </Paper>
    );
}