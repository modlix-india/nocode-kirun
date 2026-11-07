import { TokenValueExtractor } from '../../../../src';
import { ExpressionEvaluator } from '../../../../src/engine/runtime/expression/ExpressionEvaluator';

// `??` used to parse its result again whenever it held an operator character, real data included: a date
// '2026-11-20' came out as 1995 and a time '10:00' or a phone '+91...' changed (Event tracker, 2026-11-06).
// Now only a stored expression (a field named `expression`, a page title's location.expression) is evaluated again.

class StoreExtractor extends TokenValueExtractor {
    constructor(private readonly prefix: string, private readonly store: any) {
        super();
    }

    protected getValueInternal(token: string): any {
        return this.retrieveElementFrom(token, token.split('.'), 1, this.store);
    }

    public getPrefix(): string {
        return this.prefix;
    }

    public getStore(): any {
        return this.store;
    }
}

const run = (expression: string, store: any): any => {
    const ex = new StoreExtractor('Store.', store);
    return new ExpressionEvaluator(expression).evaluate(new Map([[ex.getPrefix(), ex]]));
};

describe('?? returns data as it is', () => {
    test.each([
        ['2026-11-20'],
        ['27/11/2026'],
        ['10:00'],
        ['+919876543210'],
        ['INV/2026-27/1/3'],
        ['a-b'],
        ['Store.other'],
        ['x = y ? 1 : 2'],
    ])('left side %s', (value) => {
        expect(run("Store.v ?? ''", { v: value, other: 'WRONG' })).toBe(value);
    });

    test('a fallback path holding data', () => {
        expect(run('Store.missing ?? Store.v', { v: '2026-11-20' })).toBe('2026-11-20');
    });

    test('a fallback literal with operator characters', () => {
        expect(run("Store.missing ?? '-'", {})).toBe('-');
        expect(run("Store.missing ?? 'n/a'", {})).toBe('n/a');
    });

    test('inside a larger expression', () => {
        expect(run("'On ' + (Store.d ?? '')", { d: '2026-11-20' })).toBe('On 2026-11-20');
    });
});

describe('?? still evaluates a stored expression', () => {
    const page = (name: any) => ({
        application: { properties: { title: 'My App' } },
        def: { name: 'x' },
        urlDetails: { pageName: 'home' },
        pageDefinition: { home: { properties: { title: { name } } } },
    });
    const STORED = `'Profile '+(Store.def.name ? "Edit" : "Add")`;

    // The AppBuilder app shell's title: the stored expression inside quotes, evaluated by the inner ??.
    const QUOTED =
        "Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.value ?? " +
        "'{{Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.location.expression ?? \"\"}}'";
    // The other app shells' title: the stored expression pasted in unquoted.
    const UNQUOTED =
        'Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.value ?? ' +
        '{{Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.location.expression}}';

    test('quoted form, title from the stored expression', () => {
        expect(run(QUOTED, page({ location: { expression: STORED } }))).toBe('Profile Edit');
    });

    test('quoted form, a plain title with a dash stays as it is', () => {
        expect(run(QUOTED, page({ value: 'App Builder - Build Applications in Minutes' }))).toBe(
            'App Builder - Build Applications in Minutes',
        );
    });

    test('unquoted form, title from the stored expression', () => {
        expect(
            run(UNQUOTED, page({ location: { expression: "'Application : ' + Store.application.properties.title" } })),
        ).toBe('Application : My App');
    });

    test('a stored expression read directly', () => {
        expect(run("Store.t.location.expression ?? ''", { t: { location: { expression: "'A' + 'B'" } } })).toBe('AB');
    });
});
