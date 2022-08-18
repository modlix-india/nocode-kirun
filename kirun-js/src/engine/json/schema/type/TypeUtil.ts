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
            return TypeUtil.of(types as SchemaType);
        } else if (Array.isArray(types)) {
            return TypeUtil.of(
                ...Array.of(types)
                    .map((e) => e as any)
                    .map((e) => e as SchemaType),
            );
        }
        return undefined;
    }
}
