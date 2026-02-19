import { http } from "./http";
import type { Balance } from "../types/balance";
import { toQuery } from "./query";

type ListParams = {
    resourceIds?: number[];
    unitIds?: number[];
};

export const balancesApi = {
    async list(params: ListParams = {}): Promise<Balance[]> {
        const qs = toQuery({
            resourceIds: params.resourceIds ?? [],
            unitIds: params.unitIds ?? [],
        });

        const { data } = await http.get<Balance[]>(`/balances${qs}`);
        return data;
    },
};