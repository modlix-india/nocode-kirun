import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { isNullValue } from '../../../util/NullCheck';
import getTimezoneOffset from '../../../util/date/getTimeZoneOffset';
import isValidZuluDate from '../../../util/date/isValidISODate';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE = 'isodate';
const OUTPUT = 'timeZoneOffset';
const SIGNATURE = new FunctionSignature('GetTimeZoneOffset')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([[VALUE, new Parameter(VALUE, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))]]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofInteger(OUTPUT)]]))]));

export class GetTimeZoneOffset extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let date: string = context.getArguments()?.get(VALUE);
        const offset = getTimezoneOffset(date);
        if (isNullValue(offset)) throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
        return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT, offset]]))]);
    }
}
