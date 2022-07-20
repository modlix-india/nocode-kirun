import { Schema } from '../json/schema/Schema';
import { SchemaValidator } from '../json/schema/validator/SchemaValidator';
import { Event } from '../model/Event';
import { FunctionOutput } from '../model/FunctionOutput';
import { FunctionSignature } from '../model/FunctionSignature';
import { Parameter } from '../model/Parameter';
import { FunctionExecutionParameters } from '../runtime/FunctionExecutionParameters';
import { isNullValue } from '../util/NullCheck';
import { Tuple2 } from '../util/Tuples';
import { Function } from './Function';

export abstract class AbstractFunction implements Function {
    protected validateArguments(args: Map<string, any>): Map<string, any> {
        return Array.from(this.getSignature().getParameters().entries())
            .map((e) => {
                let key: string = e[0];
                let param: Parameter = e[1];
                let jsonElement: any = args.get(e[0]);

                if (isNullValue(jsonElement)) {
                    return new Tuple2(
                        key,
                        SchemaValidator.validate(
                            undefined,
                            param.getSchema(),
                            undefined,
                            undefined,
                        ),
                    );
                }

                if (!param?.isVariableArgument())
                    return new Tuple2(
                        key,
                        SchemaValidator.validate(
                            undefined,
                            param.getSchema(),
                            undefined,
                            jsonElement,
                        ),
                    );

                let array: any[] = undefined;

                if (Array.isArray(jsonElement)) array = jsonElement as any[];
                else {
                    array = [];
                    array.push(jsonElement);
                }

                for (const je of array) {
                    SchemaValidator.validate(undefined, param.getSchema(), undefined, je);
                }

                return new Tuple2(key, jsonElement);
            })
            .reduce((a, c) => {
                a.set(c.getT1(), c.getT2());
                return a;
            }, new Map<string, any>());
    }

    public execute(context: FunctionExecutionParameters): FunctionOutput {
        context.setArguments(this.validateArguments(context.getArguments()));
        return this.internalExecute(context);
    }

    public getProbableEventSignature(
        probableParameters: Map<string, Schema[]>,
    ): Map<string, Event> {
        return this.getSignature().getEvents();
    }

    protected abstract internalExecute(context: FunctionExecutionParameters): FunctionOutput;
    public abstract getSignature(): FunctionSignature;
}
