import { Schema } from '../json/schema/Schema';
import { SchemaType } from '../json/schema/type/SchemaType';
import { TypeUtil } from '../json/schema/type/TypeUtil';
import { Namespaces } from '../namespaces/Namespaces';

export class Position {
    public static readonly SCHEMA_NAME: string = 'Position';
    public static readonly SCHEMA: Schema = new Schema()
        .setNamespace(Namespaces.SYSTEM)
        .setName(Position.SCHEMA_NAME)
        .setType(TypeUtil.of(SchemaType.OBJECT))
        .setProperties(
            new Map([
                ['left', Schema.ofFloat('left')],
                ['top', Schema.ofFloat('top')],
            ]),
        );
    private left: number;
    private top: number;

    public constructor(left: number, top: number) {
        this.left = left;
        this.top = top;
    }

    public getLeft(): number {
        return this.left;
    }
    public setLeft(left: number): Position {
        this.left = left;
        return this;
    }
    public getTop(): number {
        return this.top;
    }
    public setTop(top: number): Position {
        this.top = top;
        return this;
    }

    public static from(json: any): Position {
        if (!json) return new Position(-1, -1);
        return new Position(json.left, json.top);
    }
}
