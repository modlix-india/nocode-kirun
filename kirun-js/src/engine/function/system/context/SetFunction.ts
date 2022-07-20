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
import { ParameterType } from '../../../model/ParameterType';
import { Namespaces } from '../../../namespaces/Namespaces';
import { ContextElement } from '../../../runtime/ContextElement';
import { Expression } from '../../../runtime/expression/Expression';
import { ExpressionEvaluator } from '../../../runtime/expression/ExpressionEvaluator';
import { ExpressionToken } from '../../../runtime/expression/ExpressionToken';
import { ExpressionTokenValue } from '../../../runtime/expression/ExpressionTokenValue';
import { Operation } from '../../../runtime/expression/Operation';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { LinkedList } from '../../../util/LinkedList';
import { isNullValue } from '../../../util/NullCheck';
import { PrimitiveUtil } from '../../../util/primitive/PrimitiveUtil';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { StringUtil } from '../../../util/string/StringUtil';
import { Tuple2 } from '../../../util/Tuples';
import { AbstractFunction } from '../../AbstractFunction';

const NAME = 'name';
const VALUE = 'value';
const SIGNATURE = new FunctionSignature()
    .setName('Set')
    .setNamespace(Namespaces.SYSTEM_CTX)
    .setParameters(
        new Map([
            Parameter.ofEntry(
                NAME,
                new Schema().setName(NAME).setType(TypeUtil.of(SchemaType.STRING)).setMinLength(1),
                false,
                ParameterType.CONSTANT,
            ),
            Parameter.ofEntry(VALUE, Schema.ofAny(VALUE)),
        ]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map())]));

export class SetFunction extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let key: string = context.getArguments().get(NAME);

        if (StringUtil.isNullOrBlank(key)) {
            throw new KIRuntimeException(
                'Empty string is not a valid name for the context element',
            );
        }

        let value: any = context.getArguments().get(VALUE);

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
                        new ExpressionEvaluator(ex as Expression).evaluate(context),
                    ),
                );
        }

        let tokens: LinkedList<ExpressionToken> = exp.getTokens();
        tokens.removeLast();
        let ops: LinkedList<Operation> = exp.getOperations();
        ops.removeLast();
        let ce: ContextElement = context.getContext().get(tokens.removeLast().getExpression());

        if (isNullValue(ce)) {
            throw new KIRuntimeException(
                StringFormatter.format("Context doesn't have any element with name '$' ", key),
            );
        }

        if (ops.isEmpty()) {
            ce.setElement(value);
            return new FunctionOutput([EventResult.outputOf(new Map())]);
        }

        let el: any = ce.getElement();

        while (ops.size() > 1) {
            if (isNullValue(el)) {
                throw new KIRuntimeException(
                    StringFormatter.format('Unable to set the context in the path $', key),
                );
            }

            const op: Operation = ops.removeLast();
            const token: ExpressionToken = tokens.removeLast();
            if (op == Operation.OBJECT_OPERATOR) {
                if (typeof el != 'object') {
                    throw new KIRuntimeException(
                        StringFormatter.format('$ has no object in the context', key),
                    );
                }

                let mem: string = undefined;
                if (token instanceof ExpressionTokenValue)
                    mem = (token as ExpressionTokenValue).getTokenValue().getAsString();
                else mem = token.getExpression();

                el = el.getAsJsonObject().get(mem);
            } else {
                const je: any =
                    token instanceof ExpressionTokenValue
                        ? (token as ExpressionTokenValue).getElement()
                        : token.getExpression();

                if (Array.isArray(je) || typeof je === 'object') {
                    throw new KIRuntimeException(
                        StringFormatter.format(
                            'Cannot extract json with key $ from the object $',
                            je,
                            el,
                        ),
                    );
                }

                if (Array.isArray(el)) {
                    const prim: Tuple2<SchemaType, any> = PrimitiveUtil.findPrimitive(je);

                    if (prim.getT1() != SchemaType.INTEGER || prim.getT1() != SchemaType.LONG) {
                        throw new KIRuntimeException(
                            StringFormatter.format('Expecting a numerical index but found $', je),
                        );
                    }

                    let index = prim.getT2() as number;
                    if (index >= el.length)
                        throw new KIRuntimeException(
                            StringFormatter.format('Index out of bound while accessing $', key),
                        );
                    el = el[index];
                } else {
                    const mem: string = je.getAsString();
                    el = el[mem];
                }
            }
        }

        if (el == null) {
            throw new KIRuntimeException(
                StringFormatter.format('Unable to set the context in the path $', key),
            );
        }

        const op: Operation = ops.removeLast();
        const token: ExpressionToken = tokens.removeLast();

        // TODO: Here I need to validate the schema of the value I have to put in the
        // context.

        if (op == Operation.OBJECT_OPERATOR) {
            if (typeof el == 'object') {
                throw new KIRuntimeException(
                    StringFormatter.format('$ has no object in the context', key),
                );
            }

            let mem: string;
            if (token instanceof ExpressionTokenValue)
                mem = (token as ExpressionTokenValue).getTokenValue();
            else mem = token.getExpression();

            el[mem] = value;
        } else {
            let je =
                token instanceof ExpressionTokenValue
                    ? (token as ExpressionTokenValue).getElement()
                    : token.getExpression();

            if (Array.isArray(je) || typeof je === 'object') {
                throw new KIRuntimeException(
                    StringFormatter.format(
                        'Cannot extract json with key $ from the object $',
                        je,
                        el,
                    ),
                );
            }

            if (Array.isArray(el)) {
                const prim: Tuple2<SchemaType, any> = PrimitiveUtil.findPrimitive(je);

                if (prim.getT1() != SchemaType.INTEGER && prim.getT1() != SchemaType.LONG) {
                    throw new KIRuntimeException(
                        StringFormatter.format('Expecting a numerical index but found $', je),
                    );
                }

                let index = prim.getT2() as number;
                if (index >= el.length)
                    throw new KIRuntimeException(
                        StringFormatter.format('Index out of bound while accessing $', key),
                    );
                el[index] = value;
            } else {
                let mem: string = je;
                el[mem] = value;
            }
        }

        return new FunctionOutput([EventResult.outputOf(new Map())]);
    }
}
