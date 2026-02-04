import { SourceLocation } from '../../lexer/DSLToken';
import { ASTNode } from './ASTNode';

/**
 * Complex value node - represents literal values
 * Used for parameter values that are objects, arrays, or primitives (strings, numbers, booleans, null)
 */
export class ComplexValueNode extends ASTNode {
    constructor(
        public value: any,
        location: SourceLocation,
    ) {
        super('ComplexValue', location);
    }

    public toJSON(): any {
        return {
            type: this.type,
            value: this.value,
        };
    }
}
