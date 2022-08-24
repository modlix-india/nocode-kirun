import { SchemaValidationException } from '../../../../../src';
import { AddFirst } from '../../../../../src/engine/function/system/array/AddFirst';

import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

test('Add Test 1', async () => {
    let addFirst: AddFirst = new AddFirst();

    let source: any[] = ['c', 'p', 3, 4, 5];

    let temp: any = 'a';

    let temp2: any[] = ['a', 'c', 'p', 3, 4, 5];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [AddFirst.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [AddFirst.PARAMETER_ANY_NOT_NULL.getParameterName(), temp],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await addFirst.execute(fep);

    expect(source).toStrictEqual(temp2);
});

test('Add Test 2', async () => {
    let addFirst: AddFirst = new AddFirst();

    let source: any[] = ['a', 'b', 'c', 'd', 'a', 'b', 'c', 'e', 'd'];

    let temp: any = 'surendhar';

    let temp2: any[] = ['surendhar', 'a', 'b', 'c', 'd', 'a', 'b', 'c', 'e', 'd'];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [AddFirst.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [AddFirst.PARAMETER_ANY_NOT_NULL.getParameterName(), temp],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await addFirst.execute(fep);

    expect(source).toStrictEqual(temp2);
});

test('Add Test 3', async () => {
    let addFirst: AddFirst = new AddFirst();

    let source: any[] = [
        'doing',
        'test',
        'Driven',
        'developement',
        'I',
        'am',
        'using',
        'eclipse',
        'I',
        'to',
    ];

    let temp: any = 'surendhar';

    let temp2: any[] = [
        'surendhar',
        'doing',
        'test',
        'Driven',
        'developement',
        'I',
        'am',
        'using',
        'eclipse',
        'I',
        'to',
    ];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [AddFirst.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [AddFirst.PARAMETER_ANY_NOT_NULL.getParameterName(), temp],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await addFirst.execute(fep);

    expect(source).toStrictEqual(temp2);
});

test('Add Test 5', async () => {
    let addFirst: AddFirst = new AddFirst();

    let source1: any[] = [
        'doing',
        'test',
        'Driven',
        'developement',
        'I',
        'am',
        'using',
        'eclipse',
        'I',
        'to',
    ];

    let source2: any[] = [
        'doing',
        'test',
        'Driven',
        'developement',
        'I',
        'am',
        'using',
        'eclipse',
        'I',
        'to',
        'with',
        'typescript',
    ];

    let source3: any[] = [
        'doing',
        'test',
        'Driven',
        'developement',
        'I',
        'am',
        'using',
        'eclipse',
        'I',
        'to',
        'with',
        'typescript',
        'newone',
        'framework',
    ];

    let source4: any[] = ['am', 'using', 'eclipse', 'I', 'to', 'with', 'typescript', 'newone'];

    let source: any[] = [
        source1,
        source4,
        source1,
        source2,
        source3,
        source4,
        source1,
        source2,
        source3,
    ];

    var obj = {
        fname: 'surendhar',
        lname: 's',
        age: 23,
        company: ' Fincity Corporation ',
    };

    let temp2: any[] = [
        obj,
        source1,
        source4,
        source1,
        source2,
        source3,
        source4,
        source1,
        source2,
        source3,
    ];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map<string, any>([
                [AddFirst.PARAMETER_ARRAY_SOURCE.getParameterName(), source],
                [AddFirst.PARAMETER_ANY_NOT_NULL.getParameterName(), obj],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await addFirst.execute(fep);

    expect(source).toStrictEqual(temp2);
});

test('Add Test 4', async () => {
    let addFirst: AddFirst = new AddFirst();

    let temp: any = 'a';

    let temp2: any[] = ['a', 'c', 'p', 3, 4, 5];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [AddFirst.PARAMETER_ARRAY_SOURCE.getParameterName(), null],
                [AddFirst.PARAMETER_ANY_NOT_NULL.getParameterName(), temp],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await expect(() => addFirst.execute(fep)).rejects.toThrow();
});

test('Add Test 5', async () => {
    let addFirst: AddFirst = new AddFirst();

    let temp: any = null;

    let temp2: any[] = ['a', 'c', 'p', 3, 4, 5];

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(
            new Map([
                [AddFirst.PARAMETER_ARRAY_SOURCE.getParameterName(), temp2],
                [AddFirst.PARAMETER_ANY_NOT_NULL.getParameterName(), temp],
            ]),
        )
        .setSteps(new Map([]))
        .setContext(new Map([]));

    await expect(addFirst.execute(fep)).rejects.toThrow();
});
