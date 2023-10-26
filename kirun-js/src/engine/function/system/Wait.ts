import { Schema } from '../../json/schema/Schema';
import { Event } from '../../model/Event';
import { EventResult } from '../../model/EventResult';
import { FunctionOutput } from '../../model/FunctionOutput';
import { FunctionSignature } from '../../model/FunctionSignature';
import { Parameter } from '../../model/Parameter';
import { Namespaces } from '../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../AbstractFunction';

export class Wait extends AbstractFunction {
    private static readonly MILLIS: string = 'millis';

    private static readonly SIGNATURE: FunctionSignature = new FunctionSignature('Wait')
        .setNamespace(Namespaces.SYSTEM)
        .setParameters(
            new Map([
                Parameter.ofEntry(
                    Wait.MILLIS,
                    Schema.ofNumber(Wait.MILLIS).setMinimum(0).setDefaultValue(0),
                ),
            ]),
        )
        .setEvents(new Map([Event.outputEventMapEntry(new Map())]));

    public getSignature(): FunctionSignature {
        return Wait.SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        var condition = context.getArguments()?.get(Wait.MILLIS);

        await new Promise((resolve) => setTimeout(resolve, condition));

        return new FunctionOutput([EventResult.outputOf(new Map())]);
    }
}
