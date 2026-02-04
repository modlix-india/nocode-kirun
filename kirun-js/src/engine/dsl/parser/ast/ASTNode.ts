import { SourceLocation } from '../../lexer/DSLToken';

/**
 * Base class for all AST nodes
 */
export abstract class ASTNode {
    constructor(
        public readonly type: string,
        public readonly location: SourceLocation,
    ) {}

    /**
     * Convert AST node to JSON (for debugging/inspection)
     */
    abstract toJSON(): any;

    /**
     * Pretty-print the AST node
     */
    public toString(): string {
        return JSON.stringify(this.toJSON(), null, 2);
    }
}
