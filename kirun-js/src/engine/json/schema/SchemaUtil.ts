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

    public static getDefaultValue(
        s: Schema | undefined,
        sRepository: Repository<Schema> | undefined,
    ): any {
        if (!s) return undefined;

        if (s.getConstant()) return s.getConstant();

        if (!isNullValue(s.getDefaultValue())) return s.getDefaultValue();

        return SchemaUtil.getDefaultValue(
            SchemaUtil.getSchemaFromRef(s, sRepository, s.getRef()),
            sRepository,
        );
    }

    public static getSchemaFromRef(
        schema: Schema,
        sRepository: Repository<Schema> | undefined,
        ref: string | undefined,
        iteration: number = 0,
    ): Schema | undefined {
        iteration++;

        if (iteration == SchemaUtil.CYCLIC_REFERENCE_LIMIT_COUNTER)
            throw new SchemaValidationException(ref ?? '', 'Schema has a cyclic reference');

        if (!schema || !ref || StringUtil.isNullOrBlank(ref)) return undefined;

        if (!ref.startsWith('#')) {
            var tuple = SchemaUtil.resolveExternalSchema(schema, sRepository, ref);
            if (tuple) {
                schema = tuple.getT1();
                ref = tuple.getT2();
            }
        }

        let parts: string[] = ref.split('/');
        let i: number = 1;

        if (i === parts.length) return schema;

        return SchemaUtil.resolveInternalSchema(schema, sRepository, ref, iteration, parts, i);
    }

    private static resolveInternalSchema(
        inSchema: Schema,
        sRepository: Repository<Schema> | undefined,
        ref: string,
        iteration: number,
        parts: string[],
        i: number,
    ): Schema | undefined {
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
                schema = SchemaUtil.getSchemaFromRef(
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
        return schema;
    }

    private static resolveExternalSchema(
        inSchem: Schema,
        sRepository: Repository<Schema> | undefined,
        ref: string,
    ): Tuple2<Schema, string> | undefined {
        if (!sRepository) return undefined;

        let nms = StringUtil.splitAtFirstOccurance(ref ?? '', '/');
        if (!nms[0]) return undefined;

        let nmspnm = StringUtil.splitAtFirstOccurance(nms[0], '.');
        if (!nmspnm[0] || !nmspnm[1]) return undefined;

        let schema = sRepository.find(nmspnm[0], nmspnm[1]);
        if (!schema) return undefined;
        if (!nms[1] || nms[1] === '') return new Tuple2(schema, ref);

        ref = '#/' + nms[1];

        if (!schema)
            throw new SchemaReferenceException(
                ref,
                SchemaUtil.UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH,
            );

        return new Tuple2(schema, ref);
    }

    private constructor() {}
}
