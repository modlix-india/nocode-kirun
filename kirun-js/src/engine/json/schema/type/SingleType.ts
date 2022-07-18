import { SchemaType } from './SchemaType';
import { Type } from './Type';

export class SingleType extends Type {
    private type: SchemaType;

    constructor(type: SchemaType) {
        super();
        this.type = type;
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
