export class UiHelper {
    private validationMessages: { [key: string]: string } = {};
    private componentPriority?: string;

    constructor(obj?: any) {
        this.validationMessages = obj?.validationMessages || {};
        this.componentPriority = obj?.componentPriority;
    }

    public setValidationMessage(type: string, message: string): UiHelper {
        this.validationMessages[type] = message;
        return this;
    }

    public getValidationMessage(type: string): string | undefined {
        return this.validationMessages[type];
    }

    public setRequiredMessage(message: string): UiHelper {
        return this.setValidationMessage('required', message);
    }

    public getRequiredMessage(): string | undefined {
        return this.getValidationMessage('required');
    }

    public setMinLengthMessage(message: string): UiHelper {
        return this.setValidationMessage('minLength', message);
    }

    public getMinLengthMessage(): string | undefined {
        return this.getValidationMessage('minLength');
    }

    public setMaxLengthMessage(message: string): UiHelper {
        return this.setValidationMessage('maxLength', message);
    }

    public getMaxLengthMessage(): string | undefined {
        return this.getValidationMessage('maxLength');
    }

    public setPatternMessage(message: string): UiHelper {
        return this.setValidationMessage('pattern', message);
    }

    public getPatternMessage(): string | undefined {
        return this.getValidationMessage('pattern');
    }

    public setFormatMessage(message: string): UiHelper {
        return this.setValidationMessage('format', message);
    }

    public getFormatMessage(): string | undefined {
        return this.getValidationMessage('format');
    }

    public setMinValueMessage(message: string): UiHelper {
        return this.setValidationMessage('minimum', message);
    }

    public getMinValueMessage(): string | undefined {
        return this.getValidationMessage('minimum');
    }

    public setMaxValueMessage(message: string): UiHelper {
        return this.setValidationMessage('maximum', message);
    }

    public getMaxValueMessage(): string | undefined {
        return this.getValidationMessage('maximum');
    }

    public setExclusiveMaxMessage(message: string): UiHelper {
        return this.setValidationMessage('exclusiveMaximum', message);
    }

    public getExclusiveMaxMessage(): string | undefined {
        return this.getValidationMessage('exclusiveMaximum');
    }

    public setExclusiveMinMessage(message: string): UiHelper {
        return this.setValidationMessage('exclusiveMinimum', message);
    }

    public getExclusiveMinimumMessage(): string | undefined {
        return this.getValidationMessage('exclusiveMinimum');
    }

    public setMultipleOfMessage(message: string): UiHelper {
        return this.setValidationMessage('multipleOf', message);
    }
    public getMultipleOfMessage(): string | undefined {
        return this.getValidationMessage('multipleOf');
    }

    public getComponentPriority(): string | undefined {
        return this.componentPriority;
    }

    public setComponentPriority(component: string): UiHelper {
        this.componentPriority = component;
        return this;
    }

    public static from(obj: any): UiHelper | undefined {
        return obj ? new UiHelper(obj) : undefined;
    }

}
