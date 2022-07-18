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
import { ArithmeticAdditionOperator } from './operators/binary/ArithmeticAdditionOperator';
import { ArithmeticDivisionOperator } from './operators/binary/ArithmeticDivisionOperator';
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
import { ArgumentsTokenValueExtractor } from './tokenextractor/ArgumentsTokenValueExtractor';
import { ContextTokenValueExtractor } from './tokenextractor/ContextTokenValueExtractor';
import { LiteralTokenValueExtractor } from './tokenextractor/LiteralTokenValueExtractor';
import { OutputMapTokenValueExtractor } from './tokenextractor/OutputMapTokenValueExtractor';
import { TokenValueExtractor } from './tokenextractor/TokenValueExtractor';

export class ExpressionEvaluator {
    private static readonly UNARY_OPERATORS_MAP: Map<Operation, UnaryOperator> = new Map([
        [Operation.UNARY_BITWISE_COMPLEMENT, new BitwiseComplementOperator()],
        [Operation.UNARY_LOGICAL_NOT, new LogicalNotOperator()],
        [Operation.UNARY_MINUS, new ArithmeticUnaryMinusOperator()],
        [Operation.UNARY_PLUS, new ArithmeticUnaryPlusOperator()],
    ]);

    private static readonly BINARY_OPERATORS_MAP: Map<Operation, BinaryOperator> = new Map([
        [Operation.ADDITION, new ArithmeticAdditionOperator()],
        [Operation.DIVISION, new ArithmeticDivisionOperator()],
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

        [Operation.ARRAY_OPERATOR, new ArrayOperator()],
        [Operation.OBJECT_OPERATOR, new ObjectOperator()],
    ]);

    private static readonly UNARY_OPERATORS_MAP_KEY_SET: Set<Operation> = new Set(
        ExpressionEvaluator.UNARY_OPERATORS_MAP.keys(),
    );

    private expression: string;
    private exp: Expression;

    public constructor(exp: Expression | string) {
        if (exp instanceof Expression) {
            this.exp = exp as Expression;
            this.expression = this.exp.getExpression();
        } else {
            this.expression = exp;
        }
    }

    public evaluate(context: FunctionExecutionParameters): any {
        if (!this.exp) this.exp = new Expression(this.expression);

        let valuesMap: Map<string, TokenValueExtractor> = new Map([
            ['Steps', new OutputMapTokenValueExtractor(context.getOutput()) as TokenValueExtractor],
            [
                'Argum',
                new ArgumentsTokenValueExtractor(context.getArguments()) as TokenValueExtractor,
            ],
            ['Conte', new ContextTokenValueExtractor(context.getContext()) as TokenValueExtractor],
        ]);

        return this.evaluateExpression(this.exp, valuesMap);
    }

    public getExpression(): Expression {
        if (this.exp == null) this.exp = new Expression(this.expression);

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
        operator: Operation,
        token: ExpressionToken,
    ): void {
        const objTokens: LinkedList<ExpressionToken> = new LinkedList();
        const objOperations: LinkedList<Operation> = new LinkedList();

        do {
            objOperations.push(operator);
            if (token instanceof Expression)
                objTokens.push(
                    new ExpressionTokenValue(
                        token.toString(),
                        this.evaluateExpression(token as Expression, valuesMap),
                    ),
                );
            else objTokens.push(token);
            token = tokens.isEmpty() ? null : tokens.pop();
            operator = ops.isEmpty() ? null : ops.pop();
        } while (operator == Operation.OBJECT_OPERATOR || operator == Operation.ARRAY_OPERATOR);

        if (token != null && !token) {
            if (token instanceof Expression)
                objTokens.push(
                    new ExpressionTokenValue(
                        token.toString(),
                        this.evaluateExpression(token as Expression, valuesMap),
                    ),
                );
            else objTokens.push(token);
        }

        if (operator != null && !operator) ops.push(operator);

        let objToken: ExpressionToken = objTokens.pop();
        let sb: StringBuilder = new StringBuilder(
            objToken instanceof ExpressionTokenValue
                ? (objToken as ExpressionTokenValue).getTokenValue().getAsString()
                : objToken.toString(),
        );

        while (!objTokens.isEmpty()) {
            objToken = objTokens.pop();
            operator = objOperations.pop();
            sb.append(operator.getOperator()).append(
                objToken instanceof ExpressionTokenValue
                    ? (objToken as ExpressionTokenValue).getTokenValue().getAsString()
                    : objToken.toString(),
            );
            if (operator == Operation.ARRAY_OPERATOR) sb.append(']');
        }

        let str: string = sb.toString();
        if (str.length > 5 && valuesMap.has(sb.substring(0, 5)))
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

    private applyBinaryOperation(operator: Operation, v1: any, v2: any): ExpressionToken {
        let typv1: string = typeof v1;
        let typv2: string = typeof v2;

        if (typv1 === 'object' || typv2 === 'object' || Array.isArray(v1) || Array.isArray(v2))
            throw new ExpressionEvaluationException(
                this.expression,
                StringFormatter.format(
                    'Cannot evaluate expression $ $ $',
                    v1,
                    operator.getOperator(),
                    v2,
                ),
            );

        let op: BinaryOperator = ExpressionEvaluator.BINARY_OPERATORS_MAP.get(operator);

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

        if (typv === 'object' || Array.isArray(value))
            throw new ExpressionEvaluationException(
                this.expression,
                StringFormatter.format(
                    'The operator $ cannot be applied to $',
                    operator.getOperator(),
                    value,
                ),
            );

        let op: UnaryOperator = ExpressionEvaluator.UNARY_OPERATORS_MAP.get(operator);

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

        return (
            valuesMap.get(path.substring(0, 5)) ?? LiteralTokenValueExtractor.INSTANCE
        ).getValue(path);
    }
}
