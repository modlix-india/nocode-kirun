"""A ParameterReference `type` stored as an array must survive a DSL round trip.

`ParameterReference.SCHEMA` declares `type` as a string with enums EXPRESSION |
VALUE, but a large amount of stored data carries it as a single-element ARRAY,
`["EXPRESSION"]` -- that is what modlix-mcp and the appbuilder generator tools
have always written. The JS runtime never minded, because it compares with `==`
and `['EXPRESSION'] == 'EXPRESSION'` is true in JS, so those definitions are all
over the platform and all run correctly.

Two ways Python broke on them, both fixed alongside these tests:

1. `json_to_text._param_ref_to_text` compared `ref['type'] == 'EXPRESSION'`.
   Python has no coercion, so an array-typed reference missed the expression
   branch, fell through to the value branch, and was emitted as its `value` --
   which for an expression ref is None. Compiling that text back gives
   `{'type': 'VALUE', 'value': None}`: the expression is gone. Data loss in what
   reads as a formatting operation.
2. `ParameterReference.from_value` did `ParameterReferenceType(e['type'])`,
   which RAISES on a list, so the runtime could not load such a definition at
   all.

The fixture is the appbuilder `workspace` page's onLoad, the real function that
lost 19 of its 20 expressions to the equivalent TypeScript bug on 2026-09-02.
Its 20th, `setApp.value`, is the built-in control: the one reference in that
function whose `type` was already a plain string, and the one that survived.
"""

import asyncio
import json
from pathlib import Path

import pytest

from kirun_py.dsl.dsl_compiler import DSLCompiler
from kirun_py.model.parameter_reference import ParameterReference
from kirun_py.model.parameter_reference_type import ParameterReferenceType

FIXTURE = Path(__file__).parent / 'workspace_onload.json'


def refs_by_position(fn):
    """(step, param, order) -> ref. Never key on the ref's own key: a round trip regenerates it."""
    out = {}
    for step_name, step in (fn.get('steps') or {}).items():
        for param_name, refs in (step.get('parameterMap') or {}).items():
            if not isinstance(refs, dict):
                continue
            for ref in refs.values():
                if isinstance(ref, dict):
                    out[(step_name, param_name, ref.get('order', 1))] = ref
    return out


def is_expression(ref):
    ref_type = ref.get('type')
    if isinstance(ref_type, list):
        return bool(ref_type) and ref_type[0] == 'EXPRESSION'
    return ref_type == 'EXPRESSION'


def round_trip(fn):
    text = asyncio.get_event_loop().run_until_complete(DSLCompiler.decompile(fn))
    return DSLCompiler.compile(text)


class TestParameterReferenceTypeOf:
    """The model has to LOAD an array-typed reference before anything else matters."""

    def test_scalar_type(self):
        assert ParameterReferenceType.of('EXPRESSION') is ParameterReferenceType.EXPRESSION
        assert ParameterReferenceType.of('VALUE') is ParameterReferenceType.VALUE

    def test_single_entry_array_type(self):
        assert ParameterReferenceType.of(['EXPRESSION']) is ParameterReferenceType.EXPRESSION
        assert ParameterReferenceType.of(['VALUE']) is ParameterReferenceType.VALUE

    def test_unknown_and_empty_fall_back_to_value(self):
        assert ParameterReferenceType.of(None) is ParameterReferenceType.VALUE
        assert ParameterReferenceType.of([]) is ParameterReferenceType.VALUE
        assert ParameterReferenceType.of('NONSENSE') is ParameterReferenceType.VALUE

    def test_from_value_loads_an_array_typed_reference(self):
        # This raised ValueError: ['EXPRESSION'] is not a valid ParameterReferenceType.
        ref = ParameterReference.from_value(
            {'key': 'k', 'type': ['EXPRESSION'], 'expression': 'Page.recentNew', 'order': 1}
        )
        assert ref.get_type() is ParameterReferenceType.EXPRESSION
        assert ref.get_expression() == 'Page.recentNew'

    def test_to_json_writes_the_scalar_back(self):
        ref = ParameterReference.from_value({'key': 'k', 'type': ['EXPRESSION'], 'expression': 'A.b'})
        assert ref.to_json()['type'] == 'EXPRESSION'


class TestDSLRoundTripKeepsArrayTypedRefs:
    def test_single_array_typed_expression(self):
        fn = {
            'namespace': 'Test',
            'name': 'arrayTyped',
            'parameters': {},
            'events': {},
            'steps': {
                'fetch': {
                    'statementName': 'fetch',
                    'namespace': 'UIEngine',
                    'name': 'FetchData',
                    'parameterMap': {
                        'url': {
                            'k1': {
                                'key': 'k1',
                                'type': ['EXPRESSION'],
                                'expression': "'/api/security/applications/appCode/' + Url.pathParts[1]",
                                'value': None,
                                'order': 1,
                            }
                        }
                    },
                }
            },
        }

        ref = refs_by_position(round_trip(fn))[('fetch', 'url', 1)]

        assert is_expression(ref)
        assert ref['expression'] == "'/api/security/applications/appCode/' + Url.pathParts[1]"

    def test_array_typed_values(self):
        fn = {
            'namespace': 'Test',
            'name': 'arrayTypedValue',
            'parameters': {},
            'events': {},
            'steps': {
                'seed': {
                    'statementName': 'seed',
                    'namespace': 'UIEngine',
                    'name': 'SetStore',
                    'parameterMap': {
                        'path': {
                            'k1': {'key': 'k1', 'type': ['VALUE'], 'value': 'Page.recentNew',
                                   'expression': None, 'order': 1}
                        },
                        'value': {
                            'k2': {'key': 'k2', 'type': ['VALUE'], 'value': [],
                                   'expression': None, 'order': 1}
                        },
                    },
                }
            },
        }

        refs = refs_by_position(round_trip(fn))

        assert refs[('seed', 'path', 1)]['value'] == 'Page.recentNew'
        assert refs[('seed', 'value', 1)]['value'] == []


class TestNumberFidelity:
    """int must not become float on the way through the DSL.

    Found by `test_keeps_every_value_parameter` below: eight seeded option lists
    came back with every `order: 1` as `order: 1.0`. The parser read every
    numeric literal with `float()`, which is invisible in the TypeScript port
    because JS has one number type, and turns every int in a definition into a
    Mongo double here.
    """

    @staticmethod
    def _value_round_trip(value):
        fn = {
            'namespace': 'Test',
            'name': 'numbers',
            'parameters': {},
            'events': {},
            'steps': {
                'seed': {
                    'statementName': 'seed',
                    'namespace': 'UIEngine',
                    'name': 'SetStore',
                    'parameterMap': {
                        'value': {
                            'k': {'key': 'k', 'type': 'VALUE', 'value': value,
                                  'expression': None, 'order': 1}
                        }
                    },
                }
            },
        }
        return refs_by_position(round_trip(fn))[('seed', 'value', 1)]['value']

    def test_integer_stays_an_integer(self):
        out = self._value_round_trip({'order': 1, 'size': 25})
        assert out == {'order': 1, 'size': 25}
        assert all(isinstance(v, int) for v in out.values())

    def test_float_stays_a_float(self):
        out = self._value_round_trip({'ratio': 1.5})
        assert isinstance(out['ratio'], float)
        assert out['ratio'] == 1.5

    def test_nested_integers_in_a_list_stay_integers(self):
        out = self._value_round_trip([{'key': 'a', 'order': 1}, {'key': 'b', 'order': 2}])
        assert [row['order'] for row in out] == [1, 2]
        assert all(isinstance(row['order'], int) for row in out)

    def test_parse_number_shapes(self):
        from kirun_py.dsl.number_literal import parse_number

        assert parse_number('1') == 1
        assert isinstance(parse_number('1'), int)
        assert isinstance(parse_number('1.0'), float)
        assert isinstance(parse_number('1e3'), float)
        assert parse_number('-4') == -4
        assert isinstance(parse_number('-4'), int)
        # Beyond float's exact range: read as an int, so no digit is lost.
        assert parse_number('12345678901234567890') == 12345678901234567890
        with pytest.raises(ValueError):
            parse_number('nonsense')


@pytest.mark.skipif(not FIXTURE.exists(), reason='fixture missing')
class TestWorkspaceOnLoad:
    """The real function, the real loss."""

    @staticmethod
    def _on_load():
        return json.loads(FIXTURE.read_text())

    def test_fixture_is_the_shape_this_bug_needs(self):
        on_load = self._on_load()
        refs = list(refs_by_position(on_load).values())

        assert len(on_load['steps']) == 42
        assert len([r for r in refs if isinstance(r.get('type'), list)]) == 75
        # 19 array-typed expressions plus setApp.value, already a plain string.
        assert len([r for r in refs if is_expression(r)]) == 20

    def test_loses_no_expression(self):
        on_load = self._on_load()
        before = refs_by_position(on_load)
        after = refs_by_position(round_trip(on_load))

        lost = []
        for position, ref in before.items():
            if not is_expression(ref) or not ref.get('expression'):
                continue
            now = after.get(position)
            if now is None or not is_expression(now) or now.get('expression') != ref['expression']:
                lost.append(f"{position[0]}.{position[1]} ({ref['expression']})")

        assert lost == []

    def test_turns_no_expression_into_a_null_value(self):
        on_load = self._on_load()
        before = refs_by_position(on_load)
        after = refs_by_position(round_trip(on_load))

        emptied = [
            f'{position[0]}.{position[1]}'
            for position, ref in before.items()
            if is_expression(ref)
            and (now := after.get(position)) is not None
            and not is_expression(now)
            and now.get('value') is None
            and not now.get('expression')
        ]

        assert emptied == []

    def test_keeps_every_value_parameter(self):
        on_load = self._on_load()
        before = refs_by_position(on_load)
        after = refs_by_position(round_trip(on_load))

        changed = []
        for position, ref in before.items():
            if is_expression(ref):
                continue
            now = after.get(position)
            if now is None or json.dumps(now.get('value'), sort_keys=True) != json.dumps(
                ref.get('value'), sort_keys=True
            ):
                changed.append(f'{position[0]}.{position[1]}')

        assert changed == []
