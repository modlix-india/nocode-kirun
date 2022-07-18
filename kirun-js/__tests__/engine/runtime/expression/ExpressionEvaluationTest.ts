import { ExpressionEvaluator } from '../../../../src/engine/runtime/expression/ExpressionEvaluator';
import { FunctionExecutionParameters } from '../../../../src/engine/runtime/FunctionExecutionParameters';

test('Expression Test', () => {
    let phone = { phone1: '1234', phone2: '5678', phone3: '5678' };

    let address = {
        line1: 'Flat 202, PVR Estates',
        line2: 'Nagvara',
        city: 'Benguluru',
        pin: '560048',
        phone: phone,
    };

    let arr = [10, 20, 30];

    let obj = {
        studentName: 'Kumar',
        math: 20,
        isStudent: true,
        address: address,
        array: arr,
        num: 1,
    };

    let inMap: Map<string, any> = new Map();
    inMap.set('name', 'kiran');
    inMap.set('obj', obj);

    let output: Map<string, Map<string, Map<string, any>>> = new Map([
        ['step1', new Map([['output', inMap]])],
    ]);
    //
    let parameters: FunctionExecutionParameters = new FunctionExecutionParameters()
        .setArguments(new Map())
        .setContext(new Map())
        .setOutput(output);

    expect(new ExpressionEvaluator('3 + 7').evaluate(parameters)).toBe(10);
    expect(new ExpressionEvaluator('"asdf"+333').evaluate(parameters)).toBe('asdf333');
    expect(new ExpressionEvaluator('34 >> 2 = 8 ').evaluate(parameters)).toBe(true);
    expect(new ExpressionEvaluator('10*11+12*13*14/7').evaluate(parameters)).toBe(422);

    expect(
        new ExpressionEvaluator('Steps.step1.output.name1').evaluate(parameters),
    ).toBeUndefined();

    // assertEquals(null, );

    // assertEquals(new JsonPrimitive(true),
    //         new ExpressionEvaluator("\"Kiran\" = Steps.step1.output.name ").evaluate(parameters));

    // assertEquals(new JsonPrimitive(true),
    //         new ExpressionEvaluator("null = Steps.step1.output.name1 ").evaluate(parameters));

    // assertEquals(new JsonPrimitive(true),
    //         new ExpressionEvaluator("Steps.step1.output.obj.phone.phone2 = Steps.step1.output.obj.phone.phone2 ")
    //                 .evaluate(parameters));

    // assertEquals(new JsonPrimitive(true),
    //         new ExpressionEvaluator(
    //                 "Steps.step1.output.obj.address.phone.phone2 != Steps.step1.output.address.obj.phone.phone1 ")
    //                 .evaluate(parameters));

    // assertEquals(new JsonPrimitive(32),
    //         new ExpressionEvaluator("Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+2")
    //                 .evaluate(parameters));

    // assertEquals(new JsonPrimitive(60), new ExpressionEvaluator(
    //         "Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]")
    //         .evaluate(parameters));

    // assertEquals(new JsonPrimitive(60), new ExpressionEvaluator(
    //         "Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]")
    //         .evaluate(parameters));

    // assertEquals(new JsonPrimitive(32),
    //         new ExpressionEvaluator("Steps.step1.output.obj.array[-Steps.step1.output.obj.num + 3]+2")
    //                 .evaluate(parameters));

    // assertEquals(new JsonPrimitive(17.3533f), new ExpressionEvaluator("2.43*4.22+7.0987").evaluate(parameters));
});
