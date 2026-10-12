import { DSLCompiler } from '../../../src/engine/dsl/DSLCompiler';

/**
 * The decompiler's layout: `output` chains stay at one level, long calls take one
 * argument per line, expressions inside JSON print bare, and large objects move to
 * a VALUES section referenced as `@name`. None of it may change the compiled JSON.
 */
describe('DSL readable layout', () => {
    /** Steps with ParameterReference keys dropped and refs listed in order: keys are random per compile. */
    const shape = (fn: any): any => {
        const steps: any = {};
        for (const [name, step] of Object.entries<any>(fn.steps ?? {})) {
            const parameterMap: any = {};
            for (const [param, refs] of Object.entries<any>(step.parameterMap ?? {})) {
                parameterMap[param] = Object.values<any>(refs)
                    .sort((a, b) => (a.order ?? 0) - (b.order ?? 0))
                    .map(({ key, ...ref }) => JSON.parse(JSON.stringify(ref)));
            }
            steps[name] = { ...step, parameterMap };
        }
        return { ...fn, steps };
    };

    const roundTrip = async (text: string) => {
        const json = DSLCompiler.compile(text);
        const printed = await DSLCompiler.decompile(json);
        expect(shape(DSLCompiler.compile(printed))).toEqual(shape(json));
        return printed;
    };

    const SOURCE = `FUNCTION activity
    NAMESPACE Test
    LOGIC
        c: Test.context()
        auth: System.If(condition = Steps.c.output.ctx.isAuthenticated = true) AFTER Steps.c.output
            true
                dg: Test.rowGet(ctx = Steps.c.output.ctx, ref = Arguments.id, storage = "deal", entity = "Ticket", mode = "strict") AFTER Steps.auth.true
                    error
                        dgE: System.GenerateEvent(eventName = "error", results = {"name": "status", "value": {"isExpression": true, "value": "Steps.dg.error.status"}}) AFTER Steps.dg.error
                    output
                        cfg: System.Make(resultShape = {"rels": [{"field": "taskId", "storage": "task", "spec": "task"}, {"field": "noteId", "storage": "note", "spec": "note"}]}) AFTER Steps.dg.output
                            output
                                done: System.GenerateEvent(results = {"name": "result", "value": {"isExpression": true, "value": "Steps.cfg.output.value"}}) AFTER Steps.cfg.output
            false
                authE: System.GenerateEvent(eventName = "error") AFTER Steps.auth.false`;

    test('output chains print at one level, branches indent', async () => {
        const printed = await roundTrip(SOURCE);
        const indentOf = (step: string) => {
            const line = printed.split('\n').find((l) => l.trimStart().startsWith(`${step}:`))!;
            return line.length - line.trimStart().length;
        };
        expect(indentOf('auth')).toBe(indentOf('c'));
        expect(indentOf('cfg')).toBe(indentOf('dg'));
        expect(indentOf('done')).toBe(indentOf('dg'));
        expect(indentOf('dg')).toBe(indentOf('auth') + 8);
        expect(indentOf('dgE')).toBe(indentOf('dg') + 8);
        expect(indentOf('authE')).toBe(indentOf('dg'));
    });

    test('expressions inside JSON print bare', async () => {
        const printed = await roundTrip(SOURCE);
        expect(printed).toContain('results = {name: "status", value: Steps.dg.error.status}');
        expect(printed).not.toContain('isExpression');
    });

    test('a large object moves to VALUES and compiles to the inline value', async () => {
        const printed = await roundTrip(SOURCE);
        expect(printed).toContain('cfg: System.Make(resultShape = @cfg.resultShape) AFTER Steps.dg.output');
        expect(printed).toMatch(/\n {4}VALUES\n {8}@cfg\.resultShape = \{/);

        const cfg = DSLCompiler.compile(printed).steps.cfg;
        expect(Object.values<any>(cfg.parameterMap.resultShape)[0].value).toEqual({
            rels: [
                { field: 'taskId', storage: 'task', spec: 'task' },
                { field: 'noteId', storage: 'note', spec: 'note' },
            ],
        });
    });

    test('a long call takes one argument per line', async () => {
        const printed = await roundTrip(SOURCE);
        expect(printed).toMatch(/dg: Test\.rowGet\(\n\s+ctx = Steps\.c\.output\.ctx,\n/);
        expect(printed).toMatch(/\n\s+mode = "strict"\n\s+\) AFTER Steps\.auth\.true/);
    });

    test('the last argument before a `)` on its own line does not keep the line break', () => {
        const json = DSLCompiler.compile(`FUNCTION f
    LOGIC
        a: System.Print(
            values = Arguments.x
        )`);
        expect(Object.values<any>(json.steps.a.parameterMap.values)[0].expression).toBe('Arguments.x');
    });

    test('trailing spaces on one line are kept, as stored expressions can have them', async () => {
        const fn = {
            name: 'f',
            steps: {
                a: {
                    statementName: 'a',
                    namespace: 'System',
                    name: 'Print',
                    parameterMap: { values: { k: { key: 'k', type: 'EXPRESSION', expression: ' Arguments.x  ', order: 1 } } },
                },
            },
        };
        const back = DSLCompiler.compile(await DSLCompiler.decompile(fn));
        expect(Object.values<any>(back.steps.a.parameterMap.values)[0].expression).toBe(' Arguments.x  ');
    });

    test('an event result expression the parser would misread goes in backticks', async () => {
        const results = [
            { name: 'a', value: { isExpression: true, value: "'x' + Arguments.y" } },
            { name: 'b', value: { isExpression: true, value: '0 + 400' } },
            { name: 'c', value: { isExpression: true, value: 'max(1, 2)' } },
            { name: 'd', value: { isExpression: true, value: 'Arguments.a, Arguments.b' } },
            { name: 'e', value: { isExpression: true, value: 'true' } },
        ];
        const fn = {
            name: 'f',
            steps: {
                a: {
                    statementName: 'a',
                    namespace: 'System',
                    name: 'GenerateEvent',
                    parameterMap: {
                        results: Object.fromEntries(
                            results.map((value, i) => [`k${i}`, { key: `k${i}`, type: 'VALUE', value, order: i + 1 }]),
                        ),
                    },
                },
            },
        };
        const printed = await DSLCompiler.decompile(fn);
        expect(printed).toContain('{name: "a", value: `\'x\' + Arguments.y`}');
        expect(printed).toContain('{name: "b", value: `0 + 400`}');
        expect(printed).toContain('{name: "c", value: max(1, 2)}');
        expect(printed).toContain('{name: "d", value: `Arguments.a, Arguments.b`}');
        expect(printed).toContain('{name: "e", value: `true`}');
        const back = DSLCompiler.compile(printed);
        const values = Object.values<any>(back.steps.a.parameterMap.results)
            .sort((x, y) => x.order - y.order)
            .map((r) => r.value);
        expect(values).toEqual(results);
    });

    test('an expression object outside event results stays written out: the runtime does not evaluate it', async () => {
        const value = { title: { isExpression: true, value: 'Page.t' }, done: true };
        const fn = {
            name: 'f',
            steps: {
                a: {
                    statementName: 'a',
                    namespace: 'CoreServices.Storage',
                    name: 'Create',
                    parameterMap: { dataObject: { k: { key: 'k', type: 'VALUE', value, order: 1 } } },
                },
            },
        };
        const printed = await DSLCompiler.decompile(fn);
        expect(printed).toContain('dataObject = {title: {isExpression: true, value: "Page.t"}, done: true}');
        const back = DSLCompiler.compile(printed);
        expect(Object.values<any>(back.steps.a.parameterMap.dataObject)[0].value).toEqual(value);
    });

    test('a value used twice is copied, not shared', () => {
        const json = DSLCompiler.compile(`FUNCTION f
    LOGIC
        a: System.Make(resultShape = @shape)
        b: System.Make(resultShape = @shape)
    VALUES
        @shape = {list: [1, 2]}`);
        const a = Object.values<any>(json.steps.a.parameterMap.resultShape)[0].value;
        const b = Object.values<any>(json.steps.b.parameterMap.resultShape)[0].value;
        expect(a).toEqual({ list: [1, 2] });
        expect(b).toEqual(a);
        expect(b).not.toBe(a);
    });

    test('any value name compiles; decompiling renames it to step.parameter, one per use', async () => {
        // Over INLINE_VALUE_WIDTH (100) on one line, so the printer moves it to VALUES
        const shape =
            '{list: [1, 2], label: "a value long enough to be moved out of the step line", note: "by the printer"}';
        const printed = await DSLCompiler.decompile(
            DSLCompiler.compile(`FUNCTION f
    LOGIC
        a: System.Make(resultShape = @shape)
        b: System.Make(resultShape = @shape)
    VALUES
        @shape = ${shape}`),
        );
        expect(printed).toContain('a: System.Make(resultShape = @a.resultShape)');
        expect(printed).toContain('b: System.Make(resultShape = @b.resultShape)');
        expect(printed).toMatch(/\n {8}@a\.resultShape = \{/);
        expect(printed).toMatch(/\n {8}@b\.resultShape = \{/);
        expect(printed).not.toContain('@shape');

        const back = DSLCompiler.compile(printed);
        const value = (step: string) => Object.values<any>(back.steps[step].parameterMap.resultShape)[0].value;
        expect(value('a')).toEqual({
            list: [1, 2],
            label: 'a value long enough to be moved out of the step line',
            note: 'by the printer',
        });
        expect(value('b')).toEqual(value('a'));
    });

    test('an undefined value name is an error', () => {
        expect(() =>
            DSLCompiler.compile(`FUNCTION f
    LOGIC
        a: System.Make(resultShape = @missing)`),
        ).toThrow('Value @missing is not defined in VALUES');
    });

    test('VALUES is still an ordinary name where it is not followed by @', () => {
        const json = DSLCompiler.compile(`FUNCTION f
    LOGIC
        VALUES: System.Print(values = Arguments.x)`);
        expect(json.steps.VALUES.name).toBe('Print');
    });
});
