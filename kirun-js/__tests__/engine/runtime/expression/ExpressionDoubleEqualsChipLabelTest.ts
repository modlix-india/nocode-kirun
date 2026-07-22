import {
    ExpressionEvaluator,
    KIRunSchemaRepository,
    FunctionExecutionParameters,
    KIRunFunctionRepository,
} from '../../../../src';

// Reproduces the sitezump buyTokens status-filter chip label expression.
//
// Kirun's equality operator is '=' (Operation.EQUAL = new Operation('=')),
// NOT '=='. Writing '==' makes the lexer read two consecutive '=' tokens,
// which leaves a dangling operator and throws "Extra operator ... found."
// This is exactly the error reported on the chip label.

let inMap: Map<string, any> = new Map();
inMap.set('status', 'PAID'); // Page.invoiceQuery.status equivalent (active)
inMap.set('emptyStatus', ''); // Page.invoiceQuery.status when "All" (inactive)
inMap.set('value', 'PAID'); // Parent.value equivalent
inMap.set('label', 'Paid'); // Parent.label equivalent

let output: Map<string, Map<string, Map<string, any>>> = new Map([
    ['step1', new Map([['output', inMap]])],
]);

let parameters: FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository(),
)
    .setArguments(new Map())
    .setSteps(output);

test('Double-equals (==) is not a valid Kirun operator and throws', () => {
    const expr = new ExpressionEvaluator(
        "((Steps.step1.output.status ?? '') == Steps.step1.output.value ? '● ' : '') + Steps.step1.output.label",
    );
    expect(() => expr.evaluate(parameters.getValuesMap())).toThrow();
});

test('Single-equals (=) evaluates the chip label correctly', () => {
    // active chip: status = value -> prefixed with the bullet
    let expr = new ExpressionEvaluator(
        "((Steps.step1.output.status ?? '') = Steps.step1.output.value ? '● ' : '') + Steps.step1.output.label",
    );
    expect(expr.evaluate(parameters.getValuesMap())).toBe('● Paid');

    // inactive chip: emptyStatus (All) != value -> no prefix
    expr = new ExpressionEvaluator(
        "((Steps.step1.output.emptyStatus ?? '') = Steps.step1.output.value ? '● ' : '') + Steps.step1.output.label",
    );
    expect(expr.evaluate(parameters.getValuesMap())).toBe('Paid');
});

// Investigating Kiran's hypothesis: does an expression that ENDS with a string
// literal (like the original chip label that ended in '') need a trailing space?
test('Expression ending in a string literal: trailing space should not matter', () => {
    // exact shape Kiran quoted, ending in '' — no trailing space
    let noTrail = new ExpressionEvaluator(
        "(Steps.step1.output.status ?? '') = Steps.step1.output.value ? '● ' : ''",
    );
    expect(noTrail.evaluate(parameters.getValuesMap())).toBe('● ');

    // same, WITH a trailing space
    let withTrail = new ExpressionEvaluator(
        "(Steps.step1.output.status ?? '') = Steps.step1.output.value ? '● ' : '' ",
    );
    expect(withTrail.evaluate(parameters.getValuesMap())).toBe('● ');
});

test('Bare string literals at end of expression, with/without trailing space', () => {
    expect(new ExpressionEvaluator("'hello'").evaluate(parameters.getValuesMap())).toBe('hello');
    expect(new ExpressionEvaluator("'hello' ").evaluate(parameters.getValuesMap())).toBe('hello');
    expect(new ExpressionEvaluator("''").evaluate(parameters.getValuesMap())).toBe('');
    expect(new ExpressionEvaluator("'' ").evaluate(parameters.getValuesMap())).toBe('');
});

// Kiran's platform example: a {{ }} template that expands to a bare quoted string
// literal followed by trailing spaces. Before the fix this evaluated to undefined,
// which would have made e.g. a FetchData url resolve to undefined.
test('Template expanding to a quoted literal with trailing spaces resolves (not undefined)', () => {
    const expr = new ExpressionEvaluator(
        '"api/ui/personalization/{{Steps.step1.output.label}}/{{Steps.step1.output.value}}"  ',
    );
    expect(expr.evaluate(parameters.getValuesMap())).toBe('api/ui/personalization/Paid/PAID');
});
