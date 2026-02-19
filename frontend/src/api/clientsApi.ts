import { http } from "./http";
import type { Client } from "../types/client";
import type { EntityState } from "../types/common";

export const clientsApi = {
    list(q?: string, state?: EntityState) {
        return http.get<Client[]>("/clients", { params: { q, state } }).then((r) => r.data);
    },
    create(payload: { name: string; address?: string | null }) {
        return http.post<Client>("/clients", payload).then((r) => r.data);
    },
    update(id: number, payload: { name: string; address?: string | null }) {
        return http.put<Client>(`/clients/${id}`, payload).then((r) => r.data);
    },
    archive(id: number) {
        return http.post<Client>(`/clients/${id}/archive`).then((r) => r.data);
    },
    activate(id: number) {
        return http.post<Client>(`/clients/${id}/activate`).then((r) => r.data);
    },
    remove(id: number) {
        return http.delete<void>(`/clients/${id}`).then((r) => r.data);
    },
};