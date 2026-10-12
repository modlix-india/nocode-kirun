#!/usr/bin/env node
// Writes bench/data/records.json: deterministic records for the schema workload.
const fs = require('fs');
const path = require('path');
const cfg = require('./config.json');
let seed = 42;
const rnd = () => ((seed = (seed * 1103515245 + 12345) % 2147483648) / 2147483648);
const records = [];
for (let i = 0; i < cfg.schema.records; i++) {
    const r = {
        id: i,
        name: 'user' + i,
        email: `user${i}@example.com`,
        score: Math.round(rnd() * 10000) / 100,
        tags: Array.from({ length: 1 + Math.floor(rnd() * 5) }, (_, k) => 'tag' + k),
        address: { city: 'City' + (i % 50), zip: String(100000 + (i % 899999)) },
    };
    if (i % 3) r.active = i % 2 === 0;
    records.push(r);
}
fs.mkdirSync(path.join(__dirname, 'data'), { recursive: true });
fs.writeFileSync(path.join(__dirname, 'data/records.json'), JSON.stringify(records));
console.log('records', records.length);
