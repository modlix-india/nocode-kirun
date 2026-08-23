import { deepEqual } from '../../../src/engine/util/deepEqual';

const data = [
    {
        name: 'Single Array',
        data: [0, 10, 20, 30, 40, 50],
    },
    {
        name: 'Single Array With Labels',
        data: [0, 10, 20, 30, 40, 50],
        labels: ['First', 'Second', 'Third', 'Fourth', 'Fifth', 'Sixth'],
    },
    {
        name: 'Multiple Arrays',
        data: [
            [-10, 10, 20, 30, 40, 50],
            [-20, -10, 0, null, 20, 30],
            [-60, -30, 10, 100, 20, 30],
        ],
    },
    {
        name: 'Simple Objects With Labels',
        data: [
            {
                x: 'First',
                y: 0,
            },
            {
                x: 'Second',
                y: 10,
            },
            {
                x: 'Third',
                y: 20,
            },
            {
                x: 'Fourth',
                y: 30,
            },
            {
                x: 'Fifth',
                y: 40,
            },
            {
                x: 'Sixth',
                y: 50,
            },
        ],
    },
    {
        name: 'Multiple Objects With Labels',
        data: [
            {
                x: 'First',
                y: 0,
                z: -100,
            },
            {
                x: 'Second',
                y: 10,
                z: -20,
            },
            {
                x: 'Third',
                y: 20,
                z: 30,
            },
            {
                x: 'Fourth',
                y: 30,
                z: 40,
            },
            {
                x: 'Fifth',
                y: 40,
                z: 10,
            },
            {
                x: 'Sixth',
                y: 50,
                z: 80,
            },
        ],
    },
];

test('deepEqual', () => {
    expect(deepEqual(null, null)).toBeTruthy();
    expect(deepEqual(null, data)).toBeFalsy();
    expect(deepEqual(data[0], data[0])).toBeTruthy();
    expect(deepEqual(data[0], data[1])).toBeFalsy();
    expect(deepEqual(data[0], data[2])).toBeFalsy();
    expect(deepEqual(data[0], data[3])).toBeFalsy();
    expect(deepEqual(data[0], data[4])).toBeFalsy();
    expect(deepEqual(true, false)).toBeFalsy();
    expect(deepEqual(2, 2)).toBeTruthy();
    expect(deepEqual(null, undefined)).toBeTruthy();
    expect(deepEqual([1, 2, 3, { a: [3, 4] }], [1, 2, 3, { a: [3, 4] }])).toBeTruthy();
    expect(deepEqual([1, 2, 3, { a: [3, 4] }], [1, 2, 3, { a: [3] }])).toBeFalsy();
    expect(deepEqual([1, 2, 3, { a: [3, 4] }], [1, 2, 3, { a: [3, '4'] }])).toBeFalsy();
    expect(
        deepEqual([1, 2, 3, { a: [3, 4], b: null }], [1, 2, 3, { a: [3, 4], b: undefined }]),
    ).toBeTruthy();
});

test('deepEqual key sets of the same size but different names', () => {
    expect(deepEqual({ x: 1 }, { y: 1 })).toBeFalsy();
    expect(deepEqual({ a: 1, b: 2 }, { a: 1, c: 2 })).toBeFalsy();
    // a present-but-undefined key still counts towards the key set
    expect(deepEqual({ a: 1 }, { a: 1, b: undefined })).toBeFalsy();
});

test('deepEqual does not conflate falsy values of different types', () => {
    expect(deepEqual(0, '')).toBeFalsy();
    expect(deepEqual(0, false)).toBeFalsy();
    expect(deepEqual({ a: 0 }, { a: false })).toBeFalsy();
    expect(deepEqual([1], null)).toBeFalsy();
    expect(deepEqual(null, [1])).toBeFalsy();
});

// An array is never equal to a plain object, in either direction. This matches
// kirun-java (LogicalEqualOperator guards both directions explicitly) and
// kirun-py (deep_equal rejects on type(a) is not type(b)).
test('deepEqual never equates an object with an array', () => {
    expect(deepEqual([], {})).toBeFalsy();
    expect(deepEqual({}, [])).toBeFalsy();
    expect(deepEqual([1], { 0: 1 })).toBeFalsy();
    expect(deepEqual({ 0: 1 }, [1])).toBeFalsy();
    expect(deepEqual([1], { a: 1 })).toBeFalsy();
    expect(deepEqual({ a: 1 }, [1])).toBeFalsy();
    // nested, not just at the top level
    expect(deepEqual({ v: {} }, { v: [] })).toBeFalsy();
    expect(deepEqual({ v: [] }, { v: {} })).toBeFalsy();
    expect(deepEqual([[]], [{}])).toBeFalsy();
    // and the cases that must keep working
    expect(deepEqual([], [])).toBeTruthy();
    expect(deepEqual({}, {})).toBeTruthy();
    expect(deepEqual({ v: [1, 2] }, { v: [1, 2] })).toBeTruthy();
});

test('deepEqual short circuits on shared references', () => {
    const shared = { big: Array.from({ length: 1000 }, (_, i) => ({ i })) };
    expect(deepEqual({ v: shared }, { v: shared })).toBeTruthy();
    expect(deepEqual({ v: shared }, { v: { ...shared } })).toBeTruthy();
});

test('deepEqual handles deeply nested values without overflowing the stack', () => {
    const chain = (n: number) => {
        const root: any = {};
        let cur = root;
        for (let i = 0; i < n; i++) {
            cur.next = {};
            cur = cur.next;
        }
        cur.value = 1;
        return root;
    };
    expect(deepEqual(chain(50000), chain(50000))).toBeTruthy();

    const nestedArray = (n: number) => {
        let a: any = [1];
        for (let i = 0; i < n; i++) a = [a];
        return a;
    };
    expect(deepEqual(nestedArray(50000), nestedArray(50000))).toBeTruthy();
});
