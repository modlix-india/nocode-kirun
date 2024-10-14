import { Schema } from '../Schema';
import { SchemaType } from '../type/SchemaType';
import { ConversionMode } from './enums/ConversionMode';
import { isNullValue } from '../../../util/NullCheck';
import { ConvertorUtil } from '../../../util/json/ConvertorUtil';

export class NumberConvertor {
    public static convert(
        parents: Schema[],
        type: SchemaType,
        schema: Schema,
        mode: ConversionMode,
        element: any,
    ): number | null {
        if (isNullValue(element)) {
            return ConvertorUtil.handleUnConvertibleValueWithDefault(
                parents,
                mode,
                element,
                this.getDefault(schema),
                'Expected a number but found null',
            );
        }

        if (typeof element === 'object' || typeof element === 'boolean' || Array.isArray(element)) {
            return ConvertorUtil.handleUnConvertibleValueWithDefault(
                parents,
                mode,
                element,
                this.getDefault(schema),
                element + ' is not a ' + type,
            );
        }

        if (typeof element === 'string') {
            element = Number(element);
            if (isNaN(element)) {
                return ConvertorUtil.handleUnConvertibleValueWithDefault(
                    parents,
                    mode,
                    element,
                    this.getDefault(schema),
                    element + ' is not a ' + type,
                );
            }
        }

        const number: number | null = this.extractNumber(type, element, mode);

        if (number === null) {
            return ConvertorUtil.handleUnConvertibleValueWithDefault(
                parents,
                mode,
                element,
                this.getDefault(schema),
                element + ' is not a ' + type,
            );
        }

        return number;
    }

    private static extractNumber(
        schemaType: SchemaType,
        element: number,
        mode: ConversionMode,
    ): number | null {
        if (typeof element !== 'number') {
            return null;
        }

        switch (schemaType) {
            case SchemaType.INTEGER:
                return this.isInteger(element, mode) ? Math.floor(element) : null;
            case SchemaType.LONG:
                return this.isLong(element, mode) ? Math.floor(element) : null;
            case SchemaType.DOUBLE:
                return element;
            case SchemaType.FLOAT:
                return this.isFloat(element, mode) ? element : null;
            default:
                return null;
        }
    }

    private static isInteger(value: number, mode: ConversionMode): boolean {
        if (mode !== ConversionMode.STRICT) {
            return typeof value === 'number';
        }
        return Number.isInteger(value);
    }

    private static isLong(value: number, mode: ConversionMode): boolean {
        if (mode !== ConversionMode.STRICT) {
            return typeof value === 'number';
        }
        return (
            Number.isInteger(value) &&
            value >= Number.MIN_SAFE_INTEGER &&
            value <= Number.MAX_SAFE_INTEGER
        );
    }

    private static isFloat(value: number, mode: ConversionMode): boolean {
        if (mode !== ConversionMode.STRICT) {
            return typeof value === 'number';
        }
        return value >= -Number.MAX_VALUE && value <= Number.MAX_VALUE;
    }

    private static getDefault(schema: Schema): number | null {
        if (typeof schema.getDefaultValue() === 'number') {
            return Number(schema.getDefaultValue);
        }
        return null;
    }
}
