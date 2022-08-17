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

export class KIRuntime extends AbstractFunction {
    private static readonly PARAMETER_NEEDS_A_VALUE: string = 'Parameter "$" needs a value';

    private static readonly STEP_REGEX_PATTERN: RegExp = new RegExp(
        'Steps\\.([a-zA-Z0-9\\\\-]{1,})\\.([a-zA-Z0-9\\\\-]{1,})',
        'g',
    );

    private static readonly VERSION: number = 1;

    private static readonly MAX_EXECUTION_ITERATIONS: number = 10000000;

    private fd: FunctionDefinition;

    private fRepo: Repository<Function>;

    private sRepo: Repository<Schema>;

    public constructor(
        fd: FunctionDefinition,
        functionRepository: Repository<Function>,
        schemaRepository: Repository<Schema>,
    ) {
        super();
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

        this.fRepo = functionRepository;
        this.sRepo = schemaRepository;
    }

    public getSignature(): FunctionSignature {
        return this.fd;
    }

    private getExecutionPlan(
        context: Map<string, ContextElement>,
    ): ExecutionGraph<string, StatementExecution> {
        let g: ExecutionGraph<string, StatementExecution> = new ExecutionGraph();
        for (let s of Array.from(this.fd.getSteps().values()))
            g.addVertex(this.prepareStatementExecution(context, s));

        let unresolvedList: Tuple2<string, string>[] = this.makeEdges(g);

        if (unresolvedList.length) {
            throw new KIRuntimeException(
                StringFormatter.format(
                    'Found these unresolved dependencies : $ ',
                    unresolvedList.map((e) =>
                        StringFormatter.format('Steps.$.$', e.getT1(), e.getT2()),
                    ),
                ),
            );
        }

        return g;
    }

    protected internalExecute(inContext: FunctionExecutionParameters): FunctionOutput {
        if (!inContext.getContext()) inContext.setContext(new Map());

        if (!inContext.getEvents()) inContext.setEvents(new Map());

        if (!inContext.getSteps()) inContext.setSteps(new Map());

        let eGraph: ExecutionGraph<string, StatementExecution> = this.getExecutionPlan(
            inContext.getContext(),
        );

        console.log('Execution Graph');
        console.log(eGraph.toString());
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

        return this.executeGraph(eGraph, inContext);
    }

    private executeGraph(
        eGraph: ExecutionGraph<string, StatementExecution>,
        inContext: FunctionExecutionParameters,
    ): FunctionOutput {
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
            !inContext.getEvents().has(Event.OUTPUT)
        ) {
            this.processBranchQue(inContext, executionQue, branchQue);
            this.processExecutionQue(inContext, executionQue, branchQue);

            inContext.setCount(inContext.getCount() + 1);

            if (inContext.getCount() == KIRuntime.MAX_EXECUTION_ITERATIONS)
                throw new KIRuntimeException('Execution locked in an infinite loop');
        }

        if (!eGraph.isSubGraph() && !inContext.getEvents()?.size) {
            throw new KIRuntimeException('No events raised');
        }

        return new FunctionOutput(
            Array.from(inContext.getEvents().entries()).flatMap((e) =>
                e[1].map((v) => EventResult.of(e[0], v)),
            ),
        );
    }

    private processExecutionQue(
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
    ): void {
        if (!executionQue.isEmpty()) {
            let vertex: GraphVertex<string, StatementExecution> = executionQue.pop();

            if (!this.allDependenciesResolvedVertex(vertex, inContext.getSteps()))
                executionQue.add(vertex);
            else this.executeVertex(vertex, inContext, branchQue, executionQue);
        }
    }

    private processBranchQue(
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
    ): void {
        if (branchQue.length) {
            let branch: Tuple4<
                ExecutionGraph<string, StatementExecution>,
                Tuple2<string, string>[],
                FunctionOutput,
                GraphVertex<string, StatementExecution>
            > = branchQue.pop();

            if (!this.allDependenciesResolvedTuples(branch.getT2(), inContext.getSteps()))
                branchQue.add(branch);
            else this.executeBranch(inContext, executionQue, branch);
        }
    }

    private executeBranch(
        inContext: FunctionExecutionParameters,
        executionQue: LinkedList<GraphVertex<string, StatementExecution>>,
        branch: Tuple4<
            ExecutionGraph<string, StatementExecution>,
            Tuple2<string, string>[],
            FunctionOutput,
            GraphVertex<string, StatementExecution>
        >,
    ): void {
        let vertex: GraphVertex<string, StatementExecution> = branch.getT4();
        let nextOutput: EventResult = undefined;

        do {
            this.executeGraph(branch.getT1(), inContext);
            nextOutput = branch.getT3().next();

            if (nextOutput) {
                if (!inContext.getSteps().has(vertex.getData().getStatement().getStatementName()))
                    inContext
                        .getSteps()
                        .set(vertex.getData().getStatement().getStatementName(), new Map());

                inContext
                    .getSteps()
                    .get(vertex.getData().getStatement().getStatementName())
                    .set(
                        nextOutput.getName(),
                        this.resolveInternalExpressions(nextOutput.getResult(), inContext),
                    );
            }
        } while (nextOutput && nextOutput.getName() != Event.OUTPUT);

        if (nextOutput?.getName() == Event.OUTPUT && vertex.getOutVertices().has(Event.OUTPUT)) {
            vertex
                .getOutVertices()
                .get(Event.OUTPUT)

                .forEach((e) => executionQue.add(e));
        }
    }

    private executeVertex(
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
    ): void {
        let s: Statement = vertex.getData().getStatement();

        let fun: Function = this.fRepo.find(s.getNamespace(), s.getName());

        let paramSet: Map<string, Parameter> = fun.getSignature().getParameters();

        let args: Map<string, any> = this.getArgumentsFromParametersMap(inContext, s, paramSet);

        let context: Map<string, ContextElement> = inContext.getContext();

        let result: FunctionOutput = fun.execute(
            new FunctionExecutionParameters()
                .setContext(context)
                .setArguments(args)
                .setEvents(inContext.getEvents())
                .setSteps(inContext.getSteps())
                .setStatementExecution(vertex.getData())
                .setCount(inContext.getCount()),
        );

        let er: EventResult = result.next();

        if (!er)
            throw new KIRuntimeException(
                StringFormatter.format('Executing $ returned no events', s.getStatementName()),
            );

        let isOutput: boolean = er.getName() == Event.OUTPUT;

        if (!inContext.getSteps().has(s.getStatementName())) {
            inContext.getSteps().set(s.getStatementName(), new Map());
        }
        inContext
            .getSteps()
            .get(s.getStatementName())
            .set(er.getName(), this.resolveInternalExpressions(er.getResult(), inContext));

        if (!isOutput) {
            let subGraph = vertex.getSubGraphOfType(er.getName());
            let unResolvedDependencies: Tuple2<string, string>[] = this.makeEdges(subGraph);
            branchQue.push(new Tuple4(subGraph, unResolvedDependencies, result, vertex));
        } else {
            let out: Set<GraphVertex<string, StatementExecution>> = vertex
                .getOutVertices()
                .get(Event.OUTPUT);
            if (out) out.forEach((e) => executionQue.add(e));
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
            if (!output.get(tup.getT1()).get(tup.getT2())) return false;
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

                return !(output.has(stepName) && output.get(stepName).has(type));
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
                let prList: ParameterReference[] = e[1];

                let ret: any = undefined;

                if (!prList?.length) return new Tuple2(e[0], ret);

                let pDef: Parameter = paramSet.get(e[0]);

                if (pDef.isVariableArgument()) {
                    ret = prList
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
            let exp: ExpressionEvaluator = new ExpressionEvaluator(ref.getExpression());
            ret = exp.evaluate(inContext.getValuesMap());
        }
        return ret;
    }

    private prepareStatementExecution(
        context: Map<string, ContextElement>,
        s: Statement,
    ): StatementExecution {
        let se: StatementExecution = new StatementExecution(s);

        let fun: Function = this.fRepo.find(s.getNamespace(), s.getName());

        let paramSet: Map<string, Parameter> = new Map(fun.getSignature().getParameters());

        for (let param of Array.from(s.getParameterMap().entries())) {
            let p: Parameter = paramSet.get(param[0]);

            let refList: ParameterReference[] = param[1];

            if (!refList.length) {
                if (isNullValue(SchemaUtil.getDefaultValue(p.getSchema(), this.sRepo)))
                    se.addMessage(
                        StatementMessageType.ERROR,
                        StringFormatter.format(
                            KIRuntime.PARAMETER_NEEDS_A_VALUE,
                            p.getParameterName(),
                        ),
                    );
                continue;
            }

            if (p.isVariableArgument()) {
                for (let ref of refList) this.parameterReferenceValidation(context, se, p, ref);
            } else {
                let ref: ParameterReference = refList[0];
                this.parameterReferenceValidation(context, se, p, ref);
            }

            paramSet.delete(p.getParameterName());
        }

        if (!isNullValue(se.getStatement().getDependentStatements())) {
            for (let statement of se.getStatement().getDependentStatements())
                se.addDependency(statement);
        }

        if (paramSet.size) {
            for (let param of Array.from(paramSet.values())) {
                if (isNullValue(SchemaUtil.getDefaultValue(param.getSchema(), this.sRepo)))
                    se.addMessage(
                        StatementMessageType.ERROR,
                        StringFormatter.format(
                            KIRuntime.PARAMETER_NEEDS_A_VALUE,
                            param.getParameterName(),
                        ),
                    );
            }
        }

        return se;
    }

    private parameterReferenceValidation(
        context: Map<string, ContextElement>,
        se: StatementExecution,
        p: Parameter,
        ref: ParameterReference,
    ): void {
        // Breaking this execution doesn't make sense.

        if (!ref) {
            if (isNullValue(SchemaUtil.getDefaultValue(p.getSchema(), this.sRepo)))
                se.addMessage(
                    StatementMessageType.ERROR,
                    StringFormatter.format(KIRuntime.PARAMETER_NEEDS_A_VALUE, p.getParameterName()),
                );
        } else if (ref.getType() == ParameterReferenceType.VALUE) {
            if (
                isNullValue(ref.getValue()) &&
                isNullValue(SchemaUtil.getDefaultValue(p.getSchema(), this.sRepo))
            )
                se.addMessage(
                    StatementMessageType.ERROR,
                    StringFormatter.format(KIRuntime.PARAMETER_NEEDS_A_VALUE, p.getParameterName()),
                );
            let paramElements: LinkedList<Tuple2<Schema, any>> = new LinkedList();
            paramElements.push(new Tuple2(p.getSchema(), ref.getValue()));

            while (!paramElements.isEmpty()) {
                let e: Tuple2<Schema, any> = paramElements.pop();

                if (e.getT2() instanceof JsonExpression) {
                    this.addDependencies(se, (e.getT2() as JsonExpression).getExpression());
                } else {
                    if (isNullValue(e.getT1()) || isNullValue(e.getT1().getType())) continue;

                    if (
                        e.getT1().getType().contains(SchemaType.ARRAY) &&
                        Array.isArray(e.getT2())
                    ) {
                        let ast: ArraySchemaType = e.getT1().getItems();
                        if (ast == null) {
                            continue;
                        }
                        if (ast.isSingleType()) {
                            for (let je of e.getT2())
                                paramElements.push(new Tuple2(ast.getSingleSchema(), je));
                        } else {
                            let array: any[] = e.getT2() as any[];
                            for (let i = 0; i < array.length; i++) {
                                paramElements.push(new Tuple2(ast.getTupleSchema()[i], array[i]));
                            }
                        }
                    } else if (
                        e.getT1().getType().contains(SchemaType.OBJECT) &&
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
                            for (let entry of Object.entries(e.getT2())) {
                                paramElements.push(
                                    new Tuple2(sch.getProperties().get(entry[0]), entry[1]),
                                );
                            }
                        }
                    }
                }
            }
        } else if (ref.getType() == ParameterReferenceType.EXPRESSION) {
            if (StringUtil.isNullOrBlank(ref.getExpression())) {
                if (isNullValue(SchemaUtil.getDefaultValue(p.getSchema(), this.sRepo)))
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

    private addDependencies(se: StatementExecution, expression: string): void {
        Array.from(expression.match(KIRuntime.STEP_REGEX_PATTERN) ?? []).forEach((e) =>
            se.addDependency(e),
        );
    }

    public makeEdges(graph: ExecutionGraph<string, StatementExecution>): Tuple2<string, string>[] {
        return Array.from(graph.getNodeMap().values())
            .filter((e) => !!e.getData().getDepenedencies())
            .flatMap((e) =>
                Array.from(e.getData().getDepenedencies())
                    .map((d) => {
                        let secondDot: number = d.indexOf('.', 6);
                        let step: string = d.substring(6, secondDot);
                        let eventDot: number = d.indexOf('.', secondDot + 1);
                        let event: string =
                            eventDot == -1
                                ? d.substring(secondDot + 1)
                                : d.substring(secondDot + 1, eventDot);

                        if (!graph.getNodeMap().has(step)) return new Tuple2(step, event);

                        e.addInEdgeTo(graph.getNodeMap().get(step), event);
                        return undefined;
                    })
                    .filter((e) => !!e),
            );
    }
}
