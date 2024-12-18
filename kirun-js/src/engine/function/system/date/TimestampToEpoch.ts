import { DateTime } from 'luxon';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractFunction } from '../../AbstractFunction';
import { AbstractDateFunction } from './AbstractDateFunction';

export class TimestampToEpoch extends AbstractFunction {
    private readonly signature: FunctionSignature;
    private readonly isSeconds: boolean;

    constructor(name: string, isSeconds: boolean) {
        super();

        this.isSeconds = isSeconds;
        this.signature = new FunctionSignature(name)
            .setNamespace(Namespaces.DATE)
            .setParameters(
                new Map([
                    [
                        AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                        AbstractDateFunction.PARAMETER_TIMESTAMP,
                    ],
                ]),
            )
            .setEvents(
                new Map([
                    [
                        AbstractDateFunction.EVENT_TIMESTAMP.getName(),
                        AbstractDateFunction.EVENT_LONG,
                    ],
                ]),
            );
    }

    private readonly signature = this.signature;
    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const timestamp = context
            .getArguments()
            ?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME);
        const epoch = this.isSeconds
            ? DateTime.fromISO(timestamp).toSeconds()
            : DateTime.fromISO(timestamp).toMillis();

        return Promise.resolve(
            new FunctionOutput([
                EventResult.outputOf(MapUtil.of(AbstractDateFunction.EVENT_RESULT_NAME, epoch)),
            ]),
        );
    }
}
