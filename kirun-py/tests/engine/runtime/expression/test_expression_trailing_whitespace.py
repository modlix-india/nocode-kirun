"""Ports the trailing-whitespace literal probe from kirun-js.

A bare quoted string literal followed by trailing whitespace (or a {{ }} template
that expands to one) must still evaluate to the string, not None.
"""
from __future__ import annotations

from kirun_py.runtime.expression.expression_evaluator import ExpressionEvaluator
from kirun_py.runtime.tokenextractor.output_map_token_value_extractor import (
    OutputMapTokenValueExtractor,
)


def _vm():
    output = {'step1': {'output': {'obj': {'string': 'Hello'}}}}
    omtv = OutputMapTokenValueExtractor(output)
    return {omtv.get_prefix(): omtv}


def test_bare_string_literal_with_trailing_space():
    vm = _vm()
    assert ExpressionEvaluator("'Hello'").evaluate(vm) == 'Hello'
    assert ExpressionEvaluator("'Hello' ").evaluate(vm) == 'Hello'
    assert ExpressionEvaluator("''").evaluate(vm) == ''
    assert ExpressionEvaluator("'' ").evaluate(vm) == ''


def test_template_expanding_to_quoted_literal_with_trailing_spaces():
    vm = _vm()
    result = ExpressionEvaluator(
        '"api/x/{{Steps.step1.output.obj.string}}"  '
    ).evaluate(vm)
    assert result == 'api/x/Hello'
