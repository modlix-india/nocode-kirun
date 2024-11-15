import { Schema } from '../../../json/schema/Schema';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractDateFunction } from './AbstractDateFunction';
import { getDateTime } from './common';

export class IsBetween extends AbstractDateFunction {
    public static readonly PARAMETER_START_TIMESTAMP_NAME = 'startTimestamp';
    public static readonly PARAMETER_END_TIMESTAMP_NAME = 'endTimestamp';

    public static readonly PARAMETER_CHECK_TIMESTAMP_NAME = 'checkTimestamp';

    constructor() {
        super(
            'IsBetween',
            IsBetween.EVENT_BOOLEAN,
            Parameter.of(
                IsBetween.PARAMETER_START_TIMESTAMP_NAME,
                Schema.ofRef(Namespaces.DATE + '.Timestamp'),
            ),
            Parameter.of(
                IsBetween.PARAMETER_END_TIMESTAMP_NAME,
                Schema.ofRef(Namespaces.DATE + '.Timestamp'),
            ),
            Parameter.of(
                IsBetween.PARAMETER_CHECK_TIMESTAMP_NAME,
                Schema.ofRef(Namespaces.DATE + '.Timestamp'),
            ),
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const startTimestamp = context
            .getArguments()
            ?.get(IsBetween.PARAMETER_START_TIMESTAMP_NAME);
        const endTimestamp = context.getArguments()?.get(IsBetween.PARAMETER_END_TIMESTAMP_NAME);
        const checkTimestamp = context
            .getArguments()
            ?.get(IsBetween.PARAMETER_CHECK_TIMESTAMP_NAME);

        let startDateTime = getDateTime(startTimestamp);
        let endDateTime = getDateTime(endTimestamp);
        const checkDateTime = getDateTime(checkTimestamp);

        if (startDateTime > endDateTime) {
            [startDateTime, endDateTime] = [endDateTime, startDateTime];
        }

        return new FunctionOutput([
            EventResult.outputOf(
                MapUtil.of(
                    IsBetween.EVENT_RESULT_NAME,
                    startDateTime <= checkDateTime && checkDateTime <= endDateTime,
                ),
            ),
        ]);
    }
}
