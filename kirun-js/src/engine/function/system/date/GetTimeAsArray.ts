import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import isValidZuluDate from '../../../util/date/isValidISODate';
import { AbstractFunction } from '../../AbstractFunction';

const iso8601Pattern =
    /^([+-]?\d{6}|\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d{3})?(Z|([+-]([01]\d|2[0-3]):([0-5]\d)))?$/;

const VALUE = 'isodate';
const OUTPUT = 'result';
const SIGNATURE = new FunctionSignature('TimeAsArray')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([[VALUE, new Parameter(VALUE, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))]]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofArray(OUTPUT)]]))]));

export class GetTimeAsArray extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let date: string = context.getArguments()?.get(VALUE);

        if (!isValidZuluDate(date)) throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);

        const match = date.match(iso8601Pattern);

        const timeArray = [];

        if (match) {
            timeArray.push(parseInt(match[1]));
            timeArray.push(parseInt(match[2]));
            timeArray.push(parseInt(match[3]));
            timeArray.push(parseInt(match[4]));
            timeArray.push(parseInt(match[5]));
            timeArray.push(parseInt(match[6]));
            timeArray.push(parseInt(match[7].slice(1)));
        }

        return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT, timeArray]]))]);
    }
}
