import { AdditionalType, Schema } from '../../../../src';

// The Schema copy constructor used to read `not` and `contains` from the object being
// built instead of the source, so a copy lost `not` and turned `contains` into an empty schema.
test('Schema copy keeps not and contains', () => {
    const source = Schema.ofArray('list', Schema.ofString('item'))
        .setNot(Schema.ofString('forbidden'))
        .setContains(Schema.ofString('needed'));

    const copy = new Schema(source);

    expect(copy.getNot()?.getName()).toBe('forbidden');
    expect(copy.getNot()).not.toBe(source.getNot());
    expect(copy.getContains()?.getName()).toBe('needed');
});

test('Schema copy keeps a boolean additionalProperties', () => {
    const source = Schema.ofObject('row').setAdditionalProperties(
        new AdditionalType().setBooleanValue(false),
    );

    const copy = new Schema(source);

    expect(copy.getAdditionalProperties()?.getBooleanValue()).toBe(false);
    expect(copy.getAdditionalProperties()?.getSchemaValue()).toBeUndefined();
});
