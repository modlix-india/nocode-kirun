import { KIRunFunctionRepository } from '../src';

const repo = new KIRunFunctionRepository();

// Install ts-node
// npm install -g ts-node

// Add target to tsconfig.json
// "target": "ES2022"

// Run the script
// ts-node ./generator/generateValidationCSV.ts > ./generator/validation-js.csv

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
        csv += 'Parameter,Type,Schema\n';
        csv +=
            Array.from(signature.getParameters()?.entries() ?? [])
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
                .join('\n') + '\n';

        csv += 'Events\n';
        csv += Array.from(signature.getEvents()?.entries() ?? [])
            .map((e) => {
                const strings = [];
                strings.push(`${escapeCsv(e[0])},${escapeCsv(e[1].getName())}\n`);
                strings.push('Event Parameter,,Schema\n');
                strings.push(
                    Array.from(e[1].getParameters()?.entries() ?? [])
                        .map((p) => {
                            const schemaType = Array.from(
                                p[1].getType()?.getAllowedSchemaTypes() ?? [],
                            ).join(';');
                            return `${escapeCsv(p[0])},,${escapeCsv(schemaType)}`;
                        })
                        .map((e) => e.toString().toLowerCase())
                        .sort()
                        .join('\n'),
                );

                return strings.join('');
            })
            .join('\n');

        csv += '\n\n';
    }

    console.log(csv);
}

generate();
