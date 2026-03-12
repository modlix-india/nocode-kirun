from __future__ import annotations

import pytest
import pytest_asyncio
from kirun_py.function.system.loop.count_loop import CountLoop
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.model.function_definition import FunctionDefinition
from kirun_py.runtime.ki_runtime import KIRuntime


# ---------------------------------------------------------------------------
# CountLoop tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_count_loop_10():
    loop1 = await CountLoop().execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
            'Test',
        ).set_arguments({'count': 10}),
    )

    er = loop1.next()
    iterations = []
    while er is not None and er.get_name() != 'output':
        iterations.append(er.get_result()['index'])
        er = loop1.next()

    assert iterations == [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
    assert er is not None
    assert er.get_name() == 'output'
    assert er.get_result()['value'] == 10


@pytest.mark.asyncio
async def test_count_loop_negative():
    loop1 = await CountLoop().execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
            'Test',
        ).set_arguments({'count': -1}),
    )

    er = loop1.next()
    iterations = []
    while er is not None and er.get_name() != 'output':
        iterations.append(er.get_result()['index'])
        er = loop1.next()

    assert iterations == []
    assert er is not None
    assert er.get_name() == 'output'
    assert er.get_result()['value'] == 0


@pytest.mark.asyncio
async def test_count_loop_zero():
    loop1 = await CountLoop().execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
            'Test',
        ).set_arguments({'count': 0}),
    )

    er = loop1.next()
    iterations = []
    while er is not None and er.get_name() != 'output':
        iterations.append(er.get_result()['index'])
        er = loop1.next()

    assert iterations == []
    assert er is not None
    assert er.get_name() == 'output'
    assert er.get_result()['value'] == 0


@pytest.mark.asyncio
async def test_count_loop_no_args_raises():
    with pytest.raises(Exception):
        await CountLoop().execute(
            FunctionExecutionParameters(
                KIRunFunctionRepository(),
                KIRunSchemaRepository(),
                'Test',
            ).set_arguments({}),
        )


# ---------------------------------------------------------------------------
# RangeLoop test (via KIRuntime + FunctionDefinition)
# ---------------------------------------------------------------------------

_RANGE_LOOP_DEFINITION = {
    'name': 'Break Me 1',
    'events': {
        'output': {
            'name': 'output',
            'parameters': {
                'returnValue': {
                    'schema': {'type': 'ARRAY', 'items': {'type': 'INTEGER'}},
                },
            },
        },
    },
    'steps': {
        'create': {
            'name': 'Create',
            'namespace': 'System.Context',
            'statementName': 'create',
            'parameterMap': {
                'name': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'array'}},
                'schema': {
                    'one': {
                        'key': 'one',
                        'type': 'VALUE',
                        'value': {'type': 'ARRAY', 'items': {'type': 'INTEGER'}},
                    },
                },
            },
        },
        'createSet': {
            'name': 'Set',
            'namespace': 'System.Context',
            'statementName': 'createSet',
            'parameterMap': {
                'name': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'Context.array'}},
                'value': {'one': {'key': 'one', 'type': 'VALUE', 'value': []}},
            },
            'dependentStatements': {'Steps.create.output': True},
        },
        'loop': {
            'name': 'RangeLoop',
            'namespace': 'System.Loop',
            'statementName': 'loop',
            'parameterMap': {
                'from': {'one': {'key': 'one', 'type': 'VALUE', 'value': 5}},
                'to': {'one': {'key': 'one', 'type': 'VALUE', 'value': 10}},
                'step': {'one': {'key': 'one', 'type': 'VALUE', 'value': 1}},
            },
            'dependentStatements': {'Steps.createSet.output': True},
        },
        'insert': {
            'name': 'InsertLast',
            'namespace': 'System.Array',
            'statementName': 'insert',
            'parameterMap': {
                'source': {
                    'one': {'key': 'one', 'type': 'EXPRESSION', 'expression': 'Context.array'},
                },
                'element': {
                    'one': {
                        'key': 'one',
                        'type': 'EXPRESSION',
                        'expression': 'Steps.loop.iteration.index',
                    },
                },
            },
        },
        'set': {
            'name': 'Set',
            'namespace': 'System.Context',
            'statementName': 'set',
            'parameterMap': {
                'name': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'Context.array'}},
                'value': {
                    'one': {
                        'key': 'one',
                        'type': 'EXPRESSION',
                        'expression': 'Steps.insert.output.result',
                    },
                },
            },
        },
        'generateEvent': {
            'statementName': 'generateEvent',
            'name': 'GenerateEvent',
            'namespace': 'System',
            'parameterMap': {
                'eventName': {
                    '5OdGxruBiEyysESbAubdX2': {
                        'key': '5OdGxruBiEyysESbAubdX2',
                        'type': 'VALUE',
                        'expression': '',
                        'value': 'output',
                    },
                },
                'results': {
                    '4o0c0kvVtWiGjgb37hMTBX': {
                        'key': '4o0c0kvVtWiGjgb37hMTBX',
                        'type': 'VALUE',
                        'order': 1,
                        'value': {
                            'name': 'returnValue',
                            'value': {
                                'isExpression': True,
                                'value': 'Context.array',
                            },
                        },
                    },
                },
            },
            'dependentStatements': {'Steps.loop.output': True},
        },
    },
}


@pytest.mark.asyncio
async def test_range_loop():
    fd = FunctionDefinition.from_value(_RANGE_LOOP_DEFINITION)
    result = (
        await KIRuntime(fd, True).execute(
            FunctionExecutionParameters(
                KIRunFunctionRepository(),
                KIRunSchemaRepository(),
            ),
        )
    ).next().get_result()['returnValue']

    assert result == [5, 6, 7, 8, 9]


# ---------------------------------------------------------------------------
# BreakLoop test (via KIRuntime + FunctionDefinition)
# ---------------------------------------------------------------------------

_BREAK_LOOP_DEFINITION = {
    'name': 'Break Me 1',
    'events': {
        'output': {
            'name': 'output',
            'parameters': {
                'returnValue': {
                    'schema': {'type': 'ARRAY', 'items': {'type': 'INTEGER'}},
                },
            },
        },
    },
    'steps': {
        'create': {
            'name': 'Create',
            'namespace': 'System.Context',
            'statementName': 'create',
            'parameterMap': {
                'name': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'array'}},
                'schema': {
                    'one': {
                        'key': 'one',
                        'type': 'VALUE',
                        'value': {'type': 'ARRAY', 'items': {'type': 'INTEGER'}},
                    },
                },
            },
        },
        'createSet': {
            'name': 'Set',
            'namespace': 'System.Context',
            'statementName': 'createSet',
            'parameterMap': {
                'name': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'Context.array'}},
                'value': {'one': {'key': 'one', 'type': 'VALUE', 'value': []}},
            },
            'dependentStatements': {'Steps.create.output': True},
        },
        'loop': {
            'name': 'RangeLoop',
            'namespace': 'System.Loop',
            'statementName': 'loop',
            'parameterMap': {
                'from': {'one': {'key': 'one', 'type': 'VALUE', 'value': 5}},
                'to': {'one': {'key': 'one', 'type': 'VALUE', 'value': 10}},
                'step': {'one': {'key': 'one', 'type': 'VALUE', 'value': 1}},
            },
            'dependentStatements': {'Steps.createSet.output': True},
        },
        'insert': {
            'name': 'InsertLast',
            'namespace': 'System.Array',
            'statementName': 'insert',
            'parameterMap': {
                'source': {
                    'one': {'key': 'one', 'type': 'EXPRESSION', 'expression': 'Context.array'},
                },
                'element': {
                    'one': {
                        'key': 'one',
                        'type': 'EXPRESSION',
                        'expression': 'Steps.loop.iteration.index',
                    },
                },
            },
            'dependentStatements': {'Steps.if.false': True},
        },
        'set': {
            'name': 'Set',
            'namespace': 'System.Context',
            'statementName': 'set',
            'parameterMap': {
                'name': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'Context.array'}},
                'value': {
                    'one': {
                        'key': 'one',
                        'type': 'EXPRESSION',
                        'expression': 'Steps.insert.output.result',
                    },
                },
            },
        },
        'if': {
            'name': 'If',
            'namespace': 'System',
            'statementName': 'if',
            'parameterMap': {
                'condition': {
                    'one': {
                        'key': 'one',
                        'type': 'EXPRESSION',
                        'expression': 'Steps.loop.iteration.index = 7',
                    },
                },
            },
        },
        'break': {
            'name': 'Break',
            'namespace': 'System.Loop',
            'statementName': 'break',
            'parameterMap': {
                'stepName': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'loop'}},
            },
            'dependentStatements': {'Steps.if.true': True},
        },
        'generateEvent': {
            'statementName': 'generateEvent',
            'name': 'GenerateEvent',
            'namespace': 'System',
            'parameterMap': {
                'eventName': {
                    '5OdGxruBiEyysESbAubdX2': {
                        'key': '5OdGxruBiEyysESbAubdX2',
                        'type': 'VALUE',
                        'expression': '',
                        'value': 'output',
                    },
                },
                'results': {
                    '4o0c0kvVtWiGjgb37hMTBX': {
                        'key': '4o0c0kvVtWiGjgb37hMTBX',
                        'type': 'VALUE',
                        'order': 1,
                        'value': {
                            'name': 'returnValue',
                            'value': {
                                'isExpression': True,
                                'value': 'Context.array',
                            },
                        },
                    },
                },
            },
            'dependentStatements': {'Steps.loop.output': True},
        },
    },
}


@pytest.mark.asyncio
async def test_break_loop():
    fd = FunctionDefinition.from_value(_BREAK_LOOP_DEFINITION)
    result = (
        await KIRuntime(fd, True).execute(
            FunctionExecutionParameters(
                KIRunFunctionRepository(),
                KIRunSchemaRepository(),
            ),
        )
    ).next().get_result()['returnValue']

    assert result == [5, 6]
