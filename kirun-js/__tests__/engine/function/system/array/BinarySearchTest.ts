import { BinarySearch } from '../../../../../src/engine/function/system/array/BinarySearch';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

let bsearch: BinarySearch = new BinarySearch();

test('Binary Search test 1', () => {
    let src: any[] = [1, 4, 6, 7, 10, 14, 16, 20];

    let search: any = 16;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), src],
            [BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), 1],
            [BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), search],
            [BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), 6],
        ]),
    );

    expect(
        bsearch.execute(fep).allResults()[0].getResult().get(BinarySearch.EVENT_INDEX.getName()),
    ).toBe(6);
});

test('Binary Search test 2', () => {
    let src: any[] = [1, 4, 6, 7, 10, 14, 16, 20];

    let search: any = 78;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), src],
            [BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), 1],
            [BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), search],
            [BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), src.length - 2],
        ]),
    );

    expect(
        bsearch.execute(fep).allResults()[0].getResult().get(BinarySearch.EVENT_INDEX.getName()),
    ).toBe(-1);
});

test('Binary Search test 3', () => {
    let src: any[] = [1, 4, 6, 7, 10, 14, 16, 20];

    let search: any = 78;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), src],
            [BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), 1],
            [BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), search],
            [BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), 100],
        ]),
    );

    expect(() => bsearch.execute(fep)).toThrow();
});

test('Binary Search test 6', () => {
    let src: any[] = [1, 4, 6, 7, 10, 14, 17, 20];

    let search: number = 17;

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), src],
            [BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), 0],
            [BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), search],
            [BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), 5],
        ]),
    );

    expect(
        bsearch.execute(fep).allResults()[0].getResult().get(BinarySearch.EVENT_INDEX.getName()),
    ).toBe(-1);
});

// test('Binary Search test 7', () => {
//     let src: any[] = [1, 4, 6, 7, 10, 14, 16, 20];

//     let search: any[] = [10];

//     let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
//         new Map<string, any>([
//             [BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), src],
//             [BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), 1],
//             [BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), search],
//             [BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), 5],
//         ]),
//     );

//     expect(bsearch.execute(fep)).toThrow();
// });

test('Binary Search test 4', () => {
    let src: any[] = ['a', 'b', 'd', 'f', 'h', 'k', 'z'];

    let search: any = 'z';

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), src],

            [BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), search],
        ]),
    );

    expect(
        bsearch.execute(fep).allResults()[0].getResult().get(BinarySearch.EVENT_INDEX.getName()),
    ).toBe(src.length - 1);
});

test('Binary Search test 5', () => {
    let arr: any[] = [];
    arr.push('a');
    arr.push('b');
    arr.push('c');
    arr.push('d');
    arr.push('e');
    arr.push('g');
    arr.push('i');
    arr.push('j');
    arr.push('k');
    arr.push('r');
    arr.push('s');
    arr.push('z');

    let search: any = 's';

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters().setArguments(
        new Map<string, any>([
            [BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr],

            [BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), search],
            [BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), arr.length - 1],
        ]),
    );

    expect(
        bsearch.execute(fep).allResults()[0].getResult().get(BinarySearch.EVENT_INDEX.getName()),
    ).toBe(10);
});
