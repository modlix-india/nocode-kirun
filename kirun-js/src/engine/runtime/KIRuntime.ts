import { KIRuntimeException } from '../exception/KIRuntimeException';
import { AbstractFunction } from '../function/AbstractFunction';
import { Function } from '../function/Function';
import { Repository } from '../Repository';
import { JsonExpression } from '../json/JsonExpression';
import { Schema } from '../json/schema/Schema';
import { SchemaUtil } from '../json/schema/SchemaUtil';
import { Event } from '../model/Event';
import { EventResult } from '../model/EventResult';
import { FunctionDefinition } from '../model/FunctionDefinition';
import { FunctionOutput } from '../model/FunctionOutput';
import { FunctionSignature } from '../model/FunctionSignature';
import { Parameter } from '../model/Parameter';
import { ParameterReference } from '../model/ParameterReference';
import { ParameterReferenceType } from '../model/ParameterReferenceType';
import { Statement } from '../model/Statement';
import { LinkedList } from '../util/LinkedList';
import { StringFormatter } from '../util/string/StringFormatter';
import { StringUtil } from '../util/string/StringUtil';
import { Tuple4, Tuple2 } from '../util/Tuples';
import { ContextElement } from './ContextElement';
import { ExpressionEvaluator } from './expression/ExpressionEvaluator';
import { FunctionExecutionParameters } from './FunctionExecutionParameters';
import { ExecutionGraph } from './graph/ExecutionGraph';
import { GraphVertex } from './graph/GraphVertex';
import { StatementExecution } from './StatementExecution';
import { StatementMessageType } from './StatementMessageType';
import { isNullValue } from '../util/NullCheck';
import { SchemaType } from '../json/schema/type/SchemaType';
import { ArraySchemaType } from '../json/schema/array/ArraySchemaType';
import { ArgumentsTokenValueExtractor } from './tokenextractor/ArgumentsTokenValueExtractor';
import { OutputMapTokenValueExtractor } from './tokenextractor/OutputMapTokenValueExtractor';
import { ContextTokenValueExtractor } from './tokenextractor/ContextTokenValueExtractor';

export class KIRuntime extends AbstractFunction {
    private static readonly PARAMETER_NEEDS_A_VALUE: string = 'Parameter "$" needs a value';

    private static readonly STEP_REGEX_PATTERN: RegExp = new RegExp(
        'Steps\\.([a-zA-Z0-9\\\\-]{1,})\\.([a-zA-Z0-9\\\\-]{1,})',
        'g',
    );

    private static readonly VERSION: number = 1;

    private static readonly MAX_EXECUTION_ITERATIONS: number = 10000000;

    private fd: FunctionDefinition;

    private debugMode: boolean = false;

    public constructor(fd: FunctionDefinition, debugMode: boolean = false) {
        super();
        this.debugMode = debugMode;
        this.fd = fd;
        if (this.fd.getVersion() > KIRuntime.VERSION) {
            throw new KIRuntimeException(
                'Runtime is at a lower version ' +
                    KIRuntime.VERSION +
                    ' and trying to run code from version ' +
                    this.fd.getVersion() +
                    '.',
            );
        }
    }

    public getSignature(): FunctionSignature {
        return this.fd;
    }

    public async getExecutionPlan(
        fRepo: Repository<Function>,
        sRepo: Repository<Schema>,
    ): Promise<ExecutionGraph<string, StatementExecution>> {
        let g: ExecutionGraph<string, StatementExecution> = new ExecutionGraph();
        for (let s of Array.from(this.fd.getSteps().values()))
            g.addVertex(await this.prepareStatementExecution(s, fRepo, sRepo));

        let unresolved = this.makeEdges(g);

        Array.from(unresolved.getT2().entries()).forEach((e) => {
            let ex = g.getNodeMap().get(e[0])?.getData();
            if (!ex) return;
            ex.addMessage(StatementMessageType.ERROR, e[1]);
        });

        return g;
    }

    protected async internalExecute(
        inContext: FunctionExecutionParameters,
    ): Promise<FunctionOutput> {
        if (!inContext.getContext()) inContext.setContext(new Map());

        if (!inContext.getEvents()) inContext.setEvents(new Map());

        if (!inContext.getSteps()) inContext.setSteps(new Map());

        if (inContext.getArguments()) {
            inContext.addTokenValueExtractor(
                new ArgumentsTokenValueExtractor(inContext.getArguments()!),
            );
        }

        if (this.debugMode) {
            console.log(
                `EID: ${inContext.getExecutionId()} Executing: ${this.fd.getNamespace()}.${this.fd.getName()}`,
            );
            console.log(`EID: ${inContext.getExecutionId()} Parameters: `, inContext);
        }

        let eGraph: ExecutionGraph<string, StatementExecution> = await this.getExecutionPlan(
            inContext.getFunctionRepository(),
            inContext.getSchemaRepository(),
        );

        if (this.debugMode) {
            console.log(`EID: ${inContext.getExecutionId()} ${eGraph?.toString()}`);
        }

        // if (logger.isDebugEnabled()) {
        // 	logger.debug(StringFormatter.format("Executing : $.$", this.fd.getNamespace(), this.fd.getName()));
        // 	logger.debug(eGraph.toString());
        // }

        let messages: string[] = eGraph
            .getVerticesData()
            .filter((e) => e.getMessages().length)
            .map((e) => e.getStatement().getStatementName() + ': \n' + e.getMessages().join(','));

        if (messages?.length) {
            throw new KIRuntimeException(
                'Please fix the errors in the function definition before execution : \n' +
                    messages.join(',\n'),
            );
        }

        return await this.executeGraph(eGraph, inContext);
    }

    private async executeGraph(
        eGraph: ExecutionGraph<string, StatementExecution>,
        inContext: FunctionExecutionParameters,
    ): Promise<FunctionOutput> {
        let executionQue: LinkedList<GraphVertex<string, StatementExecution>> = new LinkedList();
        executionQue.addAll(eGraph.getVerticesWithNoIncomingEdges());

        let branchQue: LinkedList<
            Tuple4<
                ExecutionGraph<string, StatementExecution>,
                Tuple2<string, string>[],
                FunctionOutput,
                GraphVertex<string, StatementExecution>
            >
        > = new LinkedList();

        while (
            (!executionQue.isEmpty() || !branchQue.isEmpty()) &&
            !inContext.getEvents()?.has(Event.OUTPUT)
        ) {
            await this.processBranchQue(inContext, executionQue, branchQue);
            await this.processExecutionQue(inContext, executionQue, branchQue);

            inContext.setCount(inContext.getCount() + 1);

            if (inContext.getCount() == KIRuntime.MAX_EXECUTION_ITERATIONS)
                throw new KIRuntimeException('Execution locked in an infinite loop');
        }

        if (!eGraph.isSubGraph() && !inContext.getEvents()?.size) {
            const eventMap = this.getSignature().getEvents();
            if (eventMap.size && eventMap.get(Event.OUTPUT)?.getParameters()?.size)
                throw new KIRuntimeException('No events raised');
        }

        const er: EventResult[] = Array.from(inContext.getEvents()?.entries() ?? []).flatMap((e) =>
            e[1].map((v) => EventResult.of(e[0], v)),
        );

        if (er.length || eGraph.isSubGraph()) return new FunctionOutput(er);
        else return new FunctionOutput([EventResult.of(Event.OUTPUT, new Map<string, any>())]);
    }

    private async processExecutionQue(
        inContext: FunctionExecutionParameters,
        executionQue: LinkedList<GraphVertex<string, StatementExecution>>,
        branchQue: LinkedList<
            Tuple4<
                ExecutionGraph<string, StatementExecution>,
                Tuple2<string, string>[],
                FunctionOutput,
                GraphVertex<string, StatementExecution>
            >
        >,
    ) {
        if (!executionQue.isEmpty()) {
            let vertex: GraphVertex<string, StatementExecution> = executionQue.pop();

            if (!(await this.allDependenciesResolvedVertex(vertex, inContext.getSteps()!)))
                executionQue.add(vertex);
            else
                await this.executeVertex(
                    vertex,
                    inContext,
                    branchQue,
                    executionQue,
                    inContext.getFunctionRepository(),
                );
        }
    }

    private async processBranchQue(
        inContext: FunctionExecutionParameters,
        executionQue: LinkedList<GraphVertex<string, StatementExecution>>,
        branchQue: LinkedList<
            Tuple4<
                ExecutionGraph<string, StatementExecution>,
                Tuple2<string, string>[],
                FunctionOutput,
                GraphVertex<string, StatementExecution>
            >
        >,
    ) {
        if (branchQue.length) {
            let branch: Tuple4<
                ExecutionGraph<string, StatementExecution>,
                Tuple2<string, string>[],
                FunctionOutput,
                GraphVertex<string, StatementExecution>
            > = branchQue.pop();

            if (!(await this.allDependenciesResolvedTuples(branch.getT2(), inContext.getSteps()!)))
                branchQue.add(branch);
            else await this.executeBranch(inContext, executionQue, branch);
        }
    }

    private async executeBranch(
        inContext: FunctionExecutionParameters,
        executionQue: LinkedList<GraphVertex<string, StatementExecution>>,
        branch: Tuple4<
            ExecutionGraph<string, StatementExecution>,
            Tuple2<string, string>[],
            FunctionOutput,
            GraphVertex<string, StatementExecution>
        >,
    ) {
        let vertex: GraphVertex<string, StatementExecution> = branch.getT4();
        let nextOutput: EventResult | undefined = undefined;

        do {
            branch
                .getT1()
                .getVerticesData()
                .map((e) => e.getStatement().getStatementName())
                .forEach((e) => inContext.getSteps()?.delete(e));

            await this.executeGraph(branch.getT1(), inContext);
            nextOutput = branch.getT3().next();

            if (nextOutput) {
                if (!inContext.getSteps()?.has(vertex.getData().getStatement().getStatementName()))
                    inContext
                        .getSteps()
                        ?.set(vertex.getData().getStatement().getStatementName(), new Map());

                inContext
                    .getSteps()
                    ?.get(vertex.getData().getStatement().getStatementName())
                    ?.set(
                        nextOutput.getName(),
                        this.resolveInternalExpressions(nextOutput.getResult(), inContext),
                    );

                if (this.debugMode) {
                    const s = vertex.getData().getStatement();
                    console.log(
                        `EID: ${inContext.getExecutionId()} Step : ${s.getStatementName()} => ${s.getNamespace()}.${s.getName()}`,
                    );
                    console.log(
                        `EID: ${inContext.getExecutionId()} Event : ${nextOutput.getName()} : `,
                        inContext.getSteps()!.get(s.getStatementName())!.get(nextOutput.getName()),
                    );
                }
            }
        } while (nextOutput && nextOutput.getName() != Event.OUTPUT);

        if (nextOutput?.getName() == Event.OUTPUT && vertex.getOutVertices().has(Event.OUTPUT)) {
            (vertex?.getOutVertices()?.get(Event.OUTPUT) ?? []).forEach(async (e) => {
                if (await this.allDependenciesResolvedVertex(e, inContext.getSteps()!))
                    executionQue.add(e);
            });
        }
    }

    private async executeVertex(
        vertex: GraphVertex<string, StatementExecution>,
        inContext: FunctionExecutionParameters,
        branchQue: LinkedList<
            Tuple4<
                ExecutionGraph<string, StatementExecution>,
                Tuple2<string, string>[],
                FunctionOutput,
                GraphVertex<string, StatementExecution>
            >
        >,
        executionQue: LinkedList<GraphVertex<string, StatementExecution>>,
        fRepo: Repository<Function>,
    ) {
        let s: Statement = vertex.getData().getStatement();

        if (s.getExecuteIftrue().size) {
            const allTrue = (Array.from(s.getExecuteIftrue().entries()) ?? [])
                .filter((e) => e[1])
                .map(([e]) => {
                    const v = new ExpressionEvaluator(e).evaluate(inContext.getValuesMap());
                    return v;
                })
                .every((e) => !isNullValue(e) && e !== false);

            if (!allTrue) return;
        }

        let fun: Function | undefined = await fRepo.find(s.getNamespace(), s.getName());

        if (!fun) {
            throw new KIRuntimeException(
                StringFormatter.format('$.$ function is not found.', s.getNamespace(), s.getName()),
            );
        }

        let paramSet: Map<string, Parameter> | undefined = fun?.getSignature().getParameters();

        let args: Map<string, any> = this.getArgumentsFromParametersMap(
            inContext,
            s,
            paramSet ?? new Map(),
        );

        if (this.debugMode) {
            console.log(
                `EID: ${inContext.getExecutionId()} Step : ${s.getStatementName()} => ${s.getNamespace()}.${s.getName()}`,
            );
            console.log(`EID: ${inContext.getExecutionId()} Arguments : `, args);
        }

        let context: Map<string, ContextElement> = inContext.getContext()!;
        let fep: FunctionExecutionParameters;

        if (fun instanceof KIRuntime) {
            fep = new FunctionExecutionParameters(
                inContext.getFunctionRepository(),
                inContext.getSchemaRepository(),
                `${inContext.getExecutionId()}_${s.getStatementName()}`,
            )
                .setArguments(args)
                .setValuesMap(
                    new Map(
                        Array.from(inContext.getValuesMap().values())
                            .filter(
                                (e) =>
                                    e.getPrefix() !== ArgumentsTokenValueExtractor.PREFIX &&
                                    e.getPrefix() !== OutputMapTokenValueExtractor.PREFIX &&
                                    e.getPrefix() !== ContextTokenValueExtractor.PREFIX,
                            )
                            .map((e) => [e.getPrefix(), e]),
                    ),
                );
        } else {
            fep = new FunctionExecutionParameters(
                inContext.getFunctionRepository(),
                inContext.getSchemaRepository(),
                inContext.getExecutionId(),
            )
                .setValuesMap(inContext.getValuesMap())
                .setContext(context)
                .setArguments(args)
                .setEvents(inContext.getEvents()!)
                .setSteps(inContext.getSteps()!)
                .setStatementExecution(vertex.getData())
                .setCount(inContext.getCount())
                .setExecutionContext(inContext.getExecutionContext());
        }

        let result: FunctionOutput = await fun.execute(fep);

        let er: EventResult | undefined = result.next();

        if (!er)
            throw new KIRuntimeException(
                StringFormatter.format('Executing $ returned no events', s.getStatementName()),
            );

        let isOutput: boolean = er.getName() == Event.OUTPUT;

        if (!inContext.getSteps()?.has(s.getStatementName())) {
            inContext.getSteps()!.set(s.getStatementName(), new Map());
        }

        inContext
            .getSteps()!
            .get(s.getStatementName())!
            .set(er.getName(), this.resolveInternalExpressions(er.getResult(), inContext));

        if (this.debugMode) {
            console.log(
                `EID: ${inContext.getExecutionId()} Step : ${s.getStatementName()} => ${s.getNamespace()}.${s.getName()}`,
            );
            console.log(
                `EID: ${inContext.getExecutionId()} Event : ${er.getName()} : `,
                inContext.getSteps()!.get(s.getStatementName())!.get(er.getName()),
            );
        }

        if (!isOutput) {
            let subGraph = vertex.getSubGraphOfType(er.getName());
            let unResolvedDependencies: Tuple2<string, string>[] = this.makeEdges(subGraph).getT1();
            branchQue.push(new Tuple4(subGraph, unResolvedDependencies, result, vertex));
        } else {
            let out: Set<GraphVertex<string, StatementExecution>> | undefined = vertex
                .getOutVertices()
                .get(Event.OUTPUT);
            if (out)
                out.forEach(async (e) => {
                    if (await this.allDependenciesResolvedVertex(e, inContext.getSteps()!))
                        executionQue.add(e);
                });
        }
    }

    private resolveInternalExpressions(
        result: Map<string, any>,
        inContext: FunctionExecutionParameters,
    ): Map<string, any> {
        if (!result) return result;

        return Array.from(result.entries())
            .map((e) => new Tuple2(e[0], this.resolveInternalExpression(e[1], inContext)))
            .reduce((a, c) => {
                a.set(c.getT1(), c.getT2());
                return a;
            }, new Map());
    }

    private resolveInternalExpression(value: any, inContext: FunctionExecutionParameters): any {
        if (isNullValue(value) || typeof value != 'object') return value;

        if (value instanceof JsonExpression) {
            let exp: ExpressionEvaluator = new ExpressionEvaluator(
                (value as JsonExpression).getExpression(),
            );
            return exp.evaluate(inContext.getValuesMap());
        }

        if (Array.isArray(value)) {
            let retArray: any[] = [];

            for (let obj of value) {
                retArray.push(this.resolveInternalExpression(obj, inContext));
            }

            return retArray;
        }

        if (typeof value === 'object') {
            let retObject: any = {};

            for (let entry of Object.entries(value)) {
                retObject[entry[0]] = this.resolveInternalExpression(entry[1], inContext);
            }

            return retObject;
        }

        return undefined;
    }

    private allDependenciesResolvedTuples(
        unResolvedDependencies: Tuple2<string, string>[],
        output: Map<string, Map<string, Map<string, any>>>,
    ): boolean {
        for (let tup of unResolvedDependencies) {
            if (!output.has(tup.getT1())) return false;
            if (!output.get(tup.getT1())?.get(tup.getT2())) return false;
        }

        return true;
    }

    private allDependenciesResolvedVertex(
        vertex: GraphVertex<string, StatementExecution>,
        output: Map<string, Map<string, Map<string, any>>>,
    ): boolean {
        if (!vertex.getInVertices().size) return true;

        return (
            Array.from(vertex.getInVertices()).filter((e) => {
                let stepName: string = e.getT1().getData().getStatement().getStatementName();
                let type: string = e.getT2();

                return !(output.has(stepName) && output.get(stepName)?.has(type));
            }).length == 0
        );
    }

    private getArgumentsFromParametersMap(
        inContext: FunctionExecutionParameters,
        s: Statement,
        paramSet: Map<string, Parameter>,
    ): Map<string, any> {
        return Array.from(s.getParameterMap().entries())
            .map((e) => {
                let prList: ParameterReference[] = Array.from(e[1]?.values() ?? []);

                let ret: any = undefined;

                if (!prList?.length) return new Tuple2(e[0], ret);

                let pDef: Parameter | undefined = paramSet.get(e[0]);

                if (!pDef) return new Tuple2(e[0], undefined);

                if (pDef.isVariableArgument()) {
                    ret = prList
                        .sort((a, b) => (a.getOrder() ?? 0) - (b.getOrder() ?? 0))
                        .filter((r) => !isNullValue(r))
                        .map((r) => this.parameterReferenceEvaluation(inContext, r))
                        .flatMap((r) => (Array.isArray(r) ? r : [r]));
                } else {
                    ret = this.parameterReferenceEvaluation(inContext, prList[0]);
                }

                return new Tuple2(e[0], ret);
            })
            .filter((e) => !isNullValue(e.getT2()))
            .reduce((a, c) => {
                a.set(c.getT1(), c.getT2());
                return a;
            }, new Map());
    }

    private parameterReferenceEvaluation(
        inContext: FunctionExecutionParameters,
        ref: ParameterReference,
    ): any {
        let ret: any = undefined;

        if (ref.getType() == ParameterReferenceType.VALUE) {
            ret = this.resolveInternalExpression(ref.getValue(), inContext);
        } else if (
            ref.getType() == ParameterReferenceType.EXPRESSION &&
            !StringUtil.isNullOrBlank(ref.getExpression())
        ) {
            let exp: ExpressionEvaluator = new ExpressionEvaluator(ref.getExpression() ?? '');
            ret = exp.evaluate(inContext.getValuesMap());
        }
        return ret;
    }

    private async prepareStatementExecution(
        s: Statement,
        fRepo: Repository<Function>,
        sRepo: Repository<Schema>,
    ): Promise<StatementExecution> {
        let se: StatementExecution = new StatementExecution(s);

        let fun: Function | undefined = await fRepo.find(s.getNamespace(), s.getName());

        if (!fun) {
            se.addMessage(
                StatementMessageType.ERROR,
                StringFormatter.format('$.$ is not available', s.getNamespace(), s.getName()),
            );
            return Promise.resolve(se);
        }

        let paramSet: Map<string, Parameter> = new Map(fun.getSignature().getParameters());
        if (!s.getParameterMap()) return Promise.resolve(se);
        for (let param of Array.from(s.getParameterMap().entries())) {
            let p: Parameter | undefined = paramSet.get(param[0]);
            if (!p) continue;

            let refList: ParameterReference[] = Array.from(param[1]?.values() ?? []);

            if (!refList.length && !p.isVariableArgument()) {
                if (!(await SchemaUtil.hasDefaultValueOrNullSchemaType(p.getSchema(), sRepo)))
                    se.addMessage(
                        StatementMessageType.ERROR,
                        StringFormatter.format(
                            KIRuntime.PARAMETER_NEEDS_A_VALUE,
                            p.getParameterName(),
                        ),
                    );
                paramSet.delete(p.getParameterName());
                continue;
            }

            if (p.isVariableArgument()) {
                refList.sort((a, b) => (a.getOrder() ?? 0) - (b.getOrder() ?? 0));
                for (let ref of refList) this.parameterReferenceValidation(se, p, ref, sRepo);
            } else if (refList.length) {
                let ref: ParameterReference = refList[0];
                this.parameterReferenceValidation(se, p, ref, sRepo);
            }

            paramSet.delete(p.getParameterName());
        }

        if (!isNullValue(se.getStatement().getDependentStatements())) {
            for (let statement of Array.from(se.getStatement().getDependentStatements().entries()))
                if (statement[1]) se.addDependency(statement[0]);
        }

        if (!isNullValue(se.getStatement().getExecuteIftrue())) {
            for (let statement of Array.from(se.getStatement().getExecuteIftrue().entries()))
                if (statement[1]) this.addDependencies(se, statement[0]);
        }

        if (paramSet.size) {
            for (let param of Array.from(paramSet.values())) {
                if (param.isVariableArgument()) continue;
                if (!(await SchemaUtil.hasDefaultValueOrNullSchemaType(param.getSchema(), sRepo)))
                    se.addMessage(
                        StatementMessageType.ERROR,
                        StringFormatter.format(
                            KIRuntime.PARAMETER_NEEDS_A_VALUE,
                            param.getParameterName(),
                        ),
                    );
            }
        }

        return Promise.resolve(se);
    }

    private async parameterReferenceValidation(
        se: StatementExecution,
        p: Parameter,
        ref: ParameterReference,
        sRepo: Repository<Schema>,
    ): Promise<void> {
        // Breaking this execution doesn't make sense.

        if (!ref) {
            if (isNullValue(await SchemaUtil.getDefaultValue(p.getSchema(), sRepo)))
                se.addMessage(
                    StatementMessageType.ERROR,
                    StringFormatter.format(KIRuntime.PARAMETER_NEEDS_A_VALUE, p.getParameterName()),
                );
        } else if (ref.getType() == ParameterReferenceType.VALUE) {
            if (
                isNullValue(ref.getValue()) &&
                !(await SchemaUtil.hasDefaultValueOrNullSchemaType(p.getSchema(), sRepo))
            )
                se.addMessage(
                    StatementMessageType.ERROR,
                    StringFormatter.format(KIRuntime.PARAMETER_NEEDS_A_VALUE, p.getParameterName()),
                );

            if (isNullValue(ref.getValue())) return;

            let paramElements: LinkedList<Tuple2<Schema, any>> = new LinkedList();
            paramElements.push(new Tuple2(p.getSchema(), ref.getValue()));

            while (!paramElements.isEmpty()) {
                let e: Tuple2<Schema, any> = paramElements.pop();

                if (e.getT2() instanceof JsonExpression) {
                    this.addDependencies(se, (e.getT2() as JsonExpression).getExpression());
                } else {
                    if (isNullValue(e.getT1()) || isNullValue(e.getT1().getType())) continue;

                    if (
                        e.getT1().getType()?.contains(SchemaType.ARRAY) &&
                        Array.isArray(e.getT2())
                    ) {
                        let ast: ArraySchemaType | undefined = e.getT1().getItems();
                        if (!ast) {
                            continue;
                        }
                        if (ast.isSingleType()) {
                            for (let je of e.getT2())
                                paramElements.push(new Tuple2(ast.getSingleSchema()!, je));
                        } else {
                            let array: any[] = e.getT2() as any[];
                            for (let i = 0; i < array.length; i++) {
                                paramElements.push(new Tuple2(ast.getTupleSchema()![i], array[i]));
                            }
                        }
                    } else if (
                        e.getT1().getType()?.contains(SchemaType.OBJECT) &&
                        typeof e.getT2() == 'object'
                    ) {
                        let sch: Schema = e.getT1();

                        if (
                            sch.getName() === Parameter.EXPRESSION.getName() &&
                            sch.getNamespace() === Parameter.EXPRESSION.getNamespace()
                        ) {
                            let obj = e.getT2();
                            let isExpression: boolean = obj['isExpression'];
                            if (isExpression) {
                                this.addDependencies(se, obj['value']);
                            }
                        } else {
                            if (sch.getProperties()) {
                                for (let entry of Object.entries(e.getT2())) {
                                    if (!sch.getProperties()!.has(entry[0])) continue;
                                    paramElements.push(
                                        new Tuple2(sch.getProperties()!.get(entry[0])!, entry[1]),
                                    );
                                }
                            }
                        }
                    }
                }
            }
        } else if (ref.getType() == ParameterReferenceType.EXPRESSION) {
            if (StringUtil.isNullOrBlank(ref.getExpression())) {
                if (isNullValue(SchemaUtil.getDefaultValue(p.getSchema(), sRepo)))
                    se.addMessage(
                        StatementMessageType.ERROR,
                        StringFormatter.format(
                            KIRuntime.PARAMETER_NEEDS_A_VALUE,
                            p.getParameterName(),
                        ),
                    );
            } else {
                try {
                    // TODO: Type check for the resulting expression has to be done here...
                    this.addDependencies(se, ref.getExpression());
                } catch (err) {
                    se.addMessage(
                        StatementMessageType.ERROR,
                        StringFormatter.format('Error evaluating $ : $', ref.getExpression(), err),
                    );
                }
            }
        }
    }

    private addDependencies(se: StatementExecution, expression: string | undefined): void {
        if (!expression) return;

        Array.from(expression.match(KIRuntime.STEP_REGEX_PATTERN) ?? []).forEach((e) =>
            se.addDependency(e),
        );
    }

    public makeEdges(
        graph: ExecutionGraph<string, StatementExecution>,
    ): Tuple2<Tuple2<string, string>[], Map<string, string>> {
        let values = graph.getNodeMap().values();

        let retValue: Tuple2<string, string>[] = [];
        let retMap: Map<string, string> = new Map();

        for (let e of Array.from(values)) {
            for (let d of Array.from(e.getData().getDependencies())) {
                let secondDot: number = d.indexOf('.', 6);
                let step: string = d.substring(6, secondDot);
                let eventDot: number = d.indexOf('.', secondDot + 1);
                let event: string =
                    eventDot == -1
                        ? d.substring(secondDot + 1)
                        : d.substring(secondDot + 1, eventDot);

                if (!graph.getNodeMap().has(step)) {
                    retValue.push(new Tuple2(step, event));
                    retMap.set(
                        e.getData().getStatement().getStatementName(),

                        StringFormatter.format('Unable to find the step with name $', step),
                    );
                } else e.addInEdgeTo(graph.getNodeMap().get(step)!, event);
            }
        }

        return new Tuple2(retValue, retMap);
    }
}