import { SchemaType } from './SchemaType';
import { Type } from './Type';

export class MultipleType extends Type {
    private type: Set<SchemaType>;

    public constructor(type: Set<SchemaType> | MultipleType) {
        super();

        if (type instanceof MultipleType)
            this.type = new Set(Array.from((type as MultipleType).type));
        else this.type = new Set(Array.from(type));
    }

    public getType(): Set<SchemaType> {
        return this.type;
    }

    public setType(type: Set<SchemaType>): MultipleType {
        this.type = type;
        return this;
    }

    public getAllowedSchemaTypes(): Set<SchemaType> {
        return this.type;
    }

    public contains(type: SchemaType): boolean {
        return this.type?.has(type);
    }
}
