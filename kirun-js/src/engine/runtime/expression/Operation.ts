export class Operation {
    public static readonly MULTIPLICATION: Operation = new Operation('*');
    public static readonly DIVISION: Operation = new Operation('/');
    public static readonly INTEGER_DIVISION: Operation = new Operation('//');
    public static readonly MOD: Operation = new Operation('%');
    public static readonly ADDITION: Operation = new Operation('+');
    public static readonly SUBTRACTION: Operation = new Operation('-');

    public static readonly NOT: Operation = new Operation('not', undefined, true);
    public static readonly AND: Operation = new Operation('and', undefined, true);
    public static readonly OR: Operation = new Operation('or', undefined, true);
    public static readonly LESS_THAN: Operation = new Operation('<');
    public static readonly LESS_THAN_EQUAL: Operation = new Operation('<=');
    public static readonly GREATER_THAN: Operation = new Operation('>');
    public static readonly GREATER_THAN_EQUAL: Operation = new Operation('>=');
    public static readonly EQUAL: Operation = new Operation('=');
    public static readonly NOT_EQUAL: Operation = new Operation('!=');

    public static readonly BITWISE_AND: Operation = new Operation('&');
    public static readonly BITWISE_OR: Operation = new Operation('|');
    public static readonly BITWISE_XOR: Operation = new Operation('^');
    public static readonly BITWISE_COMPLEMENT: Operation = new Operation('~');
    public static readonly BITWISE_LEFT_SHIFT: Operation = new Operation('<<');
    public static readonly BITWISE_RIGHT_SHIFT: Operation = new Operation('>>');
    public static readonly BITWISE_UNSIGNED_RIGHT_SHIFT: Operation = new Operation('>>>');

    public static readonly UNARY_PLUS: Operation = new Operation('UN: +', '+');
    public static readonly UNARY_MINUS: Operation = new Operation('UN: -', '-');
    public static readonly UNARY_LOGICAL_NOT: Operation = new Operation('UN: not', 'not');
    public static readonly UNARY_BITWISE_COMPLEMENT: Operation = new Operation('UN: ~', '~');

    public static readonly ARRAY_OPERATOR: Operation = new Operation('[');
    public static readonly ARRAY_RANGE_INDEX_OPERATOR: Operation = new Operation('..');
    public static readonly OBJECT_OPERATOR: Operation = new Operation('.');

    public static readonly NULLISH_COALESCING_OPERATOR: Operation = new Operation('??');

    public static readonly CONDITIONAL_TERNARY_OPERATOR: Operation = new Operation('?');

    private static readonly VALUE_OF: Map<string, Operation> = new Map([
        ['MULTIPLICATION', Operation.MULTIPLICATION],
        ['DIVISION', Operation.DIVISION],
        ['INTEGER_DIVISION', Operation.INTEGER_DIVISION],
        ['MOD', Operation.MOD],
        ['ADDITION', Operation.ADDITION],
        ['SUBTRACTION', Operation.SUBTRACTION],
        ['NOT', Operation.NOT],
        ['AND', Operation.AND],
        ['OR', Operation.OR],
        ['LESS_THAN', Operation.LESS_THAN],
        ['LESS_THAN_EQUAL', Operation.LESS_THAN_EQUAL],
        ['GREATER_THAN', Operation.GREATER_THAN],
        ['GREATER_THAN_EQUAL', Operation.GREATER_THAN_EQUAL],
        ['EQUAL', Operation.EQUAL],
        ['NOT_EQUAL', Operation.NOT_EQUAL],
        ['BITWISE_AND', Operation.BITWISE_AND],
        ['BITWISE_OR', Operation.BITWISE_OR],
        ['BITWISE_XOR', Operation.BITWISE_XOR],
        ['BITWISE_COMPLEMENT', Operation.BITWISE_COMPLEMENT],
        ['BITWISE_LEFT_SHIFT', Operation.BITWISE_LEFT_SHIFT],
        ['BITWISE_RIGHT_SHIFT', Operation.BITWISE_RIGHT_SHIFT],
        ['BITWISE_UNSIGNED_RIGHT_SHIFT', Operation.BITWISE_UNSIGNED_RIGHT_SHIFT],
        ['UNARY_PLUS', Operation.UNARY_PLUS],
        ['UNARY_MINUS', Operation.UNARY_MINUS],
        ['UNARY_LOGICAL_NOT', Operation.UNARY_LOGICAL_NOT],
        ['UNARY_BITWISE_COMPLEMENT', Operation.UNARY_BITWISE_COMPLEMENT],
        ['ARRAY_OPERATOR', Operation.ARRAY_OPERATOR],
        ['ARRAY_RANGE_INDEX_OPERATOR', Operation.ARRAY_RANGE_INDEX_OPERATOR],
        ['OBJECT_OPERATOR', Operation.OBJECT_OPERATOR],
        ['NULLISH_COALESCING_OPERATOR', Operation.NULLISH_COALESCING_OPERATOR],
        ['CONDITIONAL_TERNARY_OPERATOR', Operation.CONDITIONAL_TERNARY_OPERATOR],
    ]);

    public static readonly UNARY_OPERATORS: Set<Operation> = new Set([
        Operation.ADDITION,
        Operation.SUBTRACTION,
        Operation.NOT,
        Operation.BITWISE_COMPLEMENT,
        Operation.UNARY_PLUS,
        Operation.UNARY_MINUS,
        Operation.UNARY_LOGICAL_NOT,
        Operation.UNARY_BITWISE_COMPLEMENT,
    ]);

    public static readonly ARITHMETIC_OPERATORS: Set<Operation> = new Set([
        Operation.MULTIPLICATION,
        Operation.DIVISION,
        Operation.INTEGER_DIVISION,
        Operation.MOD,
        Operation.ADDITION,
        Operation.SUBTRACTION,
    ]);

    public static readonly LOGICAL_OPERATORS: Set<Operation> = new Set([
        Operation.NOT,
        Operation.AND,
        Operation.OR,
        Operation.LESS_THAN,
        Operation.LESS_THAN_EQUAL,
        Operation.GREATER_THAN,
        Operation.GREATER_THAN_EQUAL,
        Operation.EQUAL,
        Operation.NOT_EQUAL,
        Operation.NULLISH_COALESCING_OPERATOR,
    ]);

    public static readonly BITWISE_OPERATORS: Set<Operation> = new Set([
        Operation.BITWISE_AND,
        Operation.BITWISE_COMPLEMENT,
        Operation.BITWISE_LEFT_SHIFT,
        Operation.BITWISE_OR,
        Operation.BITWISE_RIGHT_SHIFT,
        Operation.BITWISE_UNSIGNED_RIGHT_SHIFT,
        Operation.BITWISE_XOR,
    ]);

    public static readonly CONDITIONAL_OPERATORS: Set<Operation> = new Set([
        Operation.CONDITIONAL_TERNARY_OPERATOR,
    ]);

    public static readonly OPERATOR_PRIORITY: Map<Operation, number> = new Map([
        [Operation.UNARY_PLUS, 1],
        [Operation.UNARY_MINUS, 1],
        [Operation.UNARY_LOGICAL_NOT, 1],
        [Operation.UNARY_BITWISE_COMPLEMENT, 1],
        [Operation.ARRAY_OPERATOR, 1],
        [Operation.OBJECT_OPERATOR, 1],
        [Operation.ARRAY_RANGE_INDEX_OPERATOR, 2],
        [Operation.MULTIPLICATION, 2],
        [Operation.DIVISION, 2],
        [Operation.INTEGER_DIVISION, 2],
        [Operation.MOD, 2],
        [Operation.ADDITION, 3],
        [Operation.SUBTRACTION, 3],
        [Operation.BITWISE_LEFT_SHIFT, 4],
        [Operation.BITWISE_RIGHT_SHIFT, 4],
        [Operation.BITWISE_UNSIGNED_RIGHT_SHIFT, 4],
        [Operation.LESS_THAN, 5],
        [Operation.LESS_THAN_EQUAL, 5],
        [Operation.GREATER_THAN, 5],
        [Operation.GREATER_THAN_EQUAL, 5],
        [Operation.EQUAL, 6],
        [Operation.NOT_EQUAL, 6],
        [Operation.BITWISE_AND, 7],
        [Operation.BITWISE_XOR, 8],
        [Operation.BITWISE_OR, 9],
        [Operation.AND, 10],
        [Operation.OR, 11],
        [Operation.NULLISH_COALESCING_OPERATOR, 11],
        [Operation.CONDITIONAL_TERNARY_OPERATOR, 12],
    ]);

    public static readonly OPERATORS: Set<string> = new Set(
        [
            ...Array.from(Operation.ARITHMETIC_OPERATORS),
            ...Array.from(Operation.LOGICAL_OPERATORS),
            ...Array.from(Operation.BITWISE_OPERATORS),
            Operation.ARRAY_OPERATOR,
            Operation.ARRAY_RANGE_INDEX_OPERATOR,
            Operation.OBJECT_OPERATOR,
            ...Array.from(Operation.CONDITIONAL_OPERATORS),
        ].map((e) => e.getOperator()),
    );

    public static readonly OPERATORS_WITHOUT_SPACE_WRAP: Set<string> = new Set(
        [
            ...Array.from(Operation.ARITHMETIC_OPERATORS),
            ...Array.from(Operation.LOGICAL_OPERATORS),
            ...Array.from(Operation.BITWISE_OPERATORS),
            Operation.ARRAY_OPERATOR,
            Operation.ARRAY_RANGE_INDEX_OPERATOR,
            Operation.OBJECT_OPERATOR,
            ...Array.from(Operation.CONDITIONAL_OPERATORS),
        ]
            .filter((e) => !e.shouldBeWrappedInSpace())
            .map((e) => e.getOperator()),
    );

    public static readonly OPERATION_VALUE_OF: Map<string, Operation> = new Map(
        Array.from(Operation.VALUE_OF.entries()).map(([_, o]) => [o.getOperator(), o]),
    );

    public static readonly UNARY_MAP: Map<Operation, Operation> = new Map([
        [Operation.ADDITION, Operation.UNARY_PLUS],
        [Operation.SUBTRACTION, Operation.UNARY_MINUS],
        [Operation.NOT, Operation.UNARY_LOGICAL_NOT],
        [Operation.BITWISE_COMPLEMENT, Operation.UNARY_BITWISE_COMPLEMENT],
        [Operation.UNARY_PLUS, Operation.UNARY_PLUS],
        [Operation.UNARY_MINUS, Operation.UNARY_MINUS],
        [Operation.UNARY_LOGICAL_NOT, Operation.UNARY_LOGICAL_NOT],
        [Operation.UNARY_BITWISE_COMPLEMENT, Operation.UNARY_BITWISE_COMPLEMENT],
    ]);

    public static readonly BIGGEST_OPERATOR_SIZE: number = Array.from(Operation.VALUE_OF.values())
        .map((e) => e.getOperator())
        .filter((e) => !e.startsWith('UN: '))
        .map((e) => e.length)
        .reduce((a, c) => (a > c ? a : c), 0);

    private readonly operator: string;
    private readonly operatorName: string;
    private readonly _shouldBeWrappedInSpace: boolean;

    public constructor(
        operator: string,
        operatorName?: string,
        shouldBeWrappedInSpace: boolean = false,
    ) {
        this.operator = operator;
        this.operatorName = operatorName ?? operator;
        this._shouldBeWrappedInSpace = shouldBeWrappedInSpace;
    }

    public getOperator(): string {
        return this.operator;
    }

    public getOperatorName(): string {
        return this.operatorName;
    }

    public shouldBeWrappedInSpace(): boolean {
        return this._shouldBeWrappedInSpace;
    }

    public valueOf(str: string): Operation | undefined {
        return Operation.VALUE_OF.get(str);
    }

    public toString(): string {
        return this.operator;
    }
}
