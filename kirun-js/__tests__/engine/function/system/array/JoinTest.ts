import { Insert } from '../../../../../src/engine/function/system/array/Insert';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { Join } from '../../../../../src/engine/function/system/array/Join';

describe('Join Tests', () => {
    test('Join Test 1', async () => {
        let join: Join = new Join();

        let array: any[] = [
            'test',
            'Driven',
            'developement',
            'I',
            'am',
            'using',
            'eclipse',
            'I',
            'to',
            'test',
            'the',
            'changes',
            'with',
            'test',
            'Driven',
            'developement',
        ];

        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        )
            .setArguments(
                new Map<string, any>([
                    ['source', array],
                    ['delimiter', ' '],
                ]),
            )
            .setSteps(new Map([]))
            .setContext(new Map([]));

        let res: string =
            'test Driven developement I am using eclipse I to test the changes with test Driven developement';

        expect((await join.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
            res,
        );
    });

    test('Join Test without delimiter', async () => {
        let join: Join = new Join();

        let array: any[] = [
            'test',
            'Driven',
            'developement',
            'I',
            'am',
            'using',
            'eclipse',
            'I',
            'to',
            'test',
            'the',
            'changes',
            'with',
            'test',
            'Driven',
            'developement',
        ];

        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        )
            .setArguments(new Map<string, any>([['source', array]]))
            .setSteps(new Map([]))
            .setContext(new Map([]));

        let res: string =
            'testDrivendevelopementIamusingeclipseItotestthechangeswithtestDrivendevelopement';

        expect((await join.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
            res,
        );
    });

    test('Join Test with mixxed data types', async () => {
        let join: Join = new Join();

        let array: any[] = [
            'test',
            'Driven',
            'developement',
            'I',
            'am',
            'using',
            'eclipse',
            'I',
            'to',
            'test',
            'the',
            'changes',
            'with',
            'test',
            4,
            5,
        ];

        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        )
            .setArguments(new Map<string, any>([['source', array]]))
            .setSteps(new Map([]))
            .setContext(new Map([]));

        let res: string = 'testDrivendevelopementIamusingeclipseItotestthechangeswithtest45';

        expect((await join.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
            res,
        );
    });

    test('Join Test with undefined', async () => {
        let join: Join = new Join();

        let array: any[] = [
            'test',
            'Driven',
            'developement',
            'I',
            'am',
            'using',
            'eclipse',
            'I',
            'to',
            'test',
            'the',
            undefined,
            'with',
            'test',
            4,
            5,
        ];

        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        )
            .setArguments(new Map<string, any>([['source', array]]))
            .setSteps(new Map([]))
            .setContext(new Map([]));

        let res: string = 'testDrivendevelopementIamusingeclipseItotestthewithtest45';

        expect((await join.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
            res,
        );
    });
});
