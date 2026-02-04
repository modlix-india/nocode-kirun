import { SourceLocation } from '../../lexer/DSLToken';
import { ASTNode } from './ASTNode';
import { EventDeclNode } from './EventDeclNode';
import { ParameterDeclNode } from './ParameterDeclNode';
import { StatementNode } from './StatementNode';

/**
 * Function definition node - root AST node
 * Represents the entire function definition
 */
export class FunctionDefNode extends ASTNode {
    constructor(
        public name: string,
        public namespace?: string,
        public parameters: ParameterDeclNode[] = [],
        public events: EventDeclNode[] = [],
        public logic: StatementNode[] = [],
        location?: SourceLocation,
    ) {
        super(
            'FunctionDefinition',
            location ||
                new SourceLocation(1, 1, 0, 0), // Default location if not provided
        );
    }

    public toJSON(): any {
        return {
            type: this.type,
            name: this.name,
            namespace: this.namespace,
            parameters: this.parameters.map((p) => p.toJSON()),
            events: this.events.map((e) => e.toJSON()),
            logic: this.logic.map((s) => s.toJSON()),
        };
    }
}
