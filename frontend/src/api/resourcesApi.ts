import type { EntityState } from "../types/common";
import type { Resource } from "../types/resource";
import { http } from "./http";

type CreateResourcePayload = { name: string };
type UpdateResourcePayload = { name: string };

export const resourcesApi = {
    async list(q?: string, state?: EntityState): Promise<Resource[]> {
        const params: Record<string, string> = {};
        if (q) params.q = q;
        if (state) params.state = state;

        const res = await http.get<Resource[]>("/resources", { params });
        return res.data;
    },

    async get(id: number): Promise<Resource> {
        const res = await http.get<Resource>(`/resources/${id}`);
        return res.data;
    },

    async create(payload: CreateResourcePayload): Promise<Resource> {
        const res = await http.post<Resource>("/resources", payload);
        return res.data;
    },

    async update(id: number, payload: UpdateResourcePayload): Promise<Resource> {
        const res = await http.put<Resource>(`/resources/${id}`, payload);
        return res.data;
    },

    async archive(id: number): Promise<Resource> {
        const res = await http.post<Resource>(`/resources/${id}/archive`);
        return res.data;
    },

    async activate(id: number): Promise<Resource> {
        const res = await http.post<Resource>(`/resources/${id}/activate`);
        return res.data;
    },

    async remove(id: number): Promise<void> {
        await http.delete(`/resources/${id}`);
    },
};