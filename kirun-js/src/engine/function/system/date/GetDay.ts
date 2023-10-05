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
const OUTPUT = 'day';
const SIGNATURE = new FunctionSignature('GetDay')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([[VALUE, new Parameter(VALUE, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))]]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofInteger(OUTPUT)]]))]));

export class GetDay extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let date: string = context.getArguments()?.get(VALUE);
        if (!isValidZuluDate(date)) throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
        return new FunctionOutput([
            EventResult.outputOf(new Map([[OUTPUT, new Date(date).getDay()]])),
        ]);
    }
}
