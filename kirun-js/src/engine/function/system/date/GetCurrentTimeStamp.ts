import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { AbstractDateFunction } from './AbstractDateFunction';
import { MapUtil } from '../../../util/MapUtil';
import { DateTime } from 'luxon';

export class GetCurrentTimestamp extends AbstractDateFunction {
    public constructor() {
        super('GetCurrentTimestamp', AbstractDateFunction.EVENT_TIMESTAMP);
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        return new FunctionOutput([
            EventResult.outputOf(
                MapUtil.of(AbstractDateFunction.EVENT_TIMESTAMP_NAME, DateTime.now().toISO()),
            ),
        ]);
    }
}
