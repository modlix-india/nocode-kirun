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
    protected async validateArguments(
        args: Map<string, any>,
        schemaRepository: Repository<Schema>,
        statementExecution: StatementExecution | undefined,
    ): Promise<Map<string, any>> {
        const retmap = new Map<string, any>();
        for (let e of Array.from(this.getSignature().getParameters().entries())) {
            let param: Parameter = e[1];
            try {
                let tup = await this.validateArgument(args, schemaRepository, e, param);
                retmap.set(tup.getT1(), tup.getT2());
            } catch (err: any) {
                const signature = this.getSignature();
                throw new KIRuntimeException(
                    `Error while executing the function ${signature.getNamespace()}.${signature.getName()}'s parameter ${param.getParameterName()} with step name '${
                        statementExecution?.getStatement().getStatementName() ?? 'Unknown Step'
                    }' with error : ${err?.message}`,
                );
            }
        }
        return retmap;
    }

    private async validateArgument(
        args: Map<string, any>,
        schemaRepository: Repository<Schema>,
        e: [string, Parameter],
        param: Parameter,
    ): Promise<Tuple2<string, any>> {
        let key: string = e[0];
        let jsonElement: any = args.get(e[0]);

        // console.log('get([0])', e);

        if (isNullValue(jsonElement) && !param.isVariableArgument()) {
            return new Tuple2(
                key,
                await SchemaValidator.validate(
                    undefined,
                    param.getSchema(),
                    schemaRepository,
                    undefined,
                ),
            );
        }

        if (!param?.isVariableArgument())
            return new Tuple2(
                key,
                await SchemaValidator.validate(
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
            array[i] = await SchemaValidator.validate(
                undefined,
                param.getSchema(),
                schemaRepository,
                array[i],
            );
        }

        return new Tuple2(key, array);
    }

    public async execute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const args: Map<string, any> = await this.validateArguments(
            context.getArguments() ?? new Map(),
            context.getSchemaRepository(),
            context.getStatementExecution(),
        );
        context.setArguments(args);
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
