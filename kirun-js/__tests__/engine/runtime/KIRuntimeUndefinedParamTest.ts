import {
    Namespaces,
    FunctionDefinition,
    KIRuntime,
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    Repository,
    Function,
    HybridRepository,
    FunctionOutput,
    Parameter,
    Schema,
    FunctionSignature,
    EventResult,
    AbstractFunction,
} from '../../../src';

class Print extends AbstractFunction {
    private static readonly VALUES: string = 'values';
    private static readonly VALUE: string = 'value';

    private static readonly SIGNATURE: FunctionSignature = new FunctionSignature('Print')
        .setNamespace(Namespaces.SYSTEM)
        .setParameters(
            new Map([
                Parameter.ofEntry(Print.VALUES, Schema.ofAny(Print.VALUES), true),
                Parameter.ofEntry(Print.VALUE, Schema.ofAny(Print.VALUE)),
            ]),
        );

    public getSignature(): FunctionSignature {
        return Print.SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let values = context.getArguments()?.get(Print.VALUES);
        let value = context.getArguments()?.get(Print.VALUE);

        console?.log('Values', ...values);
        console?.log('Value', value);

        return new FunctionOutput([EventResult.outputOf(new Map())]);
    }
}

test('KIRuntime Undefined and null param type with varargs', async () => {
    var def = {
        name: 'Make an error',
        namespace: 'UIApp',
        steps: {
            print: {
                statementName: 'print',
                namespace: 'function',
                name: 'test',
                parameterMap: {
                    values: {
                        one: { key: 'one', type: 'VALUE', value: null },
                        two: { key: 'two', type: 'VALUE', value: undefined },
                        three: { key: 'three', type: 'EXPRESSION', expression: 'null' },
                        four: { key: 'four', type: 'VALUE', value: 12 },
                    },
                    value: {},
                },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    const tstPrint = new Print();

    class TestRepository implements Repository<Function> {
        public async find(namespace: string, name: string): Promise<Function | undefined> {
            return tstPrint;
        }
        public async filter(name: string): Promise<string[]> {
            throw new Error('Method not implemented.');
        }
    }

    var fo: FunctionOutput = await new KIRuntime(fd).execute(
        new FunctionExecutionParameters(
            new HybridRepository(new KIRunFunctionRepository(), new TestRepository()),
            new KIRunSchemaRepository(),
        ).setArguments(new Map()),
    );
    expect(fo.allResults()[0].getResult().size).toBe(0);
});

test('KIRuntime Undefined and null param type without varargs', async () => {
    var def = {
        name: 'Make an error',
        namespace: 'UIApp',
        steps: {
            print: {
                statementName: 'print',
                namespace: 'function',
                name: 'test',
                parameterMap: {
                    value: {
                        two: { key: 'two', type: 'VALUE', value: undefined },
                    },
                },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    const tstPrint = new Print();

    class TestRepository implements Repository<Function> {
        public async find(namespace: string, name: string): Promise<Function | undefined> {
            return tstPrint;
        }
        public async filter(name: string): Promise<string[]> {
            throw new Error('Method not implemented.');
        }
    }

    expect(
        (
            await new KIRuntime(fd).execute(
                new FunctionExecutionParameters(
                    new HybridRepository(new KIRunFunctionRepository(), new TestRepository()),
                    new KIRunSchemaRepository(),
                ).setArguments(new Map()),
            )
        )
            .allResults()[0]
            .getResult().size,
    ).toBe(0);
});
