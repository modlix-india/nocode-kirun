import { AdditionalPropertiesType } from '../json/schema/object/AdditionalPropertiesType';
import { Schema } from '../json/schema/Schema';
import { SchemaType } from '../json/schema/type/SchemaType';
import { TypeUtil } from '../json/schema/type/TypeUtil';
import { Namespaces } from '../namespaces/Namespaces';
import { AbstractStatement } from './AbstractStatement';
import { ParameterReference } from './ParameterReference';
import { Position } from './Position';

export class Statement extends AbstractStatement {
    public static readonly SCHEMA_NAME: string = 'Statement';
    public static readonly SCHEMA: Schema = new Schema()
        .setNamespace(Namespaces.SYSTEM)
        .setName(Statement.SCHEMA_NAME)
        .setType(TypeUtil.of(SchemaType.OBJECT))
        .setProperties(
            new Map([
                ['statementName', Schema.ofString('statementName')],
                ['comment', Schema.ofString('comment')],
                ['description', Schema.ofString('description')],
                ['namespace', Schema.ofString('namespace')],
                ['name', Schema.ofString('name')],
                [
                    'dependentStatements',
                    Schema.ofObject('dependentstatement').setAdditionalProperties(
                        new AdditionalPropertiesType().setSchemaValue(Schema.ofBoolean('exists')),
                    ),
                ],
                [
                    'parameterMap',
                    new Schema()
                        .setName('parameterMap')
                        .setAdditionalProperties(
                            new AdditionalPropertiesType().setSchemaValue(
                                Schema.ofObject('parameterReference').setAdditionalProperties(
                                    new AdditionalPropertiesType().setSchemaValue(
                                        ParameterReference.SCHEMA,
                                    ),
                                ),
                            ),
                        ),
                ],
                ['position', Position.SCHEMA],
            ]),
        );
    private statementName: string;
    private namespace: string;
    private name: string;
    private parameterMap?: Map<string, Map<string, ParameterReference>>;
    private dependentStatements?: Map<string, boolean>;

    public constructor(statementName: string, namespace: string, name: string) {
        super();
        this.statementName = statementName;
        this.namespace = namespace;
        this.name = name;
    }

    public getStatementName(): string {
        return this.statementName;
    }
    public setStatementName(statementName: string): Statement {
        this.statementName = statementName;
        return this;
    }
    public getNamespace(): string {
        return this.namespace;
    }
    public setNamespace(namespace: string): Statement {
        this.namespace = namespace;
        return this;
    }
    public getName(): string {
        return this.name;
    }
    public setName(name: string): Statement {
        this.name = name;
        return this;
    }
    public getParameterMap(): Map<string, Map<string, ParameterReference>> {
        if (!this.parameterMap) {
            this.parameterMap = new Map();
        }
        return this.parameterMap;
    }
    public setParameterMap(parameterMap: Map<string, Map<string, ParameterReference>>): Statement {
        this.parameterMap = parameterMap;
        return this;
    }
    public getDependentStatements(): Map<string, boolean> {
        return this.dependentStatements ?? new Map();
    }
    public setDependentStatements(dependentStatements: Map<string, boolean>): Statement {
        this.dependentStatements = dependentStatements;
        return this;
    }

    public equals(obj: any): boolean {
        if (!(obj instanceof Statement)) return false;
        let s: Statement = obj as Statement;
        return s.statementName == this.statementName;
    }

    public static ofEntry(statement: Statement): [string, Statement] {
        return [statement.statementName, statement];
    }

    public static from(json: any): Statement {
        return new Statement(json.statementName, json.namespace, json.name)
            .setParameterMap(
                new Map<string, Map<string, ParameterReference>>(
                    Object.entries(json.parameterMap ?? {}).map(([k, v]: [string, any]) => [
                        k,
                        new Map<string, ParameterReference>(
                            Object.entries(v ?? {})
                                .map(([_, iv]) => ParameterReference.from(iv))
                                .map((e) => [e.getKey(), e]),
                        ),
                    ]),
                ),
            )
            .setDependentStatements(
                new Map<string, boolean>(Object.entries(json.dependentStatements ?? {})),
            )
            .setPosition(Position.from(json.position))
            .setComment(json.comment)
            .setDescription(json.description) as Statement;
    }
}
