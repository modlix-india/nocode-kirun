import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import isValidISO8601DateTime from '../../../util/isValidISODate';
import subtractDateTime from '../../../util/subtractDateTime';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE = 'isodate';
const SUBTRACTVALUE = 'subtract';
const UNIT = 'unit';
const OUTPUT = 'dateTime';
const SIGNATURE = new FunctionSignature('SubtractTime')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([
            [VALUE, new Parameter(VALUE, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))],
            [SUBTRACTVALUE, new Parameter(SUBTRACTVALUE, Schema.ofInteger(SUBTRACTVALUE))],
            [
                UNIT,
                new Parameter(
                    UNIT,
                    Schema.ofString(UNIT).setEnums([
                        'YEARS',
                        'MONTHS',
                        'DAYS',
                        'HOURS',
                        'MINUTES',
                        'SECONDS',
                        'MILLIS',
                    ]),
                ),
            ],
        ]),
    )
    .setEvents(
        new Map([
            Event.outputEventMapEntry(
                new Map([[OUTPUT, Schema.ofRef(`${Namespaces.DATE}.timeStamp`)]]),
            ),
        ]),
    );

export class SubtractTime extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const date: string = context.getArguments()?.get(VALUE);
        const subtractValue: number = context.getArguments()?.get(SUBTRACTVALUE);
        const unit: string = context.getArguments()?.get(UNIT);
        console.log(date, subtractValue, unit);
        if (!isValidISO8601DateTime(date))
            throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
        let outputDate = new Date(date);
        const out = subtractDateTime(outputDate, subtractValue, unit);
        return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT, out]]))]);
    }
}
