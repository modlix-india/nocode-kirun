import { IsBetween } from '../../../../../src/engine/function/system/date/IsBetween';
import { KIRunFunctionRepository } from '../../../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

import { Settings } from 'luxon';

Settings.defaultZone = 'UTC+05:30';

const isBetween = new IsBetween();

describe('IsBetween', () => {
    test('should return true if the check timestamp is between the start and end timestamps', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            MapUtil.of(
                IsBetween.PARAMETER_START_TIMESTAMP_NAME,
                '2024-01-01T00:00:00.000Z',
                IsBetween.PARAMETER_END_TIMESTAMP_NAME,
                '2024-01-02T00:00:00.000Z',
                IsBetween.PARAMETER_CHECK_TIMESTAMP_NAME,
                '2024-01-01T12:00:00.000Z',
            ),
        );

        const result = await isBetween.execute(fep);

        expect(result.allResults()[0].getResult().get(IsBetween.EVENT_RESULT_NAME)).toBe(true);
    });

    test('should return false if the check timestamp is not between the start and end timestamps', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            MapUtil.of(
                IsBetween.PARAMETER_START_TIMESTAMP_NAME,
                '2024-01-01T00:00:00.000Z',
                IsBetween.PARAMETER_END_TIMESTAMP_NAME,
                '2024-01-02T00:00:00.000Z',
                IsBetween.PARAMETER_CHECK_TIMESTAMP_NAME,
                '2024-01-03T00:00:00.000Z',
            ),
        );

        const result = await isBetween.execute(fep);

        expect(result.allResults()[0].getResult().get(IsBetween.EVENT_RESULT_NAME)).toBe(false);
    });
});
