import axios, { type AxiosInstance } from "axios";
import { attachInterceptors } from "./errors";
import { toQueryString, type QueryObject } from "./query";

const baseURL = import.meta.env.VITE_API_URL ?? "http://localhost:8081/api";

export const http: AxiosInstance = axios.create({
    baseURL,
    headers: { "Content-Type": "application/json" },

    paramsSerializer: {
        serialize: (params: unknown): string => toQueryString((params ?? {}) as QueryObject),
    },
});

attachInterceptors(http);