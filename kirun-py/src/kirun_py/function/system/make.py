from __future__ import annotations

from typing import Any, Dict, Optional, TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.runtime.expression.expression_evaluator import ExpressionEvaluator
from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

RESULT_SHAPE = 'resultShape'
VALUE = 'value'


class Make(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('Make')
            .set_namespace(Namespaces.SYSTEM)
            .set_parameters(dict([
                Parameter.of_entry(RESULT_SHAPE, Schema.of_any(RESULT_SHAPE)),
            ]))
            .set_events(dict([
                Event.output_event_map_entry({VALUE: Schema.of_any(VALUE)}),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() if context.get_arguments() is not None else {}
        result_shape = args.get(RESULT_SHAPE)
        values_map: Dict[str, TokenValueExtractor] = context.get_values_map()

        result = self._process_value(result_shape, values_map)

        return FunctionOutput([EventResult.output_of({VALUE: result})])

    def _process_value(
        self, value: Any, values_map: Dict[str, TokenValueExtractor]
    ) -> Any:
        if value is None:
            return value

        if isinstance(value, str):
            return self._evaluate_expression(value, values_map)

        if isinstance(value, list):
            return [self._process_value(item, values_map) for item in value]

        if isinstance(value, dict):
            return {
                key: self._process_value(val, values_map)
                for key, val in value.items()
            }

        return value

    def _evaluate_expression(
        self, expression: str, values_map: Dict[str, TokenValueExtractor]
    ) -> Any:
        if not expression or not expression.startswith('{{') or not expression.endswith('}}'):
            return expression

        inner_expression = expression[2:-2]
        evaluator = ExpressionEvaluator(inner_expression)
        result = evaluator.evaluate(values_map)

        return result if result is not None else None
