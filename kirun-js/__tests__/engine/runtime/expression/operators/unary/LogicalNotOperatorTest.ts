import {
    LogicalGreaterThanEqualOperator,
    LogicalGreaterThanOperator,
    LogicalLessThanOperator,
    LogicalLessThanEqualOperator,
    LogicalNotOperator,
} from '../../../../../../src';

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

test('Logical Greate than', () => {
    expect(new LogicalGreaterThanOperator().apply(0, 4)).toBeFalsy();
    expect(new LogicalLessThanOperator().apply(0, 4)).toBeTruthy();
    expect(new LogicalGreaterThanEqualOperator().apply(0, 0)).toBeTruthy();
    expect(new LogicalLessThanEqualOperator().apply(0, 0)).toBeTruthy();
});
