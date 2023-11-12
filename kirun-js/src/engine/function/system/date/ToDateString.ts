import { Schema } from '../../../json/schema/Schema';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';
import { Event } from '../../../model/Event';
import { KIRuntimeException } from '../../../exception/KIRuntimeException';

import isValidISO8601DateTime from '../../../util/date/isValidISODate';
import { formattedStringFromDate } from '../../../util/date/DateFormatterUtil';
import { EventResult } from '../../../model/EventResult';

const ISODATE = 'isoDate';
const FORMAT = 'dateFormat';
const OUTPUT = 'result';

const signature: FunctionSignature = new FunctionSignature('ToDateString')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([
            [ISODATE, new Parameter(ISODATE, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))],
            [FORMAT, new Parameter(FORMAT, Schema.ofString(FORMAT))],
        ]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofString(OUTPUT)]]))]));

export class ToDateString extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const inputDate: string = context.getArguments()?.get(ISODATE);
        if (!isValidISO8601DateTime(inputDate))
            throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);

        const dateFormat: string = context.getArguments()?.get(FORMAT);

        var date: Date = new Date(inputDate);

        const result = formattedStringFromDate(date, dateFormat);

        return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT, result]]))]);
    }
}
