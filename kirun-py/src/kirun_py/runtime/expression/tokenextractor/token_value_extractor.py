from __future__ import annotations

import re
from abc import ABC, abstractmethod
from typing import Any, Dict, List, Optional

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.util.null_check import is_null_value
from kirun_py.util.string.string_formatter import StringFormatter
from kirun_py.util.string.string_util import StringUtil


class TokenValueExtractor(ABC):

    REGEX_SQUARE_BRACKETS: re.Pattern = re.compile(r'[\[\]]')
    REGEX_DOT: re.Pattern = re.compile(r'(?<!\.)\.(?!\.)')

    # Cache for parsed paths to avoid repeated regex splits
    _path_cache: Dict[str, List[str]] = {}

    # Cache for parsed bracket segments to avoid repeated regex splits
    _bracket_cache: Dict[str, List[str]] = {}

    def __init__(self) -> None:
        super().__init__()
        # Optional valuesMap for resolving dynamic bracket indices like Parent.__index
        self.values_map: Optional[Dict[str, TokenValueExtractor]] = None

    @staticmethod
    def split_path(token: str) -> List[str]:
        parts = TokenValueExtractor._path_cache.get(token)
        if parts is None:
            parts = TokenValueExtractor._split_path_internal(token)
            TokenValueExtractor._path_cache[token] = parts
        return parts

    @staticmethod
    def _split_path_internal(token: str) -> List[str]:
        parts: List[str] = []
        start = 0
        in_bracket = False

        for i in range(len(token)):
            c = token[i]

            if c == '[':
                in_bracket = True
            elif c == ']':
                in_bracket = False
            elif c == '.' and not in_bracket and not TokenValueExtractor._is_double_dot(token, i):
                # Found a separator dot
                if i > start:
                    parts.append(token[start:i])
                start = i + 1

        # Add the last part
        if start < len(token):
            parts.append(token[start:])

        return parts

    @staticmethod
    def _is_double_dot(s: str, pos: int) -> bool:
        # Check if this dot is part of a ".." range operator
        return (pos > 0 and s[pos - 1] == '.') or \
               (pos < len(s) - 1 and s[pos + 1] == '.')

    @staticmethod
    def _parse_bracket_segment(segment: str) -> List[str]:
        cached = TokenValueExtractor._bracket_cache.get(segment)
        if cached is None:
            cached = [
                e.strip() for e in TokenValueExtractor.REGEX_SQUARE_BRACKETS.split(segment)
                if not StringUtil.is_null_or_blank(e.strip())
            ]
            TokenValueExtractor._bracket_cache[segment] = cached
        return cached

    def get_value(self, token: str) -> Any:
        prefix: str = self.get_prefix()

        if not token.startswith(prefix):
            raise KIRuntimeException(
                StringFormatter.format("Token $ doesn't start with $", token, prefix)
            )

        if token.endswith('.__index'):
            parent_part = token[:len(token) - len('.__index')]
            parent_value = self.get_value_internal(parent_part)

            if not is_null_value(parent_value) and isinstance(parent_value, dict) and '__index' in parent_value:
                return parent_value['__index']

            if parent_part.endswith(']'):
                index_string = parent_part[parent_part.rfind('[') + 1: len(parent_part) - 1]
                try:
                    index_int = int(index_string)
                    return index_int
                except (ValueError, TypeError):
                    return index_string
            else:
                return parent_part[parent_part.rfind('.') + 1:]

        return self.get_value_internal(token)

    def set_values_map(self, values_map: Dict[str, TokenValueExtractor]) -> None:
        self.values_map = values_map

    def retrieve_element_from(
        self,
        token: str,
        parts: List[str],
        start_part: int,
        json_element: Any,
    ) -> Any:
        # Iterative version - avoids recursive call overhead
        current = json_element

        for part_number in range(start_part, len(parts)):
            if is_null_value(current):
                return None

            # Use cached bracket segment parsing
            segments = TokenValueExtractor._parse_bracket_segment(parts[part_number])

            for segment in segments:
                current = self._resolve_segment_fast(token, parts, part_number, segment, current)
                if current is None:
                    return None

        return current

    def _resolve_segment_fast(
        self,
        token: str,
        parts: List[str],
        part_number: int,
        segment: str,
        element: Any,
    ) -> Any:
        if element is None:
            return None

        # Skip fast path for quoted segments - they need quote stripping
        if segment.startswith('"') or segment.startswith("'"):
            return self._resolve_for_each_part_of_token_with_brackets(
                token, parts, part_number, segment, element
            )

        # Fast path: simple property access on object (most common case)
        if isinstance(element, dict):
            if segment == 'length':
                if 'length' in element:
                    length_value = element['length']
                    # If length property is a dict or list, use len(element) instead
                    if isinstance(length_value, (dict, list)):
                        return len(element)
                    return length_value
                return len(element)
            return element.get(segment)

        # Fast path: array index access
        if isinstance(element, list):
            # Check for 'length' first
            if segment == 'length':
                return len(element)

            # Only use fast path for pure integer strings (no range operators like '..')
            if re.fullmatch(r'-?\d+', segment):
                idx = int(segment)
                actual_idx = len(element) + idx if idx < 0 else idx
                if 0 <= actual_idx < len(element):
                    return element[actual_idx]
                return None

        # Fast path: string access
        if isinstance(element, str):
            if segment == 'length':
                return len(element)
            # Only use fast path for pure integer strings
            if re.fullmatch(r'-?\d+', segment):
                idx = int(segment)
                actual_idx = len(element) + idx if idx < 0 else idx
                if 0 <= actual_idx < len(element):
                    return element[actual_idx]
                return None

        # Fall back to full handling for edge cases (range operator, etc.)
        return self._resolve_for_each_part_of_token_with_brackets(
            token, parts, part_number, segment, element
        )

    def _resolve_for_each_part_of_token_with_brackets(
        self,
        token: str,
        parts: List[str],
        part_number: int,
        c_part: str,
        c_element: Any,
    ) -> Any:
        if is_null_value(c_element):
            return None

        # Check for 'length' keyword - both unquoted and quoted versions
        if c_part in ('length', '"length"', "'length'"):
            return self._get_length(token, c_element)

        if isinstance(c_element, str) or isinstance(c_element, list):
            return self._handle_array_access(token, c_part, c_element)

        return self._handle_object_access(token, parts, part_number, c_part, c_element)

    def _get_length(self, token: str, c_element: Any) -> Any:
        from kirun_py.runtime.expression.exception.expression_evaluation_exception import (
            ExpressionEvaluationException,
        )

        if isinstance(c_element, str) or isinstance(c_element, list):
            return len(c_element)

        if isinstance(c_element, dict):
            # For objects, check if there's a length property
            if 'length' in c_element:
                length_value = c_element['length']
                # If length property is a dict or list, use len(element) instead
                if isinstance(length_value, (dict, list)):
                    return len(c_element)
                return length_value
            return len(c_element)

        raise ExpressionEvaluationException(
            token,
            StringFormatter.format("Length can't be found in token $", token),
        )

    def _handle_array_access(self, token: str, c_part: str, c_array: Any) -> Any:
        from kirun_py.runtime.expression.exception.expression_evaluation_exception import (
            ExpressionEvaluationException,
        )

        dot_dot_index = c_part.find('..')
        if dot_dot_index >= 0:
            start_index_str = c_part[:dot_dot_index]
            end_index_str = c_part[dot_dot_index + 2:]

            try:
                int_start = 0 if len(start_index_str) == 0 else int(start_index_str)
            except (ValueError, TypeError):
                return None
            try:
                int_end = len(c_array) if len(end_index_str) == 0 else int(end_index_str)
            except (ValueError, TypeError):
                return None

            while int_start < 0:
                int_start += len(c_array)
            while int_end < 0:
                int_end += len(c_array)

            if int_start >= int_end:
                return '' if isinstance(c_array, str) else []

            if isinstance(c_array, str):
                return c_array[int_start:int_end]
            else:
                return c_array[int_start:int_end]

        # Try to parse as integer index
        index: Optional[int] = None
        try:
            index = int(c_part)
        except (ValueError, TypeError):
            pass

        # If parsing failed and we have a valuesMap, try to resolve c_part as a token
        # This allows Parent.__index or similar dynamic indices to work
        if index is None and self.values_map:
            dot_index = c_part.find('.')
            if dot_index > 0:
                prefix = c_part[:dot_index + 1]
                extractor = self.values_map.get(prefix)
                if extractor:
                    try:
                        resolved_value = extractor.get_value(c_part)
                        if isinstance(resolved_value, (int, float)):
                            index = int(resolved_value)
                        elif isinstance(resolved_value, str):
                            try:
                                index = int(resolved_value)
                            except (ValueError, TypeError):
                                pass
                    except Exception:
                        # Resolution failed, will use fallback below
                        pass
                # If extractor not found or resolution failed, use 0 as fallback
                if index is None:
                    index = 0

        if index is None:
            raise ExpressionEvaluationException(
                token,
                StringFormatter.format('$ is not a number', c_part),
            )

        while index < 0:
            index = len(c_array) + index

        if index >= len(c_array):
            return None

        return c_array[index]

    def _handle_object_access(
        self,
        token: str,
        parts: List[str],
        part_number: int,
        c_part: str,
        c_object: Any,
    ) -> Any:
        from kirun_py.runtime.expression.exception.expression_evaluation_exception import (
            ExpressionEvaluationException,
        )

        # Handle both single and double quoted keys
        if c_part.startswith('"') or c_part.startswith("'"):
            quote_char = c_part[0]
            # Allow empty string key: "" or ''
            if not c_part.endswith(quote_char) or len(c_part) == 1:
                raise ExpressionEvaluationException(
                    token,
                    StringFormatter.format('$ is missing a closing quote or empty key found', token),
                )
            c_part = c_part[1:-1]

        self._check_if_object(token, parts, part_number, c_object)

        if isinstance(c_object, dict):
            return c_object.get(c_part)
        return None

    def _check_if_object(
        self,
        token: str,
        parts: List[str],
        part_number: int,
        json_element: Any,
    ) -> None:
        from kirun_py.runtime.expression.exception.expression_evaluation_exception import (
            ExpressionEvaluationException,
        )

        if not isinstance(json_element, (dict, str)):
            raise ExpressionEvaluationException(
                token,
                StringFormatter.format(
                    'Unable to retrieve $ from $ in the path $',
                    parts[part_number],
                    str(json_element),
                    token,
                ),
            )

    @abstractmethod
    def get_value_internal(self, token: str) -> Any:
        ...

    @abstractmethod
    def get_prefix(self) -> str:
        ...

    @abstractmethod
    def get_store(self) -> Any:
        ...
