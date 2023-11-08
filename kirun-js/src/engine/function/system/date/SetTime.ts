import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';
import isValidZuluDate from '../../../util/date/isValidISODate';
import { EventResult } from '../../../model/EventResult';

const VALUE = 'isodate';
const SET_TIME = 'setTime';
const OUTPUT = 'date';

const SIGNATURE = new FunctionSignature('SetTime')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([
            [VALUE, new Parameter(VALUE, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))],
            [SET_TIME, new Parameter(SET_TIME, Schema.ofInteger(SET_TIME))],
        ]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofInteger(OUTPUT)]]))]));

export class SetTime extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let date = context.getArguments()?.get(VALUE);
        if (!isValidZuluDate(date)) throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
        let addTime = context.getArguments()?.get(SET_TIME);

        var updatedDate = new Date(date).setTime(addTime);

        return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT, updatedDate]]))]);
    }
}
