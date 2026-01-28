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
import { ErrorMessageFormatter } from '../util/ErrorMessageFormatter';
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
                const functionName = ErrorMessageFormatter.formatFunctionName(
                    signature.getNamespace(),
                    signature.getName(),
                );
                const statementName = ErrorMessageFormatter.formatStatementName(
                    statementExecution?.getStatement().getStatementName(),
                );
                const errorMessage = ErrorMessageFormatter.formatErrorMessage(err);
                throw new KIRuntimeException(
                    ErrorMessageFormatter.buildFunctionExecutionError(
                        functionName,
                        statementName,
                        errorMessage,
                        param.getParameterName(),
                        param.getSchema(),
                    ),
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
            return await this.internalExecute(context);
        } catch (err: any) {
            const signature = this.getSignature();
            const functionName = ErrorMessageFormatter.formatFunctionName(
                signature.getNamespace(),
                signature.getName(),
            );
            const statementName = ErrorMessageFormatter.formatStatementName(
                context.getStatementExecution()?.getStatement().getStatementName(),
            );
            const errorMessage = ErrorMessageFormatter.formatErrorMessage(err);
            throw new KIRuntimeException(
                ErrorMessageFormatter.buildFunctionExecutionError(
                    functionName,
                    statementName,
                    errorMessage,
                ),
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
