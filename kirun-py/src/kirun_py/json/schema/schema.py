from __future__ import annotations
import copy
import json
from typing import Any, Dict, List, Optional, Set, TYPE_CHECKING

from kirun_py.json.schema.array.array_schema_type import ArraySchemaType
from kirun_py.json.schema.string.string_format import StringFormat
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_util import TypeUtil
from kirun_py.json.schema.type.type_ import Type
from kirun_py.json.schema.type.single_type import SingleType
from kirun_py.json.schema.type.multiple_type import MultipleType
from kirun_py.util.null_check import is_null_value

TEMPORARY = '_'


class AdditionalType:
    def __init__(self, apt: Optional[AdditionalType] = None):
        self._boolean_value: Optional[bool] = None
        self._schema_value: Optional[Schema] = None
        if apt is not None:
            self._boolean_value = apt._boolean_value
            if apt._schema_value is not None:
                self._schema_value = Schema(apt._schema_value)

    def get_boolean_value(self) -> Optional[bool]:
        return self._boolean_value

    def get_schema_value(self) -> Optional[Schema]:
        return self._schema_value

    def set_boolean_value(self, value: bool) -> AdditionalType:
        self._boolean_value = value
        return self

    def set_schema_value(self, value: Schema) -> AdditionalType:
        self._schema_value = value
        return self

    @staticmethod
    def from_value(obj: Any) -> Optional[AdditionalType]:
        if is_null_value(obj):
            return None
        ad = AdditionalType()
        if isinstance(obj, bool):
            ad._boolean_value = obj
        elif isinstance(obj, dict):
            keys = list(obj.keys())
            if 'booleanValue' in keys:
                ad._boolean_value = obj['booleanValue']
            elif 'schemaValue' in keys:
                ad._schema_value = Schema.from_value(obj['schemaValue'])
            else:
                ad._schema_value = Schema.from_value(obj)
        return ad


class SchemaDetails:
    def __init__(self, sd: Optional[SchemaDetails] = None):
        self._preferred_component: Optional[str] = None
        self._validation_messages: Optional[Dict[str, str]] = None
        self._properties: Optional[Dict[str, Any]] = None
        self._style_properties: Optional[Dict[str, Any]] = None
        self._order: Optional[int] = None
        self._label: Optional[str] = None

        if sd is not None:
            self._preferred_component = sd._preferred_component
            if sd._validation_messages:
                self._validation_messages = dict(sd._validation_messages)
            if sd._properties:
                self._properties = dict(sd._properties)
            if sd._style_properties:
                self._style_properties = dict(sd._style_properties)
            self._order = sd._order
            self._label = sd._label

    def get_preferred_component(self) -> Optional[str]:
        return self._preferred_component

    def set_preferred_component(self, comp: Optional[str]) -> SchemaDetails:
        self._preferred_component = comp
        return self

    def get_validation_messages(self) -> Optional[Dict[str, str]]:
        return self._validation_messages

    def set_validation_messages(self, messages: Optional[Dict[str, str]]) -> SchemaDetails:
        self._validation_messages = messages
        return self

    def get_validation_message(self, key: str) -> Optional[str]:
        if self._validation_messages is None:
            return None
        return self._validation_messages.get(key)

    def set_properties(self, properties: Optional[Dict[str, Any]]) -> SchemaDetails:
        self._properties = properties
        return self

    def get_properties(self) -> Optional[Dict[str, Any]]:
        return self._properties

    def set_style_properties(self, style_properties: Optional[Dict[str, Any]]) -> SchemaDetails:
        self._style_properties = style_properties
        return self

    def get_style_properties(self) -> Optional[Dict[str, Any]]:
        return self._style_properties

    def get_order(self) -> Optional[int]:
        return self._order

    def set_order(self, order: Optional[int]) -> SchemaDetails:
        self._order = order
        return self

    def get_label(self) -> Optional[str]:
        return self._label

    def set_label(self, label: Optional[str]) -> SchemaDetails:
        self._label = label
        return self

    @staticmethod
    def from_value(detail: Any) -> Optional[SchemaDetails]:
        if not detail:
            return None
        sd = SchemaDetails()
        sd.set_preferred_component(detail.get('preferredComponent'))
        vm = detail.get('validationMessages')
        sd.set_validation_messages(dict(vm) if vm else None)
        props = detail.get('properties')
        sd.set_properties(dict(props) if props else None)
        sp = detail.get('styleProperties')
        sd.set_style_properties(dict(sp) if sp else None)
        sd.set_order(detail.get('order'))
        sd.set_label(detail.get('label'))
        return sd


class Schema:

    def __init__(self, schema: Optional[Schema] = None):
        self._namespace: str = TEMPORARY
        self._name: Optional[str] = None
        self._version: int = 1
        self._ref: Optional[str] = None
        self._type: Optional[Type] = None
        self._any_of: Optional[List[Schema]] = None
        self._all_of: Optional[List[Schema]] = None
        self._one_of: Optional[List[Schema]] = None
        self._not: Optional[Schema] = None
        self._description: Optional[str] = None
        self._examples: Optional[List[Any]] = None
        self._default_value: Any = None
        self._comment: Optional[str] = None
        self._enums: Optional[List[Any]] = None
        self._constant: Any = None

        # String
        self._pattern: Optional[str] = None
        self._format: Optional[StringFormat] = None
        self._min_length: Optional[int] = None
        self._max_length: Optional[int] = None

        # Number
        self._multiple_of: Optional[int] = None
        self._minimum: Optional[float] = None
        self._maximum: Optional[float] = None
        self._exclusive_minimum: Optional[float] = None
        self._exclusive_maximum: Optional[float] = None

        # Object
        self._properties: Optional[Dict[str, Schema]] = None
        self._additional_properties: Optional[AdditionalType] = None
        self._required: Optional[List[str]] = None
        self._property_names: Optional[Schema] = None
        self._min_properties: Optional[int] = None
        self._max_properties: Optional[int] = None
        self._pattern_properties: Optional[Dict[str, Schema]] = None

        # Array
        self._items: Optional[ArraySchemaType] = None
        self._additional_items: Optional[AdditionalType] = None
        self._contains: Optional[Schema] = None
        self._min_contains: Optional[int] = None
        self._max_contains: Optional[int] = None
        self._min_items: Optional[int] = None
        self._max_items: Optional[int] = None
        self._unique_items: Optional[bool] = None

        self._defs: Optional[Dict[str, Schema]] = None
        self._permission: Optional[str] = None
        self._details: Optional[SchemaDetails] = None
        self._view_details: Optional[SchemaDetails] = None

        if schema is not None:
            self._namespace = schema._namespace
            self._name = schema._name
            self._version = schema._version
            self._ref = schema._ref

            if schema._type is not None:
                if isinstance(schema._type, SingleType):
                    self._type = SingleType(schema._type)
                else:
                    self._type = MultipleType(schema._type)

            self._any_of = [Schema(x) for x in schema._any_of] if schema._any_of else None
            self._all_of = [Schema(x) for x in schema._all_of] if schema._all_of else None
            self._one_of = [Schema(x) for x in schema._one_of] if schema._one_of else None
            self._not = Schema(schema._not) if schema._not else None

            self._description = schema._description
            self._examples = copy.deepcopy(schema._examples) if schema._examples else None
            self._default_value = copy.deepcopy(schema._default_value) if schema._default_value is not None else None
            self._comment = schema._comment
            self._enums = list(schema._enums) if schema._enums else None
            self._constant = copy.deepcopy(schema._constant) if schema._constant is not None else None

            self._pattern = schema._pattern
            self._format = schema._format
            self._min_length = schema._min_length
            self._max_length = schema._max_length

            self._multiple_of = schema._multiple_of
            self._minimum = schema._minimum
            self._maximum = schema._maximum
            self._exclusive_minimum = schema._exclusive_minimum
            self._exclusive_maximum = schema._exclusive_maximum

            self._properties = (
                {k: Schema(v) for k, v in schema._properties.items()}
                if schema._properties else None
            )
            self._additional_properties = (
                AdditionalType(schema._additional_properties)
                if schema._additional_properties else None
            )
            self._required = list(schema._required) if schema._required else None
            self._property_names = Schema(schema._property_names) if schema._property_names else None
            self._min_properties = schema._min_properties
            self._max_properties = schema._max_properties
            self._pattern_properties = (
                {k: Schema(v) for k, v in schema._pattern_properties.items()}
                if schema._pattern_properties else None
            )

            self._items = ArraySchemaType(schema._items) if schema._items else None
            self._additional_items = (
                AdditionalType(schema._additional_items)
                if schema._additional_items else None
            )
            self._contains = Schema(schema._contains) if schema._contains else None
            self._min_contains = schema._min_contains
            self._max_contains = schema._max_contains
            self._min_items = schema._min_items
            self._max_items = schema._max_items
            self._unique_items = schema._unique_items

            self._defs = (
                {k: Schema(v) for k, v in schema._defs.items()}
                if schema._defs else None
            )
            self._permission = schema._permission
            self._details = schema._details
            self._view_details = schema._view_details

    # --- Static factory methods ---

    @staticmethod
    def of_string(id: str) -> Schema:
        return Schema().set_type(TypeUtil.of(SchemaType.STRING)).set_name(id)

    @staticmethod
    def of_integer(id: str) -> Schema:
        return Schema().set_type(TypeUtil.of(SchemaType.INTEGER)).set_name(id)

    @staticmethod
    def of_float(id: str) -> Schema:
        return Schema().set_type(TypeUtil.of(SchemaType.FLOAT)).set_name(id)

    @staticmethod
    def of_long(id: str) -> Schema:
        return Schema().set_type(TypeUtil.of(SchemaType.LONG)).set_name(id)

    @staticmethod
    def of_double(id: str) -> Schema:
        return Schema().set_type(TypeUtil.of(SchemaType.DOUBLE)).set_name(id)

    @staticmethod
    def of_any(id: str) -> Schema:
        return Schema().set_type(TypeUtil.of(
            SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE,
            SchemaType.STRING, SchemaType.BOOLEAN, SchemaType.ARRAY, SchemaType.NULL, SchemaType.OBJECT,
        )).set_name(id)

    @staticmethod
    def of_any_not_null(id: str) -> Schema:
        return Schema().set_type(TypeUtil.of(
            SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE,
            SchemaType.STRING, SchemaType.BOOLEAN, SchemaType.ARRAY, SchemaType.OBJECT,
        )).set_name(id)

    @staticmethod
    def of_number(id: str) -> Schema:
        return Schema().set_type(TypeUtil.of(
            SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE,
        )).set_name(id)

    @staticmethod
    def of_boolean(id: str) -> Schema:
        return Schema().set_type(TypeUtil.of(SchemaType.BOOLEAN)).set_name(id)

    @staticmethod
    def of(id: str, *types: SchemaType) -> Schema:
        return Schema().set_type(TypeUtil.of(*types)).set_name(id)

    @staticmethod
    def of_object(id: str) -> Schema:
        return Schema().set_type(TypeUtil.of(SchemaType.OBJECT)).set_name(id)

    @staticmethod
    def of_ref(ref: str) -> Schema:
        return Schema().set_ref(ref)

    @staticmethod
    def of_array(id: str, *item_schemas: Schema) -> Schema:
        return (
            Schema()
            .set_type(TypeUtil.of(SchemaType.ARRAY))
            .set_name(id)
            .set_items(ArraySchemaType.of(*item_schemas))
        )

    @staticmethod
    def from_list_of_schemas(lst: Any) -> Optional[List[Schema]]:
        if is_null_value(lst) or not isinstance(lst, list):
            return None
        result: List[Schema] = []
        for e in lst:
            v = Schema.from_value(e)
            if v is not None:
                result.append(v)
        return result

    @staticmethod
    def from_map_of_schemas(map_: Any) -> Optional[Dict[str, Schema]]:
        if is_null_value(map_):
            return None
        ret_map: Dict[str, Schema] = {}
        for k, v in map_.items():
            value = Schema.from_value(v)
            if value is not None:
                ret_map[k] = value
        return ret_map

    @staticmethod
    def from_value(obj: Any, is_string_schema: bool = False) -> Optional[Schema]:
        if is_null_value(obj):
            return None

        schema = Schema()
        schema._namespace = obj.get('namespace', TEMPORARY) if isinstance(obj, dict) else TEMPORARY
        schema._name = obj.get('name') if isinstance(obj, dict) else None
        schema._version = obj.get('version', 1) if isinstance(obj, dict) else 1
        schema._ref = obj.get('ref') if isinstance(obj, dict) else None

        if not isinstance(obj, dict):
            return schema

        if not is_string_schema:
            schema._type = TypeUtil.from_value(obj.get('type'))
        else:
            schema._type = SingleType(SchemaType.STRING)

        schema._any_of = Schema.from_list_of_schemas(obj.get('anyOf'))
        schema._all_of = Schema.from_list_of_schemas(obj.get('allOf'))
        schema._one_of = Schema.from_list_of_schemas(obj.get('oneOf'))
        schema._not = Schema.from_value(obj.get('not'))

        schema._description = obj.get('description')
        examples = obj.get('examples')
        schema._examples = list(examples) if examples else None
        schema._default_value = obj.get('defaultValue')
        schema._comment = obj.get('comment')
        enums = obj.get('enums')
        schema._enums = list(enums) if enums else None
        schema._constant = obj.get('constant')

        # String
        schema._pattern = obj.get('pattern')
        schema._format = obj.get('format')
        schema._min_length = obj.get('minLength')
        schema._max_length = obj.get('maxLength')

        # Number
        schema._multiple_of = obj.get('multipleOf')
        schema._minimum = obj.get('minimum')
        schema._maximum = obj.get('maximum')
        schema._exclusive_minimum = obj.get('exclusiveMinimum')
        schema._exclusive_maximum = obj.get('exclusiveMaximum')

        # Object
        schema._properties = Schema.from_map_of_schemas(obj.get('properties'))
        schema._additional_properties = AdditionalType.from_value(obj.get('additionalProperties'))
        schema._required = obj.get('required')
        schema._property_names = Schema.from_value(obj.get('propertyNames'), True)
        schema._min_properties = obj.get('minProperties')
        schema._max_properties = obj.get('maxProperties')
        schema._pattern_properties = Schema.from_map_of_schemas(obj.get('patternProperties'))

        # Array
        schema._items = ArraySchemaType.from_value(obj.get('items'))
        schema._additional_items = AdditionalType.from_value(obj.get('additionalItems'))
        schema._contains = Schema.from_value(obj.get('contains'))
        schema._min_contains = obj.get('minContains')
        schema._max_contains = obj.get('maxContains')
        schema._min_items = obj.get('minItems')
        schema._max_items = obj.get('maxItems')
        schema._unique_items = obj.get('uniqueItems')

        schema._defs = Schema.from_map_of_schemas(obj.get('$defs'))
        schema._permission = obj.get('permission')
        schema._details = SchemaDetails.from_value(obj.get('details')) if obj.get('details') else None
        schema._view_details = SchemaDetails.from_value(obj.get('viewDetails')) if obj.get('viewDetails') else None

        return schema

    # --- Getters/Setters ---

    def get_title(self) -> Optional[str]:
        if not self._namespace or self._namespace == TEMPORARY:
            return self._name
        return f'{self._namespace}.{self._name}'

    def get_full_name(self) -> str:
        return f'{self._namespace}.{self._name}'

    def get_defs(self) -> Optional[Dict[str, Schema]]:
        return self._defs

    def set_defs(self, defs: Dict[str, Schema]) -> Schema:
        self._defs = defs
        return self

    def get_namespace(self) -> str:
        return self._namespace

    def set_namespace(self, namespace: str) -> Schema:
        self._namespace = namespace
        return self

    def get_name(self) -> Optional[str]:
        return self._name

    def set_name(self, name: str) -> Schema:
        self._name = name
        return self

    def get_version(self) -> int:
        return self._version

    def set_version(self, version: int) -> Schema:
        self._version = version
        return self

    def get_ref(self) -> Optional[str]:
        return self._ref

    def set_ref(self, ref: str) -> Schema:
        self._ref = ref
        return self

    def get_type(self) -> Optional[Type]:
        return self._type

    def set_type(self, type_: Type) -> Schema:
        self._type = type_
        return self

    def get_any_of(self) -> Optional[List[Schema]]:
        return self._any_of

    def set_any_of(self, any_of: List[Schema]) -> Schema:
        self._any_of = any_of
        return self

    def get_all_of(self) -> Optional[List[Schema]]:
        return self._all_of

    def set_all_of(self, all_of: List[Schema]) -> Schema:
        self._all_of = all_of
        return self

    def get_one_of(self) -> Optional[List[Schema]]:
        return self._one_of

    def set_one_of(self, one_of: List[Schema]) -> Schema:
        self._one_of = one_of
        return self

    def get_not(self) -> Optional[Schema]:
        return self._not

    def set_not(self, not_: Schema) -> Schema:
        self._not = not_
        return self

    def get_description(self) -> Optional[str]:
        return self._description

    def set_description(self, description: str) -> Schema:
        self._description = description
        return self

    def get_examples(self) -> Optional[List[Any]]:
        return self._examples

    def set_examples(self, examples: List[Any]) -> Schema:
        self._examples = examples
        return self

    def get_default_value(self) -> Any:
        return self._default_value

    def set_default_value(self, default_value: Any) -> Schema:
        self._default_value = default_value
        return self

    def get_comment(self) -> Optional[str]:
        return self._comment

    def set_comment(self, comment: str) -> Schema:
        self._comment = comment
        return self

    def get_enums(self) -> Optional[List[Any]]:
        return self._enums

    def set_enums(self, enums: List[Any]) -> Schema:
        self._enums = enums
        return self

    def get_constant(self) -> Any:
        return self._constant

    def set_constant(self, constant: Any) -> Schema:
        self._constant = constant
        return self

    def get_pattern(self) -> Optional[str]:
        return self._pattern

    def set_pattern(self, pattern: str) -> Schema:
        self._pattern = pattern
        return self

    def get_format(self) -> Optional[StringFormat]:
        return self._format

    def set_format(self, format_: StringFormat) -> Schema:
        self._format = format_
        return self

    def get_min_length(self) -> Optional[int]:
        return self._min_length

    def set_min_length(self, min_length: int) -> Schema:
        self._min_length = min_length
        return self

    def get_max_length(self) -> Optional[int]:
        return self._max_length

    def set_max_length(self, max_length: int) -> Schema:
        self._max_length = max_length
        return self

    def get_multiple_of(self) -> Optional[int]:
        return self._multiple_of

    def set_multiple_of(self, multiple_of: int) -> Schema:
        self._multiple_of = multiple_of
        return self

    def get_minimum(self) -> Optional[float]:
        return self._minimum

    def set_minimum(self, minimum: float) -> Schema:
        self._minimum = minimum
        return self

    def get_maximum(self) -> Optional[float]:
        return self._maximum

    def set_maximum(self, maximum: float) -> Schema:
        self._maximum = maximum
        return self

    def get_exclusive_minimum(self) -> Optional[float]:
        return self._exclusive_minimum

    def set_exclusive_minimum(self, exclusive_minimum: float) -> Schema:
        self._exclusive_minimum = exclusive_minimum
        return self

    def get_exclusive_maximum(self) -> Optional[float]:
        return self._exclusive_maximum

    def set_exclusive_maximum(self, exclusive_maximum: float) -> Schema:
        self._exclusive_maximum = exclusive_maximum
        return self

    def get_properties(self) -> Optional[Dict[str, Schema]]:
        return self._properties

    def set_properties(self, properties: Dict[str, Schema]) -> Schema:
        self._properties = properties
        return self

    def get_additional_properties(self) -> Optional[AdditionalType]:
        return self._additional_properties

    def set_additional_properties(self, additional_properties: AdditionalType) -> Schema:
        self._additional_properties = additional_properties
        return self

    def get_additional_items(self) -> Optional[AdditionalType]:
        return self._additional_items

    def set_additional_items(self, additional_items: AdditionalType) -> Schema:
        self._additional_items = additional_items
        return self

    def get_required(self) -> Optional[List[str]]:
        return self._required

    def set_required(self, required: List[str]) -> Schema:
        self._required = required
        return self

    def get_property_names(self) -> Optional[Schema]:
        return self._property_names

    def set_property_names(self, property_names: Schema) -> Schema:
        self._property_names = property_names
        self._property_names._type = SingleType(SchemaType.STRING)
        return self

    def get_min_properties(self) -> Optional[int]:
        return self._min_properties

    def set_min_properties(self, min_properties: int) -> Schema:
        self._min_properties = min_properties
        return self

    def get_max_properties(self) -> Optional[int]:
        return self._max_properties

    def set_max_properties(self, max_properties: int) -> Schema:
        self._max_properties = max_properties
        return self

    def get_pattern_properties(self) -> Optional[Dict[str, Schema]]:
        return self._pattern_properties

    def set_pattern_properties(self, pattern_properties: Dict[str, Schema]) -> Schema:
        self._pattern_properties = pattern_properties
        return self

    def get_items(self) -> Optional[ArraySchemaType]:
        return self._items

    def set_items(self, items: ArraySchemaType) -> Schema:
        self._items = items
        return self

    def get_contains(self) -> Optional[Schema]:
        return self._contains

    def set_contains(self, contains: Schema) -> Schema:
        self._contains = contains
        return self

    def get_min_contains(self) -> Optional[int]:
        return self._min_contains

    def set_min_contains(self, min_contains: int) -> Schema:
        self._min_contains = min_contains
        return self

    def get_max_contains(self) -> Optional[int]:
        return self._max_contains

    def set_max_contains(self, max_contains: int) -> Schema:
        self._max_contains = max_contains
        return self

    def get_min_items(self) -> Optional[int]:
        return self._min_items

    def set_min_items(self, min_items: int) -> Schema:
        self._min_items = min_items
        return self

    def get_max_items(self) -> Optional[int]:
        return self._max_items

    def set_max_items(self, max_items: int) -> Schema:
        self._max_items = max_items
        return self

    def get_unique_items(self) -> Optional[bool]:
        return self._unique_items

    def set_unique_items(self, unique_items: bool) -> Schema:
        self._unique_items = unique_items
        return self

    def get_permission(self) -> Optional[str]:
        return self._permission

    def set_permission(self, permission: str) -> Schema:
        self._permission = permission
        return self

    def get_details(self) -> Optional[SchemaDetails]:
        return self._details

    def set_details(self, details: SchemaDetails) -> Schema:
        self._details = details
        return self

    def get_view_details(self) -> Optional[SchemaDetails]:
        return self._view_details

    def set_view_details(self, view_details: SchemaDetails) -> Schema:
        self._view_details = view_details
        return self


# Static schema constants (initialized after class definition)
Schema.NULL = (
    Schema()
    .set_namespace('System')
    .set_name('Null')
    .set_type(TypeUtil.of(SchemaType.NULL))
    .set_constant(None)
)

Schema.SCHEMA = (
    Schema()
    .set_namespace('System')
    .set_name('Schema')
    .set_type(TypeUtil.of(SchemaType.OBJECT))
)
