import { Schema } from '../json/schema/Schema';
import { SchemaType } from '../json/schema/type/SchemaType';
import { TypeUtil } from '../json/schema/type/TypeUtil';
import { Namespaces } from '../namespaces/Namespaces';
import { ParameterReferenceType } from './ParameterReferenceType';

export class ParameterReference {
    private static readonly SCHEMA_NAME: string = 'ParameterReference';
    public static readonly SCHEMA: Schema = new Schema()
        .setNamespace(Namespaces.SYSTEM)
        .setName(ParameterReference.SCHEMA_NAME)
        .setType(TypeUtil.of(SchemaType.OBJECT))
        .setProperties(
            new Map([
                ['value', Schema.ofAny('value')],
                ['expression', Schema.ofString('expression')],
                ['type', Schema.ofString('type').setEnums(['EXPRESSION', 'VALUE'])],
            ]),
        );
    private type: ParameterReferenceType;
    private value: any;
    private expression?: string;

    constructor(type: ParameterReferenceType) {
        this.type = type;
    }

    public getType(): ParameterReferenceType {
        return this.type;
    }
    public setType(type: ParameterReferenceType): ParameterReference {
        this.type = type;
        return this;
    }
    public getValue(): any {
        return this.value;
    }
    public setValue(value: any): ParameterReference {
        this.value = value;
        return this;
    }
    public getExpression(): string | undefined {
        return this.expression;
    }
    public setExpression(expression: string): ParameterReference {
        this.expression = expression;
        return this;
    }

    public static ofExpression(value: any): ParameterReference {
        return new ParameterReference(ParameterReferenceType.EXPRESSION).setExpression(value);
    }

    public static ofValue(value: any): ParameterReference {
        return new ParameterReference(ParameterReferenceType.VALUE).setValue(value);
    }

    public static from(json: any): ParameterReference[] {
        if (!json) return [];
        return Array.from(json).map((e: any) =>
            new ParameterReference(e.type).setValue(e.value).setExpression(e.expression),
        );
    }
}
