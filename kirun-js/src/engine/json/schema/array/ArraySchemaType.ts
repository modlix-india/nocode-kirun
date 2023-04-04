import { isNullValue } from '../../../util/NullCheck';
import { Schema } from '../Schema';

export class ArraySchemaType {
    private singleSchema: Schema | undefined;
    private tupleSchema: Schema[] | undefined;

    public constructor(ast?: ArraySchemaType) {
        if (!ast) return;
        this.singleSchema = ast.singleSchema ? new Schema(ast.singleSchema) : undefined;
        this.tupleSchema = ast.tupleSchema ? ast.tupleSchema.map((e) => new Schema(e)) : undefined;
    }

    public setSingleSchema(schema: Schema): ArraySchemaType {
        this.singleSchema = schema;
        return this;
    }

    public setTupleSchema(schemas: Schema[]): ArraySchemaType {
        this.tupleSchema = schemas;
        return this;
    }

    public getSingleSchema(): Schema | undefined {
        return this.singleSchema;
    }

    public getTupleSchema(): Schema[] | undefined {
        return this.tupleSchema;
    }

    public isSingleType(): boolean {
        return !isNullValue(this.singleSchema);
    }

    public static of(...schemas: Schema[]): ArraySchemaType {
        if (schemas.length == 1) return new ArraySchemaType().setSingleSchema(schemas[0]);

        return new ArraySchemaType().setTupleSchema(schemas);
    }

    public static from(obj: any): ArraySchemaType | undefined {
        if (!obj) return undefined;
        if (Array.isArray(obj))
            return new ArraySchemaType().setTupleSchema(Schema.fromListOfSchemas(obj)!);

        let x = Schema.from(obj);
        if (!x) return undefined;
        return new ArraySchemaType().setSingleSchema(x);
    }
}
