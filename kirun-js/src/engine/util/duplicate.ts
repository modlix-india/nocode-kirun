export function duplicate(obj: any): any {
    if (!obj) return obj;
    if (typeof (globalThis as any).structuredClone === 'function') {
        return (globalThis as any).structuredClone(obj);
    }
    return JSON.parse(JSON.stringify(obj));
}
