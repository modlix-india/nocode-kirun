export function duplicate(obj: any): any {
    if (!obj) return obj;
    if (structuredClone) return structuredClone(obj);
    return JSON.parse(JSON.stringify(obj));
}
