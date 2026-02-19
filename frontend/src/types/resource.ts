import type { EntityState } from "./common";

export type Resource = {
    id: number;
    name: string;
    state: EntityState;
};