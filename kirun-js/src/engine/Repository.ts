export interface Repository<T> {
    find(namespace: string, name: string): T | undefined;
}
