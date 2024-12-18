export function duplicate(obj: any): any {
    if (!obj) return obj;
    if (typeof globalThis.structuredClone === 'function') return globalThis.structuredClone(obj);
    return JSON.parse(JSON.stringify(obj));
}
