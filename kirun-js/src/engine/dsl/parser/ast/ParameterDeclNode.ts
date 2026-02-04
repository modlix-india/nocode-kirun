import { SourceLocation } from '../../lexer/DSLToken';
import { ASTNode } from './ASTNode';
import { SchemaNode } from './SchemaNode';

/**
 * Parameter declaration node
 * Example: n AS INTEGER
 */
export class ParameterDeclNode extends ASTNode {
    constructor(
        public name: string,
        public schema: SchemaNode,
        location: SourceLocation,
    ) {
        super('ParameterDecl', location);
    }

    public toJSON(): any {
        return {
            type: this.type,
            name: this.name,
            schema: this.schema.toJSON(),
        };
    }
}
