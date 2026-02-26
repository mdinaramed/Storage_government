export type ShipmentState = "DRAFT" | "SIGNED";

export type ShipmentItem = {
    resourceId: number;
    unitId: number;
    quantity: number;
};

export type Shipment = {
    id: number;
    number: string;
    date: string; // YYYY-MM-DD
    clientId: number;
    state: ShipmentState;
    items: ShipmentItem[];
};