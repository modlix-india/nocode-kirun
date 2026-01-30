import { Schema } from '../json/schema/Schema';
import { SchemaType } from '../json/schema/type/SchemaType';
import { TypeUtil } from '../json/schema/type/TypeUtil';
import { Namespaces } from '../namespaces/Namespaces';
import { AbstractStatement } from './AbstractStatement';
import { Position } from './Position';

export class StatementGroup extends AbstractStatement {
    private static readonly SCHEMA_NAME: string = 'StatementGroup';
    public static readonly SCHEMA: Schema = new Schema()
        .setNamespace(Namespaces.SYSTEM)
        .setName(StatementGroup.SCHEMA_NAME)
        .setType(TypeUtil.of(SchemaType.OBJECT))
        .setProperties(
            new Map([
                ['statementGroupName', Schema.ofString('statementGroupName')],
                ['comment', Schema.ofString('comment')],
                ['description', Schema.ofString('description')],
                ['position', Position.SCHEMA],
            ]),
        );

    private statementGroupName: string;
    private statements: Map<string, boolean>;

    constructor(statementGroupName: string, statements: Map<string, boolean> = new Map()) {
        super();
        this.statementGroupName = statementGroupName;
        this.statements = statements;
    }

    public getStatementGroupName(): string {
        return this.statementGroupName;
    }
    public setStatementGroupName(statementGroupName: string): StatementGroup {
        this.statementGroupName = statementGroupName;
        return this;
    }

    public getStatements(): Map<string, boolean> {
        return this.statements;
    }

    public setStatements(statements: Map<string, boolean>): StatementGroup {
        this.statements = statements;
        return this;
    }

    public static from(json: any): StatementGroup {
        return new StatementGroup(
            json.statementGroupName,
            new Map(
                Object.entries(json.statements || {}).map(([k, v]) => [
                    k,
                    ('' + v)?.toLowerCase() == 'true',
                ]),
            ),
        )
            .setPosition(Position.from(json.position))
            .setComment(json.comment)
            .setDescription(json.description)
            .setOverride(json.override ?? false) as StatementGroup;
    }

    public toJSON(): any {
        return {
            statementGroupName: this.statementGroupName,
            statements: Object.fromEntries(this.statements),
            position: this.getPosition(),
            comment: this.getComment(),
            description: this.getDescription(),
            override: this.isOverride(),
        };
    }
}
