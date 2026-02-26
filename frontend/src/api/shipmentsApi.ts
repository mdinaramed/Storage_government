import { http } from "./http";
import type { Shipment } from "../types/shipment";

export type ShipmentPayload = {
    number: string;
    date: string;
    clientId: number;
    items: Array<{ resourceId: number; unitId: number; quantity: number }>;
};

export type ShipmentsListParams = {
    dateFrom?: string;
    dateTo?: string;
    numbers?: string[];
    resourceIds?: number[];
    unitIds?: number[];
    clientId?: number;
    state?: string;
};

export const shipmentsApi = {
    list(params?: ShipmentsListParams) {
        return http.get<Shipment[]>("/shipments", { params }).then((r) => r.data);
    },
    numbers() {
        return http.get<string[]>("/shipments/numbers").then((r) => r.data);
    },
    get(id: number) {
        return http.get<Shipment>(`/shipments/${id}`).then((r) => r.data);
    },
    create(payload: ShipmentPayload) {
        return http.post<Shipment>("/shipments", payload).then((r) => r.data);
    },
    update(id: number, payload: ShipmentPayload) {
        return http.put<void>(`/shipments/${id}`, payload).then((r) => r.data);
    },
    remove(id: number) {
        return http.delete<void>(`/shipments/${id}`).then((r) => r.data);
    },
    sign(id: number) {
        return http.post<void>(`/shipments/${id}/sign`).then((r) => r.data);
    },
    revoke(id: number) {
        return http.post<void>(`/shipments/${id}/revoke`).then((r) => r.data);
    },
};