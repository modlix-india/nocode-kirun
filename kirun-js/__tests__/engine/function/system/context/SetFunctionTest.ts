import { Schema } from '../../../../../src';
import { SetFunction } from '../../../../../src/engine/function/system/context/SetFunction';
import { ContextElement } from '../../../../../src/engine/runtime/ContextElement';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

test('Set function test 1', () => {
    let setFunction: SetFunction = new SetFunction();

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    let contextMap: Map<string, ContextElement> = new Map();
    contextMap.set('a', new ContextElement(Schema.ofAny('test'), {}));
    fep.setContext(contextMap);
    fep.setArguments(
        new Map<string, any>([
            ['name', 'Context.a.b'],
            ['value', 20],
        ]),
    );

    setFunction.execute(fep);
    expect(contextMap.get('a')?.getElement()['b']).toBe(20);

    fep.setArguments(
        new Map<string, any>([
            ['name', 'Context.a.c[2].d'],
            ['value', 25],
        ]),
    );

    setFunction.execute(fep);
    expect(contextMap.get('a')?.getElement()['c'][2].d).toBe(25);
});

test('Set function test 2', () => {
    let setFunction: SetFunction = new SetFunction();

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    let contextMap: Map<string, ContextElement> = new Map();
    contextMap.set('a', new ContextElement(Schema.ofAny('test'), []));
    fep.setContext(contextMap);
    fep.setArguments(
        new Map<string, any>([
            ['name', 'Context.a[1]'],
            ['value', 240],
        ]),
    );

    setFunction.execute(fep);
    expect(contextMap.get('a')?.getElement()[1]).toBe(240);
});

test('Set function test 3', () => {
    let setFunction: SetFunction = new SetFunction();

    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    let contextMap: Map<string, ContextElement> = new Map();
    contextMap.set('a', new ContextElement(Schema.ofAny('test'), {}));
    fep.setContext(contextMap);
    fep.setArguments(
        new Map<string, any>([
            ['name', 'Context.a.b[1]'],
            ['value', 240],
        ]),
    );

    setFunction.execute(fep);
    expect(contextMap.get('a')?.getElement().b[1]).toBe(240);
});
