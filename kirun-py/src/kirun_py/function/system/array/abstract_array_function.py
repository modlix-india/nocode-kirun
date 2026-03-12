from __future__ import annotations

from abc import abstractmethod
from typing import Any, Dict, List, Optional, TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_util import TypeUtil
from kirun_py.model.event import Event
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class AbstractArrayFunction(AbstractFunction):

    EVENT_INDEX_NAME: str = 'index'
    EVENT_RESULT_NAME: str = 'result'

    EVENT_INDEX: Event = Event(
        Event.OUTPUT,
        {EVENT_INDEX_NAME: Schema.of_integer(EVENT_INDEX_NAME)},
    )

    EVENT_RESULT_INTEGER: Event = Event(
        Event.OUTPUT,
        {EVENT_RESULT_NAME: Schema.of_integer(EVENT_RESULT_NAME)},
    )

    EVENT_RESULT_BOOLEAN: Event = Event(
        Event.OUTPUT,
        {EVENT_RESULT_NAME: Schema.of_boolean(EVENT_RESULT_NAME)},
    )

    EVENT_RESULT_ARRAY: Event = Event(
        Event.OUTPUT,
        {EVENT_RESULT_NAME: Schema.of_array(EVENT_RESULT_NAME, Schema.of_any(EVENT_RESULT_NAME))},
    )

    EVENT_RESULT_EMPTY: Event = Event(Event.OUTPUT, {})

    EVENT_RESULT_ANY: Event = Event(
        Event.OUTPUT,
        {EVENT_RESULT_NAME: Schema.of_any(EVENT_RESULT_NAME)},
    )

    EVENT_RESULT_OBJECT: Event = Event(
        Event.OUTPUT,
        {EVENT_RESULT_NAME: Schema.of_object(EVENT_RESULT_NAME)},
    )

    EVENT_RESULT_STRING: Event = Event(
        Event.OUTPUT,
        {EVENT_RESULT_NAME: Schema.of_string(EVENT_RESULT_NAME)},
    )

    PARAMETER_INT_LENGTH: Parameter = Parameter.of(
        'length',
        Schema.of_integer('length').set_default_value(-1),
    )

    PARAMETER_ARRAY_FIND: Parameter = Parameter.of(
        'find',
        Schema.of_array('eachFind', Schema.of_any('eachFind')),
    )

    PARAMETER_INT_SOURCE_FROM: Parameter = Parameter.of(
        'srcFrom',
        Schema.of_integer('srcFrom').set_default_value(0).set_minimum(0),
    )

    PARAMETER_INT_SECOND_SOURCE_FROM: Parameter = Parameter.of(
        'secondSrcFrom',
        Schema.of_integer('secondSrcFrom').set_default_value(0),
    )

    PARAMETER_INT_FIND_FROM: Parameter = Parameter.of(
        'findFrom',
        Schema.of_integer('findFrom').set_default_value(0),
    )

    PARAMETER_INT_OFFSET: Parameter = Parameter.of(
        'offset',
        Schema.of_integer('offset').set_default_value(0),
    )

    PARAMETER_ROTATE_LENGTH: Parameter = Parameter.of(
        'rotateLength',
        Schema.of_integer('rotateLength').set_default_value(1).set_minimum(1),
    )

    PARAMETER_BOOLEAN_ASCENDING: Parameter = Parameter.of(
        'ascending',
        Schema.of_boolean('ascending').set_default_value(True),
    )

    PARAMETER_KEY_PATH: Parameter = Parameter.of(
        'keyPath',
        Schema.of_string('keyPath').set_default_value(''),
    )

    PARAMETER_FIND_PRIMITIVE: Parameter = Parameter.of(
        'findPrimitive',
        Schema.of(
            'findPrimitive',
            SchemaType.STRING,
            SchemaType.DOUBLE,
            SchemaType.FLOAT,
            SchemaType.INTEGER,
            SchemaType.LONG,
        ),
    )

    PARAMETER_ARRAY_SOURCE: Parameter = Parameter.of(
        'source',
        Schema.of_array('eachSource', Schema.of_any('eachSource')),
    )

    PARAMETER_ARRAY_SECOND_SOURCE: Parameter = Parameter.of(
        'secondSource',
        Schema.of_array('eachSecondSource', Schema.of_any('eachSecondSource')),
    )

    PARAMETER_ARRAY_SOURCE_PRIMITIVE: Parameter = Parameter.of(
        'source',
        Schema.of_array(
            'eachSource',
            Schema()
            .set_name('eachSource')
            .set_type(
                TypeUtil.of(
                    SchemaType.STRING,
                    SchemaType.NULL,
                    SchemaType.INTEGER,
                    SchemaType.FLOAT,
                    SchemaType.DOUBLE,
                    SchemaType.LONG,
                ),
            ),
        ),
    )

    PARAMETER_BOOLEAN_DEEP_COPY: Parameter = Parameter.of(
        'deepCopy',
        Schema.of_boolean('deepCopy').set_default_value(True),
    )

    PARAMETER_ANY: Parameter = Parameter.of(
        'element',
        Schema.of_any('element'),
    )

    PARAMETER_ANY_ELEMENT_OBJECT: Parameter = Parameter.of(
        'elementObject',
        Schema.of_any('elementObject'),
    )

    PARAMETER_ANY_VAR_ARGS: Parameter = Parameter.of(
        'element',
        Schema.of_any('element'),
    ).set_variable_argument(True)

    PARAMETER_ARRAY_RESULT: Parameter = Parameter.of(
        EVENT_RESULT_NAME,
        Schema.of_array('eachResult', Schema.of_any('eachResult')),
    )

    def __init__(
        self,
        function_name: str,
        parameters: List[Parameter],
        event: Event,
    ) -> None:
        super().__init__()
        param_map: Dict[str, Parameter] = {}
        for param in parameters:
            param_map[param.get_parameter_name()] = param

        self._signature = (
            FunctionSignature(function_name)
            .set_namespace(Namespaces.SYSTEM_ARRAY)
            .set_parameters(param_map)
            .set_events({event.get_name(): event})
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature
