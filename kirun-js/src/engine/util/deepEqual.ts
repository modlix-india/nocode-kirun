import { LinkedList } from './LinkedList';

/**
 * Structural equality.
 *
 * Walks iteratively with an explicit worklist so that deeply nested values
 * cannot overflow the call stack.
 *
 * Keys are read with Object.keys and indexed directly; Object.entries was
 * allocating a throwaway [key, value] pair for every key of every object
 * visited, which dominated the cost of the comparison.
 *
 * Reference equality is checked at every node, so subtrees that are shared
 * between the two values cost a single pointer comparison.
 *
 * An array and a plain object are never equal, in either direction, matching
 * kirun-java's LogicalEqualOperator and kirun-py's deep_equal.
 */
export function deepEqual(x: any, y: any) {
    let xa = new LinkedList<any>();
    xa.push(x);
    let yb = new LinkedList<any>();
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

        // An array is never equal to a plain object, in either direction. This
        // used to be tested only on the left operand, so an object compared
        // against an array fell through to the object branch and matched on key
        // count - deepEqual({}, []) was true while deepEqual([], {}) was false.
        if (Array.isArray(a) !== Array.isArray(b)) return false;

        if (Array.isArray(a)) {
            if (!Array.isArray(b) || a.length != b.length) return false;
            for (let i = 0; i < a.length; i++) {
                xa.push(a[i]);
                yb.push(b[i]);
            }
            continue;
        }

        if (typeOfA === 'object') {
            if (typeOfB !== 'object' || a === null || b === null) return false;
            const keysOfA = Object.keys(a);
            if (keysOfA.length !== Object.keys(b).length) return false;
            for (let i = 0; i < keysOfA.length; i++) {
                const k = keysOfA[i];
                xa.push(a[k]);
                yb.push(b[k]);
            }

            continue;
        }
        return false;
    }

    return true;
}
