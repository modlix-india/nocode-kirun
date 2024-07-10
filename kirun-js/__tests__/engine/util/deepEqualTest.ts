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
