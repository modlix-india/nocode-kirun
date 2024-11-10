import {
    FunctionExecutionParameters,
    KIRunSchemaRepository,
    KIRunFunctionRepository,
    MapUtil,
} from '../../../../../src';
import { AbstractDateFunction } from '../../../../../src/engine/function/system/date/AbstractDateFunction';
import { LastFirstOf } from '../../../../../src/engine/function/system/date/LastFirstOf';
import { Settings } from 'luxon';

Settings.defaultZone = 'UTC+05:30';

const lastOf = new LastFirstOf(true);
const firstOf = new LastFirstOf(false);

describe('LastFirstOf', () => {
    test('should return the last timestamp', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            MapUtil.of(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, [
                '2024-01-03T05:50:00.000+05:30',
                '2024-01-02T00:00:00.000Z',
                '2024-01-03T10:00:00.000Z',
            ]),
        );

        const result = await lastOf.execute(fep);

        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME),
        ).toBe('2024-01-03T15:30:00.000+05:30');
    });

    test('should return the first timestamp', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            MapUtil.of(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, [
                '2024-01-03T05:50:00.000+05:30',
                '2024-01-02T00:00:00.000Z',
                '2024-01-03T10:00:00.000Z',
            ]),
        );

        const result = await firstOf.execute(fep);

        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME),
        ).toBe('2024-01-02T05:30:00.000+05:30');
    });

    test('should throw an error if no timestamps are provided', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(MapUtil.of(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, []));

        await expect(lastOf.execute(fep)).rejects.toThrow('No timestamps provided');
        await expect(firstOf.execute(fep)).rejects.toThrow('No timestamps provided');
    });
});
