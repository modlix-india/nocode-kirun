import { SchemaType } from './SchemaType';

export abstract class Type {
    public abstract getAllowedSchemaTypes(): Set<SchemaType>;
    public abstract contains(type: SchemaType): boolean;
}
