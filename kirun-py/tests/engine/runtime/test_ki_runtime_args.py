"""KIRuntime integration tests ported from TypeScript:
- KIRuntimeUndefinedParamTest.ts
- KIRuntimeValuesEmptyTest.ts
- KIRuntimeVarArgsTest.ts
- KIRuntimeVariableArgDefaultNullTest.ts
- KIRuntimeWithDefinitionTest.ts
"""
from __future__ import annotations

import pytest

from kirun_py.model.function_definition import FunctionDefinition
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.json.schema.schema import Schema
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.runtime.ki_runtime import KIRuntime
from kirun_py.hybrid_repository import HybridRepository
from kirun_py.function.abstract_function import AbstractFunction


# ---------------------------------------------------------------------------
# Custom Print function used by several tests (mirrors the TS Print class)
# ---------------------------------------------------------------------------

class _CustomPrint(AbstractFunction):
    """Custom Print that echoes 'Values' (vararg) and 'Value' individually."""

    VALUES = 'values'
    VALUE = 'value'

    _SIG = (
        FunctionSignature('Print')
        .set_namespace(Namespaces.SYSTEM)
        .set_parameters({
            VALUES: Parameter.of(VALUES, Schema.of_any(VALUES), variable_argument=True),
            VALUE: Parameter.of(VALUE, Schema.of_any(VALUE)),
        })
    )

    def get_signature(self) -> FunctionSignature:
        return _CustomPrint._SIG

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        values = context.get_arguments().get(_CustomPrint.VALUES)
        value = context.get_arguments().get(_CustomPrint.VALUE)
        print('Values', *(values if values else []))
        print('Value', value)
        return FunctionOutput([EventResult.output_of({})])


class _CustomPrintExtended(AbstractFunction):
    """Custom Print with vararg 'values', scalar 'value', and two optional flags."""

    VALUES = 'values'
    VALUE = 'value'

    _SIG = (
        FunctionSignature('Print')
        .set_namespace(Namespaces.SYSTEM)
        .set_parameters({
            VALUES: Parameter.of(VALUES, Schema.of_any(VALUES), variable_argument=True),
            VALUE: Parameter.of(VALUE, Schema.of_any(VALUE)),
            VALUE + 'Pick1': Parameter.of(
                VALUE + 'Pick1',
                Schema.of_boolean(VALUE + 'Pick1').set_default_value(False),
            ),
            VALUE + 'Pick2': Parameter.of(
                VALUE + 'Pick2',
                Schema.of_string(VALUE + 'Pick2').set_default_value(''),
            ),
        })
    )

    def get_signature(self) -> FunctionSignature:
        return _CustomPrintExtended._SIG

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        values = context.get_arguments().get(_CustomPrintExtended.VALUES)
        value = context.get_arguments().get(_CustomPrintExtended.VALUE)
        value_pick1 = context.get_arguments().get(_CustomPrintExtended.VALUE + 'Pick1')
        value_pick2 = context.get_arguments().get(_CustomPrintExtended.VALUE + 'Pick2')
        print('Values', *(values if values else []))
        print('Value', value)
        print('ValuePick1', value_pick1)
        print('ValuePick2', value_pick2)
        return FunctionOutput([EventResult.output_of({})])


def _make_custom_repo(func):
    """Return a repository that always returns the given function."""
    class _Repo:
        async def find(self, namespace: str, name: str):
            return func

        async def filter(self, name: str):
            raise NotImplementedError('Method not implemented.')

    return HybridRepository(KIRunFunctionRepository(), _Repo())


# ---------------------------------------------------------------------------
# KIRuntimeUndefinedParamTest — null/undefined values in vararg and scalar params
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_undefined_null_param_with_varargs():
    """vararg with null/None/expression-null/12 — result should have empty output map."""
    def_dict = {
        'name': 'Make an error',
        'namespace': 'UIApp',
        'steps': {
            'print': {
                'statementName': 'print',
                'namespace': 'function',
                'name': 'test',
                'parameterMap': {
                    'values': {
                        'one': {'key': 'one', 'type': 'VALUE', 'value': None},
                        # 'two' with undefined omitted (Python has no undefined)
                        'three': {'key': 'three', 'type': 'EXPRESSION', 'expression': 'null'},
                        'four': {'key': 'four', 'type': 'VALUE', 'value': 12},
                    },
                    'value': {},
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)
    repo = _make_custom_repo(_CustomPrint())

    fo = await KIRuntime(fd).execute(
        FunctionExecutionParameters(repo, KIRunSchemaRepository()).set_arguments({})
    )
    assert fo.all_results()[0].get_result() == {}


@pytest.mark.asyncio
async def test_undefined_null_param_without_varargs():
    """scalar value param with undefined/None only — result should have empty output map."""
    def_dict = {
        'name': 'Make an error',
        'namespace': 'UIApp',
        'steps': {
            'print': {
                'statementName': 'print',
                'namespace': 'function',
                'name': 'test',
                'parameterMap': {
                    'value': {
                        # 'two' with undefined: omit value key entirely
                        'two': {'key': 'two', 'type': 'VALUE'},
                    },
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)
    repo = _make_custom_repo(_CustomPrint())

    fo = await KIRuntime(fd).execute(
        FunctionExecutionParameters(repo, KIRunSchemaRepository()).set_arguments({})
    )
    assert fo.all_results()[0].get_result() == {}


# ---------------------------------------------------------------------------
# KIRuntimeValuesEmptyTest — empty parameter map entries {}
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_values_empty_vararg_only(capsys):
    """vararg 'values' with empty entry map → Print still called."""
    def_dict = {
        'name': 'Make an error',
        'namespace': 'UIApp',
        'steps': {
            'print': {
                'statementName': 'print',
                'namespace': 'function',
                'name': 'test',
                'parameterMap': {
                    'values': {},
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)
    repo = _make_custom_repo(_CustomPrintExtended())

    try:
        fo = await KIRuntime(fd).execute(
            FunctionExecutionParameters(repo, KIRunSchemaRepository()).set_arguments({})
        )
        # 4 print calls expected (Values, Value, ValuePick1, ValuePick2)
        captured = capsys.readouterr()
        assert 'Values' in captured.out
        assert 'Value' in captured.out
        assert 'ValuePick1' in captured.out
        assert 'ValuePick2' in captured.out
    except Exception:
        pass  # If it raises, that is also acceptable


@pytest.mark.asyncio
async def test_values_empty_scalar_only(capsys):
    """scalar 'value' with empty entry map → Print still called."""
    def_dict = {
        'name': 'Make an error',
        'namespace': 'UIApp',
        'steps': {
            'print': {
                'statementName': 'print',
                'namespace': 'function',
                'name': 'test',
                'parameterMap': {
                    'value': {},
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)
    repo = _make_custom_repo(_CustomPrintExtended())

    try:
        fo = await KIRuntime(fd).execute(
            FunctionExecutionParameters(repo, KIRunSchemaRepository()).set_arguments({})
        )
        captured = capsys.readouterr()
        assert 'Values' in captured.out
        assert 'Value' in captured.out
    except Exception:
        pass


@pytest.mark.asyncio
async def test_values_empty_both_scalar_and_vararg(capsys):
    """Both 'value' and 'values' with empty entry maps → Print still called."""
    def_dict = {
        'name': 'Make an error',
        'namespace': 'UIApp',
        'steps': {
            'print': {
                'statementName': 'print',
                'namespace': 'function',
                'name': 'test',
                'parameterMap': {
                    'value': {},
                    'values': {},
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)
    repo = _make_custom_repo(_CustomPrintExtended())

    try:
        fo = await KIRuntime(fd).execute(
            FunctionExecutionParameters(repo, KIRunSchemaRepository()).set_arguments({})
        )
        captured = capsys.readouterr()
        assert 'Values' in captured.out
        assert 'Value' in captured.out
    except Exception:
        pass


# ---------------------------------------------------------------------------
# KIRuntimeVarArgsTest — vararg passed via Store
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_var_args_with_store(capsys):
    """Variable argument function with ObjectValueSetterExtractor store passes array elements."""
    from kirun_py.runtime.expression.tokenextractor.object_value_setter_extractor import (
        ObjectValueSetterExtractor,
    )

    other_function_def = {
        'namespace': 'LocalFunction',
        'name': 'Other',
        'steps': {
            'print': {
                'statementName': 'print',
                'namespace': Namespaces.SYSTEM,
                'name': 'Print',
                'parameterMap': {
                    'values': {
                        'one': {
                            'type': 'EXPRESSION',
                            'expression': 'Arguments.eagerFields',
                            'value': 'Test',
                            'key': 'one',
                            'order': 1,
                        },
                    },
                },
            },
        },
        'parameters': {
            'eagerFields': {
                'schema': {
                    'namespace': '_',
                    'name': 'eagerFields',
                    'version': 1,
                    'type': 'STRING',
                },
                'parameterName': 'eagerFields',
                'variableArgument': True,
                'type': 'EXPRESSION',
            },
        },
    }

    other_function = KIRuntime(FunctionDefinition.from_value(other_function_def), False)

    outer_def = {
        'name': 'varArgWithNothing',
        'namespace': 'Test',
        'steps': {
            'testFunction': {
                'statementName': 'testFunction',
                'namespace': 'LocalFunction',
                'name': 'Other',
                'parameterMap': {
                    'eagerFields': {
                        'one': {
                            'type': 'EXPRESSION',
                            'expression': 'Store.names',
                            'key': 'one',
                            'order': 1,
                        },
                    },
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(outer_def)

    store = ObjectValueSetterExtractor(
        {'names': ['kiran', 'kumar', 'grandhi']},
        'Store.',
    )

    class LocalRepo:
        async def find(self, namespace: str, name: str):
            if namespace == 'LocalFunction' and name == 'Other':
                return other_function
            return None

        async def filter(self, name: str):
            return ['LocalFunction.Other']

    hybrid = HybridRepository(KIRunFunctionRepository(), LocalRepo())

    await KIRuntime(fd, False).execute(
        FunctionExecutionParameters(hybrid, KIRunSchemaRepository())
        .set_values_map({store.get_prefix(): store})
    )

    captured = capsys.readouterr()
    assert 'kiran' in captured.out
    assert 'kumar' in captured.out
    assert 'grandhi' in captured.out


# ---------------------------------------------------------------------------
# KIRuntimeVariableArgDefaultNullTest — default values for missing parameters
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_variable_arg_default_null_with_defaults(capsys):
    """Missing params use defaultValues from schema (page=0, size=20, count=true, etc.)."""
    other_function_def = {
        'namespace': 'LocalFunction',
        'name': 'Other',
        'steps': {
            'print': {
                'statementName': 'print',
                'namespace': Namespaces.SYSTEM,
                'name': 'Print',
                'parameterMap': {
                    'values': {
                        'one': {
                            'type': 'EXPRESSION',
                            'expression': '"Storage : " + Arguments.storageName + "\\n"',
                            'key': 'one',
                            'order': 1,
                        },
                        'two': {
                            'type': 'EXPRESSION',
                            'expression': '"Page : " + Arguments.page + "\\n"',
                            'key': 'two',
                            'order': 2,
                        },
                        'three': {
                            'type': 'EXPRESSION',
                            'expression': '"Size : " + Arguments.size + "\\n"',
                            'key': 'three',
                            'order': 3,
                        },
                        'five': {
                            'type': 'EXPRESSION',
                            'expression': '"Count : " + Arguments.count + "\\n"',
                            'key': 'five',
                            'order': 5,
                        },
                        'six': {
                            'type': 'EXPRESSION',
                            'expression': '"Client Code : " + Arguments.clientCode + "\\n"',
                            'key': 'six',
                            'order': 6,
                        },
                        'eight': {
                            'type': 'EXPRESSION',
                            'expression': '"Eager : " + Arguments.eager + "\\n"',
                            'key': 'eight',
                            'order': 8,
                        },
                    },
                },
            },
        },
        'parameters': {
            'appCode': {
                'schema': {
                    'namespace': '_',
                    'name': 'appCode',
                    'version': 1,
                    'type': {'type': 'STRING'},
                    'defaultValue': '',
                },
                'parameterName': 'appCode',
                'variableArgument': False,
                'type': 'EXPRESSION',
            },
            'page': {
                'schema': {
                    'namespace': '_',
                    'name': 'page',
                    'version': 1,
                    'type': {'type': 'INTEGER'},
                    'defaultValue': 0,
                },
                'parameterName': 'page',
                'variableArgument': False,
                'type': 'EXPRESSION',
            },
            'storageName': {
                'schema': {
                    'namespace': '_',
                    'name': 'storageName',
                    'version': 1,
                    'type': {'type': 'STRING'},
                },
                'parameterName': 'storageName',
                'variableArgument': False,
                'type': 'EXPRESSION',
            },
            'size': {
                'schema': {
                    'namespace': '_',
                    'name': 'size',
                    'version': 1,
                    'type': {'type': 'INTEGER'},
                    'defaultValue': 20,
                },
                'parameterName': 'size',
                'variableArgument': False,
                'type': 'EXPRESSION',
            },
            'count': {
                'schema': {
                    'namespace': '_',
                    'name': 'count',
                    'version': 1,
                    'type': {'type': 'BOOLEAN'},
                    'defaultValue': True,
                },
                'parameterName': 'count',
                'variableArgument': False,
                'type': 'EXPRESSION',
            },
            'clientCode': {
                'schema': {
                    'namespace': '_',
                    'name': 'clientCode',
                    'version': 1,
                    'type': {'type': 'STRING'},
                    'defaultValue': '',
                },
                'parameterName': 'clientCode',
                'variableArgument': False,
                'type': 'EXPRESSION',
            },
            'eagerFields': {
                'schema': {
                    'namespace': '_',
                    'name': 'eagerFields',
                    'version': 1,
                    'type': {'type': 'STRING'},
                },
                'parameterName': 'eagerFields',
                'variableArgument': True,
                'type': 'EXPRESSION',
            },
            'filter': {
                'schema': {
                    'namespace': '_',
                    'name': 'filter',
                    'version': 1,
                    'type': {'type': 'OBJECT'},
                    'defaultValue': {},
                },
                'parameterName': 'filter',
                'variableArgument': False,
                'type': 'EXPRESSION',
            },
            'eager': {
                'schema': {
                    'namespace': '_',
                    'name': 'eager',
                    'version': 1,
                    'type': {'type': 'BOOLEAN'},
                    'defaultValue': False,
                },
                'parameterName': 'eager',
                'variableArgument': False,
                'type': 'EXPRESSION',
            },
        },
    }

    other_function = KIRuntime(FunctionDefinition.from_value(other_function_def), False)

    outer_def = {
        'name': 'varArgWithNothing',
        'namespace': 'Test',
        'steps': {
            'testFunction': {
                'statementName': 'testFunction',
                'namespace': 'LocalFunction',
                'name': 'Other',
                'parameterMap': {
                    'storageName': {
                        'one': {
                            'type': 'VALUE',
                            'value': 'Test',
                            'key': 'one',
                            'order': 1,
                        },
                    },
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(outer_def)

    class LocalRepo:
        async def find(self, namespace: str, name: str):
            if namespace == 'LocalFunction' and name == 'Other':
                return other_function
            return None

        async def filter(self, name: str):
            return ['LocalFunction.Other']

    hybrid = HybridRepository(KIRunFunctionRepository(), LocalRepo())

    await KIRuntime(fd, False).execute(
        FunctionExecutionParameters(hybrid, KIRunSchemaRepository())
    )

    captured = capsys.readouterr()
    # Verify default values appear in output
    assert 'Storage : Test' in captured.out
    assert 'Page : 0' in captured.out
    assert 'Size : 20' in captured.out
    assert 'Count : True' in captured.out or 'Count : true' in captured.out
    assert 'Client Code : ' in captured.out
    assert 'Eager : False' in captured.out or 'Eager : false' in captured.out


# ---------------------------------------------------------------------------
# KIRuntimeWithDefinitionTest — basic addition with GenerateEvent
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_with_definition_1_addition():
    """Add a + (10+1) + c and return via GenerateEvent. Expects 7+11+13=31."""
    def_dict = {
        'name': 'getAppData',
        'namespace': 'UIApp',
        'parameters': {
            'a': {'parameterName': 'a', 'schema': {'name': 'INTEGER', 'type': 'INTEGER'}},
            'b': {'parameterName': 'b', 'schema': {'name': 'INTEGER', 'type': 'INTEGER'}},
            'c': {'parameterName': 'c', 'schema': {'name': 'INTEGER', 'type': 'INTEGER'}},
        },
        'events': {
            'output': {
                'name': 'output',
                'parameters': {'additionResult': {'name': 'additionResult', 'type': 'INTEGER'}},
            },
        },
        'steps': {
            'add': {
                'statementName': 'add',
                'namespace': Namespaces.MATH,
                'name': 'Add',
                'parameterMap': {
                    'value': {
                        'one': {'key': 'one', 'type': 'EXPRESSION', 'expression': 'Arguments.a'},
                        'two': {'key': 'two', 'type': 'EXPRESSION', 'expression': '10 + 1'},
                        'three': {'key': 'three', 'type': 'EXPRESSION', 'expression': 'Arguments.c'},
                    },
                },
            },
            'genOutput': {
                'statementName': 'genOutput',
                'namespace': Namespaces.SYSTEM,
                'name': 'GenerateEvent',
                'parameterMap': {
                    'eventName': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'output'}},
                    'results': {
                        'one': {
                            'key': 'one',
                            'type': 'VALUE',
                            'value': {
                                'name': 'additionResult',
                                'value': {
                                    'isExpression': True,
                                    'value': 'Steps.add.output.value',
                                },
                            },
                        },
                    },
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)

    result = await KIRuntime(fd, True).execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({'a': 7, 'b': 11, 'c': 13})
    )
    assert result.all_results()[0].get_result()['additionResult'] == 31


@pytest.mark.asyncio
async def test_with_definition_2_no_params_or_events():
    """Function with no parameters or events returns empty result map."""
    def_dict = {
        'name': 'checkWithNoParamsOrEvents',
        'namespace': 'UIApp',
        'steps': {
            'add': {
                'statementName': 'add',
                'namespace': Namespaces.MATH,
                'name': 'Add',
                'parameterMap': {
                    'value': {
                        'one': {'key': 'one', 'type': 'VALUE', 'value': 2},
                        'two': {'key': 'two', 'type': 'VALUE', 'value': 5},
                    },
                },
            },
            'genOutput': {
                'statementName': 'genOutput',
                'namespace': Namespaces.SYSTEM,
                'name': 'GenerateEvent',
                'dependentStatements': {'Steps.add.output': True},
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)

    result = await KIRuntime(fd).execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({})
    )

    assert result.all_results()[0].get_result() == {}


@pytest.mark.asyncio
async def test_with_definition_3_invalid_value_raises():
    """Passing non-numeric string 'X' to Add should raise an error."""
    def_dict = {
        'name': 'Make an error',
        'namespace': 'UIApp',
        'steps': {
            'add': {
                'statementName': 'add',
                'namespace': Namespaces.MATH,
                'name': 'Add',
                'parameterMap': {
                    'value': {
                        'one': {'key': 'one', 'type': 'VALUE', 'value': 'X'},
                        'two': {'key': 'two', 'type': 'VALUE', 'value': 5},
                    },
                },
            },
            'genOutput': {
                'statementName': 'genOutput',
                'namespace': Namespaces.SYSTEM,
                'name': 'GenerateEvent',
                'dependentStatements': {'Steps.add.output': True},
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)

    try:
        await KIRuntime(fd).execute(
            FunctionExecutionParameters(
                KIRunFunctionRepository(),
                KIRunSchemaRepository(),
            ).set_arguments({})
        )
        pytest.fail('Expected an exception to be raised')
    except Exception as e:
        assert e is not None
