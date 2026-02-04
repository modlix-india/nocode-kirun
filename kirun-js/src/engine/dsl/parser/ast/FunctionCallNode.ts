import { SourceLocation } from '../../lexer/DSLToken';
import { ArgumentNode } from './ArgumentNode';
import { ASTNode } from './ASTNode';

/**
 * Function call node
 * Example: System.Array.InsertLast(source = Context.a, element = 10)
 */
export class FunctionCallNode extends ASTNode {
    constructor(
        public namespace: string,
        public name: string,
        public argumentsMap: Map<string, ArgumentNode>,
        location: SourceLocation,
    ) {
        super('FunctionCall', location);
    }

    public toJSON(): any {
        return {
            type: this.type,
            namespace: this.namespace,
            name: this.name,
            arguments: Object.fromEntries(
                Array.from(this.argumentsMap.entries()).map(([key, arg]) => [key, arg.toJSON()]),
            ),
        };
    }
}
