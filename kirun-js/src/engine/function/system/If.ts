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
        .setParameters(new Map([Parameter.ofEntry(If.CONDITION, Schema.ofAny(If.CONDITION))]))
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

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let condition = context.getArguments()?.get(If.CONDITION);

        let conditionValue = !!condition || condition === '';

        return new FunctionOutput([
            EventResult.of(conditionValue ? Event.TRUE : Event.FALSE, new Map()),
            EventResult.outputOf(new Map()),
        ]);
    }
}
