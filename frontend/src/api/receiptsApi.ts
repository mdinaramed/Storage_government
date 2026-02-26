import { http } from "./http";
import type { Receipt, ReceiptItem } from "../types/receipt";

export type ReceiptListParams = {
    from?: string;
    to?: string;
    numbers?: string[];
    resourceIds?: number[];
    unitIds?: number[];
};

export type ReceiptPayload = {
    number: string;
    date: string; // YYYY-MM-DD
    items: ReceiptItem[];
};

export const receiptsApi = {
    async list(params?: ReceiptListParams): Promise<Receipt[]> {
        const { data } = await http.get<Receipt[]>("/receipts", { params });
        return data;
    },

    async get(id: number): Promise<Receipt> {
        const { data } = await http.get<Receipt>(`/receipts/${id}`);
        return data;
    },

    async create(payload: ReceiptPayload): Promise<Receipt> {
        const { data } = await http.post<Receipt>("/receipts", payload);
        return data;
    },

    async update(id: number, payload: ReceiptPayload): Promise<Receipt> {
        const { data } = await http.put<Receipt>(`/receipts/${id}`, payload);
        return data;
    },

    async remove(id: number): Promise<void> {
        await http.delete(`/receipts/${id}`);
    },
};