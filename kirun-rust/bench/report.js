#!/usr/bin/env node
// Turns results.jsonl from run.sh into a Markdown table (median ms per workload, peak memory).
const fs = require('fs');
const rows = fs.readFileSync(process.argv[2], 'utf8').split('\n').filter((l) => l.startsWith('{')).map(JSON.parse);
const langs = ['js', 'java', 'rust', 'rust+mimalloc'];
const workloads = ['fibonacci', 'loopsum', 'expression', 'schema', 'dsl'];
const label = { fibonacci: 'Fibonacci (n=1000)', loopsum: 'Loop sum (100k)', expression: 'Expression x 200k', schema: 'Schema (5,000 records)', dsl: 'DSL round trip (14k functions)' };
const get = (lang, w) => rows.find((r) => r.lang === lang && r.workload === w);
const fmt = (r) => (r ? r.median_ms.toFixed(1) : 'n/a');
const out = [];
out.push('| Workload (median ms) | ' + langs.join(' | ') + ' | rust+mimalloc vs js |');
out.push('|' + ' --- |'.repeat(langs.length + 2));
for (const w of workloads) {
  const js = get('js', w), best = get('rust+mimalloc', w);
  const ratio = js && best ? (js.median_ms / best.median_ms).toFixed(1) + 'x faster' : 'n/a';
  out.push(`| ${label[w]} | ${langs.map((l) => fmt(get(l, w))).join(' | ')} | ${ratio} |`);
}
const rss = (l) => { const r = get(l, '_peak_rss'); return r ? (r.bytes / 1048576).toFixed(0) + ' MB' : 'n/a'; };
out.push(`| Peak memory (fibonacci, loopsum, expression, schema) | ${langs.map(rss).join(' | ')} | |`);
console.log(out.join('\n'));
