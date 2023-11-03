import { Schema } from '../../../json/schema/Schema';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

import isValidDate from '../../../util/isValidISODate';
import { Event } from '../../../model/Event';
import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { formattedStringFromDate } from '../../../util/date/formattedStringFromDateUtil';
import { EventResult } from '../../../model/EventResult';

const VALUE = 'isodate';
const FORMAT = 'format';
const OUTPUT = 'result';

export class ToDateString extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return new FunctionSignature('ToDateString')
            .setNamespace(Namespaces.DATE)
            .setParameters(
                new Map([
                    [VALUE, new Parameter(VALUE, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))],
                    [FORMAT, new Parameter(FORMAT, Schema.ofString(FORMAT))],
                ]),
            )
            .setEvents(
                new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofString(OUTPUT)]]))]),
            );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        var inputDate: string = context.getArguments()?.get(VALUE);

        if (!isValidDate(inputDate)) throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);

        var pattern: string = context.getArguments()?.get(FORMAT);

        var date: Date = new Date(inputDate);

        var formattedString: string = formattedStringFromDate(date, pattern);

        return new FunctionOutput([EventResult.of(OUTPUT, new Map([[OUTPUT, formattedString]]))]);
    }
}
