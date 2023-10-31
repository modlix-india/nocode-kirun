import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { DurationUtils } from '../../../util/DurationUtil';
import isValidZuluDate from '../../../util/isValidISODate';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE = 'isodate';
const OUTPUT = 'result';
const SIGNATURE = new FunctionSignature('FromNow')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([[VALUE, new Parameter(VALUE, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))]]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofInteger(OUTPUT)]]))]));

export class FromNow extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let date: string = context.getArguments()?.get(VALUE);

        if (!isValidZuluDate(date)) throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);

        const currentDate = new Date();

        const differenceInMilliseconds = currentDate.getTime() - Date.parse(date);

        const seconds = Math.floor(differenceInMilliseconds / 1000);
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);
        const days = Math.floor(hours / 24);

        const output = DurationUtils.getDuration(
            Math.abs(days),
            Math.abs(hours),
            Math.abs(minutes),
            Math.abs(seconds),
        );

        return new FunctionOutput([
            EventResult.outputOf(
                new Map([
                    [OUTPUT, differenceInMilliseconds > 0 ? output + ' ago' : 'In ' + output],
                ]),
            ),
        ]);
    }
}
