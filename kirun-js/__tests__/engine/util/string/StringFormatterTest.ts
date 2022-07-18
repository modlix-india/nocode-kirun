import { StringFormatter } from '../../../../src/engine/util/string/StringFormatter';

test('StringFormatter Test', () => {
    expect(StringFormatter.format('Hello $', 'Kiran')).toBe('Hello Kiran');
    expect(StringFormatter.format('\\$Hello $', 'Kiran')).toBe('$Hello Kiran');
    expect(StringFormatter.format('Hi Hello How are you $?')).toBe('Hi Hello How are you $?');
    expect(StringFormatter.format('Hi Hello How are you $$$$', '1', '2', '3')).toBe(
        'Hi Hello How are you 123$',
    );
    expect(StringFormatter.format('Hi Hello How are you \\$$$$', '1', '2', '3')).toBe(
        'Hi Hello How are you $123',
    );
    expect(StringFormatter.format('Hi Hello How are you $$$\\$', '1', '2')).toBe(
        'Hi Hello How are you 12$$',
    );
    expect(StringFormatter.format('Extra closing $ found', '}')).toBe('Extra closing } found');
});
