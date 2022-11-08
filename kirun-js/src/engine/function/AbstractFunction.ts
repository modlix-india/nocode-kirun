import { KIRuntimeException } from '../exception/KIRuntimeException';
import { Schema } from '../json/schema/Schema';
import { SchemaValidator } from '../json/schema/validator/SchemaValidator';
import { Event } from '../model/Event';
import { FunctionOutput } from '../model/FunctionOutput';
import { FunctionSignature } from '../model/FunctionSignature';
import { Parameter } from '../model/Parameter';
import { Repository } from '../Repository';
import { FunctionExecutionParameters } from '../runtime/FunctionExecutionParameters';
import { StatementExecution } from '../runtime/StatementExecution';
import { isNullValue } from '../util/NullCheck';
import { Tuple2 } from '../util/Tuples';
import { Function } from './Function';

export abstract class AbstractFunction implements Function {
    protected validateArguments(
        args: Map<string, any>,
        schemaRepository: Repository<Schema>,
        statementExecution: StatementExecution | undefined,
    ): Map<string, any> {
        return Array.from(this.getSignature().getParameters().entries())
            .map((e) => {
                let param: Parameter = e[1];
                try {
                    return this.validateArgument(args, schemaRepository, e, param);
                } catch (err: any) {
                    const signature = this.getSignature();
                    throw new KIRuntimeException(
                        `Error while executing the function ${signature.getNamespace()}.${signature.getName()}'s parameter ${param.getParameterName()} with step name '${
                            statementExecution?.getStatement().getStatementName() ?? 'Unknown Step'
                        }' with error : ${err?.message}`,
                    );
                }
            })
            .reduce((a, c) => {
                a.set(c.getT1(), c.getT2());
                return a;
            }, new Map<string, any>());
    }

    private validateArgument(
        args: Map<string, any>,
        schemaRepository: Repository<Schema>,
        e: [string, Parameter],
        param: Parameter,
    ): Tuple2<string, any> {
        let key: string = e[0];
        let jsonElement: any = args.get(e[0]);

        if (isNullValue(jsonElement) && !param.isVariableArgument()) {
            return new Tuple2(
                key,
                SchemaValidator.validate(undefined, param.getSchema(), schemaRepository, undefined),
            );
        }

        if (!param?.isVariableArgument())
            return new Tuple2(
                key,
                SchemaValidator.validate(
                    undefined,
                    param.getSchema(),
                    schemaRepository,
                    jsonElement,
                ),
            );

        let array: any[] | undefined = undefined;

        if (Array.isArray(jsonElement)) array = jsonElement as any[];
        else {
            array = [];
            if (!isNullValue(jsonElement)) array.push(jsonElement);
            else if (!isNullValue(param.getSchema().getDefaultValue()))
                array.push(param.getSchema().getDefaultValue());
        }

        for (let i = 0; i < array.length; i++) {
            array[i] = SchemaValidator.validate(
                undefined,
                param.getSchema(),
                schemaRepository,
                array[i],
            );
        }

        return new Tuple2(key, array);
    }

    public async execute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        context.setArguments(
            this.validateArguments(
                context.getArguments() ?? new Map(),
                context.getSchemaRepository(),
                context.getStatementExecution(),
            ),
        );
        try {
            return this.internalExecute(context);
        } catch (err: any) {
            const signature = this.getSignature();
            throw new KIRuntimeException(
                `Error while executing the function ${signature.getNamespace()}.${signature.getName()} with step name '${
                    context.getStatementExecution()?.getStatement().getStatementName() ??
                    'Unknown Step'
                }' with error : ${err?.message}`,
            );
        }
    }

    public getProbableEventSignature(
        probableParameters: Map<string, Schema[]>,
    ): Map<string, Event> {
        return this.getSignature().getEvents();
    }

    protected abstract internalExecute(
        context: FunctionExecutionParameters,
    ): Promise<FunctionOutput>;
    public abstract getSignature(): FunctionSignature;
}
