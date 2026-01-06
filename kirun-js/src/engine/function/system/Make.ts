import { AbstractFunction } from '../AbstractFunction';
import { FunctionSignature } from '../../model/FunctionSignature';
import { FunctionExecutionParameters } from '../../runtime/FunctionExecutionParameters';
import { FunctionOutput } from '../../model/FunctionOutput';
import { Namespaces } from '../../namespaces/Namespaces';
import { Event } from '../../model/Event';
import { Parameter } from '../../model/Parameter';
import { Schema } from '../../json/schema/Schema';
import { EventResult } from '../../model/EventResult';
import { MapUtil } from '../../util/MapUtil';
import { ExpressionEvaluator } from '../../runtime/expression/ExpressionEvaluator';
import { TokenValueExtractor } from '../../runtime/expression/tokenextractor/TokenValueExtractor';

const RESULT_SHAPE = 'resultShape';
const VALUE = 'value';

export class Make extends AbstractFunction {
    private readonly signature: FunctionSignature;

    public constructor() {
        super();
        this.signature = new FunctionSignature('Make')
            .setNamespace(Namespaces.SYSTEM)
            .setParameters(
                new Map([Parameter.ofEntry(RESULT_SHAPE, Schema.ofAny(RESULT_SHAPE))]),
            )
            .setEvents(
                new Map([
                    Event.outputEventMapEntry(MapUtil.of(VALUE, Schema.ofAny(VALUE))),
                ]),
            );
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const args = context.getArguments() ?? new Map<string, any>();
        const resultShape = args.get(RESULT_SHAPE);
        const valuesMap: Map<string, TokenValueExtractor> = context.getValuesMap();

        const result = this.processValue(resultShape, valuesMap);

        return new FunctionOutput([EventResult.outputOf(MapUtil.of(VALUE, result))]);
    }

    private processValue(value: any, valuesMap: Map<string, TokenValueExtractor>): any {
        if (value === null || value === undefined) {
            return value;
        }

        if (typeof value === 'string') {
            return this.evaluateExpression(value, valuesMap);
        }

        if (Array.isArray(value)) {
            return value.map((item) => this.processValue(item, valuesMap));
        }

        if (typeof value === 'object') {
            const result: Record<string, any> = {};
            for (const [key, val] of Object.entries(value)) {
                result[key] = this.processValue(val, valuesMap);
            }
            return result;
        }

        return value;
    }

    private evaluateExpression(expression: string, valuesMap: Map<string, TokenValueExtractor>): any {
        if (!expression || !expression.startsWith('{{') || !expression.endsWith('}}')) {
            return expression;
        }

        const innerExpression = expression.substring(2, expression.length - 2);
        const evaluator = new ExpressionEvaluator(innerExpression);
        const result = evaluator.evaluate(valuesMap);

        return result !== undefined ? result : null;
    }
}