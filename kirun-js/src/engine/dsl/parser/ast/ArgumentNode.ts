import { SourceLocation } from '../../lexer/DSLToken';
import { ASTNode } from './ASTNode';
import { ComplexValueNode } from './ComplexValueNode';
import { ExpressionNode } from './ExpressionNode';
import { SchemaLiteralNode } from './SchemaLiteralNode';

export type ArgumentValue = ExpressionNode | ComplexValueNode | SchemaLiteralNode;

/**
 * Argument node - represents a function argument
 * Can be a single value or multiple values (multi-value parameter)
 * Each value can be an expression, complex value (object/array), or schema literal
 */
export class ArgumentNode extends ASTNode {
    public values: ArgumentValue[];

    constructor(
        public key: string,
        value: ArgumentValue | ArgumentValue[],
        location: SourceLocation,
    ) {
        super('Argument', location);
        this.values = Array.isArray(value) ? value : [value];
    }

    /** Get the first (or only) value - for backwards compatibility */
    public get value(): ArgumentValue {
        return this.values[0];
    }

    /** Check if this is a multi-value parameter */
    public isMultiValue(): boolean {
        return this.values.length > 1;
    }

    public toJSON(): any {
        return {
            type: this.type,
            key: this.key,
            values: this.values.map((v) => v.toJSON()),
        };
    }
}
