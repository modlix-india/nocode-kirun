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
                Parameter.ofEntry(
                    Print.VALUE + 'Pick1',
                    Schema.ofBoolean(Print.VALUE + 'Pick1').setDefaultValue(false),
                ),
                Parameter.ofEntry(
                    Print.VALUE + 'Pick2',
                    Schema.ofString(Print.VALUE + 'Pick2').setDefaultValue(''),
                ),
            ]),
        );

    public getSignature(): FunctionSignature {
        return Print.SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let values = context.getArguments()?.get(Print.VALUES);
        let value = context.getArguments()?.get(Print.VALUE);
        let valuePick1 = context.getArguments()?.get(Print.VALUE + 'Pick1');
        let valuePick2 = context.getArguments()?.get(Print.VALUE + 'Pick2');

        console?.log('Values', ...values);
        console?.log('Value', value);
        console?.log('ValuePick1', valuePick1);
        console?.log('ValuePick2', valuePick2);

        return new FunctionOutput([EventResult.outputOf(new Map())]);
    }
}

const mock = jest.spyOn(global.console, 'log').mockImplementation();

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
        public async find(namespace: string, name: string): Promise<Function | undefined> {
            return tstPrint;
        }
        public async filter(name: string): Promise<string[]> {
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
        expect(mock).toHaveBeenCalledTimes(4);
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
        public async find(namespace: string, name: string): Promise<Function | undefined> {
            return tstPrint;
        }
        public async filter(name: string): Promise<string[]> {
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
        expect(mock).toHaveBeenCalledTimes(8);
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
        public async find(namespace: string, name: string): Promise<Function | undefined> {
            return tstPrint;
        }
        public async filter(name: string): Promise<string[]> {
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
        expect(mock).toHaveBeenCalledTimes(12);
    } catch (e: any) {
        console.error(e);
    }
});
