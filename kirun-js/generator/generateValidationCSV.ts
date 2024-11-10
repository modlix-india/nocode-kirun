import { KIRunFunctionRepository } from '../src';
import fs from 'fs';

const repo = new KIRunFunctionRepository();

// Install ts-node
// npm install -g ts-node

// Add target to tsconfig.json
// "target": "ES2022"

// Run the script
// cd to the folder where this script is located and run
// ts-node ./generateValidationCSV.ts

function escapeCsv(value: string) {
    if (!value) return '';
    return value.replace(/"/g, '""');
}

async function generate() {
    const functions = await repo.filter('');

    functions.sort((a, b) => a.localeCompare(b));

    let csv = '';

    for (const functionName of functions) {
        const splits = functionName.split('.');
        const func = await repo.find(
            splits.slice(0, splits.length - 1).join('.'),
            splits[splits.length - 1],
        );

        if (!func) throw new Error(`Function definition not found for ${functionName}`);

        const signature = func.getSignature();

        csv += `${escapeCsv(signature.getFullName())}\n`;
        const params = Array.from(signature.getParameters()?.entries() ?? [])
            .sort((a, b) => a[0].localeCompare(b[0]))
            .map((p) => {
                const strings = [];

                const schema = p[1].getSchema();
                const schemaType = Array.from(schema.getType()?.getAllowedSchemaTypes() ?? [])
                    .map((e) => e.toString().toLowerCase())
                    .sort()
                    .join(';');

                strings.push(
                    `${escapeCsv(p[0])},${escapeCsv(p[1].getType())},${escapeCsv(schemaType)}`,
                );

                return strings.join('\n');
            })
            .join('\n');

        if (params) {
            csv += 'Parameter,Type,Schema\n';
            csv += params + '\n';
        }

        const events = Array.from(signature.getEvents()?.entries() ?? [])
            .sort((a, b) => a[0].localeCompare(b[0]))
            .map((e) => {
                const strings = [];
                strings.push(`${escapeCsv(e[0])},${escapeCsv(e[1].getName())}`);
                let params = Array.from(e[1].getParameters()?.entries() ?? [])
                    .sort((a, b) => a[0].localeCompare(b[0]))
                    .map((p) => {
                        const schemaType = Array.from(p[1].getType()?.getAllowedSchemaTypes() ?? [])
                            .sort()
                            .join(';');
                        return `${escapeCsv(p[0])},,${escapeCsv(schemaType)}`;
                    })
                    .map((e) => e.toString().toLowerCase())
                    .sort()
                    .join('\n');

                if (params) {
                    strings.push('\nEvent Parameter,,Schema\n');
                    strings.push(params);
                }

                return strings.join('');
            })
            .filter((e) => e)
            .join('\n');

        if (events) {
            csv += 'Events\n';
            csv += events;
            csv += '\n\n';
        }
    }

    fs.writeFileSync('validation-js.csv', csv);
}

generate();
