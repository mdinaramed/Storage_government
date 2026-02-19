import { http } from "./http";
import type { Shipment } from "../types/shipment";

export type ShipmentPayload = {
    number: string;
    date: string;
    clientId: number;
    items: Array<{ resourceId: number; unitId: number; quantity: number }>;
};

export const shipmentsApi = {
    list() {
        return http.get<Shipment[]>("/shipments").then((r) => r.data);
    },
    get(id: number) {
        return http.get<Shipment>(`/shipments/${id}`).then((r) => r.data);
    },
    create(payload: ShipmentPayload) {
        return http.post<Shipment>("/shipments", payload).then((r) => r.data);
    },
    update(id: number, payload: ShipmentPayload) {
        return http.put<Shipment>(`/shipments/${id}`, payload).then((r) => r.data);
    },
    remove(id: number) {
        return http.delete<void>(`/shipments/${id}`).then((r) => r.data);
    },
    sign(id: number) {
        return http.post<Shipment>(`/shipments/${id}/sign`).then((r) => r.data);
    },
    revoke(id: number) {
        return http.post<Shipment>(`/shipments/${id}/revoke`).then((r) => r.data);
    },
};