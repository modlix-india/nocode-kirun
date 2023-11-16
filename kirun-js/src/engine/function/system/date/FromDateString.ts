import { Schema } from '../../../json/schema/Schema';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';
import { Event } from '../../../model/Event';
import { isNullValue } from '../../../util/NullCheck';
import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { dateFromFormatttedString } from '../../../util/date/DateFormatterUtil';
import { EventResult } from '../../../model/EventResult';

const DATE_IN_STRING = 'date';
const DATE_FORMAT = 'dateFormat';
const OUTPUT = 'result';
const signature: FunctionSignature = new FunctionSignature('FromDateString')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([
            [DATE_IN_STRING, new Parameter(DATE_IN_STRING, Schema.ofString(DATE_IN_STRING))],
            [DATE_FORMAT, new Parameter(DATE_FORMAT, Schema.ofString(DATE_FORMAT))],
        ]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofString(OUTPUT)]]))]));

export class FromDateString extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let formattedDate = context.getArguments()?.get(DATE_IN_STRING);
        let format = context.getArguments()?.get(DATE_FORMAT);
        if (isNullValue(formattedDate) || isNullValue(format))
            throw new KIRuntimeException('Please provide values in the date and format');

        const date = dateFromFormatttedString(formattedDate, format);

        return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT, date]]))]);
    }
}
