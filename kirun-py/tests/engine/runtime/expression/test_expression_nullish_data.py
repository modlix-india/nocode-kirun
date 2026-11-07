"""`??` returns data as it is; only a stored expression (a field named `expression`) is evaluated again.

Port of kirun-js __tests__/engine/runtime/expression/ExpressionNullishDataTest.ts.
"""
from __future__ import annotations

from typing import Any

import pytest

from kirun_py.runtime.expression.expression_evaluator import ExpressionEvaluator
from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor


class _Store(TokenValueExtractor):
    def __init__(self, store: Any):
        super().__init__()
        self._store = store

    def get_value_internal(self, token: str) -> Any:
        return self.retrieve_element_from(token, token.split('.'), 1, self._store)

    def get_prefix(self) -> str:
        return 'Store.'

    def get_store(self) -> Any:
        return self._store


def run(expression: str, store: Any) -> Any:
    ex = _Store(store)
    return ExpressionEvaluator(expression).evaluate({ex.get_prefix(): ex})


@pytest.mark.parametrize('value', ['2026-11-20', '27/11/2026', '10:00', '+919876543210', 'INV/2026-27/1/3',
                                   'a-b', 'Store.other', 'x = y ? 1 : 2'])
def test_left_side_data_is_returned_as_it_is(value):
    assert run("Store.v ?? ''", {'v': value, 'other': 'WRONG'}) == value


def test_fallback_path_holding_data():
    assert run('Store.missing ?? Store.v', {'v': '2026-11-20'}) == '2026-11-20'


def test_fallback_literal_with_operator_characters():
    assert run("Store.missing ?? '-'", {}) == '-'
    assert run("Store.missing ?? 'n/a'", {}) == 'n/a'


def test_inside_a_larger_expression():
    assert run("'On ' + (Store.d ?? '')", {'d': '2026-11-20'}) == 'On 2026-11-20'


def _page(name: Any) -> dict:
    return {'application': {'properties': {'title': 'My App'}}, 'def': {'name': 'x'},
            'urlDetails': {'pageName': 'home'}, 'pageDefinition': {'home': {'properties': {'title': {'name': name}}}}}


STORED = "'Profile '+(Store.def.name ? \"Edit\" : \"Add\")"
QUOTED = ("Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.value ?? "
          "'{{Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.location.expression ?? \"\"}}'")
UNQUOTED = ("Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.value ?? "
            "{{Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.location.expression}}")


def test_quoted_title_form_evaluates_the_stored_expression():
    assert run(QUOTED, _page({'location': {'expression': STORED}})) == 'Profile Edit'


def test_quoted_title_form_keeps_a_plain_title():
    title = 'App Builder - Build Applications in Minutes'
    assert run(QUOTED, _page({'value': title})) == title


def test_unquoted_title_form_evaluates_the_stored_expression():
    page = _page({'location': {'expression': "'Application : ' + Store.application.properties.title"}})
    assert run(UNQUOTED, page) == 'Application : My App'


def test_stored_expression_read_directly():
    assert run("Store.t.location.expression ?? ''", {'t': {'location': {'expression': "'A' + 'B'"}}}) == 'AB'
