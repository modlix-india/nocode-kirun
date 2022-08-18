import { KIRuntimeException } from '../../exception/KIRuntimeException';
import { Schema } from '../../json/schema/Schema';
import { Event } from '../../model/Event';
import { EventResult } from '../../model/EventResult';
import { FunctionOutput } from '../../model/FunctionOutput';
import { FunctionSignature } from '../../model/FunctionSignature';
import { Parameter } from '../../model/Parameter';
import { Namespaces } from '../../namespaces/Namespaces';
import { ExpressionEvaluator } from '../../runtime/expression/ExpressionEvaluator';
import { FunctionExecutionParameters } from '../../runtime/FunctionExecutionParameters';
import { KIRuntime } from '../../runtime/KIRuntime';
import { isNullValue } from '../../util/NullCheck';
import { AbstractFunction } from '../AbstractFunction';

const VALUE = 'value';
const EVENT_NAME = 'eventName';
const RESULTS = 'results';

const SIGNATURE: FunctionSignature = new FunctionSignature()
    .setName('GenerateEvent')
    .setNamespace(Namespaces.SYSTEM)
    .setParameters(
        new Map([
            Parameter.ofEntry(EVENT_NAME, Schema.ofString(EVENT_NAME)),
            Parameter.ofEntry(
                RESULTS,
                Schema.ofObject(RESULTS).setProperties(
                    new Map([
                        ['name', Schema.ofString('name')],
                        [VALUE, Parameter.EXPRESSION],
                    ]),
                ),
                true,
            ),
        ]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map())]));

interface ResultType {
    name: string;
    value: any;
}

export class GenerateEvent extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }
    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        const events: Map<string, Map<string, any>[]> = context.getEvents();
        const args: Map<string, any> = context.getArguments();

        const eventName: string = args.get(EVENT_NAME);

        const map: Map<string, any> = context
            .getArguments()
            .get(RESULTS)
            .map((e: ResultType) => {
                let je: any = e[VALUE];

                if (isNullValue(je)) throw new KIRuntimeException('Expect a value object');

                let v: any = je.value;
                if (je.isExpression)
                    v = new ExpressionEvaluator(v).evaluate(context.getValuesMap());
                return [e.name, v];
            })
            .reduce((a: Map<string, any>, c: [string, any]) => {
                a.set(c[0], c[1]);
                return a;
            }, new Map());

        if (!events.has(eventName)) events.set(eventName, []);
        events.get(eventName).push(map);

        return new FunctionOutput([EventResult.outputOf(new Map())]);
    }
}
