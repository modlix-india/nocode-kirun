import { SourceLocation } from '../../lexer/DSLToken';
import { ASTNode } from './ASTNode';
import { ComplexValueNode } from './ComplexValueNode';
import { ExpressionNode } from './ExpressionNode';
import { SchemaLiteralNode } from './SchemaLiteralNode';

/**
 * Argument node - represents a function argument
 * Can be an expression, complex value (object/array), or schema literal
 */
export class ArgumentNode extends ASTNode {
    constructor(
        public key: string,
        public value: ExpressionNode | ComplexValueNode | SchemaLiteralNode,
        location: SourceLocation,
    ) {
        super('Argument', location);
    }

    public toJSON(): any {
        return {
            type: this.type,
            key: this.key,
            value: this.value.toJSON(),
        };
    }
}
