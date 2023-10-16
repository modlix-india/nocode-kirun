import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE = 'epoch';
const OUTPUT = 'date';

const SIGNATURE = new FunctionSignature('EpochToDate')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([
            [
                VALUE,
                new Parameter(
                    VALUE,
                    new Schema().setOneOf([
                        Schema.ofInteger(VALUE),
                        Schema.ofLong(VALUE),
                        Schema.ofString(VALUE),
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

export class EpochToDate extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let date: number = parseInt(context.getArguments()?.get(VALUE));
        if (isNaN(date)) throw new KIRuntimeException('Please provide a valid value for epoch.');
        return new FunctionOutput([
            EventResult.outputOf(
                new Map([
                    [OUTPUT, new Date(date > 999999999999 ? date : date * 1000).toISOString()],
                ]),
            ),
        ]);
    }
}
