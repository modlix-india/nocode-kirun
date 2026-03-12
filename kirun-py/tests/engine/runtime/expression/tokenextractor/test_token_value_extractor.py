from __future__ import annotations

from typing import Any, List

import pytest

from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor


class TestExtractor(TokenValueExtractor):
    """Test extractor that exposes retrieve_element_from for testing."""

    def get_value_internal(self, token: str) -> Any:
        return None

    def get_prefix(self) -> str:
        return 'Testing'

    def retrieve_element_from(
        self,
        token: str,
        parts: List[str],
        part_number: int,
        json_element: Any,
    ) -> Any:
        return super().retrieve_element_from(token, parts, part_number, json_element)

    def get_store(self) -> Any:
        return None


class TestTokenValueExtractor:
    """Ported from TokenValueExtractorTest.ts"""

    @pytest.fixture
    def extractor(self):
        return TestExtractor()

    def test_array_index_access(self, extractor):
        arr = [0, 2, 4, 6]
        token = '[2]'
        assert extractor.retrieve_element_from(token, token.split('.'), 0, arr) == 4

    def test_double_array_index_access(self, extractor):
        darr0 = [2, 4, 6]
        darr1 = [3, 6, 9]
        darr2 = [4, 8, 12, 16]
        darr = [darr0, darr1, darr2]

        token = '[1][1]'
        assert extractor.retrieve_element_from(token, token.split('.'), 0, darr) == 6

    def test_array_length(self, extractor):
        darr2 = [4, 8, 12, 16]
        darr = [[2, 4, 6], [3, 6, 9], darr2]

        token = '[2].length'
        assert extractor.retrieve_element_from(token, token.split('.'), 0, darr) == 4

    def test_object_property_access(self, extractor):
        arr = [0, 2, 4, 6]
        b = {'c': 'K', 'arr': arr, 'darr': [[2, 4, 6], [3, 6, 9], [4, 8, 12, 16]]}
        a = {'b': b}
        obj = {'a': a, 'array': arr}

        token = 'a.b.c'
        assert extractor.retrieve_element_from(token, token.split('.'), 0, obj) == 'K'

    def test_nested_object_access(self, extractor):
        arr = [0, 2, 4, 6]
        b = {'c': 'K', 'arr': arr, 'darr': [[2, 4, 6], [3, 6, 9], [4, 8, 12, 16]]}
        a = {'b': b}
        obj = {'a': a, 'array': arr}

        token = 'a.b'
        assert extractor.retrieve_element_from(token, token.split('.'), 0, obj) == b

    def test_nested_object_property_with_offset(self, extractor):
        arr = [0, 2, 4, 6]
        b = {'c': 'K', 'arr': arr, 'darr': [[2, 4, 6], [3, 6, 9], [4, 8, 12, 16]]}
        a = {'b': b}

        token = 'a.b.c'
        assert extractor.retrieve_element_from(token, token.split('.'), 1, a) == 'K'

    def test_nested_object_array_access(self, extractor):
        arr = [0, 2, 4, 6]
        b = {'c': 'K', 'arr': arr, 'darr': [[2, 4, 6], [3, 6, 9], [4, 8, 12, 16]]}
        a = {'b': b}

        token = 'a.b.arr[2]'
        assert extractor.retrieve_element_from(token, token.split('.'), 1, a) == 4

    def test_nested_double_array_access(self, extractor):
        arr = [0, 2, 4, 6]
        b = {'c': 'K', 'arr': arr, 'darr': [[2, 4, 6], [3, 6, 9], [4, 8, 12, 16]]}
        a = {'b': b}

        token = 'a.b.darr[2][3]'
        assert extractor.retrieve_element_from(token, token.split('.'), 1, a) == 16

    def test_nested_double_array_length(self, extractor):
        arr = [0, 2, 4, 6]
        b = {'c': 'K', 'arr': arr, 'darr': [[2, 4, 6], [3, 6, 9], [4, 8, 12, 16]]}
        a = {'b': b}

        token = 'a.b.darr[2].length'
        assert extractor.retrieve_element_from(token, token.split('.'), 1, a) == 4


class TestBracketNotationWithDottedKeys:
    """Ported from TokenValueExtractorTest.ts - bracket notation with dotted keys."""

    @pytest.fixture
    def extractor(self):
        return TestExtractor()

    def test_double_quote_dotted_key(self, extractor):
        config = {
            'mail.props.port': 587,
            'mail.props.host': 'smtp.example.com',
            'api.key.secret': 'secret123',
            'simple': 'value',
        }
        obj = {'config': config}

        split_path = TokenValueExtractor.split_path

        token = 'config["mail.props.port"]'
        assert extractor.retrieve_element_from(token, split_path(token), 0, obj) == 587

    def test_single_quote_dotted_key(self, extractor):
        config = {
            'mail.props.port': 587,
            'mail.props.host': 'smtp.example.com',
            'api.key.secret': 'secret123',
            'simple': 'value',
        }
        obj = {'config': config}

        split_path = TokenValueExtractor.split_path

        token = "config['mail.props.host']"
        assert extractor.retrieve_element_from(token, split_path(token), 0, obj) == 'smtp.example.com'

    def test_nested_dotted_key(self, extractor):
        config = {
            'mail.props.port': 587,
            'mail.props.host': 'smtp.example.com',
            'api.key.secret': 'secret123',
            'simple': 'value',
        }
        obj = {'config': config}

        split_path = TokenValueExtractor.split_path

        token = "config['api.key.secret']"
        assert extractor.retrieve_element_from(token, split_path(token), 0, obj) == 'secret123'

    def test_mixed_dot_and_bracket(self, extractor):
        config = {
            'mail.props.port': 587,
            'mail.props.host': 'smtp.example.com',
            'api.key.secret': 'secret123',
            'simple': 'value',
            'nested': {'field.with.dots': 'nestedValue'},
        }
        obj = {'config': config}

        split_path = TokenValueExtractor.split_path

        token = "config.nested['field.with.dots']"
        assert extractor.retrieve_element_from(token, split_path(token), 0, obj) == 'nestedValue'

    def test_regular_dot_notation_still_works(self, extractor):
        config = {
            'mail.props.port': 587,
            'mail.props.host': 'smtp.example.com',
            'api.key.secret': 'secret123',
            'simple': 'value',
        }
        obj = {'config': config}

        split_path = TokenValueExtractor.split_path

        token = 'config.simple'
        assert extractor.retrieve_element_from(token, split_path(token), 0, obj) == 'value'


class TestSplitPath:
    """Ported from TokenValueExtractorTest.ts - splitPath tests."""

    def test_split_with_single_quote_bracket(self):
        parts = TokenValueExtractor.split_path("Context.obj['mail.props.port']")
        assert len(parts) == 2
        assert parts[0] == 'Context'
        assert parts[1] == "obj['mail.props.port']"

    def test_split_with_bracket_then_dot(self):
        parts = TokenValueExtractor.split_path("Context.obj['mail.props.port'].value")
        assert len(parts) == 3
        assert parts[0] == 'Context'
        assert parts[1] == "obj['mail.props.port']"
        assert parts[2] == 'value'

    def test_split_steps_with_bracket(self):
        parts = TokenValueExtractor.split_path("Steps.source.output['field.name']")
        assert len(parts) == 3
        assert parts[0] == 'Steps'
        assert parts[1] == 'source'
        assert parts[2] == "output['field.name']"

    def test_split_range_operator_preserved(self):
        parts = TokenValueExtractor.split_path('array[0..5]')
        assert len(parts) == 1
        assert parts[0] == 'array[0..5]'

    def test_split_multiple_brackets(self):
        parts = TokenValueExtractor.split_path("obj['key.one']['key.two']")
        assert len(parts) == 1
        assert parts[0] == "obj['key.one']['key.two']"
