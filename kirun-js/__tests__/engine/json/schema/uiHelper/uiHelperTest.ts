import { Schema } from '../../../../../src/engine/json/schema/Schema';
import { UiHelper } from '../../../../../src/engine/json/schema/uiHelper/UiHelper';

test('setUiHelper() should correctly set the UiHelper instance', () => {
    const schema = new Schema();
    const uiHelper = new UiHelper().setRequiredMessage('This field is required');

    schema.setUiHelper(uiHelper);

    expect(schema.getUiHelper()).toBeInstanceOf(UiHelper);
    expect(schema.getUiHelper()?.getRequiredMessage()).toBe('This field is required');
});

test('getUiHelper() should return undefined if no UiHelper is set', () => {
    const schema = new Schema();
    expect(schema.getUiHelper()).toBeUndefined();
});

test('setUiHelper() should overwrite the existing UiHelper instance', () => {
    const schema = new Schema();
    const uiHelper1 = new UiHelper().setRequiredMessage('First message');
    const uiHelper2 = new UiHelper().setRequiredMessage('Second message');

    schema.setUiHelper(uiHelper1);
    expect(schema.getUiHelper()?.getRequiredMessage()).toBe('First message');

    schema.setUiHelper(uiHelper2);
    expect(schema.getUiHelper()?.getRequiredMessage()).toBe('Second message');
});

test('UiHelper.from() should create an instance if a valid object is provided', () => {
    const obj = { validationMessages: { required: 'Field is required' } };
    const uiHelper = UiHelper.from(obj);

    expect(uiHelper).toBeInstanceOf(UiHelper);
    expect(uiHelper?.getRequiredMessage()).toBe('Field is required');
});

test('UiHelper.from() should return undefined for null object', () => {
    expect(UiHelper.from(null)).toBeUndefined();
});

test('UiHelper.from() should create an instance with an empty object', () => {
    expect(UiHelper.from({})).toBeInstanceOf(UiHelper);
});

test('setValidationMessage() should update validation message', () => {
    const uiHelper = new UiHelper();
    uiHelper.setValidationMessage('required', 'Updated Required Message');

    expect(uiHelper.getValidationMessage('required')).toBe('Updated Required Message');
});

test('getValidationMessage() should return undefined for an unset validation type', () => {
    const uiHelper = new UiHelper();
    expect(uiHelper.getValidationMessage('nonExistentType')).toBeUndefined();
});

test('setValidationMessage() should work correctly with multiple validation types', () => {
    const uiHelper = new UiHelper()
        .setRequiredMessage('Required!')
        .setMinLengthMessage('Minimum 3 characters')
        .setMaxLengthMessage('Maximum 10 characters')
        .setPatternMessage('Invalid pattern!')
        .setMinValueMessage('Value too low')
        .setMaxValueMessage('Value too high')

    expect(uiHelper.getRequiredMessage()).toBe('Required!');
    expect(uiHelper.getMinLengthMessage()).toBe('Minimum 3 characters');
    expect(uiHelper.getMaxLengthMessage()).toBe('Maximum 10 characters');
    expect(uiHelper.getPatternMessage()).toBe('Invalid pattern!');
    expect(uiHelper.getMinValueMessage()).toBe('Value too low');
    expect(uiHelper.getMaxValueMessage()).toBe('Value too high');
});


test('setComponentPreferred() should set and retrieve component Preferred', () => {
    const uiHelper = new UiHelper().setComponentPreferred('RadioButton');

    expect(uiHelper.getComponentPreferred()).toBe('RadioButton');
});

test('UiHelper.from() should initialize componentPreferred if provided', () => {
    const obj = { componentPreferred: 'Dropdown' };
    const uiHelper = UiHelper.from(obj);

    expect(uiHelper?.getComponentPreferred()).toBe('Dropdown');
});
