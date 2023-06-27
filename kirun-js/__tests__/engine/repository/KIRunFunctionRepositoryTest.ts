import { KIRunFunctionRepository, Namespaces } from '../../../src';

test('KIRunFunctionRepository Test', async () => {
    const repo = new KIRunFunctionRepository();

    let fun = await repo.find(Namespaces.STRING, 'ToString');
    expect(fun).toBeTruthy();
    expect(fun?.getSignature().getName()).toBe('ToString');

    fun = await repo.find(Namespaces.STRING, 'IndexOfWithStartPoint');
    expect(fun).toBeTruthy();
    expect(fun?.getSignature().getName()).toBe('IndexOfWithStartPoint');

    fun = await repo.find(Namespaces.SYSTEM_ARRAY, 'Compare');
    expect(fun).toBeTruthy();
    expect(fun?.getSignature().getName()).toBe('Compare');

    fun = await repo.find(Namespaces.MATH, 'RandomInt');
    expect(fun).toBeTruthy();
    expect(fun?.getSignature().getName()).toBe('RandomInt');

    fun = await repo.find(Namespaces.MATH, 'Exponential');
    expect(fun).toBeTruthy();
    expect(fun?.getSignature().getName()).toBe('Exponential');

    fun = await repo.find(Namespaces.SYSTEM, 'If');
    expect(fun).toBeTruthy();
    expect(fun?.getSignature().getName()).toBe('If');
});
