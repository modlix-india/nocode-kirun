import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../../src';
import { AbstractDateFunction } from '../../../../../src/engine/function/system/date/AbstractDateFunction';
import { IsValidISODate } from '../../../../../src/engine/function/system/date/IsValidISODate';

import { Settings } from 'luxon';

Settings.defaultZone = 'Asia/Kolkata';

const isValidISODate = new IsValidISODate();

describe('IsValidISODate', () => {
    it('should return true for a valid ISO date', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map([[AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2024-01-01T00:00:00.000Z']]),
        );

        const result = await isValidISODate.execute(fep);
        expect(result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(
            true,
        );
    });

    it('should return false for an invalid ISO date', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([[AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, 'invalid']]));

        const result = await isValidISODate.execute(fep);
        expect(result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(
            false,
        );
    });

    it('should return false for an invalid ISO date', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map([[AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2024-02-31']]));

        const result = await isValidISODate.execute(fep);
        expect(result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(
            false,
        );
    });

    it('should return false for an invalid leap year ISO date', async () => {
        let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            new Map([[AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '2022-02-29T00:00:00.000Z']]),
        );

        const result = await isValidISODate.execute(fep);
        expect(result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)).toBe(
            false,
        );
    });
});
