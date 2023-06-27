export function duplicate(obj: any): any {
    if (!obj) return obj;
    if (globalThis.structuredClone) return globalThis.structuredClone(obj);
    return JSON.parse(JSON.stringify(obj));
}
