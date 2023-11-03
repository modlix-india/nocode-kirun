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
import {} from '../../../util/date/formattedStringFromDateUtil';
import { EventResult } from '../../../model/EventResult';

const VALUE = 'date';
const OUTPUT = 'result';

export class FromDateString extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return new FunctionSignature('FromDateString')
            .setNamespace(Namespaces.DATE)
            .setParameters(new Map([[VALUE, new Parameter(VALUE, Schema.ofString(VALUE))]]))
            .setEvents(
                new Map([
                    Event.outputEventMapEntry(
                        new Map([[OUTPUT, Schema.ofRef(`${Namespaces.DATE}.timeStamp`)]]),
                    ),
                ]),
            );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        var inputDate: string = context.getArguments()?.get(VALUE);

        var formattedDate: string = '';

        if (!isValidDate(formattedDate))
            throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);

        return new FunctionOutput([EventResult.of(OUTPUT, new Map([[OUTPUT, formattedDate]]))]);
    }
}
