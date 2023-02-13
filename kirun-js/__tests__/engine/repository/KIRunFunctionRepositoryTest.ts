import { KIRunFunctionRepository, Namespaces } from '../../../src';

test('KIRunFunctionRepository Test', () => {
    const repo = new KIRunFunctionRepository();

    let fun = repo.find(Namespaces.STRING, 'ToString');
    expect(fun).toBeTruthy();
    expect(fun?.getSignature().getName()).toBe('ToString');

    fun = repo.find(Namespaces.STRING, 'IndexOfWithStartPoint');
    expect(fun).toBeTruthy();
    expect(fun?.getSignature().getName()).toBe('IndexOfWithStartPoint');

    fun = repo.find(Namespaces.SYSTEM_ARRAY, 'Compare');
    expect(fun).toBeTruthy();
    expect(fun?.getSignature().getName()).toBe('Compare');

    fun = repo.find(Namespaces.MATH, 'RandomInt');
    expect(fun).toBeTruthy();
    expect(fun?.getSignature().getName()).toBe('RandomInt');

    fun = repo.find(Namespaces.MATH, 'Exponential');
    expect(fun).toBeTruthy();
    expect(fun?.getSignature().getName()).toBe('Exponential');

    fun = repo.find(Namespaces.SYSTEM, 'If');
    expect(fun).toBeTruthy();
    expect(fun?.getSignature().getName()).toBe('If');
});
