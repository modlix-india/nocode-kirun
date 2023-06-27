import { Repository } from './Repository';

export class HybridRepository<T> implements Repository<T> {
    repos: Repository<T>[];

    constructor(...repos: Repository<T>[]) {
        this.repos = repos;
    }

    public async find(namespace: string, name: string): Promise<T | undefined> {
        for (let repo of this.repos) {
            let s = await repo.find(namespace, name);
            if (s) return s;
        }

        return undefined;
    }

    public async filter(name: string): Promise<string[]> {
        let result = new Set<string>();
        for (let repo of this.repos) {
            (await repo.filter(name)).forEach((e) => result.add(e));
        }
        return Array.from(result);
    }
}
