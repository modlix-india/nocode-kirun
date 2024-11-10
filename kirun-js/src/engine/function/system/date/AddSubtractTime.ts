import { DurationLikeObject } from 'luxon';
import { Schema } from '../../../json/schema/Schema';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { Parameter } from '../../../model/Parameter';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractDateFunction } from './AbstractDateFunction';
import { getDateTime } from './common';

export class AddSubtractTime extends AbstractDateFunction {
    public static readonly PARAMETER_YEARS_NAME = 'years';
    public static readonly PARAMETER_MONTHS_NAME = 'months';
    public static readonly PARAMETER_DAYS_NAME = 'days';
    public static readonly PARAMETER_HOURS_NAME = 'hours';
    public static readonly PARAMETER_MINUTES_NAME = 'minutes';
    public static readonly PARAMETER_SECONDS_NAME = 'seconds';
    public static readonly PARAMETER_MILLISECONDS_NAME = 'milliseconds';

    private readonly isAdd: boolean;

    constructor(isAdd: boolean) {
        super(
            isAdd ? 'AddTime' : 'SubtractTime',
            AbstractDateFunction.EVENT_TIMESTAMP,
            AbstractDateFunction.PARAMETER_TIMESTAMP,
            Parameter.of(
                AddSubtractTime.PARAMETER_YEARS_NAME,
                Schema.ofNumber(AddSubtractTime.PARAMETER_YEARS_NAME).setDefaultValue(0),
            ),
            Parameter.of(
                AddSubtractTime.PARAMETER_MONTHS_NAME,
                Schema.ofNumber(AddSubtractTime.PARAMETER_MONTHS_NAME).setDefaultValue(0),
            ),
            Parameter.of(
                AddSubtractTime.PARAMETER_DAYS_NAME,
                Schema.ofNumber(AddSubtractTime.PARAMETER_DAYS_NAME).setDefaultValue(0),
            ),
            Parameter.of(
                AddSubtractTime.PARAMETER_HOURS_NAME,
                Schema.ofNumber(AddSubtractTime.PARAMETER_HOURS_NAME).setDefaultValue(0),
            ),
            Parameter.of(
                AddSubtractTime.PARAMETER_MINUTES_NAME,
                Schema.ofNumber(AddSubtractTime.PARAMETER_MINUTES_NAME).setDefaultValue(0),
            ),
            Parameter.of(
                AddSubtractTime.PARAMETER_SECONDS_NAME,
                Schema.ofNumber(AddSubtractTime.PARAMETER_SECONDS_NAME).setDefaultValue(0),
            ),
            Parameter.of(
                AddSubtractTime.PARAMETER_MILLISECONDS_NAME,
                Schema.ofNumber(AddSubtractTime.PARAMETER_MILLISECONDS_NAME).setDefaultValue(0),
            ),
        );

        this.isAdd = isAdd;
    }
    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const timestamp = context
            .getArguments()
            ?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME);

        const dateTime = getDateTime(timestamp);
        const years = context.getArguments()?.get(AddSubtractTime.PARAMETER_YEARS_NAME);
        const months = context.getArguments()?.get(AddSubtractTime.PARAMETER_MONTHS_NAME);
        const days = context.getArguments()?.get(AddSubtractTime.PARAMETER_DAYS_NAME);
        const hours = context.getArguments()?.get(AddSubtractTime.PARAMETER_HOURS_NAME);
        const minutes = context.getArguments()?.get(AddSubtractTime.PARAMETER_MINUTES_NAME);
        const seconds = context.getArguments()?.get(AddSubtractTime.PARAMETER_SECONDS_NAME);
        const milliseconds = context
            .getArguments()
            ?.get(AddSubtractTime.PARAMETER_MILLISECONDS_NAME);

        const options = {
            years,
            months,
            days,
            hours,
            minutes,
            seconds,
            milliseconds,
        } as DurationLikeObject;

        let newDateTime;

        if (this.isAdd) {
            newDateTime = dateTime.plus(options);
        } else {
            newDateTime = dateTime.minus(options);
        }

        return new FunctionOutput([
            EventResult.outputOf(
                MapUtil.of(AbstractDateFunction.EVENT_TIMESTAMP_NAME, newDateTime.toISO()),
            ),
        ]);
    }
}
