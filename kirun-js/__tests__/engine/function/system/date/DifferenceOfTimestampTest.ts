import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { DifferenceOfTimestamp } from '../../../../../src/engine/function/system/date/DifferenceOfTimestamp';

const differenceOfTimestamp: DifferenceOfTimestamp = new DifferenceOfTimestamp();

describe('testing DifferenceOfTimestamp', () => {
    test('Test1', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodates_1', '2024-10-11T00:35:00.000Z'],
                ['isodates_2', '2024-10-10T00:35:00.000Z']
            ]),
        );

        expect((await differenceOfTimestamp.execute(fep)).allResults()[0].getResult().get('difference')).toBe(
            '86400000',
        );
    });
    test('Test2', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map<string, any>([
                ['isodates_1', '2023-10-25T13:30:04.970+07:00'],
                ['isodates_2', '2023-10-25T19:30:04.970+01:30']
            ]),
        );

        expect((await differenceOfTimestamp.execute(fep)).allResults()[0].getResult().get('difference')).toBe(
            '-41400000',
        );
    });

})