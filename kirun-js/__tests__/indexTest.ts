import { HybridRepository } from '../src/index';

class TestRepository {
    static TEST_INDEX: Map<string, string> = new Map<string, string>([
        ['one', 'one'],
        ['one1', 'one1'],
    ]);

    find(namespace: string, name: string): string | undefined {
        return TestRepository.TEST_INDEX.get(name);
    }
}

class TestRepository2 {
    static TEST_INDEX: Map<string, string> = new Map<string, string>([
        ['two', 'two'],
        ['two1', 'two1'],
    ]);

    find(namespace: string, name: string): string | undefined {
        return TestRepository2.TEST_INDEX.get(name);
    }
}

test('Hybrid Repository Test', () => {
    let hybrid: HybridRepository<string> = new HybridRepository<string>(
        new TestRepository(),
        new TestRepository2(),
    );

    expect(hybrid.find('', 'one')).toBe('one');
    expect(hybrid.find('', 'two1')).toBe('two1');
});
