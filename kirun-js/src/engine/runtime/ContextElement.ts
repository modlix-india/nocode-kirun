import { Schema } from '../json/schema/Schema';

export class ContextElement {
    public static readonly NULL: ContextElement = new ContextElement(Schema.NULL, undefined);

    private schema: Schema;
    private element: any;

    public constructor(schema?: Schema, element?: any) {
        this.schema = schema;
        this.element = element;
    }

    public getSchema(): Schema {
        return this.schema;
    }
    public setSchema(schema: Schema): ContextElement {
        this.schema = schema;
        return this;
    }
    public getElement(): any {
        return this.element;
    }
    public setElement(element: any): ContextElement {
        this.element = element;
        return this;
    }
}
