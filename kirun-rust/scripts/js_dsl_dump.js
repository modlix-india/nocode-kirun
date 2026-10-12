#!/usr/bin/env node
// Usage: node scripts/js_dsl_dump.js <out.jsonl>  (needs kirun-js built: cd ../kirun-js && npm run build)
// Dumps kirun-js DSL results for every stored function: { file, def, text, compiled | error }.
const fs = require('fs');
const path = require('path');
const { DSLCompiler } = require(path.join(__dirname, '../../kirun-js/dist/index.js'));
const root = path.join(__dirname, '../../test-data/functions');
const out = fs.createWriteStream(process.argv[2]);
(async () => {
  let n = 0;
  for (const sub of ['ui-functions', 'core-functions', 'page-event-functions']) {
    for (const f of fs.readdirSync(path.join(root, sub)).filter((x) => x.endsWith('.json')).sort()) {
      const raw = JSON.parse(fs.readFileSync(path.join(root, sub, f), 'utf-8'));
      const def = raw.definition ?? raw;
      const rec = { file: `${sub}/${f}`, def };
      try {
        rec.text = await DSLCompiler.decompile(def);
        rec.compiled = DSLCompiler.compile(rec.text);
      } catch (e) {
        rec.error = String(e && e.message || e);
      }
      out.write(JSON.stringify(rec) + '\n');
      n++;
    }
  }
  out.end();
  console.log('dumped', n);
})();
