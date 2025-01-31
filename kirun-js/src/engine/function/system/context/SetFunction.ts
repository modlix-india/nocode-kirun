import { ExecutionException } from '../../../exception/ExecutionException';
import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { SchemaType } from '../../../json/schema/type/SchemaType';
import { TypeUtil } from '../../../json/schema/type/TypeUtil';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { ContextElement } from '../../../runtime/ContextElement';
import { Expression } from '../../../runtime/expression/Expression';
import { ExpressionEvaluator } from '../../../runtime/expression/ExpressionEvaluator';
import { ExpressionToken } from '../../../runtime/expression/ExpressionToken';
import { ExpressionTokenValue } from '../../../runtime/expression/ExpressionTokenValue';
import { Operation } from '../../../runtime/expression/Operation';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { isNullValue } from '../../../util/NullCheck';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { StringUtil } from '../../../util/string/StringUtil';
import { AbstractFunction } from '../../AbstractFunction';

const NAME = 'name';
const VALUE = 'value';

export class SetFunction extends AbstractFunction {
    private readonly signature = new FunctionSignature('Set')
        .setNamespace(Namespaces.SYSTEM_CTX)
        .setParameters(
            new Map([
                Parameter.ofEntry(
                    NAME,
                    new Schema()
                        .setName(NAME)
                        .setType(TypeUtil.of(SchemaType.STRING))
                        .setMinLength(1),
                    false,
                ),
                Parameter.ofEntry(VALUE, Schema.ofAny(VALUE)),
            ]),
        )
        .setEvents(new Map([Event.outputEventMapEntry(new Map())]));
    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let key: string = context?.getArguments()?.get(NAME);

        if (StringUtil.isNullOrBlank(key)) {
            throw new KIRuntimeException(
                'Empty string is not a valid name for the context element',
            );
        }

        let value: any = context?.getArguments()?.get(VALUE);

        const exp: Expression = new Expression(key);

        const contextToken: ExpressionToken = exp.getTokens().peekLast();

        if (
            !contextToken.getExpression().startsWith('Context') ||
            contextToken instanceof Expression ||
            (contextToken instanceof ExpressionTokenValue &&
                !(contextToken as ExpressionTokenValue)
                    .getElement()
                    .toString()
                    .startsWith('Context'))
        ) {
            throw new ExecutionException(
                StringFormatter.format('The context path $ is not a valid path in context', key),
            );
        }

        for (const op of exp.getOperations().toArray()) {
            if (op == Operation.ARRAY_OPERATOR || op == Operation.OBJECT_OPERATOR) continue;

            throw new ExecutionException(
                StringFormatter.format(
                    'Expected a reference to the context location, but found an expression $',
                    key,
                ),
            );
        }

        for (let i = 0; i < exp.getTokens().size(); i++) {
            let ex = exp.getTokens().get(i);
            if (ex instanceof Expression)
                exp.getTokens().set(
                    i,
                    new ExpressionTokenValue(
                        key,
                        new ExpressionEvaluator(ex as Expression).evaluate(context.getValuesMap()),
                    ),
                );
        }
        return this.modifyContext(context, key, value, exp);
    }

    private modifyContext(
        context: FunctionExecutionParameters,
        key: string,
        value: any,
        exp: Expression,
    ): FunctionOutput {
        const tokens = exp.getTokens();
        tokens.removeLast();
        const ops = exp.getOperations();
        ops.removeLast();
        let ce: ContextElement | undefined = context
            .getContext()
            ?.get(tokens.removeLast().getExpression());

        if (isNullValue(ce)) {
            throw new KIRuntimeException(
                StringFormatter.format("Context doesn't have any element with name '$' ", key),
            );
        }

        if (ops.isEmpty()) {
            ce!.setElement(value);
            return new FunctionOutput([EventResult.outputOf(new Map())]);
        }

        let el: any = ce!.getElement();

        let op = ops.removeLast();
        let token = tokens.removeLast();
        let mem =
            token instanceof ExpressionTokenValue
                ? (token as ExpressionTokenValue).getElement()
                : token.getExpression();

        if (isNullValue(el)) {
            el = op == Operation.OBJECT_OPERATOR ? {} : [];
            ce!.setElement(el);
        }

        while (!ops.isEmpty()) {
            if (op == Operation.OBJECT_OPERATOR) {
                el = this.getDataFromObject(el, mem, ops.peekLast());
            } else {
                el = this.getDataFromArray(el, mem, ops.peekLast());
            }

            op = ops.removeLast();
            token = tokens.removeLast();
            mem =
                token instanceof ExpressionTokenValue
                    ? (token as ExpressionTokenValue).getElement()
                    : token.getExpression();
        }

        if (op == Operation.OBJECT_OPERATOR) this.putDataInObject(el, mem, value);
        else this.putDataInArray(el, mem, value);

        return new FunctionOutput([EventResult.outputOf(new Map())]);
    }

    private getDataFromArray(el: any, mem: string, nextOp: Operation): any {
        if (!Array.isArray(el))
            throw new KIRuntimeException(
                StringFormatter.format('Expected an array but found $', el),
            );

        const index = parseInt(mem);
        if (isNaN(index))
            throw new KIRuntimeException(
                StringFormatter.format('Expected an array index but found $', mem),
            );
        if (index < 0)
            throw new KIRuntimeException(
                StringFormatter.format('Array index is out of bound - $', mem),
            );

        let je = el[index];

        if (isNullValue(je)) {
            je = nextOp == Operation.OBJECT_OPERATOR ? {} : [];
            el[index] = je;
        }
        return je;
    }

    private getDataFromObject(el: any, mem: string, nextOp: Operation): any {
        if (Array.isArray(el) || typeof el !== 'object')
            throw new KIRuntimeException(
                StringFormatter.format('Expected an object but found $', el),
            );

        let je = el[mem];

        if (isNullValue(je)) {
            je = nextOp == Operation.OBJECT_OPERATOR ? {} : [];
            el[mem] = je;
        }
        return je;
    }

    private putDataInArray(el: any, mem: string, value: any): void {
        if (!Array.isArray(el))
            throw new KIRuntimeException(
                StringFormatter.format('Expected an array but found $', el),
            );

        const index = parseInt(mem);
        if (isNaN(index))
            throw new KIRuntimeException(
                StringFormatter.format('Expected an array index but found $', mem),
            );
        if (index < 0)
            throw new KIRuntimeException(
                StringFormatter.format('Array index is out of bound - $', mem),
            );

        el[index] = value;
    }

    private putDataInObject(el: any, mem: string, value: any): void {
        if (Array.isArray(el) || typeof el !== 'object')
            throw new KIRuntimeException(
                StringFormatter.format('Expected an object but found $', el),
            );

        el[mem] = value;
    }
}
