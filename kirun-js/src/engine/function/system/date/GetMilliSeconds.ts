import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import isValidZuluDate from '../../../util/isValidISODate';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE = 'isodate';
const OUTPUT = 'milliSeconds';
const iso8601Pattern =
    /^(\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d+)?(Z|([+-]\d{2}:\d{2}))?$/;
const SIGNATURE = new FunctionSignature('GetMilliSeconds')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([[VALUE, new Parameter(VALUE, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))]]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofInteger(OUTPUT)]]))]));

export class GetMilliSeconds extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let date: string = context.getArguments()?.get(VALUE);
        if (!isValidZuluDate(date)) throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
        const match = date.match(iso8601Pattern);
        if (!match) throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
        const ms = match[7].substring(1);
        return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT, parseInt(ms, 10)]]))]);
    }
}
