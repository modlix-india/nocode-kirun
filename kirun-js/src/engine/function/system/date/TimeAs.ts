import { FunctionOutput } from '../../../model/FunctionOutput';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';

import { AbstractDateFunction } from './AbstractDateFunction';
import { getDateTime } from './common';
import { Schema } from '../../../json/schema/Schema';

export class TimeAs extends AbstractDateFunction {
    public static readonly EVENT_TIME_OBJECT_NAME = 'object';
    public static readonly EVENT_TIME_ARRAY_NAME = 'array';

    private readonly isArray: boolean;

    constructor(isArray: boolean) {
        super(
            isArray ? 'TimeAsArray' : 'TimeAsObject',
            new Event(
                Event.OUTPUT,
                MapUtil.of(
                    isArray ? TimeAs.EVENT_TIME_ARRAY_NAME : TimeAs.EVENT_TIME_OBJECT_NAME,
                    isArray
                        ? Schema.ofArray(
                              TimeAs.EVENT_TIME_ARRAY_NAME,
                              Schema.ofInteger('timeParts'),
                          )
                        : Schema.ofRef(Namespaces.DATE + '.TimeObject'),
                ),
            ),
            AbstractDateFunction.PARAMETER_TIMESTAMP,
        );

        this.isArray = isArray;
    }
    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const timestamp = context
            .getArguments()
            ?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME);

        const dateTime = getDateTime(timestamp).toObject();

        return new FunctionOutput([
            EventResult.outputOf(
                MapUtil.of(
                    this.isArray ? TimeAs.EVENT_TIME_ARRAY_NAME : TimeAs.EVENT_TIME_OBJECT_NAME,
                    this.isArray
                        ? [
                              dateTime.year,
                              dateTime.month,
                              dateTime.day,
                              dateTime.hour,
                              dateTime.minute,
                              dateTime.second,
                              dateTime.millisecond,
                          ]
                        : dateTime,
                ),
            ),
        ]);
    }
}
