import { Schema } from '../../../json/schema/Schema';
import { SchemaType } from '../../../json/schema/type/SchemaType';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

const COUNT = 'CountLoop';
const VALUE = 'value';
const INDEX = 'index';

const SIGNATURE = new FunctionSignature()
    .setName('CountLoop')
    .setNamespace(Namespaces.SYSTEM_LOOP)
    .setParameters(
        new Map([
            Parameter.ofEntry(COUNT, Schema.of(COUNT, SchemaType.INTEGER).setDefaultValue(1)),
        ]),
    )
    .setEvents(
        new Map([
            Event.eventMapEntry(
                Event.ITERATION,
                new Map([[INDEX, Schema.of(INDEX, SchemaType.INTEGER)]]),
            ),
            Event.outputEventMapEntry(new Map([[VALUE, Schema.of(VALUE, SchemaType.INTEGER)]])),
        ]),
    );

export class RangeLoop extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let count: number = context.getArguments().get(COUNT);
        let current = 0;

        return new FunctionOutput({
            next(): EventResult {
                if (current >= count) {
                    return EventResult.outputOf(new Map([[VALUE, current]]));
                }

                const eve = EventResult.of(Event.ITERATION, new Map([[INDEX, current]]));
                ++current;

                return eve;
            },
        });
    }
}
