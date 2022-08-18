import { Schema } from '../Schema';

export class AdditionalPropertiesType {
    private booleanValue: boolean;
    private schemaValue: Schema;

    public getBooleanValue(): boolean {
        return this.booleanValue;
    }

    public getSchemaValue(): Schema {
        return this.schemaValue;
    }

    public setBooleanValue(booleanValue: boolean): AdditionalPropertiesType {
        this.booleanValue = booleanValue;
        return this;
    }

    public setSchemaValue(schemaValue: Schema): AdditionalPropertiesType {
        this.schemaValue = schemaValue;
        return this;
    }

    public static from(obj: any): AdditionalPropertiesType | undefined {
        if (!obj) return undefined;
        const ad = new AdditionalPropertiesType();
        ad.booleanValue = obj.booleanValue;
        ad.schemaValue = obj.schemaValue;
        return ad;
    }
}
