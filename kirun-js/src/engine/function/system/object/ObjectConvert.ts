import { AbstractFunction } from '../../AbstractFunction';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { Namespaces } from '../../../namespaces/Namespaces';
import { Event } from '../../../model/Event';
import { Parameter } from '../../../model/Parameter';
import { Schema } from '../../../json/schema/Schema';
import {
    ConversionMode,
    genericValueOf,
    getConversionModes,
} from '../../../json/schema/convertor/enums/ConversionMode';
import { Repository } from '../../../Repository';
import { SchemaValidator } from '../../../json/schema/validator/SchemaValidator';
import { MapUtil } from '../../../util/MapUtil';
import { EventResult } from '../../../model/EventResult';
import { ParameterType } from '../../../model/ParameterType';
import { KIRuntimeException } from '../../../exception/KIRuntimeException';

export class ObjectConvert extends AbstractFunction {

    private static readonly SOURCE: string = 'SOURCE';
    private static readonly SCHEMA: string = 'schema';
    private static readonly VALUE: string = 'value';
    private static readonly CONVERSION_MODE: string = 'conversionMode';

    getSignature(): FunctionSignature {
        return new FunctionSignature('ObjectConvert')
            .setNamespace(Namespaces.SYSTEM_OBJECT)
            .setParameters(new Map([
                Parameter.ofEntry(ObjectConvert.SOURCE, Schema.ofAny(ObjectConvert.SCHEMA)),
                Parameter.ofEntry(ObjectConvert.SCHEMA, Schema.SCHEMA, false, ParameterType.CONSTANT),
                Parameter.ofEntry(ObjectConvert.CONVERSION_MODE, Schema.ofString(ObjectConvert.CONVERSION_MODE)
                    .setEnums(getConversionModes())),
            ]))
            .setEvents(new Map([
                    Event.outputEventMapEntry(MapUtil.of(ObjectConvert.VALUE, Schema.ofAny(ObjectConvert.VALUE))),
                ]),
            );
    }

    protected internalExecute(
        context: FunctionExecutionParameters,
    ): Promise<FunctionOutput> {

        let element: any = context.getArguments()?.get(ObjectConvert.SOURCE);
        let schema: Schema | undefined = Schema.from(context?.getArguments()?.get(ObjectConvert.SCHEMA));

        if (!schema) {
            throw new KIRuntimeException('Schema is not supplied.');
        }

        const mode: ConversionMode = genericValueOf(context.getArguments()?.get(ObjectConvert.CONVERSION_MODE))
            || ConversionMode.STRICT;

        return this.convertToSchema(schema, context.getSchemaRepository(), element, mode);
    }

    private async convertToSchema(
        targetSchema: Schema,
        targetSchemaRepository: Repository<Schema>,
        element: any,
        mode: ConversionMode,
    ): Promise<FunctionOutput> {
        try {
            return new FunctionOutput([EventResult.outputOf(MapUtil.of(ObjectConvert.VALUE,
                SchemaValidator.validate([], targetSchema, targetSchemaRepository, element, true, mode)))]);
        } catch (error: any) {
            throw new KIRuntimeException(error?.message);
        }
    }
}
