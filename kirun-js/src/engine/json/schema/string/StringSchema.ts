import { Schema } from '../Schema';
import { SchemaType } from '../type/SchemaType';
import { SingleType } from '../type/SingleType';
import { Type } from '../type/Type';

export class StringSchema extends Schema {
    private static readonly TYPE: Type = new SingleType(SchemaType.STRING);

    public getType(): Type | undefined {
        return StringSchema.TYPE;
    }
}
