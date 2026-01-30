import { Schema } from '../json/schema/Schema';
import { SchemaType } from '../json/schema/type/SchemaType';
import { TypeUtil } from '../json/schema/type/TypeUtil';
import { Namespaces } from '../namespaces/Namespaces';
import { isNullValue } from '../util/NullCheck';
import UUID from '../util/UUID';
import { ParameterReferenceType } from './ParameterReferenceType';

export class ParameterReference {
    private static readonly SCHEMA_NAME: string = 'ParameterReference';
    public static readonly SCHEMA: Schema = new Schema()
        .setNamespace(Namespaces.SYSTEM)
        .setName(ParameterReference.SCHEMA_NAME)
        .setType(TypeUtil.of(SchemaType.OBJECT))
        .setProperties(
            new Map([
                ['key', Schema.ofString('key')],
                ['value', Schema.ofAny('value')],
                ['expression', Schema.ofString('expression')],
                ['type', Schema.ofString('type').setEnums(['EXPRESSION', 'VALUE'])],
                ['order', Schema.ofInteger('order')],
            ]),
        );

    private key: string;
    private type: ParameterReferenceType;
    private value: any;
    private expression?: string;
    private order?: number;

    constructor(type: ParameterReferenceType | ParameterReference) {
        if (type instanceof ParameterReference) {
            let pv = type as ParameterReference;
            this.key = pv.key;
            this.type = pv.type;
            this.value = isNullValue(pv.value) ? undefined : JSON.parse(JSON.stringify(pv.value));
            this.expression = pv.expression;
            this.order = pv.order;
        } else {
            this.type = type as ParameterReferenceType;
            this.key = UUID();
        }
    }

    public getType(): ParameterReferenceType {
        return this.type;
    }
    public setType(type: ParameterReferenceType): ParameterReference {
        this.type = type;
        return this;
    }

    public getKey(): string {
        return this.key;
    }
    public setKey(key: string): ParameterReference {
        this.key = key;
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

    public setOrder(order: number): ParameterReference {
        this.order = order;
        return this;
    }
    public getOrder(): number | undefined {
        return this.order;
    }

    public static ofExpression(value: any): [string, ParameterReference] {
        const param = new ParameterReference(ParameterReferenceType.EXPRESSION).setExpression(
            value,
        );
        return [param.getKey(), param];
    }

    public static ofValue(value: any): [string, ParameterReference] {
        const param = new ParameterReference(ParameterReferenceType.VALUE).setValue(value);
        return [param.getKey(), param];
    }

    public static from(e: any): ParameterReference {
        return new ParameterReference(e.type)
            .setValue(e.value)
            .setExpression(e.expression)
            .setKey(e.key)
            .setOrder(e.order);
    }

    public toJSON(): any {
        return {
            key: this.key,
            type: this.type,
            value: this.value,
            expression: this.expression,
            order: this.order,
        };
    }
}
