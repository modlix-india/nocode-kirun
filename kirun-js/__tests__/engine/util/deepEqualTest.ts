import { deepEqual } from '../../../src/engine/util/deepEqual';

test('deepEqual', () => {
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
