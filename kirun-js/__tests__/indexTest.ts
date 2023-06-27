import { HybridRepository, Repository } from '../src/index';

class TestRepository implements Repository<string> {
    static TEST_INDEX: Map<string, string> = new Map<string, string>([
        ['one', 'one'],
        ['one1', 'one1'],
    ]);

    async find(namespace: string, name: string): Promise<string | undefined> {
        return TestRepository.TEST_INDEX.get(name);
    }

    async filter(name: string): Promise<string[]> {
        return Array.from(TestRepository.TEST_INDEX.keys()).filter(
            (e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1,
        );
    }
}

class TestRepository2 implements Repository<string> {
    static TEST_INDEX: Map<string, string> = new Map<string, string>([
        ['two', 'two'],
        ['two1', 'two1'],
    ]);

    async find(namespace: string, name: string): Promise<string | undefined> {
        return TestRepository2.TEST_INDEX.get(name);
    }

    async filter(name: string): Promise<string[]> {
        return Array.from(TestRepository.TEST_INDEX.keys()).filter(
            (e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1,
        );
    }
}

test('Hybrid Repository Test', async () => {
    let hybrid: HybridRepository<string> = new HybridRepository<string>(
        new TestRepository(),
        new TestRepository2(),
    );

    expect(await hybrid.find('', 'one')).toBe('one');
    expect(await hybrid.find('', 'two1')).toBe('two1');
});
