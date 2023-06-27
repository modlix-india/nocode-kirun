export interface Repository<T> {
    find(namespace: string, name: string): Promise<T | undefined>;
    filter(name: string): Promise<string[]>;
}
