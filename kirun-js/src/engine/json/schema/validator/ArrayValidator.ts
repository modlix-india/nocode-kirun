import { Repository } from '../../../Repository';
import { isNullValue } from '../../../util/NullCheck';
import { ArraySchemaType } from '../array/ArraySchemaType';
import { Schema } from '../Schema';
import { SchemaValidationException } from './exception/SchemaValidationException';
import { SchemaValidator } from './SchemaValidator';

export class ArrayValidator {
    public static validate(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema>,
        element: any,
    ): any {
        if (isNullValue(element))
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Expected an array but found null',
            );

        if (!Array.isArray(element))
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                element.toString() + ' is not an Array',
            );

        let array: any[] = element as any[];

        ArrayValidator.checkMinMaxItems(parents, schema, array);

        ArrayValidator.checkItems(parents, schema, repository, array);

        ArrayValidator.checkUniqueItems(parents, schema, array);

        ArrayValidator.checkContains(parents, schema, repository, array);

        return element;
    }

    public static checkContains(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema>,
        array: any[],
    ) {
        if (!schema.getContains()) return;

        let flag: boolean = false;
        for (let i = 0; i < array.length; i++) {
            let newParents: Schema[] = !parents ? [] : [...parents];

            try {
                SchemaValidator.validate(newParents, schema.getContains(), repository, array[i]);
                flag = true;
                break;
            } catch (err) {
                flag = false;
            }
        }

        if (!flag) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'None of the items are of type contains schema',
            );
        }
    }

    public static checkUniqueItems(parents: Schema[], schema: Schema, array: any[]): void {
        if (schema.getUniqueItems() && schema.getUniqueItems()) {
            let set: Set<any> = new Set<any>(array);

            if (set.size !== array.length)
                throw new SchemaValidationException(
                    SchemaValidator.path(parents),
                    'Items on the array are not unique',
                );
        }
    }

    public static checkMinMaxItems(parents: Schema[], schema: Schema, array: any[]): void {
        if (schema.getMinItems() && schema.getMinItems()! > array.length) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Array should have minimum of ' + schema.getMinItems() + ' elements',
            );
        }

        if (schema.getMaxItems() && schema.getMaxItems()! < array.length) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Array can have  maximum of ' + schema.getMaxItems() + ' elements',
            );
        }
    }

    public static checkItems(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema>,
        array: any[],
    ) {
        if (!schema.getItems()) return;

        let type: ArraySchemaType = schema.getItems()!;

        if (type.getSingleSchema()) {
            for (let i = 0; i < array.length; i++) {
                let newParents: Schema[] = !parents ? [] : [...parents];
                let element: any = SchemaValidator.validate(
                    newParents,
                    type.getSingleSchema(),
                    repository,
                    array[i],
                );
                array[i] = element;
            }
        }

        if (type.getTupleSchema()) {
            if (type.getTupleSchema()!.length !== array.length) {
                throw new SchemaValidationException(
                    SchemaValidator.path(parents),
                    'Expected an array with only ' +
                        type.getTupleSchema()!.length +
                        ' but found ' +
                        array.length,
                );
            }

            for (let i = 0; i < array.length; i++) {
                let newParents: Schema[] = !parents ? [] : [...parents];
                let element: any = SchemaValidator.validate(
                    newParents,
                    type.getTupleSchema()![i],
                    repository,
                    array[i],
                );
                array[i] = element;
            }
        }
    }

    private constructor() {}
}
