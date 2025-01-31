import { Schema } from '../../../json/schema/Schema';
import { SchemaType } from '../../../json/schema/type/SchemaType';
import { SingleType } from '../../../json/schema/type/SingleType';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

export class Concatenate extends AbstractFunction {
    public static VALUE: string = 'value';

    private readonly signature: FunctionSignature = new FunctionSignature('Concatenate')
        .setNamespace(Namespaces.STRING)
        .setParameters(
            new Map([
                [
                    Concatenate.VALUE,
                    new Parameter(
                        Concatenate.VALUE,
                        new Schema()
                            .setName(Concatenate.VALUE)
                            .setType(new SingleType(SchemaType.STRING)),
                    ).setVariableArgument(true),
                ],
            ]),
        )
        .setEvents(
            new Map([
                Event.outputEventMapEntry(
                    new Map([[Concatenate.VALUE, Schema.ofString(Concatenate.VALUE)]]),
                ),
            ]),
        );

    public constructor() {
        super();
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let contextArgs: string[] = context.getArguments()?.get(Concatenate.VALUE);

        let concatenatedString: string = '';

        contextArgs.reduce((curr, next) => (concatenatedString = curr + next), concatenatedString);

        return new FunctionOutput([
            EventResult.outputOf(new Map([[Concatenate.VALUE, concatenatedString]])),
        ]);
    }
}
