import { ArrayValidator, KIRunSchemaRepository, Schema } from '../../../../../src';

const repo = new KIRunSchemaRepository();

test('schema array validator test for null', () => {
    let schema: Schema = Schema.ofArray('arraySchema');
    expect(() => ArrayValidator.validate([], schema, repo, null)).toThrow(
        'Expected an array but found null',
    );
});

test('schema array validator test for boolean ', () => {
    let schema: Schema = Schema.ofArray('arraySchema');
    let obj = {
        val: false,
    };
    expect(() => ArrayValidator.validate([], schema, repo, obj)).toThrow(
        obj.toString() + ' is not an Array',
    );
});
