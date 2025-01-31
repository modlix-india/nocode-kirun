import { FunctionSignature } from '../../model/FunctionSignature';
import { AbstractFunction } from '../AbstractFunction';
import { Namespaces } from '../../namespaces/Namespaces';
import { Event } from '../../model/Event';
import { Parameter } from '../../model/Parameter';
import { Schema } from '../../json/schema/Schema';
import { ParameterType } from '../../model/ParameterType';
import { MapUtil } from '../../util/MapUtil';
import { FunctionExecutionParameters } from '../../runtime/FunctionExecutionParameters';
import { FunctionOutput } from '../../model/FunctionOutput';
import { KIRuntimeException } from '../../exception/KIRuntimeException';
import { Repository } from '../../Repository';
import { EventResult } from '../../model/EventResult';
import { SchemaValidator } from '../../json/schema/validator/SchemaValidator';

export class ValidateSchema extends AbstractFunction {
    private static readonly SOURCE: string = 'source';
    private static readonly SCHEMA: string = 'schema';
    private static readonly IS_VALID: string = 'isValid';

    private readonly signature: FunctionSignature = new FunctionSignature('ValidateSchema')
        .setNamespace(Namespaces.SYSTEM_OBJECT)
        .setParameters(
            new Map([
                Parameter.ofEntry(ValidateSchema.SOURCE, Schema.ofAny(ValidateSchema.SCHEMA)),
                Parameter.ofEntry(
                    ValidateSchema.SCHEMA,
                    Schema.SCHEMA,
                    false,
                    ParameterType.CONSTANT,
                ),
            ]),
        )
        .setEvents(
            new Map([
                Event.outputEventMapEntry(
                    MapUtil.of(ValidateSchema.IS_VALID, Schema.ofBoolean(ValidateSchema.IS_VALID)),
                ),
            ]),
        );

    getSignature(): FunctionSignature {
        return this.signature;
    }

    protected internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let element: any = context.getArguments()?.get(ValidateSchema.SOURCE);
        let schema: Schema | undefined = Schema.from(
            context?.getArguments()?.get(ValidateSchema.SCHEMA),
        );

        if (!schema) {
            throw new KIRuntimeException('Schema is not supplied.');
        }

        return this.validateSchema(schema, context.getSchemaRepository(), element);
    }

    private async validateSchema(
        targetSchema: Schema,
        targetSchemaRepository: Repository<Schema>,
        element: any,
    ): Promise<FunctionOutput> {
        try {
            await SchemaValidator.validate([], targetSchema, targetSchemaRepository, element, true);

            return new FunctionOutput([
                EventResult.outputOf(MapUtil.of(ValidateSchema.IS_VALID, true)),
            ]);
        } catch (error: any) {
            return new FunctionOutput([
                EventResult.outputOf(MapUtil.of(ValidateSchema.IS_VALID, false)),
            ]);
        }
    }
}
