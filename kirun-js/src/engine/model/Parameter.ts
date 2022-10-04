import { Schema } from '../json/schema/Schema';
import { SchemaType } from '../json/schema/type/SchemaType';
import { TypeUtil } from '../json/schema/type/TypeUtil';
import { SchemaReferenceException } from '../json/schema/validator/exception/SchemaReferenceException';
import { Namespaces } from '../namespaces/Namespaces';
import { ParameterType } from './ParameterType';

const VALUE: string = 'value';

export class Parameter {
    private static readonly SCHEMA_NAME: string = 'Parameter';

    public static readonly SCHEMA: Schema = new Schema()
        .setNamespace(Namespaces.SYSTEM)
        .setName(Parameter.SCHEMA_NAME)
        .setProperties(
            new Map([
                ['schema', Schema.SCHEMA],
                ['parameterName', Schema.ofString('parameterName')],
                [
                    'variableArgument',
                    Schema.of('variableArgument', SchemaType.BOOLEAN).setDefaultValue(false),
                ],
                ['type', Schema.ofString('type').setEnums(['EXPRESSION', 'CONSTANT'])],
            ]),
        );

    // Place holder for the expression parameter when required to represent an expression in the value.
    public static readonly EXPRESSION: Schema = new Schema()
        .setNamespace(Namespaces.SYSTEM)
        .setName('ParameterExpression')
        .setType(TypeUtil.of(SchemaType.OBJECT))
        .setProperties(
            new Map([
                ['isExpression', Schema.ofBoolean('isExpression').setDefaultValue(true)],
                [VALUE, Schema.ofAny(VALUE)],
            ]),
        );

    private schema: Schema;

    private parameterName: string;
    private variableArgument: boolean = false;
    private type: ParameterType = ParameterType.EXPRESSION;

    constructor(pn: string | Parameter, schema?: Schema) {
        if (pn instanceof Parameter) {
            this.schema = new Schema(pn.schema);
            this.parameterName = pn.parameterName;
            this.variableArgument = pn.variableArgument;
            this.type = pn.type;
        } else {
            if (!schema) {
                throw new Error('Unknown constructor signature');
            }
            this.schema = schema;
            this.parameterName = pn;
        }
    }

    public getSchema(): Schema {
        return this.schema;
    }
    public setSchema(schema: Schema): Parameter {
        this.schema = schema;
        return this;
    }
    public getParameterName(): string {
        return this.parameterName;
    }
    public setParameterName(parameterName: string): Parameter {
        this.parameterName = parameterName;
        return this;
    }
    public isVariableArgument(): boolean {
        return this.variableArgument;
    }
    public setVariableArgument(variableArgument: boolean): Parameter {
        this.variableArgument = variableArgument;
        return this;
    }
    public getType(): ParameterType {
        return this.type;
    }
    public setType(type: ParameterType): Parameter {
        this.type = type;
        return this;
    }

    public static ofEntry(
        name: string,
        schema: Schema,
        variableArgument: boolean = false,
        type: ParameterType = ParameterType.EXPRESSION,
    ): [string, Parameter] {
        return [
            name,
            new Parameter(name, schema).setType(type).setVariableArgument(variableArgument),
        ];
    }

    public static of(
        name: string,
        schema: Schema,
        variableArgument: boolean = false,
        type: ParameterType = ParameterType.EXPRESSION,
    ): Parameter {
        return new Parameter(name, schema).setType(type).setVariableArgument(variableArgument);
    }

    public static from(json: any): Parameter {
        const paramSchema = Schema.from(json.schema);
        if (!paramSchema) throw new SchemaReferenceException('', 'Parameter requires Schema');
        return new Parameter(json.parameterName, paramSchema)
            .setVariableArgument(!!json.variableArguments)
            .setType(json.type ?? ParameterType.EXPRESSION);
    }
}
