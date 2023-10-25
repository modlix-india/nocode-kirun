import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import isValidISODate from '../../../util/isValidISODate';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE1 = 'isodate1';
const VALUE2 = 'isodate2';
const OUTPUT = 'output';

const SIGNATURE = new FunctionSignature('IsValidISODate')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([
            [VALUE1, new Parameter(VALUE1, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))],
            [VALUE2, new Parameter(VALUE2, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))],
        ]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofInteger(OUTPUT)]]))]));

export class IsValidISODate extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let date1: string = context.getArguments()?.get(VALUE1);
        let date2: string = context.getArguments()?.get(VALUE2);
        if (!isValidISODate(date1) || !isValidISODate(date2))
            throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
        const dateObj1 = new Date(date1);
        const dateObj2 = new Date(date2);
        const diffMilliseconds = dateObj2.getTime() - dateObj1.getTime();
        return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT, diffMilliseconds]]))]);
    }
}
