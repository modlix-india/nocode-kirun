import { Statement } from '../model/Statement';
import { GraphVertexType } from './graph/GraphVertexType';
import { StatementMessage } from './StatementMessage';
import { StatementMessageType } from './StatementMessageType';

// @EqualsAndHashCode(exclude = { "messages", "dependencies" })
export class StatementExecution implements GraphVertexType<String> {
    private statement: Statement;
    private messages: StatementMessage[] = new Array();
    private dependencies: Set<string> = new Set();

    public constructor(statement: Statement) {
        this.statement = statement;
    }

    public getStatement(): Statement {
        return this.statement;
    }
    public setStatement(statement: Statement): StatementExecution {
        this.statement = statement;
        return this;
    }
    public getMessages(): StatementMessage[] {
        return this.messages;
    }
    public setMessages(messages: StatementMessage[]): StatementExecution {
        this.messages = messages;
        return this;
    }
    public getDependencies(): Set<string> {
        return this.dependencies;
    }
    public setDependencies(dependencies: Set<string>): StatementExecution {
        this.dependencies = dependencies;
        return this;
    }

    public getUniqueKey(): string {
        return this.statement.getStatementName();
    }

    public addMessage(type: StatementMessageType, message: string): void {
        this.messages.push(new StatementMessage(type, message));
    }

    public addDependency(dependency: string): void {
        this.dependencies.add(dependency);
    }

    public getDepenedencies(): Set<string> {
        return this.dependencies;
    }

    public equals(obj: Object): boolean {
        if (!(obj instanceof StatementExecution)) return false;

        let se: StatementExecution = obj as StatementExecution;

        return se.statement.equals(this.statement);
    }
}
