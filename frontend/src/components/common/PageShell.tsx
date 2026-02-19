import * as React from "react";
import { Box, Stack, Typography } from "@mui/material";

type Props = {
    title: string;
    subtitle?: string;
    actions?: React.ReactNode;
    children: React.ReactNode;
};

export default function PageShell({ title, subtitle, actions, children }: Props) {
    return (
        <Box sx={{ width: "100%" }}>
            <Stack
                direction="row"
                alignItems="flex-start"
                justifyContent="space-between"
                spacing={2}
                sx={{ mb: 2 }}
            >
                <Box>
                    <Typography variant="h2" sx={{ fontWeight: 950, letterSpacing: -1 }}>
                        {title}
                    </Typography>
                    {subtitle ? (
                        <Typography variant="body2" sx={{ mt: 0.5, color: "text.secondary" }}>
                            {subtitle}
                        </Typography>
                    ) : null}
                </Box>

                {actions ? <Box sx={{ pt: 0.5 }}>{actions}</Box> : null}
            </Stack>

            {children}
        </Box>
    );
}