import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { DurationUtils } from '../../../util/date/DurationUtil';
import getOffsetAndDateString from '../../../util/date/getOffsetAndDateString';
import isValidZuluDate from '../../../util/date/isValidISODate';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE = 'isodates';
const KEY = 'key';
const OUTPUT = 'result';
const SIGNATURE = new FunctionSignature('FromNow')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([
            [VALUE, new Parameter(VALUE, Schema.ofAny(`${Namespaces.DATE}.timeStamp`))],
            [
                KEY,
                new Parameter(
                    KEY,
                    Schema.ofString(KEY).setEnums(['N', 'A', 'I', 'EN', 'EA', 'EI']),
                ),
            ],
        ]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofString(OUTPUT)]]))]));

export class FromNow extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let dates = context.getArguments()?.get(VALUE);
        let key = context.getArguments()?.get(KEY);

        if (dates.length == 1) {
            if (!isValidZuluDate(dates[0]))
                throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);

            const currentDate = new Date();

            const differenceInMilliseconds =
                currentDate.getTime() - Date.parse(getOffsetAndDateString(dates[0])[0]);

            const seconds = Math.floor(differenceInMilliseconds / 1000);
            const minutes = Math.floor(seconds / 60);
            const hours = Math.floor(minutes / 60);
            const days = Math.floor(hours / 24);

            const output = DurationUtils.getDuration(
                differenceInMilliseconds,
                key,
                Math.abs(days),
                Math.abs(hours),
                Math.abs(minutes),
                Math.abs(seconds),
            );
            return new FunctionOutput([
                EventResult.outputOf(
                    new Map([
                        [OUTPUT, output],
                    ]),
                ),
            ]);
        }
        if (dates.length == 2) {
            if (!isValidZuluDate(dates[0]) || !isValidZuluDate(dates[1]))
                throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);

            const differenceInMilliseconds =
                Date.parse(getOffsetAndDateString(dates[1])[0]) -
                Date.parse(getOffsetAndDateString(dates[0])[0]);

            const seconds = Math.floor(differenceInMilliseconds / 1000);
            const minutes = Math.floor(seconds / 60);
            const hours = Math.floor(minutes / 60);
            const days = Math.floor(hours / 24);

            const output = DurationUtils.getDuration(
                differenceInMilliseconds,
                key,
                Math.abs(days),
                Math.abs(hours),
                Math.abs(minutes),
                Math.abs(seconds),
            );
            return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT, output]]))]);
        }
        throw new KIRuntimeException(`Please provide valid dates.`);
    }
}
