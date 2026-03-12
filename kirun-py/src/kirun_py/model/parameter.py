from __future__ import annotations
from typing import Any, Optional, Tuple

from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_util import TypeUtil
from kirun_py.model.parameter_type import ParameterType


class Parameter:
    EXPRESSION: Schema = (
        Schema()
        .set_namespace('System')
        .set_name('ParameterExpression')
        .set_type(TypeUtil.of(SchemaType.OBJECT))
        .set_properties({
            'isExpression': Schema.of_boolean('isExpression').set_default_value(True),
            'value': Schema.of_any('value'),
        })
    )

    def __init__(self, pn, schema: Optional[Schema] = None):
        if isinstance(pn, Parameter):
            self._schema = Schema(pn._schema)
            self._parameter_name = pn._parameter_name
            self._variable_argument = pn._variable_argument
            self._type = pn._type
        else:
            if schema is None:
                raise ValueError('Schema is required when creating Parameter with name')
            self._schema: Schema = schema
            self._parameter_name: str = pn
            self._variable_argument: bool = False
            self._type: ParameterType = ParameterType.EXPRESSION

    def get_schema(self) -> Schema:
        return self._schema

    def set_schema(self, schema: Schema) -> Parameter:
        self._schema = schema
        return self

    def get_parameter_name(self) -> str:
        return self._parameter_name

    def set_parameter_name(self, name: str) -> Parameter:
        self._parameter_name = name
        return self

    def is_variable_argument(self) -> bool:
        return self._variable_argument

    def set_variable_argument(self, va: bool) -> Parameter:
        self._variable_argument = va
        return self

    def get_type(self) -> ParameterType:
        return self._type

    def set_type(self, type_: ParameterType) -> Parameter:
        self._type = type_
        return self

    @staticmethod
    def of_entry(
        name: str,
        schema: Schema,
        variable_argument: bool = False,
        type_: ParameterType = ParameterType.EXPRESSION,
    ) -> Tuple[str, Parameter]:
        return (
            name,
            Parameter(name, schema).set_type(type_).set_variable_argument(variable_argument),
        )

    @staticmethod
    def of(
        name: str,
        schema: Schema,
        variable_argument: bool = False,
        type_: ParameterType = ParameterType.EXPRESSION,
    ) -> Parameter:
        return Parameter(name, schema).set_type(type_).set_variable_argument(variable_argument)

    @staticmethod
    def from_value(json: dict) -> Parameter:
        param_schema = Schema.from_value(json.get('schema'))
        if param_schema is None:
            raise ValueError('Parameter requires Schema')
        return (
            Parameter(json.get('parameterName', ''), param_schema)
            .set_variable_argument(bool(json.get('variableArgument', False)))
            .set_type(ParameterType(json.get('type', 'EXPRESSION')))
        )

    def to_json(self) -> dict:
        return {
            'parameterName': self._parameter_name,
            'schema': self._schema,
            'variableArgument': self._variable_argument,
            'type': self._type.value if self._type else None,
        }
