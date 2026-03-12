from __future__ import annotations

import re
from typing import Any, Dict, List, Optional, TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.exception.execution_exception import ExecutionException
from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_util import TypeUtil
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.runtime.context_element import ContextElement
from kirun_py.runtime.expression.expression_evaluator import ExpressionEvaluator
from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor
from kirun_py.util.null_check import is_null_value
from kirun_py.util.string.string_formatter import StringFormatter
from kirun_py.util.string.string_util import StringUtil

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

NAME = 'name'
VALUE = 'value'


class SetFunction(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('Set')
            .set_namespace(Namespaces.SYSTEM_CTX)
            .set_parameters(dict([
                Parameter.of_entry(
                    NAME,
                    Schema()
                    .set_name(NAME)
                    .set_type(TypeUtil.of(SchemaType.STRING))
                    .set_min_length(1),
                    False,
                ),
                Parameter.of_entry(VALUE, Schema.of_any(VALUE)),
            ]))
            .set_events(dict([
                Event.output_event_map_entry({}),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments()
        key: str = args.get(NAME) if args else None

        if StringUtil.is_null_or_blank(key):
            raise KIRuntimeException(
                'Empty string is not a valid name for the context element'
            )

        value: Any = args.get(VALUE) if args else None

        parts = TokenValueExtractor.split_path(key)

        if len(parts) < 1 or parts[0] != 'Context':
            raise ExecutionException(
                StringFormatter.format(
                    'The context path $ is not a valid path in context', key
                )
            )

        evaluated_parts = self._evaluate_dynamic_parts(parts, context)

        return self._modify_context_with_parts(context, key, value, evaluated_parts)

    def _evaluate_dynamic_parts(
        self, parts: List[str], context: FunctionExecutionParameters
    ) -> List[str]:
        result: List[str] = []
        for part in parts:
            evaluated = self._evaluate_bracket_expressions(part, context)
            result.append(evaluated)
        return result

    def _evaluate_bracket_expressions(
        self, part: str, context: FunctionExecutionParameters
    ) -> str:
        result = ''
        i = 0

        while i < len(part):
            if part[i] == '[':
                result += '['
                i += 1

                bracket_content = ''
                depth = 1
                in_quote = False
                quote_char = ''

                while i < len(part) and depth > 0:
                    ch = part[i]

                    if in_quote:
                        if ch == quote_char and (i == 0 or part[i - 1] != '\\'):
                            in_quote = False
                        bracket_content += ch
                    else:
                        if ch in ('"', "'"):
                            in_quote = True
                            quote_char = ch
                            bracket_content += ch
                        elif ch == '[':
                            depth += 1
                            bracket_content += ch
                        elif ch == ']':
                            depth -= 1
                            if depth > 0:
                                bracket_content += ch
                        else:
                            bracket_content += ch
                    i += 1

                # Check if bracket content is a static value
                if (re.match(r'^-?\d+$', bracket_content) or
                        (bracket_content.startswith('"') and bracket_content.endswith('"')) or
                        (bracket_content.startswith("'") and bracket_content.endswith("'"))):
                    result += bracket_content + ']'
                else:
                    try:
                        evaluator = ExpressionEvaluator(bracket_content)
                        evaluated_value = evaluator.evaluate(context.get_values_map())
                        result += str(evaluated_value) + ']'
                    except Exception:
                        result += bracket_content + ']'
            else:
                result += part[i]
                i += 1

        return result

    def _modify_context_with_parts(
        self,
        context: FunctionExecutionParameters,
        key: str,
        value: Any,
        parts: List[str],
    ) -> FunctionOutput:
        if len(parts) < 2:
            raise KIRuntimeException(
                StringFormatter.format("Context path '$' is too short", key)
            )

        first_segment = parts[1]
        first_segment_parts = self._parse_bracket_segments(first_segment)
        context_key = first_segment_parts[0]

        ctx = context.get_context()
        ce: Optional[ContextElement] = ctx.get(context_key) if ctx else None

        if is_null_value(ce):
            raise KIRuntimeException(
                StringFormatter.format(
                    "Context doesn't have any element with name '$' ", context_key
                )
            )

        # If we just have "Context.a" with no further path
        if len(parts) == 2 and len(first_segment_parts) == 1:
            ce.set_element(value)
            return FunctionOutput([EventResult.output_of({})])

        el: Any = ce.get_element()

        # Initialize element if null
        if is_null_value(el):
            if len(first_segment_parts) > 1:
                next_is_array = self._is_array_index(first_segment_parts[1])
            elif len(parts) > 2:
                next_is_array = self._is_array_access(parts[2])
            else:
                next_is_array = False
            el = [] if next_is_array else {}
            ce.set_element(el)

        # Collect all path segments
        all_segments: List[Dict[str, Any]] = []

        for j in range(1, len(first_segment_parts)):
            all_segments.append({
                'value': self._strip_quotes(first_segment_parts[j]),
                'is_array': self._is_array_index(first_segment_parts[j]),
            })

        for idx in range(2, len(parts)):
            segment_parts = self._parse_bracket_segments(parts[idx])
            for seg in segment_parts:
                all_segments.append({
                    'value': self._strip_quotes(seg),
                    'is_array': self._is_array_index(seg),
                })

        # Navigate to the parent of the final element
        for idx in range(len(all_segments) - 1):
            segment = all_segments[idx]
            next_segment = all_segments[idx + 1]

            if segment['is_array']:
                el = self._get_data_from_array(el, segment['value'], next_segment['is_array'])
            else:
                el = self._get_data_from_object(el, segment['value'], next_segment['is_array'])

        # Set the final value
        last_segment = all_segments[-1]
        if last_segment['is_array']:
            self._put_data_in_array(el, last_segment['value'], value)
        else:
            self._put_data_in_object(el, last_segment['value'], value)

        return FunctionOutput([EventResult.output_of({})])

    def _parse_bracket_segments(self, part: str) -> List[str]:
        segments: List[str] = []
        start = 0
        i = 0

        while i < len(part):
            if part[i] == '[':
                if i > start:
                    segments.append(part[start:i])
                end = i + 1
                in_quote = False
                quote_char = ''
                while end < len(part):
                    if in_quote:
                        if part[end] == quote_char and part[end - 1] != '\\':
                            in_quote = False
                    else:
                        if part[end] in ('"', "'"):
                            in_quote = True
                            quote_char = part[end]
                        elif part[end] == ']':
                            break
                    end += 1
                segments.append(part[i + 1:end])
                start = end + 1
                i = start
            else:
                i += 1

        if start < len(part):
            segments.append(part[start:])

        return segments if segments else [part]

    def _is_array_index(self, segment: str) -> bool:
        return bool(re.match(r'^-?\d+$', segment))

    def _is_array_access(self, part: str) -> bool:
        return part.startswith('[') or self._is_array_index(part)

    def _strip_quotes(self, segment: str) -> str:
        if ((segment.startswith('"') and segment.endswith('"')) or
                (segment.startswith("'") and segment.endswith("'"))):
            return segment[1:-1]
        return segment

    def _get_data_from_array(self, el: Any, mem: str, next_is_array: bool) -> Any:
        if not isinstance(el, list):
            raise KIRuntimeException(
                StringFormatter.format('Expected an array but found $', el)
            )

        try:
            index = int(mem)
        except ValueError:
            raise KIRuntimeException(
                StringFormatter.format('Expected an array index but found $', mem)
            )

        if index < 0:
            raise KIRuntimeException(
                StringFormatter.format('Array index is out of bound - $', mem)
            )

        # Extend list if needed
        while len(el) <= index:
            el.append(None)

        je = el[index]
        if is_null_value(je):
            je = [] if next_is_array else {}
            el[index] = je
        return je

    def _get_data_from_object(self, el: Any, mem: str, next_is_array: bool) -> Any:
        if isinstance(el, list) or not isinstance(el, dict):
            raise KIRuntimeException(
                StringFormatter.format('Expected an object but found $', el)
            )

        je = el.get(mem)
        if is_null_value(je):
            je = [] if next_is_array else {}
            el[mem] = je
        return je

    def _put_data_in_array(self, el: Any, mem: str, value: Any) -> None:
        if not isinstance(el, list):
            raise KIRuntimeException(
                StringFormatter.format('Expected an array but found $', el)
            )

        try:
            index = int(mem)
        except ValueError:
            raise KIRuntimeException(
                StringFormatter.format('Expected an array index but found $', mem)
            )

        if index < 0:
            raise KIRuntimeException(
                StringFormatter.format('Array index is out of bound - $', mem)
            )

        while len(el) <= index:
            el.append(None)

        el[index] = value

    def _put_data_in_object(self, el: Any, mem: str, value: Any) -> None:
        if isinstance(el, list) or not isinstance(el, dict):
            raise KIRuntimeException(
                StringFormatter.format('Expected an object but found $', el)
            )

        el[mem] = value
