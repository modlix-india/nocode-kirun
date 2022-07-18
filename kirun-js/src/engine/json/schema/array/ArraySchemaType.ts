import { Schema } from '../Schema';

export class ArraySchemaType {
    private singleSchema: Schema;
    private tupleSchema: Schema[];

    public setSingleSchema(schema: Schema): ArraySchemaType {
        this.singleSchema = this.singleSchema;
        return this;
    }

    public setTupleSchema(schemas: Schema[]): ArraySchemaType {
        this.tupleSchema = schemas;
        return this;
    }

    public getSingleSchema(): Schema {
        return this.singleSchema;
    }

    public getTupleSchema(): Schema[] {
        return this.tupleSchema;
    }

    public static of(...schemas: Schema[]): ArraySchemaType {
        if (schemas.length == 1) return new ArraySchemaType().setSingleSchema(schemas[0]);

        return new ArraySchemaType().setTupleSchema(schemas);
    }
}
