import { LinkedList } from './LinkedList';

export function deepEqual(x: any, y: any) {
    let xa = new LinkedList();
    xa.push(x);
    let yb = new LinkedList();
    yb.push(y);

    while (!xa.isEmpty() && !yb.isEmpty()) {
        const a: any = xa.pop();
        const b: any = yb.pop();

        if (a === b) continue;

        const typeOfA = typeof a;
        const typeOfB = typeof b;

        if (typeOfA === 'undefined' || typeOfB === 'undefined') {
            if (!a && !b) continue;
            return false;
        }

        if (typeOfA !== typeOfB) return false;

        if (Array.isArray(a)) {
            if (!Array.isArray(b) || a.length != b.length) return false;
            for (let i = 0; i < a.length; i++) {
                xa.push(a[i]);
                yb.push(b[i]);
            }
            continue;
        }

        if (typeOfA === 'object') {
            const entriesOfA = Object.entries(a);
            const entriesOfB = Object.entries(b);
            if (entriesOfA.length !== entriesOfB.length) return false;
            for (const [k, v] of entriesOfA) {
                xa.push(v);
                yb.push(b[k]);
            }

            continue;
        }
        return false;
    }

    return true;
}
