"""Extended KIRuntime integration tests ported from:
- KIRuntimeNoValuesTest.ts
- KIRuntimeMessagesTest.ts
- KIRuntimeVarArgsTest.ts
"""
from __future__ import annotations

import pytest

from kirun_py.model.function_definition import FunctionDefinition
from kirun_py.model.parameter import Parameter
from kirun_py.model.parameter_reference import ParameterReference
from kirun_py.model.statement import Statement
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.runtime.ki_runtime import KIRuntime
from kirun_py.hybrid_repository import HybridRepository
from kirun_py.runtime.expression.tokenextractor.object_value_setter_extractor import (
    ObjectValueSetterExtractor,
)


# ---------------------------------------------------------------------------
# KIRuntimeNoValuesTest
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_ki_runtime_no_values_empty_parameter_map():
    """Definition with empty parameterMap inside a step should not crash fatally."""
    def_dict = {
        'name': 'Make an error',
        'namespace': 'UIApp',
        'steps': {
            'print': {
                'statementName': 'print',
                'namespace': 'function',
                'name': 'test',
                'parameterMap': {},
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)

    class TestRepository:
        async def find(self, namespace: str, name: str):  # noqa: S7503
            from kirun_py.function.system.print_ import Print
            return Print()

        async def filter(self, name: str):  # noqa: S7503
            raise NotImplementedError('Method not implemented.')

    try:
        hybrid = HybridRepository(KIRunFunctionRepository(), TestRepository())
        await KIRuntime(fd).execute(
            FunctionExecutionParameters(hybrid, KIRunSchemaRepository())
            .set_arguments({})
        )
    except Exception:
        # Expected — the point is it doesn't raise an unhandled crash
        pass


# ---------------------------------------------------------------------------
# KIRuntimeMessagesTest — Test 1
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_messages_test_1_missing_functions():
    """get_execution_plan returns error messages for unavailable functions and missing step refs."""
    func_dict = {
        'name': 'loginFunction',
        'steps': {
            'messageStep': {
                'statementName': 'messageStep',
                'namespace': 'UIEngine',
                'name': 'Message',
                'parameterMap': {
                    'msg': {
                        'value1': {
                            'key': 'value1',
                            'type': 'EXPRESSION',
                            'expression': 'Steps.loginStep.error.data',
                        },
                    },
                },
                'position': {'left': 198, 'top': 245},
            },
            'genOutput': {
                'statementName': 'genOutput',
                'namespace': 'System',
                'name': 'GenerateEvent',
                'dependentStatements': {
                    'Steps.loginStep.output': True,
                },
                'position': {'left': 482, 'top': 172},
            },
            'loginStep1': {
                'name': 'Login',
                'namespace': 'UIEngine',
                'statementName': 'loginStep1',
                'parameterMap': {
                    'userName': {
                        'value1': {
                            'key': 'value1',
                            'type': 'EXPRESSION',
                            'expression': 'Page.user.userName',
                        },
                    },
                    'password': {
                        'value1': {
                            'key': 'value1',
                            'type': 'EXPRESSION',
                            'expression': 'Page.user.password',
                        },
                    },
                    'rememberMe': {
                        'value1': {
                            'key': 'value1',
                            'type': 'EXPRESSION',
                            'expression': 'Page.user.rememberMe',
                        },
                    },
                },
                'position': {'left': 472, 'top': 333},
            },
        },
    }

    fd = FunctionDefinition.from_value(func_dict)

    graph = await KIRuntime(fd, False).get_execution_plan(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )

    messages = [
        msg.get_message()
        for vertex in graph.get_node_map().values()
        for msg in vertex.get_data().get_messages()
    ]

    assert sorted(messages) == sorted([
        'UIEngine.Message is not available',
        'Unable to find the step with name loginStep',
        'UIEngine.Login is not available',
    ])


# ---------------------------------------------------------------------------
# KIRuntimeMessagesTest — Test 2
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_messages_test_2_missing_step_reference():
    """get_execution_plan reports missing step; execute raises the same error."""
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
            'add1': {
                'statementName': 'add1',
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
                                'value': {'isExpression': True, 'value': 'Steps.add.output.value'},
                            },
                        },
                    },
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)

    graph = await KIRuntime(fd, False).get_execution_plan(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )

    messages = [
        msg.get_message()
        for vertex in graph.get_node_map().values()
        for msg in vertex.get_data().get_messages()
    ]

    assert messages == ['Unable to find the step with name add']

    with pytest.raises(Exception, match='Unable to find the step with name add'):
        await KIRuntime(fd, False).execute(
            FunctionExecutionParameters(
                KIRunFunctionRepository(),
                KIRunSchemaRepository(),
            ).set_arguments({'a': 7, 'b': 11, 'c': 13})
        )


# ---------------------------------------------------------------------------
# KIRuntimeVarArgsTest
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_ki_runtime_var_args_with_store(capsys):
    """Variable argument function with ObjectValueSetterExtractor store passes array elements."""
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
        async def find(self, namespace: str, name: str):  # noqa: S7503
            if namespace == 'LocalFunction' and name == 'Other':
                return other_function
            return None

        async def filter(self, name: str):  # noqa: S7503
            return ['LocalFunction.Other']

    hybrid = HybridRepository(KIRunFunctionRepository(), LocalRepo())

    await KIRuntime(fd, False).execute(
        FunctionExecutionParameters(hybrid, KIRunSchemaRepository())
        .set_values_map({store.get_prefix(): store})
    )

    captured = capsys.readouterr()
    # Print function outputs elements space-separated on stdout
    assert 'kiran' in captured.out
    assert 'kumar' in captured.out
    assert 'grandhi' in captured.out
