export type ReceiptItem = {
    resourceId: number;
    unitId: number;
    quantity: number;
};

export type Receipt = {
    id: number;
    number: string;
    date: string; // "YYYY-MM-DD"
    items?: ReceiptItem[];
};