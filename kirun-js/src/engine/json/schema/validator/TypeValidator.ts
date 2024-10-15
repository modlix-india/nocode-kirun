import { Repository } from '../../../Repository';
import { Schema } from '../Schema';
import { SchemaType } from '../type/SchemaType';
import { ArrayValidator } from './ArrayValidator';
import { BooleanValidator } from './BooleanValidator';
import { NullValidator } from './NullValidator';
import { NumberValidator } from './NumberValidator';
import { ObjectValidator } from './ObjectValidator';
import { StringValidator } from './StringValidator';
import { ConversionMode } from '../convertor/enums/ConversionMode';
import { ConvertorUtil } from '../../../util/json/ConvertorUtil';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { StringConvertor } from '../convertor/StringConvertor';
import { NumberConvertor } from '../convertor/NumberConvertor';
import { BooleanConvertor } from '../convertor/BooleanConvertor';
import { NullConvertor } from '../convertor/NullConvertor';
import { isNullValue } from '../../../util/NullCheck';
import { ValidatorUtil } from '../../../util/json/ValidatorUtil';

export class TypeValidator {
    public static async validate(
        parents: Schema[],
        type: SchemaType,
        schema: Schema,
        repository: Repository<Schema> | undefined,
        element: any,
        convert?: boolean,
        mode?: ConversionMode,
    ): Promise<any> {
        if (type == SchemaType.OBJECT) {
            return await ObjectValidator.validate(
                parents,
                schema,
                repository,
                element,
                convert,
                mode,
            );
        } else if (type == SchemaType.ARRAY) {
            return await ArrayValidator.validate(
                parents,
                schema,
                repository,
                element,
                convert,
                mode,
            );
        }

        return this.handleTypeValidationAndConversion(
            parents,
            type,
            schema,
            element,
            convert,
            mode,
        );
    }

    private static async handleTypeValidationAndConversion(
        parents: Schema[],
        type: SchemaType,
        schema: Schema,
        element: any,
        convert?: boolean,
        mode?: ConversionMode,
    ): Promise<any> {
        const cElement = convert
            ? this.convertElement(parents, type, schema, element, mode ?? ConversionMode.STRICT)
            : element;

        return await this.validateElement(
            parents,
            type,
            schema,
            cElement,
            mode ?? ConversionMode.STRICT,
        );
    }

    private static convertElement(
        parents: Schema[],
        type: SchemaType,
        schema: Schema,
        element: any,
        mode: ConversionMode,
    ): any | null {
        if (isNullValue(type)) {
            return ConvertorUtil.handleUnConvertibleValueWithDefault(
                parents,
                mode,
                element,
                schema.getDefaultValue() ?? null,
                StringFormatter.format('$ is not a valid type for conversion.', type),
            );
        }

        switch (type) {
            case SchemaType.STRING:
                return StringConvertor.convert(parents, schema, mode, element);
            case SchemaType.INTEGER:
            case SchemaType.LONG:
            case SchemaType.DOUBLE:
            case SchemaType.FLOAT:
                return NumberConvertor.convert(parents, type, schema, mode, element);
            case SchemaType.BOOLEAN:
                return BooleanConvertor.convert(parents, schema, mode, element);
            case SchemaType.NULL:
                return NullConvertor.convert(parents, schema, mode, element);
            default:
                return ConvertorUtil.handleUnConvertibleValueWithDefault(
                    parents,
                    mode,
                    element,
                    schema.getDefaultValue() ?? null,
                    StringFormatter.format('$ is not a valid type for conversion.', type),
                );
        }
    }

    private static validateElement(
        parents: Schema[],
        type: SchemaType,
        schema: Schema,
        element: any,
        mode: ConversionMode,
    ): any | null {
        if (isNullValue(type)) {
            return ValidatorUtil.handleValidationError(
                parents,
                mode,
                element,
                schema.getDefaultValue() ?? null,
                StringFormatter.format('$ is not a valid type.', type),
            );
        }

        switch (type) {
            case SchemaType.STRING:
                return StringValidator.validate(parents, schema, element);
            case SchemaType.INTEGER:
            case SchemaType.LONG:
            case SchemaType.DOUBLE:
            case SchemaType.FLOAT:
                return NumberValidator.validate(type, parents, schema, element);
            case SchemaType.BOOLEAN:
                return BooleanValidator.validate(parents, schema, element);
            case SchemaType.NULL:
                return NullValidator.validate(parents, schema, element);
            default:
                return ValidatorUtil.handleValidationError(
                    parents,
                    mode,
                    element,
                    schema.getDefaultValue() ?? null,
                    StringFormatter.format('$ is not a valid type.', type),
                );
        }
    }

    private constructor() {}
}
