import { FromDateString } from '../../../../../src/engine/function/system/date/FromDateString';
import { KIRunFunctionRepository } from '../../../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../../../src/engine/util/MapUtil';

const fromDateString = new FromDateString();

describe('FromDateString', () => {
    test('should return the timestamp', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            MapUtil.of(
                FromDateString.PARAMETER_TIMESTAMP_STRING_NAME,
                '2024-01-01',
                FromDateString.PARAMETER_FORMAT_NAME,
                'yyyy-MM-dd',
            ),
        );

        const result = (await fromDateString.execute(fep))
            .allResults()[0]
            .getResult()
            .get(FromDateString.EVENT_RESULT_NAME);

        expect(result).toEqual('2024-01-01T00:00:00.000+05:30');
    });

    test('should return the timestamp with the format', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            MapUtil.of(
                FromDateString.PARAMETER_TIMESTAMP_STRING_NAME,
                '2024-05',
                FromDateString.PARAMETER_FORMAT_NAME,
                'yyyy-MM',
            ),
        );

        const result = (await fromDateString.execute(fep))
            .allResults()[0]
            .getResult()
            .get(FromDateString.EVENT_RESULT_NAME);

        expect(result).toEqual('2024-05-01T00:00:00.000+05:30');
    });

    test('should return the timestamp with the only few fields', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            MapUtil.of(
                FromDateString.PARAMETER_TIMESTAMP_STRING_NAME,
                '03 12 123',
                FromDateString.PARAMETER_FORMAT_NAME,
                'dd ss SSS',
            ),
        );

        const result = (await fromDateString.execute(fep))
            .allResults()[0]
            .getResult()
            .get(FromDateString.EVENT_RESULT_NAME);

        expect(result).toEqual('2024-11-03T00:00:12.123+05:30');
    });
});
