export type EntityState = "ACTIVE" | "ARCHIVED";
export type StateFilter = "ALL" | EntityState;

export function toApiState(filter: StateFilter): EntityState | undefined {
    return filter === "ALL" ? undefined : filter;
}