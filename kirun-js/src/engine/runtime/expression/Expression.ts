import { LinkedList } from '../../util/LinkedList';
import { StringBuilder } from '../../util/string/StringBuilder';
import { StringFormatter } from '../../util/string/StringFormatter';
import { StringUtil } from '../../util/string/StringUtil';
import { Tuple2 } from '../../util/Tuples';
import { ExpressionEvaluationException } from './exception/ExpressionEvaluationException';
import { ExpressionToken } from './ExpressionToken';
import { ExpressionTokenValue } from './ExpressionTokenValue';
import { Operation } from './Operation';
import { ExpressionParser } from './ExpressionParser';

export class Expression extends ExpressionToken {
    // Data structure for storing tokens
    private tokens: LinkedList<ExpressionToken> = new LinkedList();
    // Data structure for storing operations
    private ops: LinkedList<Operation> = new LinkedList();
    
    // Cached arrays for fast evaluation (avoids LinkedList traversal)
    private cachedTokensArray?: ExpressionToken[];
    private cachedOpsArray?: Operation[];

    /**
     * Create a ternary expression with condition, trueExpr, and falseExpr.
     * Using push() to maintain compatibility with evaluator (push adds to head).
     * Tokens will be in order: [falseExpr, trueExpr, condition] from head to tail.
     */
    public static createTernary(condition: ExpressionToken, trueExpr: ExpressionToken, falseExpr: ExpressionToken): Expression {
        const expr = new Expression('', undefined, undefined, undefined, true);
        // Push in reverse order so get() returns them correctly
        // push(condition) -> head=condition
        // push(trueExpr) -> head=trueExpr, trueExpr.next=condition  
        // push(falseExpr) -> head=falseExpr, falseExpr.next=trueExpr.next=condition
        // get(0)=falseExpr, get(1)=trueExpr, get(2)=condition
        expr.tokens.push(condition);
        expr.tokens.push(trueExpr);
        expr.tokens.push(falseExpr);
        expr.ops.push(Operation.CONDITIONAL_TERNARY_OPERATOR);
        return expr;
    }

    /**
     * Create a leaf expression (identifier/number) without re-parsing.
     * Used by the parser for leaf nodes.
     */
    public static createLeaf(value: string): Expression {
        const expr = new Expression('', undefined, undefined, undefined, true);
        expr.expression = value;
        // Push a token for the leaf value - the evaluator needs at least one token
        expr.tokens.push(new ExpressionToken(value));
        return expr;
    }

    public constructor(
        expression?: string,
        l?: ExpressionToken,
        r?: ExpressionToken,
        op?: Operation,
        skipParsing?: boolean,
    ) {
        super(expression ? expression : '');
        
        // If skipParsing is true, don't do anything (used by createLeaf/createTernary)
        if (skipParsing) {
            return;
        }
        
        // If we have left/right tokens and operation, construct directly (for AST building)
        // Using push() to maintain compatibility with evaluator (push adds to head).
        // For binary ops: push(left), push(right) -> get(0)=right, get(1)=left
        if (l !== undefined || r !== undefined || op !== undefined) {
            if (op?.getOperator() == '..') {
                if (!l) l = new ExpressionTokenValue('', '');
                else if (!r) r = new ExpressionTokenValue('', '');
            }
            if (l) this.tokens.push(l);
            if (r) this.tokens.push(r);
            if (op) this.ops.push(op);
            // For constructed expressions, don't re-parse
            return;
        }
        
        // If we have an expression string, use the new parser
        if (expression && expression.trim().length > 0) {
            try {
                // Trim leading/trailing whitespace before parsing
                const trimmedExpression = expression.trim();
                const parser = new ExpressionParser(trimmedExpression);
                const parsed = parser.parse();
                // Update the stored expression to the trimmed version
                this.expression = trimmedExpression;
                // Copy tokens and operations from parsed expression
                this.tokens = parsed.getTokens();
                this.ops = parsed.getOperations();
                // Invalidate cache
                this.cachedTokensArray = undefined;
                this.cachedOpsArray = undefined;
            } catch (error) {
                // Fall back to old parser if new one fails (for backward compatibility during migration)
                this.evaluate();
                if (
                    !this.ops.isEmpty() &&
                    this.ops.peekLast().getOperator() == '..' &&
                    this.tokens.length == 1
                ) {
                    this.tokens.push(new ExpressionToken(''));
                }
            }
        }
    }

    public getTokens(): LinkedList<ExpressionToken> {
        return this.tokens;
    }

    public getOperations(): LinkedList<Operation> {
        return this.ops;
    }
    
    // Fast array access for evaluation (cached)
    public getTokensArray(): ExpressionToken[] {
        if (!this.cachedTokensArray) {
            this.cachedTokensArray = this.tokens.toArray();
        }
        return this.cachedTokensArray;
    }
    
    public getOperationsArray(): Operation[] {
        if (!this.cachedOpsArray) {
            this.cachedOpsArray = this.ops.toArray();
        }
        return this.cachedOpsArray;
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
                case "'":
                case '"': {
                    // If we're inside a bracket (ARRAY_OPERATOR was just added),
                    // don't treat quotes as string literals - they're part of the bracket notation
                    if (isPrevOp && this.ops.peek() == Operation.ARRAY_OPERATOR) {
                        let result: Tuple2<number, boolean> = this.process(length, sb, i);
                        i = result.getT1();
                        isPrevOp = result.getT2();
                    } else {
                        let result: Tuple2<number, boolean> = this.processStringLiteral(length, chr, i);
                        i = result.getT1();
                        isPrevOp = result.getT2();
                    }
                    break;
                }
                case '?': {
                    if (
                        i + 1 < length &&
                        this.expression.charAt(i + 1) != '?' &&
                        i != 0 &&
                        this.expression.charAt(i - 1) != '?'
                    ) {
                        i = this.processTernaryOperator(length, sb, buff, i, isPrevOp);
                    } else {
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
                    break;
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

    private processStringLiteral(length: number, chr: string, i: number): Tuple2<number, boolean> {
        let strConstant: string = '';

        let j: number = i + 1;
        for (; j < length; j++) {
            let nextChar = this.expression.charAt(j);

            if (nextChar == chr && this.expression.charAt(j - 1) != '\\') break;

            strConstant += nextChar;
        }

        if (j == length && this.expression.charAt(j - 1) != chr) {
            throw new ExpressionEvaluationException(
                this.expression,
                'Missing string ending marker ' + chr,
            );
        }

        let result = new Tuple2(j, false);

        this.tokens.push(new ExpressionTokenValue(strConstant, strConstant));
        return result;
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
                } else if (op == '..' && this.tokens.isEmpty()) {
                    this.tokens.push(new ExpressionToken('0'));
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

    private processTernaryOperator(
        length: number,
        sb: StringBuilder,
        buff: string,
        i: number,
        isPrevOp: boolean,
    ): number {
        if (isPrevOp) {
            throw new ExpressionEvaluationException(
                this.expression,
                'Ternary operator is followed by an operator',
            );
        }

        if (buff.trim() != '') {
            this.tokens.push(new Expression(buff));
            sb.setLength(0);
        }

        ++i;
        let cnt: number = 1;
        let inChr = '';
        const start = i;
        while (i < length && cnt > 0) {
            inChr = this.expression.charAt(i);
            if (inChr == '?') ++cnt;
            else if (inChr == ':') --cnt;
            ++i;
        }

        if (inChr != ':') {
            throw new ExpressionEvaluationException(this.expression, "':' operater is missing");
        }

        if (i >= length) {
            throw new ExpressionEvaluationException(
                this.expression,
                'Third part of the ternary expression is missing',
            );
        }

        while (
            !this.ops.isEmpty() &&
            this.hasPrecedence(Operation.CONDITIONAL_TERNARY_OPERATOR, this.ops.peek())
        ) {
            let prev: Operation = this.ops.pop();

            if (Operation.UNARY_OPERATORS.has(prev)) {
                const l: ExpressionToken = this.tokens.pop();
                this.tokens.push(new Expression('', l, undefined, prev));
            } else {
                let r = this.tokens.pop();
                let l = this.tokens.pop();

                this.tokens.push(new Expression('', l, r, prev));
            }
        }

        this.ops.push(Operation.CONDITIONAL_TERNARY_OPERATOR);
        this.tokens.push(new Expression(this.expression.substring(start, i - 1)));

        const secondExp: string = this.expression.substring(i);
        if (secondExp.trim() === '') {
            throw new ExpressionEvaluationException(
                this.expression,
                'Third part of the ternary expression is missing',
            );
        }

        this.tokens.push(new Expression(secondExp));

        return length - 1;
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

        // Only remove outer parentheses if they actually match
        while (subExp.length() > 2 && this.hasMatchingOuterParentheses(subExp.toString())) {
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
        // For left-associative operators with same precedence, combine the previous
        // operation before adding the new one. This ensures 5 - 1 - 2 is parsed as
        // (5-1) - 2, not 5 - (1-2).
        // Exception: OBJECT_OPERATOR and ARRAY_OPERATOR should NOT combine -
        // they need to stay flat for path building (e.g., a.b.c).
        if (pre2 === pre1) {
            return op2 !== Operation.OBJECT_OPERATOR &&
                   op2 !== Operation.ARRAY_OPERATOR &&
                   op1 !== Operation.OBJECT_OPERATOR &&
                   op1 !== Operation.ARRAY_OPERATOR;
        }
        return pre2 < pre1;
    }

    private hasMatchingOuterParentheses(str: string): boolean {
        if (str.length < 2 || str.charAt(0) !== '(' || str.charAt(str.length - 1) !== ')') {
            return false;
        }
        // Check if the first '(' matches the last ')'
        // by verifying that the nesting level never drops to 0 before the end
        let level = 0;
        for (let i = 0; i < str.length - 1; i++) {
            const ch = str.charAt(i);
            if (ch === '(') level++;
            else if (ch === ')') level--;
            if (level === 0) return false; // First paren closed before end
        }
        return level === 1; // Should be 1 just before the last ')'
    }

    /**
     * Simple recursive toString() that walks the AST tree.
     * The tree structure is in the tokens LinkedList:
     * - For Expression('', left, right, op): tokens[0] = left, tokens[1] = right
     * - For leaf nodes: just the expression string
     */
    public toString(): string {
        // Leaf node: no operations, just return the token
        if (this.ops.isEmpty()) {
            if (this.tokens.size() == 1) {
                return this.tokenToString(this.tokens.get(0));
            }
            // Handle special case: expression string without parsed structure
            if (this.expression && this.expression.length > 0) {
                return this.expression;
            }
            return 'Error: No tokens';
        }

        // Get the single operation and its operands
        const op = this.ops.get(0);
        
        // Unary operation: (operator operand)
        if (op.getOperator().startsWith('UN: ')) {
            return this.formatUnaryOperation(op);
        }
        
        // Ternary operation: (condition ? trueExpr : falseExpr)
        if (op == Operation.CONDITIONAL_TERNARY_OPERATOR) {
            return this.formatTernaryOperation();
        }
        
        // Binary operation
        return this.formatBinaryOperation(op);
    }

    /**
     * Format a unary operation: (operator operand)
     */
    private formatUnaryOperation(op: Operation): string {
        const operand = this.tokens.get(0);
        const operandStr = this.tokenToString(operand);
        return '(' + op.getOperator().substring(4) + operandStr + ')';
    }

    /**
     * Format a ternary operation: (condition ? trueExpr : falseExpr)
     * Note: With push() order, tokens are [falseExpr, trueExpr, condition]
     */
    private formatTernaryOperation(): string {
        // With push() order: get(0)=falseExpr, get(1)=trueExpr, get(2)=condition
        const falseExpr = this.tokens.get(0);
        const trueExpr = this.tokens.get(1);
        const condition = this.tokens.get(2);
        
        const conditionStr = this.tokenToString(condition);
        const trueStr = this.tokenToString(trueExpr);
        const falseStr = this.tokenToString(falseExpr);
        
        return '(' + conditionStr + '?' + trueStr + ':' + falseStr + ')';
    }

    /**
     * Format a binary operation based on operator type
     * Note: With push() order, tokens are [right, left] (push(left), push(right) -> get(0)=right)
     */
    private formatBinaryOperation(op: Operation): string {
        // Safety check: ensure we have at least 2 tokens for binary operation
        if (this.tokens.size() < 2) {
            // Fall back to expression string if available
            if (this.expression && this.expression.length > 0) {
                return this.expression;
            }
            // Single token case - just return the token
            if (this.tokens.size() === 1) {
                return this.tokenToString(this.tokens.get(0));
            }
            return 'Error: Invalid binary expression';
        }
        
        // With push() order: get(0)=right, get(1)=left
        const right = this.tokens.get(0);
        const left = this.tokens.get(1);
        
        // ARRAY_OPERATOR: left[index] - NO outer parens, brackets are enough
        if (op == Operation.ARRAY_OPERATOR) {
            const leftStr = this.tokenToString(left);
            const indexStr = this.formatArrayIndex(right);
            return leftStr + '[' + indexStr + ']';
        }
        
        // OBJECT_OPERATOR: left.right or left.(right) if right has operations
        if (op == Operation.OBJECT_OPERATOR) {
            const leftStr = this.tokenToString(left);
            const rightStr = this.tokenToString(right);
            
            // Check what operation the right side has
            if (right instanceof Expression) {
                const rightOps = right.getOperations();
                if (!rightOps.isEmpty()) {
                    const rightOp = rightOps.get(0);
                    // ARRAY_OPERATOR doesn't add outer parens, so we need to wrap
                    // OBJECT_OPERATOR and other ops already add parens in their toString()
                    if (rightOp == Operation.ARRAY_OPERATOR) {
                        return '(' + leftStr + '.(' + rightStr + '))';
                    } else {
                        // Other ops (like OBJECT_OPERATOR) already include parens
                        return '(' + leftStr + '.' + rightStr + ')';
                    }
                }
            }
            
            // No operations - simple identifier
            return '(' + leftStr + '.' + rightStr + ')';
        }
        
        // ARRAY_RANGE_INDEX_OPERATOR: (start..end)
        if (op == Operation.ARRAY_RANGE_INDEX_OPERATOR) {
            const leftStr = this.tokenToString(left);
            const rightStr = this.tokenToString(right);
            return '(' + leftStr + '..' + rightStr + ')';
        }
        
        // Other binary operators: (left op right)
        const leftStr = this.tokenToString(left);
        const rightStr = this.tokenToString(right);
        return '(' + leftStr + op.getOperator() + rightStr + ')';
    }

    /**
     * Convert a token to string, handling nested expressions recursively
     */
    private tokenToString(token: ExpressionToken): string {
        if (token instanceof Expression) {
            return token.toString();
        }
        
        // Check if token is an ExpressionTokenValue (use duck typing for minified code)
        if (token && typeof (token as any).getExpression === 'function' && 
            typeof (token as any).getTokenValue === 'function') {
            const originalExpr = (token as any).getExpression();
            // If it's a quoted string, use the original expression with quotes
            if (originalExpr && (originalExpr.startsWith('"') || originalExpr.startsWith("'"))) {
                return originalExpr;
            }
        }
        
        return token.toString();
    }

    /**
     * Format array index, preserving quotes for bracket notation
     */
    private formatArrayIndex(token: ExpressionToken): string {
        if (token instanceof Expression) {
            // Check if this is a simple quoted string
            const expr = token as Expression;
            if (expr.getOperations().isEmpty() && expr.getTokens().size() == 1) {
                const innerToken = expr.getTokens().get(0);
                // Check if innerToken is an ExpressionTokenValue with quoted expression
                if (innerToken && typeof (innerToken as any).getExpression === 'function') {
                    const originalExpr = (innerToken as any).getExpression();
                    if (originalExpr && (originalExpr.startsWith('"') || originalExpr.startsWith("'"))) {
                        return originalExpr;
                    }
                }
            }
            return expr.toString();
        }
        
        // Check if token is an ExpressionTokenValue with quoted expression
        if (token && typeof (token as any).getExpression === 'function') {
            const originalExpr = (token as any).getExpression();
            if (originalExpr && (originalExpr.startsWith('"') || originalExpr.startsWith("'"))) {
                return originalExpr;
            }
        }
        
        return token.toString();
    }

    /**
     * Check if a token is an Expression with operations
     */
    private tokenHasOperations(token: ExpressionToken): boolean {
        if (token instanceof Expression) {
            return !token.getOperations().isEmpty();
        }
        return false;
    }

    public equals(o: Expression): boolean {
        return this.expression == o.expression;
    }
}
