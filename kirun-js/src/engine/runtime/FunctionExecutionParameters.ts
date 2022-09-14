import { Function } from '../function/Function';
import { Schema } from '../json/schema/Schema';
import { Repository } from '../Repository';
import UUID from '../util/UUID';
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
    private functionRepository: Repository<Function>;
    private schemaRepository: Repository<Schema>;
    private executionId: string;

    private valueExtractors: Map<string, TokenValueExtractor> = new Map();

    public constructor(
        functionRepository: Repository<Function>,
        schemaRepository: Repository<Schema>,
        executionId?: string,
    ) {
        this.functionRepository = functionRepository;
        this.schemaRepository = schemaRepository;
        this.executionId = executionId ?? UUID();
    }

    public getExecutionId(): string {
        return this.executionId;
    }

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

    public getFunctionRepository(): Repository<Function> {
        return this.functionRepository;
    }
    public setFunctionRepository(
        functionRepository: Repository<Function>,
    ): FunctionExecutionParameters {
        this.functionRepository = functionRepository;
        return this;
    }
    public getSchemaRepository(): Repository<Schema> {
        return this.schemaRepository;
    }
    public setSchemaRepository(schemaRepository: Repository<Schema>): FunctionExecutionParameters {
        this.schemaRepository = schemaRepository;
        return this;
    }

    public addTokenValueExtractor(
        ...extractors: TokenValueExtractor[]
    ): FunctionExecutionParameters {
        for (const tve of extractors) this.valueExtractors.set(tve.getPrefix(), tve);
        return this;
    }

    public setValuesMap(valuesMap: Map<string, TokenValueExtractor>): FunctionExecutionParameters {
        for (const [k, v] of valuesMap.entries()) this.valueExtractors.set(k, v);

        return this;
    }
}
