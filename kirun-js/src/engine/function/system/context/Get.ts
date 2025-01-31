import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { StringFormat } from '../../../json/schema/string/StringFormat';
import { SchemaType } from '../../../json/schema/type/SchemaType';
import { TypeUtil } from '../../../json/schema/type/TypeUtil';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { ParameterType } from '../../../model/ParameterType';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { AbstractFunction } from '../../AbstractFunction';

const NAME = 'name';
const VALUE = 'value';

export class Get extends AbstractFunction {
    private readonly signature = new FunctionSignature('Get')
        .setNamespace(Namespaces.SYSTEM_CTX)
        .setParameters(
            new Map([
                Parameter.ofEntry(
                    NAME,
                    new Schema()
                        .setName(NAME)
                        .setType(TypeUtil.of(SchemaType.STRING))
                        .setMinLength(1)
                        .setFormat(StringFormat.REGEX)
                        .setPattern('^[a-zA-Z_$][a-zA-Z_$0-9]*$'),
                    false,
                    ParameterType.CONSTANT,
                ),
            ]),
        )
        .setEvents(new Map([Event.outputEventMapEntry(new Map([[VALUE, Schema.ofAny(VALUE)]]))]));
    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const name: string = context?.getArguments()?.get(NAME);

        if (!context.getContext()?.has(name))
            throw new KIRuntimeException(
                StringFormatter.format("Context don't have an element for '$' ", name),
            );

        return new FunctionOutput([
            EventResult.outputOf(new Map([VALUE, context.getContext()?.get(name)?.getElement()])),
        ]);
    }
}
