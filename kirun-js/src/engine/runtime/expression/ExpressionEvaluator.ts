import { ExecutionException } from '../../exception/ExecutionException';
import { LinkedList } from '../../util/LinkedList';
import { StringBuilder } from '../../util/string/StringBuilder';
import { StringFormatter } from '../../util/string/StringFormatter';
import { FunctionExecutionParameters } from '../FunctionExecutionParameters';
import { ExpressionEvaluationException } from './exception/ExpressionEvaluationException';
import { Expression } from './Expression';
import { ExpressionToken } from './ExpressionToken';
import { ExpressionTokenValue } from './ExpressionTokenValue';
import { Operation } from './Operation';
import { LogicalNullishCoalescingOperator } from './operators/binary/LogicalNullishCoalescingOperator';
import { ArithmeticAdditionOperator } from './operators/binary/ArithmeticAdditionOperator';
import { ArithmeticDivisionOperator } from './operators/binary/ArithmeticDivisionOperator';
import { ArithmeticIntegerDivisionOperator } from './operators/binary/ArithmeticInetgerDivisionOperator';
import { ArithmeticModulusOperator } from './operators/binary/ArithmeticModulusOperator';
import { ArithmeticMultiplicationOperator } from './operators/binary/ArithmeticMultiplicationOperator';
import { ArithmeticSubtractionOperator } from './operators/binary/ArithmeticSubtractionOperator';
import { ArrayOperator } from './operators/binary/ArrayOperator';
import { BinaryOperator } from './operators/binary/BinaryOperator';
import { BitwiseAndOperator } from './operators/binary/BitwiseAndOperator';
import { BitwiseLeftShiftOperator } from './operators/binary/BitwiseLeftShiftOperator';
import { BitwiseOrOperator } from './operators/binary/BitwiseOrOperator';
import { BitwiseRightShiftOperator } from './operators/binary/BitwiseRightShiftOperator';
import { BitwiseUnsignedRightShiftOperator } from './operators/binary/BitwiseUnsignedRightShiftOperator';
import { BitwiseXorOperator } from './operators/binary/BitwiseXorOperator';
import { LogicalAndOperator } from './operators/binary/LogicalAndOperator';
import { LogicalEqualOperator } from './operators/binary/LogicalEqualOperator';
import { LogicalGreaterThanEqualOperator } from './operators/binary/LogicalGreaterThanEqualOperator';
import { LogicalGreaterThanOperator } from './operators/binary/LogicalGreaterThanOperator';
import { LogicalLessThanEqualOperator } from './operators/binary/LogicalLessThanEqualOperator';
import { LogicalLessThanOperator } from './operators/binary/LogicalLessThanOperator';
import { LogicalNotEqualOperator } from './operators/binary/LogicalNotEqualOperator';
import { LogicalOrOperator } from './operators/binary/LogicalOrOperator';
import { ObjectOperator } from './operators/binary/ObjectOperator';
import { ArithmeticUnaryMinusOperator } from './operators/unary/ArithmeticUnaryMinusOperator';
import { ArithmeticUnaryPlusOperator } from './operators/unary/ArithmeticUnaryPlusOperator';
import { BitwiseComplementOperator } from './operators/unary/BitwiseComplementOperator';
import { LogicalNotOperator } from './operators/unary/LogicalNotOperator';
import { UnaryOperator } from './operators/unary/UnaryOperator';
import { LiteralTokenValueExtractor } from './tokenextractor/LiteralTokenValueExtractor';
import { TokenValueExtractor } from './tokenextractor/TokenValueExtractor';
import { Tuple2 } from '../../util/Tuples';
import { ConditionalTernaryOperator } from './operators/ternary';
import { TernaryOperator } from './operators/ternary/TernaryOperator';
import { ExpressionInternalValueExtractor } from './tokenextractor/ExpressionInternalValueExtractor';

export class ExpressionEvaluator {
    private static readonly UNARY_OPERATORS_MAP: Map<Operation, UnaryOperator> = new Map([
        [Operation.UNARY_BITWISE_COMPLEMENT, new BitwiseComplementOperator()],
        [Operation.UNARY_LOGICAL_NOT, new LogicalNotOperator()],
        [Operation.UNARY_MINUS, new ArithmeticUnaryMinusOperator()],
        [Operation.UNARY_PLUS, new ArithmeticUnaryPlusOperator()],
    ]);

    private static readonly TERNARY_OPERATORS_MAP: Map<Operation, TernaryOperator> = new Map([
        [Operation.CONDITIONAL_TERNARY_OPERATOR, new ConditionalTernaryOperator()],
    ]);

    private static readonly BINARY_OPERATORS_MAP: Map<Operation, BinaryOperator> = new Map([
        [Operation.ADDITION, new ArithmeticAdditionOperator()],
        [Operation.DIVISION, new ArithmeticDivisionOperator()],
        [Operation.INTEGER_DIVISION, new ArithmeticIntegerDivisionOperator()],
        [Operation.MOD, new ArithmeticModulusOperator()],
        [Operation.MULTIPLICATION, new ArithmeticMultiplicationOperator()],
        [Operation.SUBTRACTION, new ArithmeticSubtractionOperator()],

        [Operation.BITWISE_AND, new BitwiseAndOperator()],
        [Operation.BITWISE_LEFT_SHIFT, new BitwiseLeftShiftOperator()],
        [Operation.BITWISE_OR, new BitwiseOrOperator()],
        [Operation.BITWISE_RIGHT_SHIFT, new BitwiseRightShiftOperator()],
        [Operation.BITWISE_UNSIGNED_RIGHT_SHIFT, new BitwiseUnsignedRightShiftOperator()],
        [Operation.BITWISE_XOR, new BitwiseXorOperator()],

        [Operation.AND, new LogicalAndOperator()],
        [Operation.EQUAL, new LogicalEqualOperator()],
        [Operation.GREATER_THAN, new LogicalGreaterThanOperator()],
        [Operation.GREATER_THAN_EQUAL, new LogicalGreaterThanEqualOperator()],
        [Operation.LESS_THAN, new LogicalLessThanOperator()],
        [Operation.LESS_THAN_EQUAL, new LogicalLessThanEqualOperator()],
        [Operation.OR, new LogicalOrOperator()],
        [Operation.NOT_EQUAL, new LogicalNotEqualOperator()],
        [Operation.NULLISH_COALESCING_OPERATOR, new LogicalNullishCoalescingOperator()],

        [Operation.ARRAY_OPERATOR, new ArrayOperator()],
        [Operation.OBJECT_OPERATOR, new ObjectOperator()],
    ]);

    private static readonly UNARY_OPERATORS_MAP_KEY_SET: Set<Operation> = new Set(
        ExpressionEvaluator.UNARY_OPERATORS_MAP.keys(),
    );

    private expression: string;
    private exp?: Expression;
    private internalTokenValueExtractor: ExpressionInternalValueExtractor =
        new ExpressionInternalValueExtractor();

    public constructor(exp: Expression | string) {
        if (exp instanceof Expression) {
            this.exp = exp as Expression;
            this.expression = this.exp.getExpression();
        } else {
            this.expression = exp;
        }
    }

    public evaluate(valuesMap: Map<string, TokenValueExtractor>): any {
        const tuple: Tuple2<string, Expression> = this.processNestingExpression(
            this.expression,
            valuesMap,
        );
        this.expression = tuple.getT1();
        this.exp = tuple.getT2();
        valuesMap = new Map(valuesMap.entries());
        valuesMap.set(
            this.internalTokenValueExtractor.getPrefix(),
            this.internalTokenValueExtractor,
        );

        return this.evaluateExpression(this.exp, valuesMap);
    }

    private processNestingExpression(
        expression: string,
        valuesMap: Map<string, TokenValueExtractor>,
    ): Tuple2<string, Expression> {
        let start = 0;
        let i = 0;

        const tuples: LinkedList<Tuple2<number, number>> = new LinkedList();

        while (i < expression.length - 1) {
            if (expression.charAt(i) == '{' && expression.charAt(i + 1) == '{') {
                if (start == 0) tuples.push(new Tuple2(i + 2, -1));

                start++;
                i++;
            } else if (expression.charAt(i) == '}' && expression.charAt(i + 1) == '}') {
                start--;

                if (start < 0)
                    throw new ExpressionEvaluationException(
                        expression,
                        'Expecting {{ nesting path operator to be started before closing',
                    );

                if (start == 0) {
                    tuples.push(tuples.pop().setT2(i));
                }
                i++;
            }
            i++;
        }

        let newExpression = this.replaceNestingExpression(expression, valuesMap, tuples);

        return new Tuple2(newExpression, new Expression(newExpression));
    }

    private replaceNestingExpression(
        expression: string,
        valuesMap: Map<string, TokenValueExtractor>,
        tuples: LinkedList<Tuple2<number, number>>,
    ): string {
        let newExpression = expression;

        for (var tuple of tuples.toArray()) {
            if (tuple.getT2() == -1)
                throw new ExpressionEvaluationException(
                    expression,
                    'Expecting }} nesting path operator to be closed',
                );

            let expStr: string = new ExpressionEvaluator(
                newExpression.substring(tuple.getT1(), tuple.getT2()),
            ).evaluate(valuesMap);

            newExpression =
                newExpression.substring(0, tuple.getT1() - 2) +
                expStr +
                newExpression.substring(tuple.getT2() + 2);
        }
        return newExpression;
    }

    public getExpression(): Expression {
        if (!this.exp) this.exp = new Expression(this.expression);

        return this.exp;
    }

    public getExpressionString(): string {
        return this.expression;
    }

    private evaluateExpression(exp: Expression, valuesMap: Map<string, TokenValueExtractor>): any {
        let ops: LinkedList<Operation> = exp.getOperations();
        let tokens: LinkedList<ExpressionToken> = exp.getTokens();

        while (!ops.isEmpty()) {
            let operator: Operation = ops.pop();
            let token: ExpressionToken = tokens.pop();

            if (ExpressionEvaluator.UNARY_OPERATORS_MAP_KEY_SET.has(operator)) {
                tokens.push(
                    this.applyUnaryOperation(operator, this.getValueFromToken(valuesMap, token)),
                );
            } else if (
                operator == Operation.OBJECT_OPERATOR ||
                operator == Operation.ARRAY_OPERATOR
            ) {
                this.processObjectOrArrayOperator(valuesMap, ops, tokens, operator, token);
            } else if (operator == Operation.CONDITIONAL_TERNARY_OPERATOR) {
                const token2: ExpressionToken = tokens.pop();
                const token3: ExpressionToken = tokens.pop();
                var v1 = this.getValueFromToken(valuesMap, token3);
                var v2 = this.getValueFromToken(valuesMap, token2);
                var v3 = this.getValueFromToken(valuesMap, token);
                tokens.push(this.applyTernaryOperation(operator, v1, v2, v3));
            } else {
                const token2: ExpressionToken = tokens.pop();
                var v1 = this.getValueFromToken(valuesMap, token2);
                var v2 = this.getValueFromToken(valuesMap, token);
                tokens.push(this.applyBinaryOperation(operator, v1, v2));
            }
        }

        if (tokens.isEmpty())
            throw new ExecutionException(
                StringFormatter.format('Expression : $ evaluated to null', exp),
            );

        if (tokens.size() != 1)
            throw new ExecutionException(
                StringFormatter.format('Expression : $ evaluated multiple values $', exp, tokens),
            );

        const token: ExpressionToken = tokens.get(0);
        if (token instanceof ExpressionTokenValue)
            return (token as ExpressionTokenValue).getElement();
        else if (!(token instanceof Expression)) return this.getValueFromToken(valuesMap, token);

        throw new ExecutionException(
            StringFormatter.format('Expression : $ evaluated to $', exp, tokens.get(0)),
        );
    }

    private processObjectOrArrayOperator(
        valuesMap: Map<string, TokenValueExtractor>,
        ops: LinkedList<Operation>,
        tokens: LinkedList<ExpressionToken>,
        operator?: Operation,
        token?: ExpressionToken,
    ): void {
        const objTokens: LinkedList<ExpressionToken> = new LinkedList();
        const objOperations: LinkedList<Operation> = new LinkedList();

        if (!operator || !token) return;

        do {
            objOperations.push(operator);
            if (token instanceof Expression)
                objTokens.push(
                    new ExpressionTokenValue(
                        token.toString(),
                        this.evaluateExpression(token as Expression, valuesMap),
                    ),
                );
            else if (token) objTokens.push(token);
            token = tokens.isEmpty() ? undefined : tokens.pop();
            operator = ops.isEmpty() ? undefined : ops.pop();
        } while (operator == Operation.OBJECT_OPERATOR || operator == Operation.ARRAY_OPERATOR);

        if (token) {
            if (token instanceof Expression)
                objTokens.push(
                    new ExpressionTokenValue(
                        token.toString(),
                        this.evaluateExpression(token as Expression, valuesMap),
                    ),
                );
            else objTokens.push(token);
        }

        if (operator) ops.push(operator);

        let objToken: ExpressionToken = objTokens.pop();

        if (
            objToken instanceof ExpressionTokenValue &&
            typeof objToken.getTokenValue() === 'object'
        ) {
            const key = new Date().getTime() + '' + Math.round(Math.random() * 1000);
            this.internalTokenValueExtractor.addValue(key, objToken.getTokenValue());
            objToken = new ExpressionToken(ExpressionInternalValueExtractor.PREFIX + key);
        }

        let sb: StringBuilder = new StringBuilder(
            objToken instanceof ExpressionTokenValue
                ? (objToken as ExpressionTokenValue).getTokenValue()
                : objToken.toString(),
        );

        while (!objTokens.isEmpty()) {
            objToken = objTokens.pop();
            operator = objOperations.pop();
            sb.append(operator.getOperator()).append(
                objToken instanceof ExpressionTokenValue
                    ? (objToken as ExpressionTokenValue).getTokenValue()
                    : objToken.toString(),
            );
            if (operator == Operation.ARRAY_OPERATOR) sb.append(']');
        }

        let str: string = sb.toString();
        let key: string = str.substring(0, str.indexOf('.') + 1);
        if (key.length > 2 && valuesMap.has(key))
            tokens.push(new ExpressionTokenValue(str, this.getValue(str, valuesMap)));
        else {
            let v: any = undefined;
            try {
                v = LiteralTokenValueExtractor.INSTANCE.getValue(str);
            } catch (err) {
                v = str;
            }
            tokens.push(new ExpressionTokenValue(str, v));
        }
    }

    private applyTernaryOperation(operator: Operation, v1: any, v2: any, v3: any): ExpressionToken {
        let op: TernaryOperator | undefined =
            ExpressionEvaluator.TERNARY_OPERATORS_MAP.get(operator);

        if (!op)
            throw new ExpressionEvaluationException(
                this.expression,
                StringFormatter.format('No operator found to evaluate $', this.getExpression()),
            );

        return new ExpressionTokenValue(operator.toString(), op.apply(v1, v2, v3));
    }

    private applyBinaryOperation(operator: Operation, v1: any, v2: any): ExpressionToken {
        let typv1: string = typeof v1;
        let typv2: string = typeof v2;

        let op: BinaryOperator | undefined = ExpressionEvaluator.BINARY_OPERATORS_MAP.get(operator);

        if (
            (typv1 === 'object' || typv2 === 'object') &&
            operator !== Operation.EQUAL &&
            operator !== Operation.NOT_EQUAL &&
            operator !== Operation.NULLISH_COALESCING_OPERATOR &&
            operator !== Operation.AND &&
            operator !== Operation.OR
        )
            throw new ExpressionEvaluationException(
                this.expression,
                StringFormatter.format(
                    'Cannot evaluate expression $ $ $',
                    v1,
                    operator.getOperator(),
                    v2,
                ),
            );

        if (!op)
            throw new ExpressionEvaluationException(
                this.expression,
                StringFormatter.format(
                    'No operator found to evaluate $ $ $',
                    v1,
                    operator.getOperator(),
                    v2,
                ),
            );

        return new ExpressionTokenValue(operator.toString(), op.apply(v1, v2));
    }

    private applyUnaryOperation(operator: Operation, value: any): ExpressionToken {
        let typv: string = typeof value;

        if (
            operator.getOperator() != Operation.NOT.getOperator() &&
            operator.getOperator() != Operation.UNARY_LOGICAL_NOT.getOperator() &&
            (typv === 'object' || Array.isArray(value))
        )
            throw new ExpressionEvaluationException(
                this.expression,
                StringFormatter.format(
                    'The operator $ cannot be applied to $',
                    operator.getOperator(),
                    value,
                ),
            );

        let op: UnaryOperator | undefined = ExpressionEvaluator.UNARY_OPERATORS_MAP.get(operator);

        if (!op)
            throw new ExpressionEvaluationException(
                this.expression,
                StringFormatter.format(
                    'No Unary operator $ is found to apply on $',
                    operator.getOperator(),
                    value,
                ),
            );

        return new ExpressionTokenValue(operator.toString(), op.apply(value));
    }

    private getValueFromToken(
        valuesMap: Map<string, TokenValueExtractor>,
        token: ExpressionToken,
    ): any {
        if (token instanceof Expression) {
            return this.evaluateExpression(token as Expression, valuesMap);
        } else if (token instanceof ExpressionTokenValue) {
            return (token as ExpressionTokenValue).getElement();
        }
        return this.getValue(token.getExpression(), valuesMap);
    }

    private getValue(path: string, valuesMap: Map<string, TokenValueExtractor>): any {
        if (path.length <= 5) return LiteralTokenValueExtractor.INSTANCE.getValue(path);

        const pathPrefix: string = path.substring(0, path.indexOf('.') + 1);
        return (valuesMap.get(pathPrefix) ?? LiteralTokenValueExtractor.INSTANCE).getValue(path);
    }
}
