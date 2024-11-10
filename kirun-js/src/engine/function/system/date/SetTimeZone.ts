import { Schema } from '../../../json/schema/Schema';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { Parameter } from '../../../model/Parameter';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractDateFunction } from './AbstractDateFunction';
import { getDateTime } from './common';

export class SetTimeZone extends AbstractDateFunction {
    public static readonly PARAMETER_TIMEZONE_NAME: string = 'timezone';

    constructor() {
        super(
            'SetTimeZone',
            AbstractDateFunction.EVENT_TIMESTAMP,
            AbstractDateFunction.PARAMETER_TIMESTAMP,
            Parameter.of(
                SetTimeZone.PARAMETER_TIMEZONE_NAME,
                Schema.ofString(SetTimeZone.PARAMETER_TIMEZONE_NAME),
            ),
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const timestamp = context
            .getArguments()
            ?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME);

        const dateTime = getDateTime(timestamp);

        const timeZone = context.getArguments()?.get(SetTimeZone.PARAMETER_TIMEZONE_NAME);

        return new FunctionOutput([
            EventResult.outputOf(
                MapUtil.of(
                    AbstractDateFunction.EVENT_RESULT_NAME,
                    dateTime.setZone(timeZone).toISO()!,
                ),
            ),
        ]);
    }
}
