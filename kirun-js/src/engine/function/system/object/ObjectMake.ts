import { AbstractFunction } from '../../AbstractFunction';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { Namespaces } from '../../../namespaces/Namespaces';
import { Event } from '../../../model/Event';
import { Parameter } from '../../../model/Parameter';
import { Schema } from '../../../json/schema/Schema';
import { EventResult } from '../../../model/EventResult';
import { MapUtil } from '../../../util/MapUtil';
import { duplicate } from '../../../util/duplicate';
import { ExpressionEvaluator } from '../../../runtime/expression/ExpressionEvaluator';
import { TokenValueExtractor } from '../../../runtime/expression/tokenextractor/TokenValueExtractor';

const SOURCE = 'source';
const RESULT_STRUCT = 'resultStruct';
const VALUE = 'value';

export class ObjectMake extends AbstractFunction {
    private readonly signature: FunctionSignature;

    public constructor() {
        super();
        this.signature = new FunctionSignature('ObjectMake')
            .setNamespace(Namespaces.SYSTEM_OBJECT)
            .setParameters(
                new Map([
                    Parameter.ofEntry(SOURCE, Schema.ofAny(SOURCE)),
                    Parameter.ofEntry(RESULT_STRUCT, Schema.ofObject(RESULT_STRUCT)),
                ]),
            )
            .setEvents(
                new Map([
                    Event.outputEventMapEntry(
                        MapUtil.of(VALUE, Schema.ofAny(VALUE)),
                    ),
                ]),
            );
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const args = context.getArguments() ?? new Map<string, any>();

        const source = args.get(SOURCE);
        const resultStruct = (args.get(RESULT_STRUCT) ?? {}) as Record<string, any>;

        const result: Record<string, any> = {};

        const valuesMap: Map<string, TokenValueExtractor> = context.getValuesMap();

        for (const [key, keyValue] of Object.entries(resultStruct)) {
            let valueToSet: any;

            if (typeof keyValue === 'string') {
                valueToSet = this.evaluateKeyValue(source, keyValue, valuesMap);
            } else {
                valueToSet = duplicate(keyValue);
            }

            result[key] = valueToSet;
        }

        return new FunctionOutput([
            EventResult.outputOf(MapUtil.of(VALUE, result)),
        ]);
    }

    private evaluateKeyValue(
        source: any,
        expression: string,
        valuesMap: Map<string, TokenValueExtractor>,
    ): any {
        if (!expression || !expression.startsWith('{{') || !expression.endsWith('}}')) {
            return source !== undefined ? duplicate(source) : null;
        }

        const innerExpression = expression.substring(2, expression.length - 2);

        const evaluator = new ExpressionEvaluator(innerExpression);
        const result = evaluator.evaluate(valuesMap);

        return result !== undefined ? result : null;
    }
}


