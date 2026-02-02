import { SourceLocation } from '../../lexer/DSLToken';
import { ASTNode } from './ASTNode';
import { ParameterDeclNode } from './ParameterDeclNode';

/**
 * Event declaration node
 * Example:
 *   output
 *       result AS ARRAY OF INTEGER
 */
export class EventDeclNode extends ASTNode {
    constructor(
        public name: string,
        public parameters: ParameterDeclNode[] = [],
        location: SourceLocation,
    ) {
        super('EventDecl', location);
    }

    public toJSON(): any {
        return {
            type: this.type,
            name: this.name,
            parameters: this.parameters.map((p) => p.toJSON()),
        };
    }
}
