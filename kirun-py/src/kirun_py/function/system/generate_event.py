from __future__ import annotations

from typing import Any, Dict, List, Optional, TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.json.schema.schema import Schema
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.runtime.expression.expression_evaluator import ExpressionEvaluator
from kirun_py.util.null_check import is_null_value

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

VALUE = 'value'
EVENT_NAME = 'eventName'
RESULTS = 'results'


class GenerateEvent(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('GenerateEvent')
            .set_namespace(Namespaces.SYSTEM)
            .set_parameters(dict([
                Parameter.of_entry(
                    EVENT_NAME,
                    Schema.of_string(EVENT_NAME).set_default_value('output'),
                ),
                Parameter.of_entry(
                    RESULTS,
                    Schema.of_object(RESULTS).set_properties({
                        'name': Schema.of_string('name'),
                        VALUE: Parameter.EXPRESSION,
                    }),
                    True,
                ),
            ]))
            .set_events(dict([
                Event.output_event_map_entry({}),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        events: Optional[Dict[str, List[Dict[str, Any]]]] = context.get_events()
        args: Optional[Dict[str, Any]] = context.get_arguments()

        event_name: str = args.get(EVENT_NAME) if args else 'output'
        results_list: List[Any] = args.get(RESULTS, []) if args else []

        result_map: Dict[str, Any] = {}
        for e in results_list:
            je = e.get(VALUE) if isinstance(e, dict) else None

            if is_null_value(je):
                raise KIRuntimeException('Expect a value object')

            v = je.get('value') if isinstance(je, dict) else je
            if isinstance(je, dict) and je.get('isExpression'):
                v = ExpressionEvaluator(v).evaluate(context.get_values_map())

            name = e.get('name') if isinstance(e, dict) else None
            if name is not None:
                result_map[name] = v

        if events is not None:
            if event_name not in events:
                events[event_name] = []
            events[event_name].append(result_map)

        return FunctionOutput([EventResult.output_of({})])
