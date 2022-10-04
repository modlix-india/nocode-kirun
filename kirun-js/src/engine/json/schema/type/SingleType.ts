import { SchemaType } from './SchemaType';
import { Type } from './Type';

export class SingleType extends Type {
    private type: SchemaType;

    public constructor(type: SchemaType | SingleType) {
        super();

        if (type instanceof SingleType) this.type = (type as SingleType).type;
        else this.type = type as SchemaType;
    }

    public getType(): SchemaType {
        return this.type;
    }

    public getAllowedSchemaTypes(): Set<SchemaType> {
        return new Set([this.type]);
    }

    public contains(type: SchemaType): boolean {
        return this.type == type;
    }
}
