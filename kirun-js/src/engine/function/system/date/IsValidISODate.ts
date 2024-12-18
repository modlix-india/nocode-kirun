import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractFunction } from '../../AbstractFunction';
import { AbstractDateFunction } from './AbstractDateFunction';
import { DateTime } from 'luxon';

export class IsValidISODate extends AbstractFunction {
    private readonly signature = new FunctionSignature('IsValidISODate')
        .setNamespace(Namespaces.DATE)
        .setParameters(
            new Map([
                Parameter.ofEntry(
                    AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                    Schema.ofString(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME),
                ),
            ]),
        )
        .setEvents(
            new Map([
                Event.outputEventMapEntry(
                    MapUtil.of(
                        AbstractDateFunction.EVENT_RESULT_NAME,
                        Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME),
                    ),
                ),
            ]),
        );

    protected internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const timestamp = context
            .getArguments()
            ?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME);

        const dt = DateTime.fromISO(timestamp);

        return Promise.resolve(
            new FunctionOutput([
                EventResult.outputOf(
                    MapUtil.of(AbstractDateFunction.EVENT_RESULT_NAME, dt.isValid),
                ),
            ]),
        );
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }
}
