import { Schema } from '../../../../../src/engine/json/schema/Schema';
import { SchemaType } from '../../../../../src/engine/json/schema/type/SchemaType';
import { TypeUtil } from '../../../../../src/engine/json/schema/type/TypeUtil';
import { SchemaValidator } from '../../../../../src/engine/json/schema/validator/SchemaValidator';
import { TypeValidator } from '../../../../../src/engine/json/schema/validator/TypeValidator';
import { SchemaValidationException } from '../../../../../src/engine/json/schema/validator/exception/SchemaValidationException';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';
import { KIRunFunctionRepository } from '../../../../../src/engine/repository/KIRunFunctionRepository';
import { SetFunction } from '../../../../../src/engine/function/system/context/SetFunction';
import { ContextElement } from '../../../../../src/engine/runtime/ContextElement';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { ErrorMessageFormatter } from '../../../../../src/engine/util/ErrorMessageFormatter';

/**
 * A text value given to a schema that allows several types (an "any" schema, as on Context.Set's
 * value) is checked as STRING first. These tests pin that the result is the same as the old
 * ordered walk (OBJECT, ARRAY, DOUBLE, FLOAT, LONG, INTEGER, STRING, BOOLEAN, NULL).
 */

const repo = new KIRunSchemaRepository();

const ORDER: SchemaType[] = [
    SchemaType.OBJECT,
    SchemaType.ARRAY,
    SchemaType.DOUBLE,
    SchemaType.FLOAT,
    SchemaType.LONG,
    SchemaType.INTEGER,
    SchemaType.STRING,
    SchemaType.BOOLEAN,
    SchemaType.NULL,
];

// The old algorithm: every allowed type in ORDER, first success wins, else the combined error.
async function oldWalk(schema: Schema, element: any, convert?: boolean): Promise<any> {
    const allowed = schema.getType()!.getAllowedSchemaTypes();
    const errors: SchemaValidationException[] = [];
    for (const type of ORDER.filter((t) => allowed.has(t))) {
        try {
            return await TypeValidator.validate([schema], type, schema, repo, element, convert);
        } catch (err: any) {
            errors.push(err);
        }
    }
    if (errors.length == 1) throw new SchemaValidationException('', errors[0].message);
    throw new SchemaValidationException(
        '',
        'Value ' + JSON.stringify(element) + ' is not of valid type(s)',
        errors,
    );
}

async function outcome(p: () => Promise<any>): Promise<{ ok: boolean; value: any }> {
    try {
        return { ok: true, value: await p() };
    } catch (err: any) {
        return { ok: false, value: err.message };
    }
}

const SCHEMAS: [string, () => Schema][] = [
    ['any', () => Schema.ofAny('value')],
    ['string|integer', () => new Schema().setType(TypeUtil.of(SchemaType.STRING, SchemaType.INTEGER))],
    ['string|boolean', () => new Schema().setType(TypeUtil.of(SchemaType.STRING, SchemaType.BOOLEAN))],
    ['double|string', () => new Schema().setType(TypeUtil.of(SchemaType.DOUBLE, SchemaType.STRING))],
    [
        'string(minLength 3)|integer',
        () =>
            new Schema()
                .setType(TypeUtil.of(SchemaType.STRING, SchemaType.INTEGER))
                .setMinLength(3),
    ],
    [
        'string(date)|long',
        () =>
            Schema.from({ name: 'd', type: ['STRING', 'LONG'], format: 'DATE' } as any)!,
    ],
];

const VALUES: any[] = ['', 'abc', '123', '12.5', 'true', 'false', 'null', '{"a":1}', '[1,2]', '2026-11-13', 'ab', 7, true, null, { a: 1 }, [1]];

describe('SchemaValidator tries STRING first for a text value', () => {
    for (const [name, mk] of SCHEMAS) {
        for (const convert of [false, true]) {
            test(`${name}, convert ${convert}: same result as the ordered walk`, async () => {
                for (const v of VALUES) {
                    const now = await outcome(() =>
                        SchemaValidator.validate([], mk(), repo, v, convert),
                    );
                    const before = await outcome(() => oldWalk(mk(), v, convert));
                    expect({ v, ok: now.ok }).toEqual({ v, ok: before.ok });
                    if (now.ok) expect(now.value).toEqual(before.value);
                    else expect(now.value).toBe(before.value);
                }
            });
        }
    }

    test('an any schema keeps a text value as the same text', async () => {
        const big = 'x'.repeat(100000);
        expect(await SchemaValidator.validate([], Schema.ofAny('v'), repo, big)).toBe(big);
        expect(await SchemaValidator.validate([], Schema.ofAny('v'), repo, '123')).toBe('123');
        expect(await SchemaValidator.validate([], Schema.ofAny('v'), repo, 'true')).toBe('true');
    });

    test('with conversion on, numbers and booleans given as text still convert', async () => {
        const any = () => Schema.ofAny('v');
        expect(await SchemaValidator.validate([], any(), repo, '123', true)).toBe(123);
        expect(await SchemaValidator.validate([], any(), repo, '12.5', true)).toBe(12.5);
        expect(await SchemaValidator.validate([], any(), repo, 'abc', true)).toBe('abc');
        const num = new Schema().setType(TypeUtil.of(SchemaType.INTEGER, SchemaType.STRING));
        expect(await SchemaValidator.validate([], num, repo, '42', true)).toBe(42);
        const bool = new Schema().setType(TypeUtil.of(SchemaType.BOOLEAN, SchemaType.NULL));
        expect(await SchemaValidator.validate([], bool, repo, 'true', true)).toBe(true);
        expect(await SchemaValidator.validate([], bool, repo, 'false', true)).toBe(false);
    });

    test('a long text never lands whole in an error message', async () => {
        const big = 'y'.repeat(5000);
        const intOnly = new Schema().setType(TypeUtil.of(SchemaType.INTEGER, SchemaType.BOOLEAN));
        let message = '';
        try {
            await SchemaValidator.validate([], intOnly, repo, big);
        } catch (err: any) {
            message = err.message;
        }
        expect(message).toContain('a text of 5000 characters');
        expect(message.includes('yyyyyyyyyy')).toBe(false);
        expect(ErrorMessageFormatter.formatValue(big)).toBe('a text of 5000 characters');
        expect(ErrorMessageFormatter.formatValue('short')).toBe('"short"');
        expect(ErrorMessageFormatter.formatValue('x'.repeat(200))).toBe(`"${'x'.repeat(200)}"`);
        expect(ErrorMessageFormatter.formatValue('x'.repeat(201))).toBe('a text of 201 characters');
    });

    test('a 20k row text accumulator through Context.Set stays fast', async () => {
        const setFunction = new SetFunction();
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        );
        const contextMap: Map<string, ContextElement> = new Map();
        contextMap.set('acc', new ContextElement(Schema.ofAny('acc'), ''));
        fep.setContext(contextMap);

        const start = Date.now();
        for (let i = 0; i < 20000; i++) {
            const acc = contextMap.get('acc')!.getElement();
            fep.setArguments(
                new Map<string, any>([
                    ['name', 'Context.acc'],
                    ['value', acc + '\n' + 'row ' + i + ',someone' + i + '@example.com,+91000' + i],
                ]),
            );
            await setFunction.execute(fep);
        }
        const ms = Date.now() - start;

        const acc: string = contextMap.get('acc')!.getElement();
        expect(acc.split('\n').length).toBe(20001);
        // Loose bound: about 0.15 s on a laptop with the text first check, about 2.8 s without it.
        expect(ms).toBeLessThan(2000);
    }, 60000);
});
