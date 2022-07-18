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

    public getStatementGroupName(): string {
        return this.statementGroupName;
    }
    public setStatementGroupName(statementGroupName: string): StatementGroup {
        this.statementGroupName = statementGroupName;
        return this;
    }
}
