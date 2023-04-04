export interface Repository<T> {
    find(namespace: string, name: string): T | undefined;
    filter(name:string): string[];
}
