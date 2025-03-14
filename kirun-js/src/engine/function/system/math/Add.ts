import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE = 'value';
export class Add extends AbstractFunction {
    private readonly signature = new FunctionSignature('Add')
        .setNamespace(Namespaces.MATH)
        .setParameters(
            new Map([
                [VALUE, new Parameter(VALUE, Schema.ofNumber(VALUE)).setVariableArgument(true)],
            ]),
        )
        .setEvents(
            new Map([Event.outputEventMapEntry(new Map([[VALUE, Schema.ofNumber(VALUE)]]))]),
        );
    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let nums: number[] = context.getArguments()?.get(VALUE);

        return new FunctionOutput([
            EventResult.outputOf(new Map([[VALUE, nums.reduce((a, c) => (a += c), 0)]])),
        ]);
    }
}
