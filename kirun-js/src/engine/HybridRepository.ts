import { Repository } from './Repository';

export class HybridRepository<T> implements Repository<T> {
    repos: Repository<T>[];

    constructor(...repos: Repository<T>[]) {
        this.repos = repos;
    }

    find(namespace: string, name: string): T | undefined {
        for (let repo of this.repos) {
            let s = repo.find(namespace, name);
            if (s) return s;
        }

        return undefined;
    }
}
