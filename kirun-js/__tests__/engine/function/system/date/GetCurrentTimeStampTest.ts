import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { GetCurrentTimeStamp } from '../../../../../src/engine/function/system/date/GetCurrentTimeStamp';

const gcts: GetCurrentTimeStamp = new GetCurrentTimeStamp();

describe('testing GetCurrentTimeStamp', () => {
    test('checking with current time stamp', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([]));

        const d = new Date(Date.now());

        expect((await gcts.execute(fep)).allResults()[0].getResult().get('date').substring(0,21)).toBe(
            d.toISOString().substring(0,21)
        );
    });
})

describe('testing false case of GetCurrentTimeStamp', () => {
    test('checking with current time stamp', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([]));

        const d = new Date(Date.now()-1000000);

        expect((await gcts.execute(fep)).allResults()[0].getResult().get('date')).not.toBe(
            d.toISOString()
        );
    });
})
