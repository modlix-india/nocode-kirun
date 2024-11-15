import { MapUtil } from '../../../../../src/engine/util/MapUtil';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { TimeAs } from '../../../../../src/engine/function/system/date/TimeAs';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { Settings } from 'luxon';
const timeAsArray = new TimeAs(true);
const timeAsObject = new TimeAs(false);

Settings.defaultZone = 'UTC+05:30';

describe('TimeAs', () => {
    it('should return the time as an array', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            MapUtil.of(TimeAs.PARAMETER_TIMESTAMP_NAME, '2024-11-10T10:10:10.100-05:00'),
        );
        const result = await timeAsArray.execute(fep);
        expect(result.allResults()[0].getResult().get(TimeAs.EVENT_TIME_ARRAY_NAME)).toEqual([
            2024, 11, 10, 10, 10, 10, 100,
        ]);
    });
});
