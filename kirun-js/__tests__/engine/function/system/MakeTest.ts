import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../src';
import { Make } from '../../../../src/engine/function/system/Make';

const make: Make = new Make();

function createOutputMap(data: Record<string, any>): Map<string, Map<string, Map<string, any>>> {
    return new Map([
        [
            'step1',
            new Map([
                [
                    'output',
                    new Map(Object.entries(data)),
                ],
            ]),
        ],
    ]);
}

test('test simple object with expression', async () => {
    const source = { name: 'John', age: 30 };

    const resultShape = {
        fullName: '{{Steps.step1.output.source.name}}',
        userAge: '{{Steps.step1.output.source.age}}',
    };

    const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(new Map<string, any>([['resultShape', resultShape]]));
    fep.setContext(new Map());
    fep.setSteps(createOutputMap({ source }));

    const result = await make.execute(fep);
    const value = result.allResults()[0]?.getResult()?.get('value');

    expect(value).toStrictEqual({
        fullName: 'John',
        userAge: 30,
    });
});

test('test nested object with expressions', async () => {
    const source = {
        user: { firstName: 'John', lastName: 'Doe' },
        address: { city: 'NYC', zip: '10001' },
    };

    const resultShape = {
        person: {
            name: '{{Steps.step1.output.source.user.firstName}}',
            surname: '{{Steps.step1.output.source.user.lastName}}',
        },
        location: {
            cityName: '{{Steps.step1.output.source.address.city}}',
            postalCode: '{{Steps.step1.output.source.address.zip}}',
        },
    };

    const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(new Map<string, any>([['resultShape', resultShape]]));
    fep.setContext(new Map());
    fep.setSteps(createOutputMap({ source }));

    const result = await make.execute(fep);
    const value = result.allResults()[0]?.getResult()?.get('value');

    expect(value).toStrictEqual({
        person: {
            name: 'John',
            surname: 'Doe',
        },
        location: {
            cityName: 'NYC',
            postalCode: '10001',
        },
    });
});

test('test array with expressions', async () => {
    const source = {
        items: ['apple', 'banana', 'cherry'],
    };

    const resultShape = {
        fruits: [
            '{{Steps.step1.output.source.items[0]}}',
            '{{Steps.step1.output.source.items[1]}}',
            '{{Steps.step1.output.source.items[2]}}',
        ],
    };

    const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(new Map<string, any>([['resultShape', resultShape]]));
    fep.setContext(new Map());
    fep.setSteps(createOutputMap({ source }));

    const result = await make.execute(fep);
    const value = result.allResults()[0]?.getResult()?.get('value');

    expect(value).toStrictEqual({
        fruits: ['apple', 'banana', 'cherry'],
    });
});

test('test deeply nested structure with arrays', async () => {
    const source = {
        data: {
            users: [
                { id: 1, name: 'Alice' },
                { id: 2, name: 'Bob' },
            ],
        },
    };

    const resultShape = {
        level1: {
            level2: {
                level3: {
                    userList: [
                        {
                            userId: '{{Steps.step1.output.source.data.users[0].id}}',
                            userName: '{{Steps.step1.output.source.data.users[0].name}}',
                        },
                        {
                            userId: '{{Steps.step1.output.source.data.users[1].id}}',
                            userName: '{{Steps.step1.output.source.data.users[1].name}}',
                        },
                    ],
                },
            },
        },
    };

    const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(new Map<string, any>([['resultShape', resultShape]]));
    fep.setContext(new Map());
    fep.setSteps(createOutputMap({ source }));

    const result = await make.execute(fep);
    const value = result.allResults()[0]?.getResult()?.get('value');

    expect(value).toStrictEqual({
        level1: {
            level2: {
                level3: {
                    userList: [
                        { userId: 1, userName: 'Alice' },
                        { userId: 2, userName: 'Bob' },
                    ],
                },
            },
        },
    });
});

test('test mixed static and dynamic values', async () => {
    const source = { dynamicValue: 'from source' };

    const resultShape = {
        static: 'static string',
        dynamic: '{{Steps.step1.output.source.dynamicValue}}',
        nested: {
            staticNum: 42,
            dynamicNum: '{{Steps.step1.output.source.dynamicValue}}',
        },
        array: ['static', '{{Steps.step1.output.source.dynamicValue}}', 123],
    };

    const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(new Map<string, any>([['resultShape', resultShape]]));
    fep.setContext(new Map());
    fep.setSteps(createOutputMap({ source }));

    const result = await make.execute(fep);
    const value = result.allResults()[0]?.getResult()?.get('value');

    expect(value).toStrictEqual({
        static: 'static string',
        dynamic: 'from source',
        nested: {
            staticNum: 42,
            dynamicNum: 'from source',
        },
        array: ['static', 'from source', 123],
    });
});

test('test null and undefined handling', async () => {
    const resultShape = {
        nullValue: null,
        nested: {
            inner: null,
        },
    };

    const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(new Map<string, any>([['resultShape', resultShape]]));
    fep.setContext(new Map());
    fep.setSteps(new Map());

    const result = await make.execute(fep);
    const value = result.allResults()[0]?.getResult()?.get('value');

    expect(value).toStrictEqual({
        nullValue: null,
        nested: {
            inner: null,
        },
    });
});

test('test array of arrays', async () => {
    const source = {
        matrix: [
            [1, 2],
            [3, 4],
        ],
    };

    const resultShape = {
        grid: [
            ['{{Steps.step1.output.source.matrix[0][0]}}', '{{Steps.step1.output.source.matrix[0][1]}}'],
            ['{{Steps.step1.output.source.matrix[1][0]}}', '{{Steps.step1.output.source.matrix[1][1]}}'],
        ],
    };

    const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(new Map<string, any>([['resultShape', resultShape]]));
    fep.setContext(new Map());
    fep.setSteps(createOutputMap({ source }));

    const result = await make.execute(fep);
    const value = result.allResults()[0]?.getResult()?.get('value');

    expect(value).toStrictEqual({
        grid: [
            [1, 2],
            [3, 4],
        ],
    });
});

test('test primitive result shape', async () => {
    const source = { value: 'hello' };

    const resultShape = '{{Steps.step1.output.source.value}}';

    const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(new Map<string, any>([['resultShape', resultShape]]));
    fep.setContext(new Map());
    fep.setSteps(createOutputMap({ source }));

    const result = await make.execute(fep);
    const value = result.allResults()[0]?.getResult()?.get('value');

    expect(value).toBe('hello');
});

test('test array as root result shape', async () => {
    const source = { a: 1, b: 2, c: 3 };

    const resultShape = [
        '{{Steps.step1.output.source.a}}',
        '{{Steps.step1.output.source.b}}',
        '{{Steps.step1.output.source.c}}',
    ];

    const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(new Map<string, any>([['resultShape', resultShape]]));
    fep.setContext(new Map());
    fep.setSteps(createOutputMap({ source }));

    const result = await make.execute(fep);
    const value = result.allResults()[0]?.getResult()?.get('value');

    expect(value).toStrictEqual([1, 2, 3]);
});
