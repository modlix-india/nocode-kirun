import { DateTime } from 'luxon';
import { KIRunSchemaRepository, Namespaces } from '../../../../../src';
import { KIRunFunctionRepository } from '../../../../../src';
import { FunctionExecutionParameters } from '../../../../../src';
import { AbstractDateFunction } from '../../../../../src/engine/function/system/date/AbstractDateFunction';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';

import { Settings } from 'luxon';

Settings.defaultZone = 'Asia/Kolkata';

describe('GetCurrentTimestamp', () => {
    test('should return the current timestamp', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(new Map());

        const result = await (await new DateFunctionRepository().find(
            Namespaces.DATE,
            'GetCurrentTimestamp',
        ))!.execute(fep);

        expect(
            result.allResults()[0].getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME),
        ).toBeDefined();
    });
});
