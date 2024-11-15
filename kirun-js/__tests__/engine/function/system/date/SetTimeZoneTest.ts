import { KIRunFunctionRepository, MapUtil, KIRunSchemaRepository } from '../../../../../src';
import { AbstractDateFunction } from '../../../../../src/engine/function/system/date/AbstractDateFunction';
import { SetTimeZone } from '../../../../../src/engine/function/system/date/SetTimeZone';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const setTimeZone = new SetTimeZone();

describe('SetTimeZone', () => {
    test('should set the time zone', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            MapUtil.of(
                AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                '2024-01-01T00:00:00.000Z',
                SetTimeZone.PARAMETER_TIMEZONE_NAME,
                'Asia/Tokyo',
            ),
        );

        const result = await setTimeZone.execute(fep);

        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME),
        ).toBe('2024-01-01T09:00:00.000+09:00');
    });

    test('shoud set the time zone with offset', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            MapUtil.of(
                AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                '2024-01-01T00:00:00.000Z',
                SetTimeZone.PARAMETER_TIMEZONE_NAME,
                'UTC+05:30',
            ),
        );

        const result = await setTimeZone.execute(fep);

        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME),
        ).toBe('2024-01-01T05:30:00.000+05:30');
    });
});
