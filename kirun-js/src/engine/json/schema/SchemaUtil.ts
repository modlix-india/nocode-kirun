import { Repository } from '../../Repository';
import { isNullValue } from '../../util/NullCheck';
import { StringUtil } from '../../util/string/StringUtil';
import { Tuple2 } from '../../util/Tuples';
import { Schema } from './Schema';
import { SchemaType } from './type/SchemaType';
import { SchemaReferenceException } from './validator/exception/SchemaReferenceException';
import { SchemaValidationException } from './validator/exception/SchemaValidationException';

export class SchemaUtil {
    private static readonly UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH: string =
        'Unable to retrive schema from referenced path';

    private static readonly CYCLIC_REFERENCE_LIMIT_COUNTER: number = 20;

    public static async getDefaultValue(
        s: Schema | undefined,
        sRepository: Repository<Schema> | undefined,
    ): Promise<any> {
        if (!s) return undefined;

        if (s.getConstant()) return s.getConstant();

        if (!isNullValue(s.getDefaultValue())) return s.getDefaultValue();

        return SchemaUtil.getDefaultValue(
            await SchemaUtil.getSchemaFromRef(s, sRepository, s.getRef()),
            sRepository,
        );
    }

    public static async hasDefaultValueOrNullSchemaType(
        s: Schema | undefined,
        sRepository: Repository<Schema> | undefined,
    ): Promise<boolean> {
        if (!s) return Promise.resolve(false);
        if (s.getConstant()) return Promise.resolve(true);

        if (!isNullValue(s.getDefaultValue())) return Promise.resolve(true);

        if (isNullValue(s.getRef())) {
            if (s.getType()?.getAllowedSchemaTypes().has(SchemaType.NULL))
                return Promise.resolve(true);
            return Promise.resolve(false);
        }

        return this.hasDefaultValueOrNullSchemaType(
            await SchemaUtil.getSchemaFromRef(s, sRepository, s.getRef()),
            sRepository,
        );
    }

    public static async getSchemaFromRef(
        schema: Schema,
        sRepository: Repository<Schema> | undefined,
        ref: string | undefined,
        iteration: number = 0,
    ): Promise<Schema | undefined> {
        iteration++;
        if (iteration == SchemaUtil.CYCLIC_REFERENCE_LIMIT_COUNTER)
            throw new SchemaValidationException(ref ?? '', 'Schema has a cyclic reference');

        if (!schema || !ref || StringUtil.isNullOrBlank(ref)) return Promise.resolve(undefined);

        if (!ref.startsWith('#')) {
            var tuple = await SchemaUtil.resolveExternalSchema(schema, sRepository, ref);
            if (tuple) {
                schema = tuple.getT1();
                ref = tuple.getT2();
            }
        }

        let parts: string[] = ref.split('/');
        let i: number = 1;

        if (i === parts.length) return Promise.resolve(schema);

        return Promise.resolve(
            SchemaUtil.resolveInternalSchema(schema, sRepository, ref, iteration, parts, i),
        );
    }

    private static async resolveInternalSchema(
        inSchema: Schema,
        sRepository: Repository<Schema> | undefined,
        ref: string,
        iteration: number,
        parts: string[],
        i: number,
    ): Promise<Schema | undefined> {
        let schema: Schema | undefined = inSchema;
        if (i === parts.length) return undefined;
        while (i < parts.length) {
            if (parts[i] === '$defs') {
                i++;

                if (i >= parts.length || !schema.get$defs())
                    throw new SchemaReferenceException(
                        ref,
                        SchemaUtil.UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH,
                    );

                schema = schema.get$defs()?.get(parts[i]);
            } else {
                if (
                    schema &&
                    (!schema.getType()?.contains(SchemaType.OBJECT) || !schema.getProperties())
                )
                    throw new SchemaReferenceException(
                        ref,
                        'Cannot retrievie schema from non Object type schemas',
                    );

                schema = schema.getProperties()?.get(parts[i]);
            }

            i++;

            if (!schema)
                throw new SchemaReferenceException(
                    ref,
                    SchemaUtil.UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH,
                );

            if (!StringUtil.isNullOrBlank(schema.getRef())) {
                schema = await SchemaUtil.getSchemaFromRef(
                    schema,
                    sRepository,
                    schema.getRef(),
                    iteration,
                );
                if (!schema)
                    throw new SchemaReferenceException(
                        ref,
                        SchemaUtil.UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH,
                    );
            }
        }
        return Promise.resolve(schema);
    }

    private static async resolveExternalSchema(
        inSchem: Schema,
        sRepository: Repository<Schema> | undefined,
        ref: string,
    ): Promise<Tuple2<Schema, string> | undefined> {
        if (!sRepository) return Promise.resolve(undefined);

        let nms = StringUtil.splitAtFirstOccurance(ref ?? '', '/');
        if (!nms[0]) return Promise.resolve(undefined);

        let nmspnm = StringUtil.splitAtLastOccurance(nms[0], '.');
        if (!nmspnm[0] || !nmspnm[1]) return Promise.resolve(undefined);

        let schema = await sRepository.find(nmspnm[0], nmspnm[1]);
        if (!schema) return Promise.resolve(undefined);
        if (!nms[1] || nms[1] === '') return Promise.resolve(new Tuple2(schema, ref));

        ref = '#/' + nms[1];

        if (!schema)
            throw new SchemaReferenceException(
                ref,
                SchemaUtil.UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH,
            );

        return Promise.resolve(new Tuple2(schema, ref));
    }

    private constructor() {}
}
