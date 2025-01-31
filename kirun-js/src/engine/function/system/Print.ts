import { Schema } from '../../json/schema/Schema';
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
    private static readonly STREAM: string = 'stream';

    private static readonly LOG: string = 'LOG';
    private static readonly ERROR: string = 'ERROR';

    private readonly signature = new FunctionSignature('Print')
        .setNamespace(Namespaces.SYSTEM)
        .setParameters(
            new Map([
                Parameter.ofEntry(Print.VALUES, Schema.ofAny(Print.VALUES), true),
                Parameter.ofEntry(
                    Print.STREAM,
                    Schema.ofString(Print.STREAM)
                        .setEnums([Print.LOG, Print.ERROR])
                        .setDefaultValue(Print.LOG),
                ),
            ]),
        )
        .setEvents(new Map([Event.outputEventMapEntry(new Map())]));
    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let values = context.getArguments()?.get(Print.VALUES);

        const stream = context.getArguments()?.get(Print.STREAM);

        (stream === Print.LOG ? console?.log : console?.error)?.(...values);

        return new FunctionOutput([EventResult.outputOf(new Map())]);
    }
}
