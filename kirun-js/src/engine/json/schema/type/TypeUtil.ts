import { MultipleType } from './MultipleType';
import { SchemaType } from './SchemaType';
import { SingleType } from './SingleType';
import { Type } from './Type';

export class TypeUtil {
    public static of(...types: SchemaType[]): Type {
        if (types.length == 1) return new SingleType(types[0]);

        return new MultipleType(new Set(types));
    }

    public static from(types: any): Type | undefined {
        if (typeof types === 'string') {
            return new SingleType(
                SchemaType[TypeUtil.fromJSONType(types) as keyof typeof SchemaType],
            );
        } else if (Array.isArray(types)) {
            return new MultipleType(
                new Set(
                    types
                        .map(TypeUtil.fromJSONType)
                        .map((e) => e as keyof typeof SchemaType)
                        .map((e) => SchemaType[e]),
                ),
            );
        }
        return undefined;
    }

    private static fromJSONType(jtype: string): string {
        const nType = jtype.toUpperCase();

        if (nType === 'NUMBER') return 'DOUBLE';

        return nType;
    }
}
