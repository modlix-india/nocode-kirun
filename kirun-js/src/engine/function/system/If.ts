import { Schema } from '../../json/schema/Schema';
import { SchemaType } from '../../json/schema/type/SchemaType';
import { Event } from '../../model/Event';
import { EventResult } from '../../model/EventResult';
import { FunctionOutput } from '../../model/FunctionOutput';
import { FunctionSignature } from '../../model/FunctionSignature';
import { Parameter } from '../../model/Parameter';
import { Namespaces } from '../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../AbstractFunction';

export class If extends AbstractFunction {
    private static readonly CONDITION: string = 'condition';

    private static readonly SIGNATURE: FunctionSignature = new FunctionSignature('If')
        .setNamespace(Namespaces.SYSTEM)
        .setParameters(
            new Map([Parameter.ofEntry(If.CONDITION, Schema.of(If.CONDITION, SchemaType.BOOLEAN))]),
        )
        .setEvents(
            new Map([
                Event.eventMapEntry(Event.TRUE, new Map()),
                Event.eventMapEntry(Event.FALSE, new Map()),
                Event.outputEventMapEntry(new Map()),
            ]),
        );

    public getSignature(): FunctionSignature {
        return If.SIGNATURE;
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        var condition = context.getArguments()?.get(If.CONDITION);

        return new FunctionOutput([
            EventResult.of(condition ? Event.TRUE : Event.FALSE, new Map()),
            EventResult.outputOf(new Map()),
        ]);
    }
}
