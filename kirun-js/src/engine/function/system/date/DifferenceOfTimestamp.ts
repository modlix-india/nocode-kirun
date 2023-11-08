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

const VALUE_1 = 'isodates_1';
const VALUE_2 = 'isodates_2';
const OUTPUT = 'difference';
const SIGNATURE = new FunctionSignature('DifferenceOfTimestamp')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([
            [VALUE_1, new Parameter(VALUE_1, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))],
            [VALUE_2, new Parameter(VALUE_2, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))],
        ]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofInteger(OUTPUT)]]))]));

export class DifferenceOfTimestamp extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let timestamp1: string = context.getArguments()?.get(VALUE_1);
        let timestamp2: string = context.getArguments()?.get(VALUE_2);

        if (!isValidZuluDate(timestamp1) && !isValidZuluDate(timestamp2))
            throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);

        const timeDifferenceInSeconds = (Date.parse(timestamp1) - Date.parse(timestamp2));

        return new FunctionOutput([
            EventResult.outputOf(new Map([[OUTPUT, timeDifferenceInSeconds.toString()]])),
        ]);
    }
}
