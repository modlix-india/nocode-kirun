import {
    EventResult,
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../../src';
import { CountLoop } from '../../../../../src/engine/function/system/loop/CountLoop';

test('Count Loop1', async () => {
    const loop1 = await new CountLoop().execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
            'Test',
        ).setArguments(new Map([['count', 10]])),
    );

    let er: EventResult | undefined;
    let iterations = new Array<number>();
    while ((er = loop1.next())?.getName() !== 'output') {
        iterations.push(er?.getResult().get('index'));
    }

    expect(iterations).toMatchObject([0, 1, 2, 3, 4, 5, 6, 7, 8, 9]);
    expect(er?.getName()).toBe('output');
    expect(er?.getResult().get('value')).toBe(10);
});

test('Count Loop2', async () => {
    const loop1 = await new CountLoop().execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
            'Test',
        ).setArguments(new Map([['count', -1]])),
    );

    let er: EventResult | undefined;
    let iterations = new Array<number>();
    while ((er = loop1.next())?.getName() !== 'output') {
        iterations.push(er?.getResult().get('index'));
    }

    expect(iterations).toMatchObject([]);
    expect(er?.getName()).toBe('output');
    expect(er?.getResult().get('value')).toBe(0);
});

test('Count Loop3', async () => {
    const loop1 = await new CountLoop().execute(
        new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
            'Test',
        ).setArguments(new Map([['count', 0]])),
    );

    let er: EventResult | undefined;
    let iterations = new Array<number>();
    while ((er = loop1.next())?.getName() !== 'output') {
        iterations.push(er?.getResult().get('index'));
    }

    expect(iterations).toMatchObject([]);
    expect(er?.getName()).toBe('output');
    expect(er?.getResult().get('value')).toBe(0);
});

test('Count Loop4', async () => {
    (
        await expect(async () => {
            const loop1 = await new CountLoop().execute(
                new FunctionExecutionParameters(
                    new KIRunFunctionRepository(),
                    new KIRunSchemaRepository(),
                    'Test',
                ).setArguments(new Map([])),
            );
        })
    ).rejects.toThrow();
});
