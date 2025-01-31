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
import { ContextElement } from '../../../runtime/ContextElement';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { isNullValue } from '../../../util/NullCheck';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { AbstractFunction } from '../../AbstractFunction';

const NAME = 'name';
const SCHEMA = 'schema';
export class Create extends AbstractFunction {
    private readonly signature = new FunctionSignature('Create')
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
                Parameter.ofEntry(SCHEMA, Schema.SCHEMA, false, ParameterType.CONSTANT),
            ]),
        )
        .setEvents(new Map([Event.outputEventMapEntry(new Map())]));
    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const name: string = context?.getArguments()?.get(NAME);

        if (context?.getContext()?.has(name))
            throw new KIRuntimeException(
                StringFormatter.format("Context already has an element for '$' ", name),
            );

        let s: Schema | undefined = Schema.from(context?.getArguments()?.get(SCHEMA));

        if (!s) {
            throw new KIRuntimeException('Schema is not supplied.');
        }

        context
            .getContext()!
            .set(
                name,
                new ContextElement(
                    s,
                    isNullValue(s.getDefaultValue()) ? undefined : s.getDefaultValue(),
                ),
            );

        return new FunctionOutput([EventResult.outputOf(new Map())]);
    }
}
