import {
    ExpressionEvaluator,
    KIRunSchemaRepository,
    FunctionExecutionParameters,
    KIRunFunctionRepository,
} from '../../../../src';

let obj = {
    number: 20,
    zero: 0,
    booleanTrue: true,
    booleanFalse: false,
    string: 'Hello',
    emptyString: '',
    nullValue: null,
    undefinedValue: undefined,
    emptyObject: {},
    emptyArray: [],
};

let inMap: Map<string, any> = new Map();
inMap.set('name', 'Kiran');
inMap.set('obj', obj);

let output: Map<string, Map<string, Map<string, any>>> = new Map([
    ['step1', new Map([['output', inMap]])],
]);

let parameters: FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository(),
)
    .setArguments(new Map())
    .setSteps(output);

test('Expression Equality Test', () => {
    let exp = new ExpressionEvaluator(
        'Steps.step1.output.obj.number = Steps.step1.output.obj.zero',
    );
    let result = exp.evaluate(parameters.getValuesMap());
    expect(result).toBe(false);

    exp = new ExpressionEvaluator(
        'Steps.step1.output.obj.booleanFalse = (not Steps.step1.output.obj.number)',
    );
    result = exp.evaluate(parameters.getValuesMap());
    expect(result).toBe(true);

    exp = new ExpressionEvaluator(
        'Steps.step1.output.obj.booleanFalse = (not Steps.step1.output.obj.emptyString)',
    );
    result = exp.evaluate(parameters.getValuesMap());
    expect(result).toBe(true);

    exp = new ExpressionEvaluator(
        'Steps.step1.output.obj.booleanTrue = (not Steps.step1.output.obj.zero)',
    );
    result = exp.evaluate(parameters.getValuesMap());
    expect(result).toBe(true);

    exp = new ExpressionEvaluator("Steps.step1.output.obj.emptyString = ''");
    result = exp.evaluate(parameters.getValuesMap());
    expect(result).toBe(true);

    exp = new ExpressionEvaluator("Steps.step1.output.obj.emptyString != ''");
    result = exp.evaluate(parameters.getValuesMap());
    expect(result).toBe(false);

    exp = new ExpressionEvaluator("Steps.step1.output.obj.string != ''");
    result = exp.evaluate(parameters.getValuesMap());
    expect(result).toBe(true);

    exp = new ExpressionEvaluator("Steps.step1.output.obj.string = ''");
    result = exp.evaluate(parameters.getValuesMap());
    expect(result).toBe(false);
});
