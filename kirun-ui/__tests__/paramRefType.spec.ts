import { normalizeParamRefTypes, paramRefType } from '../src/util/paramRefType';
import { stringValue } from '../src/util/stringValue';

/**
 * A ParameterReference's `type` is a string in kirun-js's ParameterReference.SCHEMA
 * (enums EXPRESSION | VALUE), but a large amount of stored data carries it as a
 * single-element array, `["EXPRESSION"]` — what modlix-mcp and the appbuilder
 * generator tools have always written. The runtime tolerates it because KIRuntime
 * compares with `==`, so those definitions run and reach this editor intact.
 *
 * Comparing with `===` here made an array-typed reference invisible as an
 * expression: ParamEditor drew an empty VALUE box with neither toggle active and
 * would persist `{type: 'VALUE', value: null}` on the next change, stringValue
 * previewed `null` on the graph node, and ExecutionGraphLines drew no dependency
 * line. The appbuilder workspace page's onLoad lost 19 expressions this way on
 * 2026-09-02 (the DSL half of that is fixed in kirun-js 3.15.0).
 */
describe('paramRefType', () => {
    it('passes a scalar type through', () => {
        expect(paramRefType('EXPRESSION')).toBe('EXPRESSION');
        expect(paramRefType('VALUE')).toBe('VALUE');
    });

    it('unwraps a single-element array type', () => {
        expect(paramRefType(['EXPRESSION'])).toBe('EXPRESSION');
        expect(paramRefType(['VALUE'])).toBe('VALUE');
    });

    it('does not guess at anything else', () => {
        expect(paramRefType(undefined)).toBeUndefined();
        expect(paramRefType(null)).toBeUndefined();
        expect(paramRefType([])).toBeUndefined();
        expect(paramRefType('NONSENSE')).toBeUndefined();
        expect(paramRefType(['EXPRESSION', 'VALUE'])).toBe('EXPRESSION');
    });
});

describe('normalizeParamRefTypes, what ParamEditor loads through', () => {
    it('rewrites an array type to the scalar the editor compares against', () => {
        const refs = [
            { key: 'k1', type: ['EXPRESSION'], expression: 'Page.recentNew', value: null, order: 1 },
            { key: 'k2', type: ['VALUE'], expression: null, value: 'Page.x', order: 2 },
        ];

        normalizeParamRefTypes(refs);

        expect(refs.map(r => r.type)).toEqual(['EXPRESSION', 'VALUE']);
    });

    it('keeps the expression intact, which is the whole point', () => {
        const refs = [{ key: 'k1', type: ['EXPRESSION'], expression: 'Page.recentNew', value: null }];

        normalizeParamRefTypes(refs);

        expect(refs[0].expression).toBe('Page.recentNew');
        expect(refs[0].value).toBeNull();
    });

    it('leaves a scalar type alone', () => {
        const refs = [{ key: 'k1', type: 'EXPRESSION', expression: 'A.b' }];

        normalizeParamRefTypes(refs);

        expect(refs[0].type).toBe('EXPRESSION');
    });

    it('does not overwrite a type it cannot recognise', () => {
        const refs: any[] = [{ key: 'k1', type: 'SOMETHING_NEW' }, { key: 'k2' }, null];

        normalizeParamRefTypes(refs);

        expect(refs[0].type).toBe('SOMETHING_NEW');
        expect(refs[1].type).toBeUndefined();
        expect(refs[2]).toBeNull();
    });
});

describe('stringValue reads an array-typed reference as an expression', () => {
    const expressionRef = (type: any) => ({
        k1: { key: 'k1', type, expression: 'Page.recentNew', value: null, order: 1 },
    });

    it('previews the expression, not the null value', () => {
        const out = stringValue(expressionRef(['EXPRESSION']));

        expect(out?.isExpression).toBe(true);
        expect(out?.isValue).toBe(false);
        expect(out?.string).toBe('Page.recentNew');
    });

    it('matches what the scalar form produces', () => {
        expect(stringValue(expressionRef(['EXPRESSION']))).toEqual(
            stringValue(expressionRef('EXPRESSION')),
        );
    });

    it('still reads an array-typed VALUE as a value', () => {
        const out = stringValue({
            k1: { key: 'k1', type: ['VALUE'], value: 'Page.recentNew', expression: null, order: 1 },
        });

        expect(out?.isValue).toBe(true);
        expect(out?.isExpression).toBe(false);
        expect(out?.string).toBe('Page.recentNew');
    });

    it('renders an object value as JSON', () => {
        const out = stringValue({
            k1: { key: 'k1', type: ['VALUE'], value: { order: 1 }, expression: null, order: 1 },
        });

        expect(out?.isValue).toBe(true);
        expect(out?.string).toContain('"order": 1');
    });
});
