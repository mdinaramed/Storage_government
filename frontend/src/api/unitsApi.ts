import { http } from "./http";
import type { Unit } from "../types/unit";
import type { EntityState } from "../types/common";

export const unitsApi = {
    list(q?: string, state?: EntityState) {
        return http.get<Unit[]>("/units", { params: { q, state } }).then((r) => r.data);
    },
    create(payload: { name: string }) {
        return http.post<Unit>("/units", payload).then((r) => r.data);
    },
    update(id: number, payload: { name: string }) {
        return http.put<Unit>(`/units/${id}`, payload).then((r) => r.data);
    },
    archive(id: number) {
        return http.post<Unit>(`/units/${id}/archive`).then((r) => r.data);
    },
    activate(id: number) {
        return http.post<Unit>(`/units/${id}/activate`).then((r) => r.data);
    },
    remove(id: number) {
        return http.delete<void>(`/units/${id}`).then((r) => r.data);
    },
};