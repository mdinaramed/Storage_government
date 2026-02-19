import * as React from "react";
import { Box, Paper, Stack } from "@mui/material";

type Props = {
    left: React.ReactNode;
    right?: React.ReactNode;
};

export default function ToolbarCard({ left, right }: Props) {
    return (
        <Paper sx={{ p: 2, mb: 2 }}>
            <Stack direction="row" spacing={2} alignItems="center" sx={{ minWidth: 0 }}>
                <Box sx={{ display: "flex", gap: 16 / 8, alignItems: "center", minWidth: 0, flexWrap: "wrap" }}>
                    {left}
                </Box>

                <Box sx={{ flex: 1 }} />

                {right ? (
                    <Stack direction="row" spacing={1.25} alignItems="center">
                        {right}
                    </Stack>
                ) : null}
            </Stack>
        </Paper>
    );
}