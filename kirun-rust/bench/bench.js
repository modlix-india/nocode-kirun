#!/usr/bin/env node
// kirun-js side of the benchmarks (see README.md). Needs ../../kirun-js/dist (npm run build).
// Usage: node bench/bench.js [workload...]
const fs = require('fs');
const path = require('path');
const K = require(path.join(__dirname, '../../kirun-js/dist/index.js'));

const read = (p) => JSON.parse(fs.readFileSync(p, 'utf8'));
const cfg = read(path.join(__dirname, 'config.json'));
const only = process.argv.slice(2);
const wants = (w) => !only.length || only.includes(w);

async function measure(workload, warmup, runs, f) {
    for (let i = 0; i < warmup; i++) await f();
    const times = [];
    for (let i = 0; i < runs; i++) {
        const t = process.hrtime.bigint();
        await f();
        times.push(Number(process.hrtime.bigint() - t) / 1e6);
    }
    times.sort((a, b) => a - b);
    console.log(JSON.stringify({ lang: 'js', workload, runs, median_ms: times[Math.floor(times.length / 2)], min_ms: times[0] }));
}

(async () => {
    const fRepo = new K.KIRunFunctionRepository();
    const sRepo = new K.KIRunSchemaRepository();

    for (const [workload, file] of [['fibonacci', 'fibonacci.json'], ['loopsum', 'loopsum.json']]) {
        if (!wants(workload)) continue;
        const c = cfg[workload];
        const fd = K.FunctionDefinition.from(read(path.join(__dirname, 'defs', file)));
        await measure(workload, c.warmup, c.runs, async () => {
            const fep = new K.FunctionExecutionParameters(fRepo, sRepo).setArguments(new Map([['n', c.n]]));
            const out = await new K.KIRuntime(fd).execute(fep);
            if (!out.next()) throw new Error('no output');
        });
    }

    if (wants('expression')) {
        const c = cfg.expression;
        const ext = new K.ArgumentsTokenValueExtractor(new Map(Object.entries(c.args)));
        const values = new Map([[ext.getPrefix(), ext]]);
        if (new K.ExpressionEvaluator(c.expression).evaluate(values) !== true) throw new Error('expression is not true');
        await measure('expression', c.warmup, c.runs, async () => {
            for (let i = 0; i < c.evaluations; i++) new K.ExpressionEvaluator(c.expression).evaluate(values);
        });
    }

    if (wants('schema')) {
        const c = cfg.schema;
        const schema = K.Schema.from(read(path.join(__dirname, 'defs/record_schema.json')));
        const text = fs.readFileSync(path.join(__dirname, 'data/records.json'), 'utf8');
        await measure('schema', c.warmup, c.runs, async () => {
            // A fresh copy each run: validation fills defaults in place.
            const out = await K.SchemaValidator.validate(undefined, schema, sRepo, JSON.parse(text));
            if (out.length !== 5000) throw new Error('bad output');
        });
    }

    if (wants('dsl')) {
        const c = cfg.dsl;
        const root = path.join(__dirname, '../../test-data/functions');
        const corpus = [];
        for (const sub of ['ui-functions', 'core-functions', 'page-event-functions']) {
            const dir = path.join(root, sub);
            if (!fs.existsSync(dir)) continue;
            for (const f of fs.readdirSync(dir).filter((x) => x.endsWith('.json')).sort()) {
                const raw = read(path.join(dir, f));
                corpus.push(raw.definition ?? raw);
            }
        }
        if (!corpus.length) return console.error('dsl: no functions in test-data/functions, skipped');
        await measure('dsl', c.warmup, c.runs, async () => {
            for (const def of corpus) {
                try {
                    K.DSLCompiler.compile(await K.DSLCompiler.decompile(def));
                } catch (e) {}
            }
        });
    }
})();
