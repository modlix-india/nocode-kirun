import { Repository } from '../../Repository';
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

    public static getDefaultValue(s: Schema, sRepository: Repository<Schema>): any {
        if (!s) return null;

        if (s.getConstant()) return s.getConstant();

        if (s.getDefaultValue()) return s.getDefaultValue();

        return SchemaUtil.getDefaultValue(
            SchemaUtil.getSchemaFromRef(s, sRepository, s.getRef()),
            sRepository,
        );
    }

    public static getSchemaFromRef(
        schema: Schema,
        sRepository: Repository<Schema>,
        ref: string,
        iteration: number = 0,
    ): Schema {
        iteration++;

        if (iteration == SchemaUtil.CYCLIC_REFERENCE_LIMIT_COUNTER)
            throw new SchemaValidationException(ref, 'Schema has a cyclic reference');

        if (!schema || !ref || ref === '') return undefined;

        if (!ref.startsWith('#')) {
            var tuple = SchemaUtil.resolveExternalSchema(schema, sRepository, ref);
            schema = tuple.getT1();
            ref = tuple.getT2();
        }

        let parts: string[] = ref.split('/');
        let i: number = 1;

        schema = SchemaUtil.resolveInternalSchema(schema, sRepository, ref, iteration, parts, i);

        return schema;
    }

    private static resolveInternalSchema(
        schema: Schema,
        sRepository: Repository<Schema>,
        ref: string,
        iteration: number,
        parts: string[],
        i: number,
    ): Schema {
        while (i < parts.length) {
            if (parts[i] === '$defs') {
                i++;

                if (i >= parts.length || !schema.get$defs())
                    throw new SchemaReferenceException(
                        ref,
                        SchemaUtil.UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH,
                    );

                schema = schema.get$defs().get(parts[i]);
            } else {
                if (!schema.getType().contains(SchemaType.OBJECT) || !schema.getProperties())
                    throw new SchemaReferenceException(
                        ref,
                        'Cannot retrievie schema from non Object type schemas',
                    );

                schema = schema.getProperties().get(parts[i]);
            }

            i++;

            if (!schema)
                throw new SchemaReferenceException(
                    ref,
                    SchemaUtil.UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH,
                );

            if (schema.getRef() !== null && schema.getRef() !== '') {
                schema = SchemaUtil.getSchemaFromRef(
                    schema,
                    sRepository,
                    schema.getRef(),
                    iteration,
                );
                if (schema == null)
                    throw new SchemaReferenceException(
                        ref,
                        SchemaUtil.UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH,
                    );
            }
        }
        return schema;
    }

    private static resolveExternalSchema(
        schema: Schema,
        sRepository: Repository<Schema>,
        ref: string,
    ): Tuple2<Schema, string> {
        let nms: string[] = StringUtil.splitAtFirstOccurance(schema.getRef(), '/');
        let nmspnm: string[] = StringUtil.splitAtFirstOccurance(nms[0], '.');

        schema = sRepository.find(nmspnm[0], nmspnm[1]);
        if (!nms[1] || nms[1] === '') return new Tuple2(schema, ref);

        ref = '#/' + nms[1];

        if (schema == null)
            throw new SchemaReferenceException(
                ref,
                SchemaUtil.UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH,
            );

        return new Tuple2(schema, ref);
    }

    private constructor() {}
}
