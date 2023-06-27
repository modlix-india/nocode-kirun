import {
    ArraySchemaType,
    ArrayValidator,
    isNullValue,
    KIRunSchemaRepository,
    Schema,
    SchemaType,
    Type,
    TypeUtil,
} from '../../../../../src';

const repo = new KIRunSchemaRepository();

test('schema array contains test  ', async () => {
    let tupleS: Schema[] = [
        Schema.ofString('item1'),
        Schema.ofInteger('item2'),
        Schema.ofObject('item3'),
    ];

    let ast: ArraySchemaType = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    let schema: Schema = Schema.ofArray('arraySchema')
        .setItems(ast)
        .setContains(Schema.ofObject('containsS'));

    let obj = {
        val: false,
    };

    let array: any[] = ['jimmy', 31, obj];

    expect(await ArrayValidator.validate([], schema, repo, array)).toStrictEqual(array);
});

test('schema array error contains test  ', async () => {
    let tupleS: Schema[] = [
        Schema.ofString('item1'),
        Schema.ofInteger('item2'),
        Schema.ofObject('item3'),
    ];

    let ast: ArraySchemaType = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    let schema: Schema = Schema.ofArray('arraySchema')
        .setItems(ast)
        .setContains(Schema.ofBoolean('containsS'));

    let obj = {
        val: false,
    };

    let array: any[] = ['jimmy', 31, obj];

    expect(ArrayValidator.validate([], schema, repo, array)).rejects.toThrow(
        'None of the items are of type contains schema',
    );
});

test('schema array min contains test  ', async () => {
    let tupleS: Schema[] = [
        Schema.ofString('item1'),
        Schema.ofInteger('item2'),
        Schema.ofObject('item3'),
        Schema.ofBoolean('item4'),
        Schema.ofObject('item5'),
    ];

    let ast: ArraySchemaType = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    let schema: Schema = Schema.ofArray('arraySchema')
        .setItems(ast)
        .setContains(Schema.ofObject('containsS'))
        .setMinContains(2);

    let obj = {
        val: false,
    };
    let obj1 = {
        name: 'mcgill',
    };

    let array: any[] = ['jimmy', 31, obj, true, obj1];

    expect(await ArrayValidator.validate([], schema, repo, array)).toStrictEqual(array);
});

test('schema array max contains test  ', async () => {
    let tupleS: Schema[] = [
        Schema.ofString('item1'),
        Schema.ofInteger('item2'),
        Schema.ofObject('item3'),
        Schema.ofBoolean('item4'),
        Schema.ofObject('item5'),
    ];

    let ast: ArraySchemaType = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    let schema: Schema = Schema.ofArray('arraySchema')
        .setItems(ast)
        .setContains(Schema.ofObject('containsS'))
        .setMaxContains(3);

    let obj = {
        val: false,
    };
    let obj1 = {
        name: 'mcgill',
    };

    let array: any[] = ['jimmy', 31, obj, true, obj1];

    expect(await ArrayValidator.validate([], schema, repo, array)).toStrictEqual(array);
});

test('schema array min contains test  ', async () => {
    let tupleS: Schema[] = [
        Schema.ofString('item1'),
        Schema.ofInteger('item2'),
        Schema.ofObject('item3'),
        Schema.ofBoolean('item4'),
        Schema.ofObject('item5'),
    ];

    let ast: ArraySchemaType = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    let schema: Schema = Schema.ofArray('arraySchema')
        .setItems(ast)
        .setContains(Schema.ofObject('containsS'))
        .setMinContains(4);

    let obj = {
        val: false,
    };
    let obj1 = {
        name: 'mcgill',
    };

    let array: any[] = ['jimmy', 31, obj, true, obj1];

    expect(ArrayValidator.validate([], schema, repo, array)).rejects.toThrow(
        'The minimum number of the items of type contains schema should be ' +
            schema.getMinContains() +
            ' but found 2',
    );
});

test('schema array max contains test  ', async () => {
    let tupleS: Schema[] = [
        Schema.ofString('item1'),
        Schema.ofInteger('item2'),
        Schema.ofObject('item3'),
        Schema.ofBoolean('item4'),
        Schema.ofObject('item5'),
    ];

    let ast: ArraySchemaType = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    let schema: Schema = Schema.ofArray('arraySchema')
        .setItems(ast)
        .setContains(Schema.ofObject('containsS'))
        .setMaxContains(1);

    let obj = {
        val: false,
    };
    let obj1 = {
        name: 'mcgill',
    };

    let array: any[] = ['jimmy', 31, obj, true, obj1];

    expect(ArrayValidator.validate([], schema, repo, array)).rejects.toThrow(
        'The maximum number of the items of type contains schema should be ' +
            schema.getMaxContains() +
            ' but found 2',
    );
});

test('schema array min error contains test  ', async () => {
    let tupleS: Schema[] = [
        Schema.ofString('item1'),
        Schema.ofInteger('item2'),
        Schema.ofObject('item3'),
        Schema.ofBoolean('item4'),
        Schema.ofObject('item5'),
    ];

    let ast: ArraySchemaType = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    let schema: Schema = Schema.ofArray('arraySchema')
        .setItems(ast)
        .setContains(Schema.ofObject('containsS'))
        .setMinContains(4);

    let obj = {
        val: false,
    };
    let obj1 = {
        name: 'mcgill',
    };

    let array: any[] = ['jimmy', 31, obj, true, obj1];

    expect(ArrayValidator.validate([], schema, repo, array)).rejects.toThrow(
        'The minimum number of the items of type contains schema should be ' +
            schema.getMinContains() +
            ' but found 2',
    );
});

test('schema array min max contains test  ', async () => {
    let tupleS: Schema[] = [
        Schema.ofString('item1'),
        Schema.ofInteger('item2'),
        Schema.ofObject('item3'),
        Schema.ofBoolean('item4'),
        Schema.ofObject('item5'),
    ];

    let ast: ArraySchemaType = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    let schema: Schema = Schema.ofArray('arraySchema')
        .setItems(ast)
        .setContains(Schema.ofObject('containsS'))
        .setMinContains(1)
        .setMaxContains(3);

    let obj = {
        val: false,
    };
    let obj1 = {
        name: 'mcgill',
    };

    let array: any[] = ['jimmy', 31, obj, true, obj1];

    expect(await ArrayValidator.validate([], schema, repo, array)).toBe(array);
});

test('schema array min max contains test  ', async () => {
    let tupleS: Schema[] = [
        Schema.ofString('item1'),
        Schema.ofInteger('item2'),
        Schema.ofObject('item3'),
        Schema.ofBoolean('item4'),
        Schema.ofObject('item5'),
    ];

    let ast: ArraySchemaType = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    let schema: Schema = Schema.ofArray('arraySchema')
        .setItems(ast)
        .setContains(Schema.ofObject('containsS'))
        .setMinContains(1)
        .setMaxContains(0);

    let obj = {
        val: false,
    };
    let obj1 = {
        name: 'mcgill',
    };

    let array: any[] = ['jimmy', 31, obj, true, obj1];

    expect(ArrayValidator.validate([], schema, repo, array)).rejects.toThrow(
        'The maximum number of the items of type contains schema should be ' +
            schema.getMaxContains() +
            ' but found 2',
    );
});

test('schema array min max contains without contains test  ', async () => {
    let tupleS: Schema[] = [
        Schema.ofString('item1'),
        Schema.ofInteger('item2'),
        Schema.ofObject('item3'),
        Schema.ofBoolean('item4'),
        Schema.ofObject('item5'),
    ];

    let ast: ArraySchemaType = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    let schema: Schema = Schema.ofArray('arraySchema')
        .setItems(ast)
        .setMinContains(5)
        .setMaxContains(45);

    let obj = {
        val: false,
    };
    let obj1 = {
        name: 'mcgill',
    };

    let array: any[] = ['jimmy', 31, obj, true, obj1];

    expect(await ArrayValidator.validate([], schema, repo, array)).toBe(array);
});

test('schema array min max contains without contains test  ', async () => {
    let tupleS: Schema[] = [
        Schema.ofString('item1'),
        Schema.ofInteger('item2'),
        Schema.ofObject('item3'),
        Schema.ofBoolean('item4'),
        Schema.ofObject('item5'),
    ];

    let ast: ArraySchemaType = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    let schema: Schema = Schema.ofArray('arraySchema')
        .setItems(ast)
        .setMinContains(1)
        .setMaxContains(0);

    let obj = {
        val: false,
    };
    let obj1 = {
        name: 'mcgill',
    };

    let array: any[] = ['jimmy', 31, obj, true, obj1];

    expect(await ArrayValidator.validate([], schema, repo, array)).toBe(array);
});
