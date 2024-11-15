import { Schema } from '../../../json/schema/Schema';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { Parameter } from '../../../model/Parameter';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractDateFunction } from './AbstractDateFunction';
import { getDateTime } from './common';

export class ToDateString extends AbstractDateFunction {
    public static readonly PARAMETER_FORMAT_NAME = 'format';
    public static readonly PARAMETER_LOCALE_NAME = 'locale';

    constructor() {
        super(
            'ToDateString',
            AbstractDateFunction.EVENT_STRING,
            AbstractDateFunction.PARAMETER_TIMESTAMP,
            Parameter.of(
                ToDateString.PARAMETER_FORMAT_NAME,
                Schema.ofString(ToDateString.PARAMETER_FORMAT_NAME),
            ),
            Parameter.of(
                ToDateString.PARAMETER_LOCALE_NAME,
                Schema.ofString(ToDateString.PARAMETER_LOCALE_NAME).setDefaultValue(''),
            ),
        );
    }
    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const timestamp = context
            .getArguments()
            ?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME);

        const dateTime = getDateTime(timestamp);

        const format = context.getArguments()?.get(ToDateString.PARAMETER_FORMAT_NAME);
        let locale = context.getArguments()?.get(ToDateString.PARAMETER_LOCALE_NAME);
        if (locale === '') locale = 'system';

        return new FunctionOutput([
            EventResult.outputOf(
                MapUtil.of(
                    AbstractDateFunction.EVENT_RESULT_NAME,
                    dateTime.toFormat(format, { locale }),
                ),
            ),
        ]);
    }
}
