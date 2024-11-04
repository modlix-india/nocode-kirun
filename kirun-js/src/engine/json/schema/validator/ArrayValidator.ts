import { Repository } from '../../../Repository';
import { isNullValue } from '../../../util/NullCheck';
import { ArraySchemaType } from '../array/ArraySchemaType';
import { Schema } from '../Schema';
import { SchemaValidationException } from './exception/SchemaValidationException';
import { SchemaValidator } from './SchemaValidator';
import { ConversionMode } from '../convertor/enums/ConversionMode';

export class ArrayValidator {
    public static async validate(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        element: any,
        convert?: boolean,
        mode?: ConversionMode,
    ): Promise<any> {
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

        await ArrayValidator.checkItems(parents, schema, repository, array, convert, mode);

        ArrayValidator.checkUniqueItems(parents, schema, array);

        await ArrayValidator.checkContains(schema, parents, repository, array);

        return element;
    }

    private static async checkContains(
        schema: Schema,
        parents: Schema[],
        repository: Repository<Schema> | undefined,
        array: any[],
    ) {
        if (isNullValue(schema.getContains())) return;

        let count = await ArrayValidator.countContains(
            parents,
            schema,
            repository,
            array,
            isNullValue(schema.getMinContains()) && isNullValue(schema.getMaxContains()),
        );

        if (count === 0) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'None of the items are of type contains schema',
            );
        }

        if (!isNullValue(schema.getMinContains()) && schema.getMinContains()! > count)
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'The minimum number of the items of type contains schema should be ' +
                    schema.getMinContains() +
                    ' but found ' +
                    count,
            );

        if (!isNullValue(schema.getMaxContains()) && schema.getMaxContains()! < count)
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'The maximum number of the items of type contains schema should be ' +
                    schema.getMaxContains() +
                    ' but found ' +
                    count,
            );
    }

    public static async countContains(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        array: any[],
        stopOnFirst?: boolean,
    ): Promise<number> {
        let count = 0;
        for (let i = 0; i < array.length; i++) {
            let newParents: Schema[] = !parents ? [] : [...parents];

            try {
                await SchemaValidator.validate(
                    newParents,
                    schema.getContains(),
                    repository,
                    array[i],
                );
                count++;
                if (stopOnFirst) break;
            } catch (err) {}
        }
        return count;
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

    public static async checkItems(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        array: any[],
        convert?: boolean,
        mode?: ConversionMode,
    ) {
        if (!schema.getItems()) return;

        let type: ArraySchemaType = schema.getItems()!;

        if (type.getSingleSchema()) {
            for (let i = 0; i < array.length; i++) {
                let newParents: Schema[] = !parents ? [] : [...parents];
                array[i] = await SchemaValidator.validate(
                    newParents,
                    type.getSingleSchema(),
                    repository,
                    array[i],
                    convert,
                    mode,
                );
            }
        }

        if (type.getTupleSchema()) {
            if (
                type.getTupleSchema()!.length !== array.length &&
                isNullValue(schema?.getAdditionalItems())
            ) {
                throw new SchemaValidationException(
                    SchemaValidator.path(parents),
                    'Expected an array with only ' +
                        type.getTupleSchema()!.length +
                        ' but found ' +
                        array.length,
                );
            }

            await this.checkItemsInTupleSchema(parents, repository, array, type);

            await this.checkAdditionalItems(parents, schema, repository, array, type);
        }
    }

    private static async checkItemsInTupleSchema(
        parents: Schema[],
        repository: Repository<Schema> | undefined,
        array: any[],
        type: ArraySchemaType,
        convert?: boolean,
        mode?: ConversionMode,
    ) {
        for (let i = 0; i < type.getTupleSchema()?.length!; i++) {
            let newParents: Schema[] = !parents ? [] : [...parents];
            array[i] = await SchemaValidator.validate(
                newParents,
                type.getTupleSchema()![i],
                repository,
                array[i],
                convert,
                mode,
            );
        }
    }

    private static async checkAdditionalItems(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        array: any[],
        type: ArraySchemaType,
    ) {
        if (!isNullValue(schema.getAdditionalItems())) {
            let additionalSchemaType = schema.getAdditionalItems();
            if (additionalSchemaType?.getBooleanValue()) {
                //validate the additional items whether schema is valid or not
                let anySchemaType = Schema.ofAny('item'); // as additional items is true it should validate against any valid schema defined in the system
                if (
                    additionalSchemaType?.getBooleanValue() === false &&
                    array.length > type.getTupleSchema()?.length!
                )
                    throw new SchemaValidationException(
                        SchemaValidator.path(parents),
                        'No Additional Items are defined',
                    );

                await this.checkEachItemInAdditionalItems(
                    parents,
                    schema,
                    repository,
                    array,
                    type,
                    anySchemaType,
                );
            } else if (additionalSchemaType?.getSchemaValue()) {
                let schemaType = additionalSchemaType.getSchemaValue();
                await this.checkEachItemInAdditionalItems(
                    parents,
                    schema,
                    repository,
                    array,
                    type,
                    schemaType,
                );
            }
        }
    }

    private static async checkEachItemInAdditionalItems(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        array: any[],
        type: ArraySchemaType,
        schemaType: Schema | undefined,
    ) {
        for (let i = type.getTupleSchema()?.length!; i < array.length; i++) {
            let newParents: Schema[] = !parents ? [] : [...parents];
            array[i] = await SchemaValidator.validate(
                newParents,
                schemaType!,
                repository,
                array[i],
            );
        }
        return;
    }

    private constructor() {}
}
