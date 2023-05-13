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
    AbstractFunction,
    EventResult,
    FunctionSignature,
    Schema,
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

test('KIRuntime With Definition no values passed ', async () => {
    var def = {
        name: 'Make an error',
        namespace: 'UIApp',
        steps: {
            print: {
                statementName: 'print',
                namespace: 'function',
                name: 'test',
                parameterMap: {
                    values: {},
                },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    const tstPrint = new Print();

    class TestRepository implements Repository<Function> {
        public find(namespace: string, name: string): Function | undefined {
            return tstPrint;
        }
        filter(name: string): string[] {
            throw new Error('Method not implemented.');
        }
    }

    try {
        const fo: FunctionOutput = await new KIRuntime(fd).execute(
            new FunctionExecutionParameters(
                new HybridRepository(new KIRunFunctionRepository(), new TestRepository()),
                new KIRunSchemaRepository(),
            ).setArguments(new Map()),
        );
        console.log(fo);
    } catch (e: any) {
        console.error(e);
    }
});

test('KIRuntime With Definition with no value passed', async () => {
    var def = {
        name: 'Make an error',
        namespace: 'UIApp',
        steps: {
            print: {
                statementName: 'print',
                namespace: 'function',
                name: 'test',
                parameterMap: {
                    value: {},
                },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    const tstPrint = new Print();

    class TestRepository implements Repository<Function> {
        public find(namespace: string, name: string): Function | undefined {
            return tstPrint;
        }
        filter(name: string): string[] {
            throw new Error('Method not implemented.');
        }
    }

    try {
        const fo: FunctionOutput = await new KIRuntime(fd).execute(
            new FunctionExecutionParameters(
                new HybridRepository(new KIRunFunctionRepository(), new TestRepository()),
                new KIRunSchemaRepository(),
            ).setArguments(new Map()),
        );
        console.log(fo);
    } catch (e: any) {
        console.error(e);
    }
});

test('KIRuntime With Definition with no value and values passed', async () => {
    var def = {
        name: 'Make an error',
        namespace: 'UIApp',
        steps: {
            print: {
                statementName: 'print',
                namespace: 'function',
                name: 'test',
                parameterMap: {
                    value: {},
                    values: {},
                },
            },
        },
    };

    const fd = FunctionDefinition.from(def);

    const tstPrint = new Print();

    class TestRepository implements Repository<Function> {
        public find(namespace: string, name: string): Function | undefined {
            return tstPrint;
        }
        filter(name: string): string[] {
            throw new Error('Method not implemented.');
        }
    }

    try {
        const fo: FunctionOutput = await new KIRuntime(fd).execute(
            new FunctionExecutionParameters(
                new HybridRepository(new KIRunFunctionRepository(), new TestRepository()),
                new KIRunSchemaRepository(),
            ).setArguments(new Map()),
        );
        console.log(fo);
    } catch (e: any) {
        console.error(e);
    }
});
