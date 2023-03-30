import {
    AdditionalType,
    ArraySchemaType,
    ArrayValidator,
    KIRunSchemaRepository,
    Schema,
    SchemaValidator,
} from '../../../../../src';

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

test('schema array contains test  ', () => {
    let tupleS: Schema[] = [
        Schema.ofString('item1'),
        Schema.ofInteger('item2'),
        Schema.ofInteger('item3'),
    ];

    let ast: ArraySchemaType = new ArraySchemaType();
    ast.setTupleSchema(tupleS);

    let schema: Schema = Schema.ofArray('arraySchema')
        .setItems(ast)
        .setAdditionalItems(new AdditionalType().setBooleanValue(true));

    let array: any[] = ['jimmy', 1, 2, 'surendhar'];

    expect(ArrayValidator.validate([], schema, repo, array)).toStrictEqual(array);
});

test('schema array validator test for additional items with boolean true', () => {
    let singleS = Schema.ofInteger('singleS');

    let ast = new ArraySchemaType();
    ast.setSingleSchema(singleS);

    let schema = Schema.from({
        type: 'ARRAY',
        items: {
            singleSchema: 'STRING',
        },
        additionalItems: false,
    });
    let obj = [1, 2, 3, 4, 'surendhar'];

    console.log(schema?.getAdditionalItems());
    expect(SchemaValidator.validate([], schema, repo, obj)).toStrictEqual(obj);
});

test('schema array validator test for additional items with boolean true', () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: 'INTEGER',
        additionalItems: true,
    });
    let obj = [1, 2, 3, 4];
    console.log(schema?.getItems());
    expect(SchemaValidator.validate([], schema, repo, obj)).toStrictEqual(obj);
});

test('schema array validator test for additional items with boolean true different datatype', () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: 'INTEGER',
        additionalItems: true,
    });
    let obj = [1, 2, 3, 4, 'stringtype', true];

    expect(SchemaValidator.validate([], schema, repo, obj)).toStrictEqual(obj);
});

test('schema array validator test for additional items with boolean true', () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: 'INTEGER',
        additionalItems: {
            schemaValue: {
                type: 'STRING',
            },
        },
    });
    let obj = [1, 2, 3, '4'];

    expect(SchemaValidator.validate([], schema, repo, obj)).toStrictEqual(obj);
});

test('schema array validator test for additional items with boolean true different datatype', () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: 'INTEGER',
        additionalItems: {
            schemaValue: {
                name: 'addType',
                type: 'STRING',
            },
        },
    });
    let obj = [1, 2, 3, 'stringtype', true];

    expect(SchemaValidator.validate([], schema, repo, obj)).toStrictEqual(obj);
});
