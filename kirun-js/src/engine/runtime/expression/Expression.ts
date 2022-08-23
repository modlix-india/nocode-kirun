import { LinkedList } from '../../util/LinkedList';
import { StringBuilder } from '../../util/string/StringBuilder';
import { StringFormatter } from '../../util/string/StringFormatter';
import { StringUtil } from '../../util/string/StringUtil';
import { Tuple2 } from '../../util/Tuples';
import { ExpressionEvaluationException } from './exception/ExpressionEvaluationException';
import { ExpressionToken } from './ExpressionToken';
import { Operation } from './Operation';

export class Expression extends ExpressionToken {
    // Data structure for storing tokens
    private tokens: LinkedList<ExpressionToken> = new LinkedList();
    // Data structure for storing operations
    private ops: LinkedList<Operation> = new LinkedList();

    public constructor(
        expression?: string,
        l?: ExpressionToken,
        r?: ExpressionToken,
        op?: Operation,
    ) {
        super(expression ? expression : '');
        if (l) this.tokens.push(l);
        if (r) this.tokens.push(r);
        if (op) this.ops.push(op);
        this.evaluate();
    }

    public getTokens(): LinkedList<ExpressionToken> {
        return this.tokens;
    }

    public getOperations(): LinkedList<Operation> {
        return this.ops;
    }

    private evaluate(): void {
        const length: number = this.expression.length;
        let chr: string = '';

        let sb: StringBuilder = new StringBuilder('');
        let buff: string | undefined = undefined;
        let i: number = 0;
        let isPrevOp: boolean = false;

        while (i < length) {
            chr = this.expression[i];
            buff = sb.toString();

            switch (chr) {
                case ' ': {
                    isPrevOp = this.processTokenSepearator(sb, buff, isPrevOp);
                    break;
                }
                case '(': {
                    i = this.processSubExpression(length, sb, buff, i, isPrevOp);
                    isPrevOp = false;
                    break;
                }
                case ')': {
                    throw new ExpressionEvaluationException(
                        this.expression,
                        'Extra closing parenthesis found',
                    );
                }
                case ']': {
                    throw new ExpressionEvaluationException(
                        this.expression,
                        'Extra closing square bracket found',
                    );
                }
                default:
                    let result: Tuple2<number, boolean> = this.processOthers(
                        chr,
                        length,
                        sb,
                        buff,
                        i,
                        isPrevOp,
                    );
                    i = result.getT1();
                    isPrevOp = result.getT2();
                    if (isPrevOp && this.ops.peek() == Operation.ARRAY_OPERATOR) {
                        result = this.process(length, sb, i);
                        i = result.getT1();
                        isPrevOp = result.getT2();
                    }
            }

            ++i;
        }

        buff = sb.toString();
        if (!StringUtil.isNullOrBlank(buff)) {
            if (Operation.OPERATORS.has(buff)) {
                throw new ExpressionEvaluationException(
                    this.expression,
                    'Expression is ending with an operator',
                );
            } else {
                this.tokens.push(new ExpressionToken(buff));
            }
        }
    }

    private process(length: number, sb: StringBuilder, i: number): Tuple2<number, boolean> {
        let cnt: number = 1;
        ++i;
        while (i < length && cnt != 0) {
            let c: string = this.expression.charAt(i);
            if (c == ']') --cnt;
            else if (c == '[') ++cnt;
            if (cnt != 0) {
                sb.append(c);
                i++;
            }
        }
        this.tokens.push(new Expression(sb.toString()));
        sb.setLength(0);

        return new Tuple2(i, false);
    }

    private processOthers(
        chr: string,
        length: number,
        sb: StringBuilder,
        buff: string,
        i: number,
        isPrevOp: boolean,
    ): Tuple2<number, boolean> {
        let start: number = length - i;
        start = start < Operation.BIGGEST_OPERATOR_SIZE ? start : Operation.BIGGEST_OPERATOR_SIZE;

        for (let size = start; size > 0; size--) {
            let op: string = this.expression.substring(i, i + size);
            if (Operation.OPERATORS_WITHOUT_SPACE_WRAP.has(op)) {
                if (!StringUtil.isNullOrBlank(buff)) {
                    this.tokens.push(new ExpressionToken(buff));
                    isPrevOp = false;
                }
                this.checkUnaryOperator(
                    this.tokens,
                    this.ops,
                    Operation.OPERATION_VALUE_OF.get(op),
                    isPrevOp,
                );
                isPrevOp = true;
                sb.setLength(0);
                return new Tuple2(i + size - 1, isPrevOp);
            }
        }

        sb.append(chr);
        return new Tuple2(i, false);
    }

    private processSubExpression(
        length: number,
        sb: StringBuilder,
        buff: string,
        i: number,
        isPrevOp: boolean,
    ): number {
        if (Operation.OPERATORS.has(buff)) {
            this.checkUnaryOperator(
                this.tokens,
                this.ops,
                Operation.OPERATION_VALUE_OF.get(buff),
                isPrevOp,
            );
            sb.setLength(0);
        } else if (!StringUtil.isNullOrBlank(buff)) {
            throw new ExpressionEvaluationException(
                this.expression,
                StringFormatter.format('Unkown token : $ found.', buff),
            );
        }

        let cnt: number = 1;
        const subExp: StringBuilder = new StringBuilder();
        let inChr: string = this.expression.charAt(i);
        i++;
        while (i < length && cnt > 0) {
            inChr = this.expression.charAt(i);
            if (inChr == '(') cnt++;
            else if (inChr == ')') cnt--;
            if (cnt != 0) {
                subExp.append(inChr);
                i++;
            }
        }

        if (inChr != ')')
            throw new ExpressionEvaluationException(
                this.expression,
                'Missing a closed parenthesis',
            );

        while (
            subExp.length() > 2 &&
            subExp.charAt(0) == '(' &&
            subExp.charAt(subExp.length() - 1) == ')'
        ) {
            subExp.deleteCharAt(0);
            subExp.setLength(subExp.length() - 1);
        }

        this.tokens.push(new Expression(subExp.toString().trim()));
        return i;
    }

    private processTokenSepearator(sb: StringBuilder, buff: string, isPrevOp: boolean): boolean {
        if (!StringUtil.isNullOrBlank(buff)) {
            if (Operation.OPERATORS.has(buff)) {
                this.checkUnaryOperator(
                    this.tokens,
                    this.ops,
                    Operation.OPERATION_VALUE_OF.get(buff),
                    isPrevOp,
                );
                isPrevOp = true;
            } else {
                this.tokens.push(new ExpressionToken(buff));
                isPrevOp = false;
            }
        }
        sb.setLength(0);

        return isPrevOp;
    }

    private checkUnaryOperator(
        tokens: LinkedList<ExpressionToken>,
        ops: LinkedList<Operation>,
        op: Operation | undefined,
        isPrevOp: boolean,
    ): void {
        if (!op) return;
        if (isPrevOp || tokens.isEmpty()) {
            if (Operation.UNARY_OPERATORS.has(op)) {
                const x = Operation.UNARY_MAP.get(op);
                if (x) ops.push(x);
            } else
                throw new ExpressionEvaluationException(
                    this.expression,
                    StringFormatter.format('Extra operator $ found.', op),
                );
        } else {
            while (!ops.isEmpty() && this.hasPrecedence(op, ops.peek())) {
                let prev: Operation = ops.pop();

                if (Operation.UNARY_OPERATORS.has(prev)) {
                    let l: ExpressionToken = tokens.pop();
                    tokens.push(new Expression('', l, undefined, prev));
                } else {
                    let r: ExpressionToken = tokens.pop();
                    let l: ExpressionToken = tokens.pop();

                    tokens.push(new Expression('', l, r, prev));
                }
            }
            ops.push(op);
        }
    }

    private hasPrecedence(op1: Operation, op2: Operation): boolean {
        let pre1: number | undefined = Operation.OPERATOR_PRIORITY.get(op1);
        let pre2: number | undefined = Operation.OPERATOR_PRIORITY.get(op2);
        if (!pre1 || !pre2) {
            throw new Error('Unknown operators provided');
        }
        return pre2 < pre1;
    }

    public toString(): string {
        if (this.ops.isEmpty()) {
            if (this.tokens.size() == 1) return this.tokens.get(0).toString();
            return 'Error: No tokens';
        }

        let sb: StringBuilder = new StringBuilder();
        let ind: number = 0;

        const ops: Operation[] = this.ops.toArray();
        const tokens: ExpressionToken[] = this.tokens.toArray();

        for (let i = 0; i < ops.length; i++) {
            if (ops[i].getOperator().startsWith('UN: ')) {
                sb.append('(')
                    .append(ops[i].getOperator().substring(4))
                    .append(
                        tokens[ind] instanceof Expression
                            ? (tokens[ind] as Expression).toString()
                            : tokens[ind],
                    )
                    .append(')');
                ind++;
            } else {
                if (ind == 0) {
                    const temp: ExpressionToken = tokens[ind++];
                    sb.insert(
                        0,
                        temp instanceof Expression
                            ? (temp as Expression).toString()
                            : temp.toString(),
                    );
                }
                const temp: ExpressionToken = tokens[ind++];
                sb.insert(0, ops[i].getOperator())
                    .insert(
                        0,
                        temp instanceof Expression
                            ? (temp as Expression).toString()
                            : temp.toString(),
                    )
                    .insert(0, '(')
                    .append(')');
            }
        }

        return sb.toString();
    }

    public equals(o: Expression): boolean {
        return this.expression == o.expression;
    }
}
