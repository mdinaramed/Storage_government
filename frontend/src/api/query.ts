export type QueryValue =
    | string
    | number
    | boolean
    | null
    | undefined
    | Date
    | Array<string | number | boolean | null | undefined | Date>;

export type QueryObject = Record<string, QueryValue>;
export function toQueryString(params: QueryObject = {}): string {
    const sp = new URLSearchParams();

    for (const [key, value] of Object.entries(params)) {
        if (value === null || value === undefined) continue;

        if (Array.isArray(value)) {
            for (const v0 of value) {
                if (v0 === null || v0 === undefined) continue;
                const v =
                    v0 instanceof Date ? v0.toISOString().slice(0, 10) : String(v0).trim();
                if (!v) continue;
                sp.append(key, v);
            }
            continue;
        }

        const v =
            value instanceof Date ? value.toISOString().slice(0, 10) : String(value).trim();
        if (!v) continue;
        sp.append(key, v);
    }

    return sp.toString();
}

export function toQuery(params: QueryObject = {}): string {
    const qs = toQueryString(params);
    return qs ? `?${qs}` : "";
}