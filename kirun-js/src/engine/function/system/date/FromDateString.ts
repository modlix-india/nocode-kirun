import { DateTime } from 'luxon';
import { Schema } from '../../../json/schema/Schema';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { Parameter } from '../../../model/Parameter';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractDateFunction } from './AbstractDateFunction';

export class FromDateString extends AbstractDateFunction {
    public static readonly PARAMETER_FORMAT_NAME = 'format';

    public static readonly PARAMETER_TIMESTAMP_STRING_NAME = 'timestampString';

    constructor() {
        super(
            'FromDateString',
            AbstractDateFunction.EVENT_TIMESTAMP,
            Parameter.of(
                FromDateString.PARAMETER_TIMESTAMP_STRING_NAME,
                Schema.ofString(FromDateString.PARAMETER_TIMESTAMP_STRING_NAME),
            ),
            Parameter.of(
                FromDateString.PARAMETER_FORMAT_NAME,
                Schema.ofString(FromDateString.PARAMETER_FORMAT_NAME),
            ),
        );
    }
    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const timestampString = context
            .getArguments()
            ?.get(FromDateString.PARAMETER_TIMESTAMP_STRING_NAME);

        const format = context.getArguments()?.get(FromDateString.PARAMETER_FORMAT_NAME);

        const dateTime = DateTime.fromFormat(timestampString, format);

        return new FunctionOutput([
            EventResult.outputOf(
                MapUtil.of(AbstractDateFunction.EVENT_RESULT_NAME, dateTime.toISO()),
            ),
        ]);
    }
}
