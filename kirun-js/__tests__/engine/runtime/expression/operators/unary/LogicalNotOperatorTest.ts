import { LogicalNotOperator } from '../../../../../../src';

test('Logical Not Test', () => {
    expect(new LogicalNotOperator().apply(null)).toBeTruthy();
    expect(new LogicalNotOperator().apply(undefined)).toBeTruthy();
    expect(new LogicalNotOperator().apply(false)).toBeTruthy();
    expect(new LogicalNotOperator().apply(0)).toBeTruthy();

    expect(new LogicalNotOperator().apply(true)).toBeFalsy();
    expect(new LogicalNotOperator().apply(1)).toBeFalsy();

    expect(new LogicalNotOperator().apply({ name: 'Kiran' })).toBeFalsy();

    expect(new LogicalNotOperator().apply([])).toBeFalsy();
    expect(new LogicalNotOperator().apply('')).toBeFalsy();
    expect(new LogicalNotOperator().apply('TRUE')).toBeFalsy();
});
