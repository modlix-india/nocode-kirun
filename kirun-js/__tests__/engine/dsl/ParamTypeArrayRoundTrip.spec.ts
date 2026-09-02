import { readFileSync } from 'fs';
import { join } from 'path';
import { DSLCompiler } from '../../../src/engine/dsl/DSLCompiler';

/**
 * A ParameterReference's `type` is a string in ParameterReference.SCHEMA (enums
 * EXPRESSION | VALUE), but a large amount of stored data carries it as a
 * single-element ARRAY, `["EXPRESSION"]` — that is what modlix-mcp and the
 * appbuilder generator tools have always written. The runtime does not care:
 * KIRuntime compares with `==`, and `['EXPRESSION'] == 'EXPRESSION'` is true in
 * JS, so those functions run correctly.
 *
 * `JSONToText.paramRefToText` compared with `===`. An array-typed reference
 * therefore missed the EXPRESSION branch, fell through to the value branch and
 * was emitted as its `value` — which for an expression ref is `null`. Compiling
 * that text back gives `{type: 'VALUE', value: null}`: the expression is gone.
 *
 * That is a DATA LOSS on a round trip that reads as a formatting operation.
 * Opening the appbuilder `workspace` page's onLoad in the DSL editor and toggling
 * to the graph editor and back destroyed 19 of its 20 expressions on 2026-09-02.
 * The one survivor is the control in the fixture below: `setApp.value` is the
 * only ref in that function whose `type` was already a plain string.
 */
describe('DSL round trip preserves array-typed ParameterReference.type', () => {
    type Ref = { key: string; type: any; expression?: string; value?: any; order?: number };

    /** (step, param, order) -> ref. Never key on the ref's own key: a round trip regenerates it. */
    const refsByPosition = (fn: any): Map<string, Ref> => {
        const out = new Map<string, Ref>();
        for (const [stepName, step] of Object.entries<any>(fn.steps ?? {})) {
            for (const [paramName, refs] of Object.entries<any>(step.parameterMap ?? {})) {
                for (const ref of Object.values<any>(refs ?? {})) {
                    out.set(`${stepName}.${paramName}#${ref.order ?? 1}`, ref);
                }
            }
        }
        return out;
    };

    const isExpression = (ref: Ref): boolean =>
        Array.isArray(ref.type) ? ref.type[0] === 'EXPRESSION' : ref.type === 'EXPRESSION';

    const roundTrip = async (fn: any): Promise<any> =>
        DSLCompiler.compile(await DSLCompiler.decompile(fn));

    it('keeps the expression on a single array-typed reference', async () => {
        const fn = {
            name: 'arrayTyped',
            steps: {
                fetch: {
                    statementName: 'fetch',
                    namespace: 'UIEngine',
                    name: 'FetchData',
                    parameterMap: {
                        url: {
                            k1: {
                                key: 'k1',
                                type: ['EXPRESSION'],
                                expression:
                                    "'/api/security/applications/appCode/' + Url.pathParts[1]",
                                value: null,
                                order: 1,
                            },
                        },
                    },
                },
            },
        };

        const out = await roundTrip(fn);
        const ref = refsByPosition(out).get('fetch.url#1')!;

        expect(ref).toBeDefined();
        expect(isExpression(ref)).toBe(true);
        expect(ref.expression).toBe("'/api/security/applications/appCode/' + Url.pathParts[1]");
    });

    it('keeps an array-typed VALUE reference', async () => {
        const fn = {
            name: 'arrayTypedValue',
            steps: {
                seed: {
                    statementName: 'seed',
                    namespace: 'UIEngine',
                    name: 'SetStore',
                    parameterMap: {
                        path: {
                            k1: {
                                key: 'k1',
                                type: ['VALUE'],
                                value: 'Page.recentNew',
                                expression: null,
                                order: 1,
                            },
                        },
                        value: {
                            k2: {
                                key: 'k2',
                                type: ['VALUE'],
                                value: [],
                                expression: null,
                                order: 1,
                            },
                        },
                    },
                },
            },
        };

        const out = await roundTrip(fn);
        const refs = refsByPosition(out);

        expect(refs.get('seed.path#1')!.value).toBe('Page.recentNew');
        expect(refs.get('seed.value#1')!.value).toEqual([]);
    });

    describe("the appbuilder workspace page's onLoad", () => {
        const onLoad = JSON.parse(readFileSync(join(__dirname, 'workspaceOnLoad.json'), 'utf-8'));

        it('is the shape this bug needs: 42 steps, expressions typed as arrays', () => {
            const refs = [...refsByPosition(onLoad).values()];
            expect(Object.keys(onLoad.steps)).toHaveLength(42);
            expect(refs.filter((r) => Array.isArray(r.type))).toHaveLength(75);
            // 19 array-typed expressions plus setApp.value, whose type is already
            // a plain string. That one is the control: it survived in production.
            expect(refs.filter(isExpression)).toHaveLength(20);
        });

        it('loses no expression through decompile -> compile', async () => {
            const before = refsByPosition(onLoad);
            const after = refsByPosition(await roundTrip(onLoad));

            const lost: string[] = [];
            for (const [position, ref] of before) {
                if (!isExpression(ref) || !ref.expression) continue;
                const now = after.get(position);
                if (!now || !isExpression(now) || now.expression !== ref.expression) {
                    lost.push(`${position} (${ref.expression})`);
                }
            }

            expect(lost).toEqual([]);
        });

        it('turns no expression into a null VALUE, which is how the loss presented', async () => {
            const before = refsByPosition(onLoad);
            const after = refsByPosition(await roundTrip(onLoad));

            const emptied = [...before.entries()]
                .filter(([position, ref]) => {
                    const now = after.get(position);
                    return (
                        isExpression(ref) &&
                        !!now &&
                        !isExpression(now) &&
                        now.value === null &&
                        !now.expression
                    );
                })
                .map(([position]) => position);

            expect(emptied).toEqual([]);
        });

        it('keeps every VALUE parameter as well', async () => {
            const before = refsByPosition(onLoad);
            const after = refsByPosition(await roundTrip(onLoad));

            const changed: string[] = [];
            for (const [position, ref] of before) {
                if (isExpression(ref)) continue;
                const now = after.get(position);
                if (!now || JSON.stringify(now.value) !== JSON.stringify(ref.value)) {
                    changed.push(position);
                }
            }

            expect(changed).toEqual([]);
        });
    });
});
