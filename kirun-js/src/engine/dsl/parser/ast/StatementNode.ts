import { SourceLocation } from '../../lexer/DSLToken';
import { ASTNode } from './ASTNode';
import { FunctionCallNode } from './FunctionCallNode';

/**
 * Statement node - represents a single statement in the LOGIC block
 * Example:
 *   create: System.Context.Create(name = "a") AFTER Steps.prev
 *       iteration
 *           if: System.If(condition = true)
 */
export class StatementNode extends ASTNode {
    constructor(
        public statementName: string,
        public functionCall: FunctionCallNode,
        public afterSteps: string[] = [],
        public executeIfSteps: string[] = [],
        public nestedBlocks: Map<string, StatementNode[]> = new Map(),
        location: SourceLocation,
        public comment: string = '',
    ) {
        super('Statement', location);
    }

    public toJSON(): any {
        return {
            type: this.type,
            statementName: this.statementName,
            functionCall: this.functionCall.toJSON(),
            afterSteps: this.afterSteps,
            executeIfSteps: this.executeIfSteps,
            nestedBlocks: Object.fromEntries(
                Array.from(this.nestedBlocks.entries()).map(([blockName, statements]) => [
                    blockName,
                    statements.map((s) => s.toJSON()),
                ]),
            ),
            comment: this.comment,
        };
    }
}
