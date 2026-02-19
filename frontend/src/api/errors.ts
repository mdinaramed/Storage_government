import type { AxiosError, AxiosInstance } from "axios";

type BackendErrorShape = {
    message?: unknown;
    error?: unknown;
    validationErrors?: unknown;
};

function asString(x: unknown): string | undefined {
    return typeof x === "string" ? x : undefined;
}

function buildUserMessage(err: AxiosError): string {
    const status = err.response?.status;
    const data = err.response?.data as BackendErrorShape | undefined;

    const backendMsg =
        asString(data?.message) ??
        asString(data?.error);

    if (err.code === "ERR_NETWORK") return "Backend is not reachable (network error).";
    if (status === 400) return backendMsg ?? "Bad request.";
    if (status === 401) return "Unauthorized.";
    if (status === 403) return "Forbidden.";
    if (status === 404) return "Not found.";
    if (status === 409) return backendMsg ?? "Conflict.";
    if (status && status >= 500) return backendMsg ?? "Server error.";
    return backendMsg ?? "Request failed.";
}

export function attachInterceptors(http: AxiosInstance) {
    http.interceptors.response.use(
        (r) => r,
        (error: AxiosError) => {
            (error as unknown as { userMessage?: string }).userMessage = buildUserMessage(error);
            return Promise.reject(error);
        }
    );
}