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
                ['references', Schema.ofString('references')],
                ['value', Schema.ofAny('value')],
                ['expression', Schema.ofString('expression')],
            ]),
        );
    private type: ParameterReferenceType;
    private value: any;
    private expression: string;

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
    public getExpression(): string {
        return this.expression;
    }
    public setExpression(expression: string): ParameterReference {
        this.expression = expression;
        return this;
    }

    public static ofExpression(value: any): ParameterReference {
        return new ParameterReference()
            .setType(ParameterReferenceType.EXPRESSION)
            .setExpression(value);
    }

    public static ofValue(value: any): ParameterReference {
        return new ParameterReference().setType(ParameterReferenceType.VALUE).setValue(value);
    }
}
