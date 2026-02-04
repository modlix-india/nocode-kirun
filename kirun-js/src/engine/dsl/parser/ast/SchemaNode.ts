import { SourceLocation } from '../../lexer/DSLToken';
import { ASTNode } from './ASTNode';

/**
 * Schema node - represents a schema specification
 * Can be either a simple string (e.g., "INTEGER", "ARRAY OF STRING")
 * or a complex JSON Schema object
 */
export class SchemaNode extends ASTNode {
    constructor(
        public schemaSpec: string | object,
        location: SourceLocation,
    ) {
        super('Schema', location);
    }

    public toJSON(): any {
        return {
            type: this.type,
            schemaSpec: this.schemaSpec,
        };
    }
}
