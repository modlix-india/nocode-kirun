import { Repository } from './Repository';

export class HybridRepository extends Repository {
    repos;

    constructor(...repos) {
        this.repos = repos;
        for (let repo of this.repos) {
        }
    }

    find(namespace, name) {
        for (let repo of this.repos) {
            let s = repo.find(namespace, name);
            if (s) return s;
        }

        return null;
    }
}
