import type { EntityState } from "./common";

export type Unit = {
    id: number;
    name: string;
    state: EntityState;
};