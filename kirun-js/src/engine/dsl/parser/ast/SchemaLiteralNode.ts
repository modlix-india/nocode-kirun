import { SourceLocation } from '../../lexer/DSLToken';
import { ASTNode } from './ASTNode';
import { ExpressionNode } from './ExpressionNode';
import { SchemaNode } from './SchemaNode';

/**
 * Schema literal node - represents inline schema definitions with default values
 * Example: (ARRAY OF INTEGER) WITH DEFAULT VALUE []
 */
export class SchemaLiteralNode extends ASTNode {
    constructor(
        public schema: SchemaNode,
        public defaultValue: ExpressionNode | undefined,
        location: SourceLocation,
    ) {
        super('SchemaLiteral', location);
    }

    public toJSON(): any {
        return {
            type: this.type,
            schema: this.schema.toJSON(),
            defaultValue: this.defaultValue?.toJSON(),
        };
    }
}
