from __future__ import annotations
from typing import Any, List, Optional, TYPE_CHECKING

from kirun_py.json.schema.validator.exception.schema_validation_exception import SchemaValidationException
from kirun_py.util.error_message_formatter import ErrorMessageFormatter
from kirun_py.util.null_check import is_null_value

if TYPE_CHECKING:
    from kirun_py.json.schema.schema import Schema
    from kirun_py.repository import Repository


class ArrayValidator:

    @staticmethod
    async def validate(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        element: Any,
    ) -> Any:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        if is_null_value(element):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'Expected an array but found null',
            )

        if not isinstance(element, list):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                f'Expected an array but found {ErrorMessageFormatter.format_value(element)}',
            )

        array: list = element

        ArrayValidator._check_min_max_items(parents, schema, array)

        await ArrayValidator._check_items(parents, schema, repository, array)

        ArrayValidator._check_unique_items(parents, schema, array)

        await ArrayValidator._check_contains(schema, parents, repository, array)

        return element

    @staticmethod
    async def _check_contains(
        schema: Schema,
        parents: List[Schema],
        repository: Optional[Repository],
        array: list,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        if is_null_value(schema.get_contains()):
            return

        stop_on_first = (
            is_null_value(schema.get_min_contains())
            and is_null_value(schema.get_max_contains())
        )

        count = await ArrayValidator._count_contains(
            parents, schema, repository, array, stop_on_first,
        )

        if count == 0:
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'None of the items are of type contains schema',
            )

        if (
            not is_null_value(schema.get_min_contains())
            and schema.get_min_contains() > count
        ):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'The minimum number of the items of type contains schema should be '
                + str(schema.get_min_contains())
                + ' but found '
                + str(count),
            )

        if (
            not is_null_value(schema.get_max_contains())
            and schema.get_max_contains() < count
        ):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'The maximum number of the items of type contains schema should be '
                + str(schema.get_max_contains())
                + ' but found '
                + str(count),
            )

    @staticmethod
    async def _count_contains(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        array: list,
        stop_on_first: bool = False,
    ) -> int:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        count = 0
        for i in range(len(array)):
            new_parents: List[Schema] = list(parents) if parents else []
            try:
                await SchemaValidator.validate(
                    new_parents,
                    schema.get_contains(),
                    repository,
                    array[i],
                )
                count += 1
                if stop_on_first:
                    break
            except SchemaValidationException:
                pass
        return count

    @staticmethod
    def _check_unique_items(
        parents: List[Schema],
        schema: Schema,
        array: list,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        if schema.get_unique_items():
            # Use a list-based uniqueness check since items may not be hashable
            seen: list = []
            for item in array:
                for s in seen:
                    if item == s:
                        raise SchemaValidationException(
                            SchemaValidator.path(parents),
                            'Items on the array are not unique',
                        )
                seen.append(item)

    @staticmethod
    def _check_min_max_items(
        parents: List[Schema],
        schema: Schema,
        array: list,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        if (
            schema.get_min_items() is not None
            and schema.get_min_items() > len(array)
        ):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'Array should have minimum of ' + str(schema.get_min_items()) + ' elements',
            )

        if (
            schema.get_max_items() is not None
            and schema.get_max_items() < len(array)
        ):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'Array can have  maximum of ' + str(schema.get_max_items()) + ' elements',
            )

    @staticmethod
    async def _check_items(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        array: list,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator
        from kirun_py.json.schema.array.array_schema_type import ArraySchemaType

        if not schema.get_items():
            return

        items_type: ArraySchemaType = schema.get_items()

        if items_type.get_single_schema():
            for i in range(len(array)):
                new_parents: List[Schema] = list(parents) if parents else []
                array[i] = await SchemaValidator.validate(
                    new_parents,
                    items_type.get_single_schema(),
                    repository,
                    array[i],
                )

        if items_type.get_tuple_schema():
            tuple_schema = items_type.get_tuple_schema()

            if (
                len(tuple_schema) != len(array)
                and is_null_value(schema.get_additional_items())
            ):
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    'Expected an array with only '
                    + str(len(tuple_schema))
                    + ' but found '
                    + str(len(array)),
                )

            await ArrayValidator._check_items_in_tuple_schema(
                parents, repository, array, items_type,
            )

            await ArrayValidator._check_additional_items(
                parents, schema, repository, array, items_type,
            )

    @staticmethod
    async def _check_items_in_tuple_schema(
        parents: List[Schema],
        repository: Optional[Repository],
        array: list,
        items_type: Any,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        tuple_schema = items_type.get_tuple_schema()
        for i in range(len(tuple_schema)):
            new_parents: List[Schema] = list(parents) if parents else []
            array[i] = await SchemaValidator.validate(
                new_parents,
                tuple_schema[i],
                repository,
                array[i],
            )

    @staticmethod
    async def _check_additional_items(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        array: list,
        items_type: Any,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator
        from kirun_py.json.schema.schema import Schema as SchemaClass

        if is_null_value(schema.get_additional_items()):
            return

        additional_schema_type = schema.get_additional_items()
        tuple_len = len(items_type.get_tuple_schema())

        if additional_schema_type.get_boolean_value() is not None:
            if additional_schema_type.get_boolean_value() is False and len(array) > tuple_len:
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    'No Additional Items are defined',
                )

            # additional items is true => validate against any schema
            any_schema_type = SchemaClass.of_any('item')
            await ArrayValidator._check_each_item_in_additional_items(
                parents, schema, repository, array, items_type, any_schema_type,
            )
        elif additional_schema_type.get_schema_value():
            schema_type = additional_schema_type.get_schema_value()
            await ArrayValidator._check_each_item_in_additional_items(
                parents, schema, repository, array, items_type, schema_type,
            )

    @staticmethod
    async def _check_each_item_in_additional_items(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        array: list,
        items_type: Any,
        schema_type: Schema,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        tuple_len = len(items_type.get_tuple_schema())
        for i in range(tuple_len, len(array)):
            new_parents: List[Schema] = list(parents) if parents else []
            array[i] = await SchemaValidator.validate(
                new_parents,
                schema_type,
                repository,
                array[i],
            )
