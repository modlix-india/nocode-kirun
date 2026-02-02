import { SourceLocation } from '../../lexer/DSLToken';
import { ASTNode } from './ASTNode';

/**
 * Complex value node - represents JSON objects or arrays
 * Used for parameter values that are objects or arrays
 */
export class ComplexValueNode extends ASTNode {
    constructor(
        public value: object | any[],
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
