import { ContextElement } from './ContextElement';
import { TokenValueExtractor } from './expression/tokenextractor/TokenValueExtractor';
import { StatementExecution } from './StatementExecution';
import { ArgumentsTokenValueExtractor } from './tokenextractor/ArgumentsTokenValueExtractor';
import { ContextTokenValueExtractor } from './tokenextractor/ContextTokenValueExtractor';
import { OutputMapTokenValueExtractor } from './tokenextractor/OutputMapTokenValueExtractor';

export class FunctionExecutionParameters {
    private context?: Map<string, ContextElement>;
    private args?: Map<string, any>;
    private events?: Map<string, Map<string, any>[]>;
    private statementExecution?: StatementExecution;
    private steps?: Map<string, Map<string, Map<string, any>>>;
    private count: number = 0;

    private valueExtractors: Map<string, TokenValueExtractor> = new Map();

    public getContext(): Map<string, ContextElement> | undefined {
        return this.context;
    }
    public setContext(context: Map<string, ContextElement>): FunctionExecutionParameters {
        this.context = context;
        let x: TokenValueExtractor = new ContextTokenValueExtractor(context);
        this.valueExtractors.set(x.getPrefix(), x);
        return this;
    }
    public getArguments(): Map<string, any> | undefined {
        return this.args;
    }
    public setArguments(args: Map<string, any>): FunctionExecutionParameters {
        this.args = args;
        let x: TokenValueExtractor = new ArgumentsTokenValueExtractor(args);
        this.valueExtractors.set(x.getPrefix(), x);
        return this;
    }
    public getEvents(): Map<string, Map<string, any>[]> | undefined {
        return this.events;
    }
    public setEvents(events: Map<string, Map<string, any>[]>): FunctionExecutionParameters {
        this.events = events;
        return this;
    }
    public getStatementExecution(): StatementExecution | undefined {
        return this.statementExecution;
    }
    public setStatementExecution(
        statementExecution: StatementExecution,
    ): FunctionExecutionParameters {
        this.statementExecution = statementExecution;
        return this;
    }
    public getSteps(): Map<string, Map<string, Map<string, any>>> | undefined {
        return this.steps;
    }
    public setSteps(
        steps: Map<string, Map<string, Map<string, any>>>,
    ): FunctionExecutionParameters {
        this.steps = steps;
        let x: TokenValueExtractor = new OutputMapTokenValueExtractor(steps);
        this.valueExtractors.set(x.getPrefix(), x);
        return this;
    }
    public getCount(): number {
        return this.count;
    }
    public setCount(count: number): FunctionExecutionParameters {
        this.count = count;
        return this;
    }

    public getValuesMap(): Map<string, TokenValueExtractor> {
        return this.valueExtractors;
    }
}
