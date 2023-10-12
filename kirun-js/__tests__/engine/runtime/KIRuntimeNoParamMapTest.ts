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
} from '../../../src';
import { Print } from '../../../src/engine/function/system/Print';

test('KIRuntime Without any parameter map passed in definition', async () => {
    var def = {
        name: 'Make an error',
        namespace: 'UIApp',
        steps: {
            print: {
                statementName: 'print',
                namespace: 'function',
                name: 'test',
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
        await new KIRuntime(fd).execute(
            new FunctionExecutionParameters(
                new HybridRepository(new KIRunFunctionRepository(), new TestRepository()),
                new KIRunSchemaRepository(),
            ).setArguments(new Map()),
        );
    } catch (e: any) {
        console.error(e);
    }
});
