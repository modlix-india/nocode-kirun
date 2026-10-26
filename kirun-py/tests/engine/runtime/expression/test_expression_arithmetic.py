"""Arithmetic as kirun-js does it (JavaScript numbers): whole numbers never wrap, `/` is true
division (an exact whole division stays an int), `//` floors and `%` keeps the dividend's sign.
The same cases run in kirun-js, kirun-rust and kirun-java."""

from __future__ import annotations

import pytest

from kirun_py.runtime.expression.expression_evaluator import ExpressionEvaluator
from kirun_py.runtime.tokenextractor.arguments_token_value_extractor import ArgumentsTokenValueExtractor

ARGS = {'a': 40000, 'b': 12, 'c': 10000, 'p': 480000, 'q': 6000000, 'n': -40000, 'f': 1.5, 's': 'ab', 'x': 7, 'y': 2}


def ev(expr: str):
    atv = ArgumentsTokenValueExtractor(dict(ARGS))
    return ExpressionEvaluator(expr).evaluate({atv.get_prefix(): atv})


@pytest.mark.parametrize('expr, expected', [
    ('40000 * 12 * 10000', 4800000000),
    ('Arguments.a * Arguments.b * Arguments.c', 4800000000),
    ('100000 * 100000', 10000000000),
    ('65536 * 65536', 4294967296),
    ('2147483647 + 1', 2147483648),
    ('2147483647 * 2147483647', 4611686014132420609),
    ('-40000 * 12 * 10000', -4800000000),
    ('Arguments.n * Arguments.b * Arguments.c', -4800000000),
    ('-2147483648 - 1', -2147483649),
    ('-(-2147483648)', 2147483648),
    ('2 * -3', -6),
    ('6 / 3', 2),
    ('4800000000 / 1000', 4800000),
    ('7 // 2', 3),
    ('-7 // 2', -4),
    ('7.5 // 2', 3),
    ('7 % 3', 1),
    ('-7 % 3', -1),
    ('7 % -3', 1),
    ('-7 % -3', -1),
])
def test_whole_results(expr, expected):
    v = ev(expr)
    assert v == expected
    assert isinstance(v, int) and not isinstance(v, bool), f'{expr} gave {v!r}'


@pytest.mark.parametrize('expr, expected', [
    ('480000 / 6000000', 0.08),
    ('Arguments.p / Arguments.q', 0.08),
    ('7 / 2', 3.5),
    ('Arguments.x / Arguments.y', 3.5),
    ('-7 / 2', -3.5),
    ('1 / 3', 1 / 3),
    ('10 / 4', 2.5),
    ('4800000000 / 7', 4800000000 / 7),
    ('7.5 % 2', 1.5),
    ('-7.5 % 2', -1.5),
    ('1.5 * 4', 6.0),
    ('Arguments.f * Arguments.a', 60000.0),
    ('7 * 0.5', 3.5),
    ('10 - 2.5', 7.5),
    ('0.1 + 0.2', 0.1 + 0.2),
    # `*` and `/` group from the right in every KIRun runtime: a / b * c is a / (b * c).
    ('Arguments.p / Arguments.q * 100', 0.0008),
    ('Arguments.a * Arguments.b * Arguments.c * 10000.0 / Arguments.q', 8000000.0),
])
def test_decimal_results(expr, expected):
    assert ev(expr) == expected


@pytest.mark.parametrize('expr', [
    '40000 * 12 * 10000 > 4000000000',
    'Arguments.a * Arguments.b * Arguments.c = 4800000000',
    '480000 / 6000000 = 0.08',
    '480000 / 6000000 < 0.1',
    '6 / 3 = 2',
])
def test_comparisons_on_results(expr):
    assert ev(expr) is True


@pytest.mark.parametrize('expr, expected', [
    ("'ab' + 40000 * 12 * 10000", 'ab4800000000'),
    ('Arguments.s + 1', 'ab1'),
    ("'x' + 7 / 2", 'x3.5'),
    ("1 + 2 + 'a'", '3a'),
    ("'a' + 1 + 2", 'a12'),
    ('Arguments.s * 2', 'abab'),
])
def test_string_concatenation_unaffected(expr, expected):
    assert ev(expr) == expected
