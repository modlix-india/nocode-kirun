from __future__ import annotations

from typing import Callable, List, Optional, TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_util import TypeUtil
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.util.primitive.primitive_util import PrimitiveUtil

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

VALUE = 'value'
VALUE1 = 'value1'
VALUE2 = 'value2'


class GenericMathFunction(AbstractFunction):

    def __init__(
        self,
        function_name: str,
        math_function: Callable[..., float],
        parameters_number: int = 1,
        return_types: Optional[List[SchemaType]] = None,
    ) -> None:
        super().__init__()
        if return_types is None:
            return_types = [SchemaType.DOUBLE]

        self._parameters_number = parameters_number
        self._math_function = math_function

        param_builders = [
            lambda: {VALUE: Parameter(VALUE, Schema.of_number(VALUE))},
            lambda: {
                VALUE1: Parameter(VALUE1, Schema.of_number(VALUE1)),
                VALUE2: Parameter(VALUE2, Schema.of_number(VALUE2)),
            },
        ]

        self._signature = (
            FunctionSignature(function_name)
            .set_namespace(Namespaces.MATH)
            .set_parameters(param_builders[parameters_number - 1]())
            .set_events(
                dict([
                    Event.output_event_map_entry({
                        VALUE: Schema()
                        .set_type(TypeUtil.of(*return_types))
                        .set_name(VALUE),
                    })
                ])
            )
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        args = context.get_arguments()

        v1 = PrimitiveUtil.find_primitive_number_type(
            args.get(VALUE if self._parameters_number == 1 else VALUE1)
        ).get_t2()

        v2 = None
        if self._parameters_number == 2:
            v2 = PrimitiveUtil.find_primitive_number_type(
                args.get(VALUE2)
            ).get_t2()

        if v2 is not None:
            result = self._math_function(v1, v2)
        else:
            result = self._math_function(v1)

        return FunctionOutput([
            EventResult.output_of({VALUE: result})
        ])
