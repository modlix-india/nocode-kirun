import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import addDays from '../../../util/addDays';
import addHours from '../../../util/addHours';
import addMilliSeconds from '../../../util/addMilliSeconds';
import addMinutes from '../../../util/addMinutes';
import addMonths from '../../../util/addMonths';
import addSeconds from '../../../util/addSeconds';
import addYears from '../../../util/addYears';
import isValidISO8601DateTime from '../../../util/isValidISODate';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE = 'isodate';
const OUTPUT = 'epoch';
const SIGNATURE = new FunctionSignature('DateToEpoch')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([[VALUE, new Parameter(VALUE, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))]]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofInteger(OUTPUT)]]))]));

export class AddTime extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const date: string = context.getArguments()?.get(VALUE);
        const addValue: number = context.getArguments()?.get(ADDVALUE);
        const unit: string = context.getArguments()?.get(UNIT);
        console.log(date, addValue, unit);
        if (!isValidISO8601DateTime(date))
            throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
        let outputDate = new Date(date);
        switch (unit) {
            case 'YEARS':
                outputDate = addYears(outputDate, addValue);
                break;
            case 'MONTHS':
                outputDate = addMonths(outputDate, addValue);
                break;
            case 'DAYS':
                outputDate = addDays(outputDate, addValue);
                break;
            case 'HOURS':
                outputDate = addHours(outputDate, addValue);
                break;
            case 'MINUTES':
                outputDate = addMinutes(outputDate, addValue);
                break;
            case 'SECONDS':
                outputDate = addSeconds(outputDate, addValue);
                break;
            case 'MILLIS':
                outputDate = addMilliSeconds(outputDate, addValue);
                break;
        }

        return new FunctionOutput([
            EventResult.outputOf(new Map([[OUTPUT, outputDate.toISOString()]])),
        ]);
    }
}
