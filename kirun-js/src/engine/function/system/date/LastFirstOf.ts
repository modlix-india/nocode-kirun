import { DateTime } from 'luxon';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractFunction } from '../../AbstractFunction';
import { AbstractDateFunction } from './AbstractDateFunction';
import { Parameter } from '../../../model/Parameter';
import { Schema } from '../../../json/schema/Schema';
import { getDateTime } from './common';

export class LastFirstOf extends AbstractFunction {
    private readonly signature: FunctionSignature;
    private readonly isLast: boolean;

    constructor(isLast: boolean) {
        super();

        this.isLast = isLast;
        this.signature = new FunctionSignature(isLast ? 'LastOf' : 'FirstOf')
            .setNamespace(Namespaces.DATE)
            .setParameters(
                new Map([
                    [
                        AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                        new Parameter(
                            AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                            Schema.ofRef(Namespaces.DATE + '.Timestamp'),
                        ).setVariableArgument(true),
                    ],
                ]),
            )
            .setEvents(
                new Map([
                    [
                        AbstractDateFunction.EVENT_TIMESTAMP_NAME,
                        AbstractDateFunction.EVENT_TIMESTAMP,
                    ],
                ]),
            );
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const timestamps = context
            .getArguments()
            ?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME);

        if (!timestamps?.length) {
            throw new Error('No timestamps provided');
        }

        const dateTimes: DateTime[] = timestamps.map((ts: string) => getDateTime(ts));
        dateTimes.sort((a, b) => a.toMillis() - b.toMillis());

        return Promise.resolve(
            new FunctionOutput([
                EventResult.outputOf(
                    MapUtil.of(
                        AbstractDateFunction.EVENT_TIMESTAMP_NAME,
                        dateTimes[this.isLast ? dateTimes.length - 1 : 0].toISO(),
                    ),
                ),
            ]),
        );
    }
}
