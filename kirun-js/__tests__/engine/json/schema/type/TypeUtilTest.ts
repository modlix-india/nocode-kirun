import { SchemaType, TypeUtil } from '../../../../../src';

test('TypeUtilTest', () => {
    expect(TypeUtil.from('OBJECT')?.contains(SchemaType.OBJECT)).toBeTruthy();

    let type = TypeUtil.from(['OBJECT', 'STRING']);

    expect(type?.contains(SchemaType.STRING)).toBeTruthy();
    expect(type?.contains(SchemaType.OBJECT)).toBeTruthy();
});
