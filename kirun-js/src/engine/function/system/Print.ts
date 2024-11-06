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

export class Print extends AbstractFunction {
    private static readonly VALUES: string = 'values';

    private static readonly SIGNATURE: FunctionSignature = new FunctionSignature('Print')
        .setNamespace(Namespaces.SYSTEM)
        .setParameters(new Map([Parameter.ofEntry(Print.VALUES, Schema.ofAny(Print.VALUES), true)]))
        .setEvents(new Map([Event.outputEventMapEntry(new Map())]));

    public getSignature(): FunctionSignature {
        return Print.SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        var values = context.getArguments()?.get(Print.VALUES);

        console?.log(...values);

        return new FunctionOutput([EventResult.outputOf(new Map())]);
    }
}
