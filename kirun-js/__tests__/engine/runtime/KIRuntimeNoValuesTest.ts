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
} from '../../../src';
import { Print } from '../../../src/engine/function/system/Print';

test('KIRuntime With Definition 3', async () => {
    var def = {
        name: 'Make an error',
        namespace: 'UIApp',
        steps: {
            print: {
                statementName: 'print',
                namespace: 'function',
                name: 'test',
                parameterMap: {},
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
