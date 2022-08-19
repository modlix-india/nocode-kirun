import { isNullValue } from './NullCheck';

export class MapUtil {
    public static of<K, V>(
        k1?: K,
        v1?: V,
        k2?: K,
        v2?: V,
        k3?: K,
        v3?: V,
        k4?: K,
        v4?: V,
        k5?: K,
        v5?: V,
        k6?: K,
        v6?: V,
        k7?: K,
        v7?: V,
        k8?: K,
        v8?: V,
        k9?: K,
        v9?: V,
        k10?: K,
        v10?: V,
    ): Map<K, V> {
        const map: Map<K, V> = new Map();

        if (!isNullValue(k1) && !isNullValue(v1)) map.set(k1!, v1!);

        if (!isNullValue(k2) && !isNullValue(v2)) map.set(k2!, v2!);

        if (!isNullValue(k3) && !isNullValue(v3)) map.set(k3!, v3!);

        if (!isNullValue(k4) && !isNullValue(v4)) map.set(k4!, v4!);

        if (!isNullValue(k5) && !isNullValue(v5)) map.set(k5!, v5!);

        if (!isNullValue(k6) && !isNullValue(v6)) map.set(k6!, v6!);

        if (!isNullValue(k7) && !isNullValue(v7)) map.set(k7!, v7!);

        if (!isNullValue(k8) && !isNullValue(v8)) map.set(k8!, v8!);

        if (!isNullValue(k9) && !isNullValue(v9)) map.set(k9!, v9!);

        if (!isNullValue(k10) && !isNullValue(v10)) map.set(k10!, v10!);

        return map;
    }

    public static ofArrayEntries<K, V>(...entry: [K, V][]): Map<K, V> {
        const map: Map<K, V> = new Map();

        for (const [k, v] of entry) {
            map.set(k, v);
        }

        return map;
    }

    public static entry<K, V>(k: K, v: V): MapEntry<K, V> {
        return new MapEntry(k, v);
    }

    public static ofEntries<K, V>(...entry: MapEntry<K, V>[]): Map<K, V> {
        const map: Map<K, V> = new Map();

        for (const eachEntry of entry) {
            map.set(eachEntry.k, eachEntry.v);
        }

        return map;
    }

    private constructor() {}
}

export class MapEntry<K, V> {
    k: K;
    v: V;

    public constructor(k: K, v: V) {
        this.k = k;
        this.v = v;
    }
}
