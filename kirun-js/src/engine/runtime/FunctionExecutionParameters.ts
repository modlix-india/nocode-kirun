import { ContextElement } from './ContextElement';
import { StatementExecution } from './StatementExecution';

export class FunctionExecutionParameters {
    private context: Map<string, ContextElement>;
    private args: Map<string, any>;
    private events: Map<string, Map<string, any>[]>;
    private statementExecution: StatementExecution;
    private output: Map<string, Map<string, Map<string, any>>>;
    private count: number = 0;
    public getContext(): Map<string, ContextElement> {
        return this.context;
    }
    public setContext(context: Map<string, ContextElement>): FunctionExecutionParameters {
        this.context = context;
        return this;
    }
    public getArguments(): Map<string, any> {
        return this.args;
    }
    public setArguments(args: Map<string, any>): FunctionExecutionParameters {
        this.args = args;
        return this;
    }
    public getEvents(): Map<string, Map<string, any>[]> {
        return this.events;
    }
    public setEvents(events: Map<string, Map<string, any>[]>): FunctionExecutionParameters {
        this.events = events;
        return this;
    }
    public getStatementExecution(): StatementExecution {
        return this.statementExecution;
    }
    public setStatementExecution(
        statementExecution: StatementExecution,
    ): FunctionExecutionParameters {
        this.statementExecution = statementExecution;
        return this;
    }
    public getOutput(): Map<string, Map<string, Map<string, any>>> {
        return this.output;
    }
    public setOutput(
        output: Map<string, Map<string, Map<string, any>>>,
    ): FunctionExecutionParameters {
        this.output = output;
        return this;
    }
    public getCount(): number {
        return this.count;
    }
    public setCount(count: number): FunctionExecutionParameters {
        this.count = count;
        return this;
    }
}
