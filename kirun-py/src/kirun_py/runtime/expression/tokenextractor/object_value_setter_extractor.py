from __future__ import annotations

import re
from typing import Any, List, Optional

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.runtime.expression.operation import Operation
from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor
from kirun_py.util.duplicate import duplicate
from kirun_py.util.null_check import is_null_value
from kirun_py.util.string.string_formatter import StringFormatter


class ObjectValueSetterExtractor(TokenValueExtractor):

    def __init__(self, store: Any, prefix: str) -> None:
        super().__init__()
        self._store: Any = store
        self._prefix: str = prefix

    def get_value_internal(self, token: str) -> Any:
        parts: List[str] = TokenValueExtractor.split_path(token)
        return self.retrieve_element_from(token, parts, 1, self._store)

    def get_store(self) -> Any:
        return self._store

    def set_store(self, store: Any) -> ObjectValueSetterExtractor:
        self._store = store
        return self

    def get_prefix(self) -> str:
        return self._prefix

    def set_value(
        self,
        token: str,
        value: Any,
        overwrite: bool = True,
        delete_on_null: bool = False,
    ) -> None:
        self._store = duplicate(self._store)
        self._modify_store(token, value, overwrite, delete_on_null)

    def _modify_store(
        self,
        string_token: str,
        value: Any,
        overwrite: bool,
        delete_on_null: bool,
    ) -> None:
        parts = TokenValueExtractor.split_path(string_token)

        if len(parts) < 2:
            raise KIRuntimeException(
                StringFormatter.format('Invalid path: $', string_token)
            )

        el = self._store

        # Navigate to the parent of the final element
        for i in range(1, len(parts) - 1):
            part = parts[i]
            next_part = parts[i + 1]

            segments = self._parse_bracket_segments(part)

            for j in range(len(segments)):
                segment = segments[j]
                is_last_segment_of_part = j == len(segments) - 1

                if is_last_segment_of_part:
                    is_last_part = i == len(parts) - 2
                    if is_last_part:
                        next_op = self._get_op_for_segment(parts[len(parts) - 1])
                    else:
                        next_op = self._get_op_for_segment(next_part)
                else:
                    next_op = (
                        Operation.ARRAY_OPERATOR
                        if self._is_array_index(segments[j + 1])
                        else Operation.OBJECT_OPERATOR
                    )

                if self._is_array_index(segment) and isinstance(el, list):
                    el = self._get_data_from_array(el, segment, next_op)
                else:
                    el = self._get_data_from_object(
                        el, self._strip_quotes(segment), next_op
                    )

        # Handle the final part (set the value)
        final_part = parts[len(parts) - 1]
        final_segments = self._parse_bracket_segments(final_part)

        # Navigate through all but the last segment of the final part
        for j in range(len(final_segments) - 1):
            segment = final_segments[j]
            next_op = (
                Operation.ARRAY_OPERATOR
                if self._is_array_index(final_segments[j + 1])
                else Operation.OBJECT_OPERATOR
            )

            if self._is_array_index(segment) and isinstance(el, list):
                el = self._get_data_from_array(el, segment, next_op)
            else:
                el = self._get_data_from_object(
                    el, self._strip_quotes(segment), next_op
                )

        # Set the final value
        last_segment = final_segments[len(final_segments) - 1]
        if self._is_array_index(last_segment) and isinstance(el, list):
            self._put_data_in_array(el, last_segment, value, overwrite, delete_on_null)
        else:
            self._put_data_in_object(
                el, self._strip_quotes(last_segment), value, overwrite, delete_on_null
            )

    @staticmethod
    def _parse_bracket_segments(part: str) -> List[str]:
        segments: List[str] = []
        start = 0
        i = 0

        while i < len(part):
            if part[i] == '[':
                if i > start:
                    segments.append(part[start:i])
                # Find matching ]
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
                segments.append(part[i + 1 : end])
                start = end + 1
                i = start
            else:
                i += 1

        if start < len(part):
            segments.append(part[start:])

        return segments if segments else [part]

    @staticmethod
    def _is_array_index(segment: str) -> bool:
        return bool(re.fullmatch(r'-?\d+', segment))

    @staticmethod
    def _strip_quotes(segment: str) -> str:
        if (
            (segment.startswith('"') and segment.endswith('"'))
            or (segment.startswith("'") and segment.endswith("'"))
        ):
            return segment[1:-1]
        return segment

    @staticmethod
    def _get_op_for_segment(segment: str) -> Operation:
        if ObjectValueSetterExtractor._is_array_index(segment) or segment.startswith(
            '['
        ):
            return Operation.ARRAY_OPERATOR
        return Operation.OBJECT_OPERATOR

    @staticmethod
    def _get_data_from_array(el: Any, mem: str, next_op: Operation) -> Any:
        if not isinstance(el, list):
            raise KIRuntimeException(
                StringFormatter.format('Expected an array but found $', str(el))
            )

        try:
            index = int(mem)
        except (ValueError, TypeError):
            raise KIRuntimeException(
                StringFormatter.format(
                    'Expected an array index but found $', mem
                )
            )

        if index < 0:
            raise KIRuntimeException(
                StringFormatter.format('Array index is out of bound - $', mem)
            )

        je = el[index] if index < len(el) else None

        if is_null_value(je):
            je = {} if next_op == Operation.OBJECT_OPERATOR else []
            # Extend list if necessary
            while len(el) <= index:
                el.append(None)
            el[index] = je

        return je

    @staticmethod
    def _get_data_from_object(el: Any, mem: str, next_op: Operation) -> Any:
        if isinstance(el, list) or not isinstance(el, dict):
            raise KIRuntimeException(
                StringFormatter.format('Expected an object but found $', str(el))
            )

        je = el.get(mem)

        if is_null_value(je):
            je = {} if next_op == Operation.OBJECT_OPERATOR else []
            el[mem] = je

        return je

    @staticmethod
    def _put_data_in_array(
        el: Any,
        mem: str,
        value: Any,
        overwrite: bool,
        delete_on_null: bool,
    ) -> None:
        if not isinstance(el, list):
            raise KIRuntimeException(
                StringFormatter.format('Expected an array but found $', str(el))
            )

        try:
            index = int(mem)
        except (ValueError, TypeError):
            raise KIRuntimeException(
                StringFormatter.format(
                    'Expected an array index but found $', mem
                )
            )

        if index < 0:
            raise KIRuntimeException(
                StringFormatter.format('Array index is out of bound - $', mem)
            )

        # Extend list if necessary
        while len(el) <= index:
            el.append(None)

        current = el[index]
        if overwrite or is_null_value(current):
            if delete_on_null and is_null_value(value):
                if index < len(el):
                    el.pop(index)
            else:
                el[index] = value

    @staticmethod
    def _put_data_in_object(
        el: Any,
        mem: str,
        value: Any,
        overwrite: bool,
        delete_on_null: bool,
    ) -> None:
        if isinstance(el, list) or not isinstance(el, dict):
            raise KIRuntimeException(
                StringFormatter.format('Expected an object but found $', str(el))
            )

        current = el.get(mem)
        if overwrite or is_null_value(current):
            if delete_on_null and is_null_value(value):
                el.pop(mem, None)
            else:
                el[mem] = value
