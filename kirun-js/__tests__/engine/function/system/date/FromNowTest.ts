import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../../src';
import { FromNow } from '../../../../../src/engine/function/system/date/FromNow';

const fromNow: FromNow = new FromNow();

describe('testing FromNow', () => {
    let arr = new Array();
    arr.push('2022-11-16T23:33:04.970+01:30');
    arr.push('2023-11-16T23:32:04.970+01:30');

    test('test1', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodates', arr],
                ['key', 'EI'],
            ]),
        );

        expect((await fromNow.execute(fep)).allResults()[0].getResult().get('result')).toBe(
            '',
        );
    });
    // test('test2', async () => {
    //     let arr = new Array();
    //     arr.push('2023-11-16T23:32:04.970+01:30');

    //     let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
    //         new KIRunFunctionRepository(),
    //         new KIRunSchemaRepository(),
    //     ).setArguments(
    //         new Map<string, any>([
    //             ['isodates', arr],
    //             ['key', 'N'],
    //         ]),
    //     );

    //     expect((await fromNow.execute(fep)).allResults()[0].getResult().get('result')).toBe(
    //         'In 6 days',
    //     );
    // });
});
