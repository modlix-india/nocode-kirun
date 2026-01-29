import {
    AdditionalType,
    ArraySchemaType,
    ArrayValidator,
    KIRunSchemaRepository,
    Schema,
    SchemaValidator,
} from '../../../../../src';

const repo = new KIRunSchemaRepository();

test('schema array validator test for single', async () => {
    let ast = new ArraySchemaType();
    ast.setSingleSchema(Schema.ofInteger('ast'));

    const schema: Schema = Schema.ofArray('schema').setItems(ast);

    let arr = [12, 23, 54, 45];

    expect(await ArrayValidator.validate([], schema, repo, arr)).toStrictEqual(arr);
});

test('schema array validator test for tuple', async () => {
    let tupleS: Schema[] = [
        Schema.ofInteger('item1'),
        Schema.ofString('item2'),
        Schema.ofObject('item3'),
    ];

    let ast = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    const schema: Schema = Schema.ofArray('schema').setItems(ast);

    let arr = [12, 'surendhar', { a: 'val1', b: 'val2' }];

    expect(await ArrayValidator.validate([], schema, repo, arr)).toStrictEqual(arr);
});

test('schema array validator test for single', async () => {
    let ast = new ArraySchemaType();
    ast.setSingleSchema(Schema.ofInteger('ast'));

    const schema: Schema = Schema.ofArray('schema')
        .setItems(ast)
        .setAdditionalItems(new AdditionalType().setBooleanValue(false));

    let arr = [12, 23, 54, 45];

    expect(await ArrayValidator.validate([], schema, repo, arr)).toStrictEqual(arr);
});

test('schema array validator test for tuple', async () => {
    let tupleS: Schema[] = [
        Schema.ofInteger('item1'),
        Schema.ofString('item2'),
        Schema.ofObject('item3'),
    ];

    let ast = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    const schema: Schema = Schema.ofArray('schema')
        .setItems(ast)
        .setAdditionalItems(new AdditionalType().setBooleanValue(true));

    let arr = [12, 'surendhar', { a: 'val1', b: 'val2' }, 1, 2, 4];

    expect(await ArrayValidator.validate([], schema, repo, arr)).toStrictEqual(arr);
});

test('schema array validator test for tuple with add schema', async () => {
    let tupleS: Schema[] = [
        Schema.ofInteger('item1'),
        Schema.ofString('item2'),
        Schema.ofObject('item3'),
    ];

    let ast = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    const schema: Schema = Schema.ofArray('schema')
        .setItems(ast)
        .setAdditionalItems(new AdditionalType().setSchemaValue(Schema.ofInteger('itemInt')));

    let arr = [12, 'surendhar', { a: 'val1', b: 'val2' }, 1, 2];

    expect(await ArrayValidator.validate([], schema, repo, arr)).toStrictEqual(arr);
});

test('schema array validator test for tuple with add schema fail', async () => {
    let tupleS: Schema[] = [
        Schema.ofInteger('item1'),
        Schema.ofString('item2'),
        Schema.ofObject('item3'),
    ];

    let ast = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    const schema: Schema = Schema.ofArray('schema')
        .setItems(ast)
        .setAdditionalItems(new AdditionalType().setSchemaValue(Schema.ofInteger('itemInt')));

    let arr = [12, 'surendhar', { a: 'val1', b: 'val2' }, 1, 2, 4, 'surendhar'];

    expect(ArrayValidator.validate([], schema, repo, arr)).rejects.toThrow();
});

test('schema array validator test for tuple with add schema fail', async () => {
    let tupleS: Schema[] = [Schema.ofInteger('item1'), Schema.ofString('item2')];

    let ast = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    const schema: Schema = Schema.ofArray('schema')
        .setItems(ast)
        .setAdditionalItems(new AdditionalType().setSchemaValue(Schema.ofInteger('itemInt')));

    let arr = [12, 'surendhar', { a: 'val1', b: 'val2' }, 1, 2, 4, ['ve', 23, 'ctor']];

    expect(ArrayValidator.validate([], schema, repo, arr)).rejects.toThrow();
});

test('schema array validator test for single with additional', async () => {
    let ast = new ArraySchemaType();
    ast.setSingleSchema(Schema.ofInteger('ast'));

    const schema: Schema = Schema.ofArray('schema')
        .setItems(ast)
        .setAdditionalItems(new AdditionalType().setBooleanValue(true));

    let arr = [12, 23, 54, 45, 'abcd', 'df'];

    expect(ArrayValidator.validate([], schema, repo, arr)).rejects.toThrow();
});

test('schema array validator test for tuple without additional', async () => {
    let tupleS: Schema[] = [
        Schema.ofInteger('item1'),
        Schema.ofString('item2'),
        Schema.ofObject('item3'),
    ];

    let ast = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    const schema: Schema = Schema.ofArray('schema').setItems(ast);

    let arr = [12, 'surendhar', { a: 'val1', b: 'val2' }, 'add'];

    expect(ArrayValidator.validate([], schema, repo, arr)).rejects.toThrow();
});

test('schema array validator tuple schema with json object', async () => {
    let tupleS: Schema[] = [
        Schema.ofInteger('item1'),
        Schema.ofString('item2'),
        Schema.ofObject('item3'),
    ];

    let ast = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    const schema: Schema = Schema.ofArray('schema').setItems(ast);

    let obj = [1, 'asd', { val: 'stringtype' }, 'stringOnemore'];

    expect(SchemaValidator.validate([], schema, repo, obj)).rejects.toThrow();
});

test('schema array validator tuple schema similar to json object', async () => {
    let tupleS: Schema[] = [
        Schema.ofInteger('item1'),
        Schema.ofString('item2'),
        Schema.ofBoolean('item3'),
    ];

    let ast = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    let objSc = Schema.ofObject('obj');

    const schema: Schema = Schema.ofArray('schema')
        .setItems(ast)
        .setAdditionalItems(new AdditionalType().setSchemaValue(objSc));

    let obj = [1, 'asd', true, { val: 'stringtype' }, false];

    expect(SchemaValidator.validate([], schema, repo, obj)).rejects.toThrow();
});
