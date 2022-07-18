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
                    Schema.ofArray('dependentstatement', Schema.ofString('dependentstatement')),
                ],
                [
                    'parameterMap',
                    new Schema()
                        .setName('parameterMap')
                        .setAdditionalProperties(
                            new AdditionalPropertiesType().setSchemaValue(
                                Schema.ofArray('parameterReference', ParameterReference.SCHEMA),
                            ),
                        ),
                ],
                ['position', Position.SCHEMA],
            ]),
        );
    private statementName: string;
    private namespace: string;
    private name: string;
    private parameterMap: Map<string, ParameterReference[]>;
    private dependentStatements: string[];

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
    public getParameterMap(): Map<string, ParameterReference[]> {
        if (this.parameterMap == null) {
            this.parameterMap = new Map();
        }
        return this.parameterMap;
    }
    public setParameterMap(parameterMap: Map<string, ParameterReference[]>): Statement {
        this.parameterMap = parameterMap;
        return this;
    }
    public getDependentStatements(): string[] {
        return this.dependentStatements;
    }
    public setDependentStatements(dependentStatements: string[]): Statement {
        this.dependentStatements = dependentStatements;
        return this;
    }

    public equals(obj: any): boolean {
        if (!(obj instanceof Statement)) return false;
        let s: Statement = obj as Statement;
        return s.statementName == this.statementName;
    }
}
