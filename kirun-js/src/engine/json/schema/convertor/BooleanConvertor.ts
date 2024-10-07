import {Schema} from '../Schema';
import {ConversionMode} from './enums/ConversionMode';
import {handleUnConvertibleValue} from '../../../util/ConvertorUtil';

const BOOLEAN_MAP: Record<string, boolean> = {
    "true": true, "t": true, "yes": true, "y": true, "1": true,
    "false": false, "f": false, "no": false, "n": false, "0": false
};

export class BooleanConvertor {

    public static convert(parents: Schema[], schema: Schema, mode: ConversionMode, element: any): boolean | null {

        if (element == null) {
            return handleUnConvertibleValue(parents, mode, element, this.getDefault(schema), "Expected a boolean but found null");
        }

        const primitive = this.getBooleanPrimitive(element);

        return primitive ?? handleUnConvertibleValue(parents, mode, element, this.getDefault(schema), "Unable to convert to boolean");
    }

    private static getBooleanPrimitive(element: any): boolean | null {
        if (typeof element === 'boolean') {
            return element;
        } else if (typeof element === 'string') {
            return this.handleStringValue(element);
        } else if (typeof element === 'number') {
            return this.handleNumberValue(element);
        }
        return null;
    }

    private static handleStringValue(value: string): boolean | null {
        const trimmedValue = value.toLowerCase().trim();
        const result = BOOLEAN_MAP[trimmedValue];

        return result ?? null;
    }

    private static handleNumberValue(value: number): boolean | null {
        return (value === 0 || value === 1) ? (value === 1) : null;
    }

    private static getDefault(schema: Schema): boolean | null {
        return schema.getDefaultValue() !== null ? schema.getDefaultValue() : false; // Default to false
    }
}
