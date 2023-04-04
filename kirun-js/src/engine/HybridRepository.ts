import { Repository } from './Repository';

export class HybridRepository<T> implements Repository<T> {
    repos: Repository<T>[];

    constructor(...repos: Repository<T>[]) {
        this.repos = repos;
    }

    public find(namespace: string, name: string): T | undefined {
        for (let repo of this.repos) {
            let s = repo.find(namespace, name);
            if (s) return s;
        }

        return undefined;
    }

    public filter(name: string): string[] {
        let result= new Set<string>;
        for (let repo of this.repos) {
            repo.filter(name).forEach(e => result.add(e));
        }
        return Array.from(result);
    }
}
