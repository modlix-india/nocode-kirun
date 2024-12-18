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

const STEP_NAME = 'stepName';
export class Break extends AbstractFunction {
    private readonly signature = new FunctionSignature('Break')
        .setNamespace(Namespaces.SYSTEM_LOOP)
        .setParameters(
            new Map([Parameter.ofEntry(STEP_NAME, Schema.of(STEP_NAME, SchemaType.STRING))]),
        )
        .setEvents(new Map([Event.outputEventMapEntry(new Map([]))]));
    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let stepName: string = context.getArguments()?.get(STEP_NAME);

        context.getExecutionContext().set(stepName, true);

        return new FunctionOutput([EventResult.outputOf(new Map())]);
    }
}
