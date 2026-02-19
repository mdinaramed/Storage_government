import type { EntityState } from "./common";

export type Client = {
    id: number;
    name: string;
    address?: string | null;
    state: EntityState;
};