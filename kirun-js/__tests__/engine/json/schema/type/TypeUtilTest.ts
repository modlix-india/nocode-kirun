import { SchemaType, TypeUtil } from '../../../../../src';

test('TypeUtilTest', () => {
    expect(TypeUtil.from('Object')?.contains(SchemaType.OBJECT)).toBeTruthy();

    let type = TypeUtil.from(['Object', 'String']);

    expect(type?.contains(SchemaType.STRING)).toBeTruthy();
    expect(type?.contains(SchemaType.OBJECT)).toBeTruthy();
});
