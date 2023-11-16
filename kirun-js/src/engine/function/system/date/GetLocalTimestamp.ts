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

const iso8601Pattern =
    /^([+-]?\d{6}|\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d{3})?(Z|([+-]([01]\d|2[0-3]):([0-5]\d)))?$/;

const VALUE = 'isodate';
const OUTPUT = 'timestamp';
const SIGNATURE = new FunctionSignature('getLocalTimestamp')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([[VALUE, new Parameter(VALUE, Schema.ofRef(`${Namespaces.DATE}.timeStamp`))]]),
    )
    .setEvents(
        new Map([
            Event.outputEventMapEntry(
                new Map([[OUTPUT, Schema.ofRef(`${Namespaces.DATE}.timeStamp`)]]),
            ),
        ]),
    );

export class GetLocalTimestamp extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let inputDate: string = context.getArguments()?.get(VALUE);

        if (!isValidZuluDate(inputDate))
            throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);

        let date = new Date(inputDate);

        const currDate = new Date();

        date = new Date(date.getTime() - currDate.getTimezoneOffset() * 60000);

        let LocalOffsetValue;

        if (currDate.getTimezoneOffset() < 0) {
            LocalOffsetValue = `+${Math.abs(Math.ceil(currDate.getTimezoneOffset() / 60))
                .toString()
                .padStart(2, '0')}:${Math.abs(currDate.getTimezoneOffset() % 60)
                .toString()
                .padStart(2, '0')}`;
        } else {
            LocalOffsetValue = `-${Math.abs(Math.ceil(currDate.getTimezoneOffset() / 60))
                .toString()
                .padStart(2, '0')}:${Math.abs(currDate.getTimezoneOffset() % 60)
                .toString()
                .padStart(2, '0')}`;
        }

        return new FunctionOutput([
            EventResult.outputOf(
                new Map([[OUTPUT, date.toISOString().slice(0, -1) + LocalOffsetValue]]),
            ),
        ]);
    }
}
