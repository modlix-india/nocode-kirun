"""Regression tests for stored-definition shapes that used to crash the decompiler.

Both shapes are legitimate and present in real saved functions; the transformers
assumed the narrower form and raised, failing the whole decompile.
"""

import asyncio

from kirun_py.dsl.transformer.schema_transformer import SchemaTransformer


class TestSchemaTransformerTypeShapes:
    """`type` is stored as an array as often as a scalar, and may be a union."""

    def test_scalar_type_still_renders_as_text(self):
        assert SchemaTransformer.to_text({'type': 'STRING'}) == 'STRING'

    def test_single_entry_array_type_renders_as_text(self):
        assert SchemaTransformer.to_text({'type': ['STRING']}) == 'STRING'

    def test_union_type_does_not_raise_and_falls_back_to_json(self):
        # {"type": ["STRING", "NULL"]} is a nullable field. It is not a simple
        # type, so it renders as JSON rather than raising `unhashable type: list`.
        out = SchemaTransformer.to_text({'type': ['STRING', 'NULL']})
        assert 'STRING' in out and 'NULL' in out

    def test_array_of_renders_through_an_array_typed_declaration(self):
        assert SchemaTransformer.to_text(
            {'type': ['ARRAY'], 'items': {'type': ['INTEGER']}}
        ) == 'ARRAY OF INTEGER'

    def test_is_simple_schema_handles_every_type_shape(self):
        assert SchemaTransformer.is_simple_schema({'type': 'STRING'}) is True
        assert SchemaTransformer.is_simple_schema({'type': ['STRING']}) is True
        assert SchemaTransformer.is_simple_schema({'type': ['STRING', 'NULL']}) is False


class TestDependentStatementsShapes:
    """`dependentStatements` is normally a map, but is stored as a list too."""

    @staticmethod
    def _decompile(steps):
        from kirun_py.dsl.dsl_compiler import DSLCompiler
        definition = {
            'namespace': 'Test', 'name': 'Fn',
            'parameters': {}, 'events': {}, 'steps': steps,
        }
        return asyncio.get_event_loop().run_until_complete(
            DSLCompiler.decompile(definition)
        )

    def _one_step(self, deps):
        return {
            'first': {
                'statementName': 'first', 'namespace': 'System', 'name': 'Print',
                'parameterMap': {},
            },
            'second': {
                'statementName': 'second', 'namespace': 'System', 'name': 'Print',
                'parameterMap': {}, 'dependentStatements': deps,
            },
        }

    def test_empty_list_dependent_statements_decompiles(self):
        text = self._decompile(self._one_step([]))
        assert 'second' in text
        assert 'AFTER' not in text.split('second')[1].split('\n')[0]

    def test_list_of_keys_becomes_an_after_clause(self):
        text = self._decompile(self._one_step(['Steps.first.output']))
        assert 'AFTER Steps.first.output' in text

    def test_map_form_still_works(self):
        text = self._decompile(self._one_step({'Steps.first.output': True}))
        assert 'AFTER Steps.first.output' in text

    def test_none_dependent_statements_decompiles(self):
        text = self._decompile(self._one_step(None))
        assert 'second' in text
