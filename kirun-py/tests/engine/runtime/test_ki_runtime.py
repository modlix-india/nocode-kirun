from __future__ import annotations

import pytest
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.runtime.ki_runtime import KIRuntime
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.model.function_definition import FunctionDefinition
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.event import Event
from kirun_py.model.statement import Statement
from kirun_py.model.parameter import Parameter
from kirun_py.model.parameter_reference import ParameterReference
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.hybrid_repository import HybridRepository
from kirun_py.json.schema.schema import Schema
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.function.system.generate_event import GenerateEvent
from kirun_py.function.system.if_ import If
from kirun_py.function.system.context.create import Create
from kirun_py.function.system.context.set_function import SetFunction
from kirun_py.function.system.loop.range_loop import RangeLoop


def _pmap(**kwargs):
    """Build a parameter map: {param_name: {key: ParameterReference}}"""
    result = {}
    for name, refs in kwargs.items():
        if not isinstance(refs, list):
            refs = [refs]
        result[name] = dict(refs)
    return result


@pytest.mark.asyncio
async def test_kiruntime_1_fibonacci():
    """Fibonacci sequence via KIRuntime with 7000 elements."""
    num = 7000
    expected = []
    a, b = 0, 1
    expected.append(a)
    expected.append(b)
    for i in range(2, num):
        expected.append(expected[i - 2] + expected[i - 1])

    create_sig = Create().get_signature()
    integer_schema = {'name': 'EachElement', 'type': 'INTEGER'}
    array_schema = {
        'name': 'ArrayType',
        'type': 'ARRAY',
        'defaultValue': [],
        'items': integer_schema,
    }
    create_array = (
        Statement('createArray', create_sig.get_namespace(), create_sig.get_name())
        .set_parameter_map(_pmap(
            name=[ParameterReference.of_value('a')],
            schema=[ParameterReference.of_value(array_schema)],
        ))
    )

    range_loop_sig = RangeLoop().get_signature()
    loop = (
        Statement('loop', range_loop_sig.get_namespace(), range_loop_sig.get_name())
        .set_parameter_map(_pmap(
            **{'from': [ParameterReference.of_value(0)],
               'to': [ParameterReference.of_expression('Arguments.Count')]},
        ))
        .set_dependent_statements({'Steps.createArray.output': True})
    )

    result_obj = {'name': 'result', 'value': {'isExpression': True, 'value': 'Context.a'}}
    gen_sig = GenerateEvent().get_signature()
    output_step = (
        Statement('outputStep', gen_sig.get_namespace(), gen_sig.get_name())
        .set_parameter_map(_pmap(
            eventName=[ParameterReference.of_value('output')],
            results=[ParameterReference.of_value(result_obj)],
        ))
        .set_dependent_statements({'Steps.loop.output': True})
    )

    if_sig = If().get_signature()
    if_step = (
        Statement('if', if_sig.get_namespace(), if_sig.get_name())
        .set_parameter_map(_pmap(
            condition=[ParameterReference.of_expression(
                'Steps.loop.iteration.index = 0 or Steps.loop.iteration.index = 1'
            )],
        ))
    )

    set_sig = SetFunction().get_signature()
    set1 = (
        Statement('setOnTrue', set_sig.get_namespace(), set_sig.get_name())
        .set_parameter_map(_pmap(
            name=[ParameterReference.of_value('Context.a[Steps.loop.iteration.index]')],
            value=[ParameterReference.of_expression('Steps.loop.iteration.index')],
        ))
        .set_dependent_statements({'Steps.if.true': True})
    )
    set2 = (
        Statement('setOnFalse', set_sig.get_namespace(), set_sig.get_name())
        .set_parameter_map(_pmap(
            name=[ParameterReference.of_value('Context.a[Steps.loop.iteration.index]')],
            value=[ParameterReference.of_expression(
                'Context.a[Steps.loop.iteration.index - 1] + Context.a[Steps.loop.iteration.index - 2]'
            )],
        ))
        .set_dependent_statements({'Steps.if.false': True})
    )

    fd = (
        FunctionDefinition('Fibonacci')
        .set_namespace('Test')
        .set_steps({
            'createArray': create_array,
            'loop': loop,
            'outputStep': output_step,
            'if': if_step,
            'setOnTrue': set1,
            'setOnFalse': set2,
        })
        .set_events(dict([
            Event.output_event_map_entry({'result': Schema.of_array('result', Schema.of_integer('result'))})
        ]))
        .set_parameters({'Count': Parameter('Count', Schema.of_integer('Count'))})
    )

    out = (
        await KIRuntime(fd).execute(
            FunctionExecutionParameters(
                KIRunFunctionRepository(),
                KIRunSchemaRepository(),
            ).set_arguments({'Count': num})
        )
    ).all_results()

    assert out[0].get_result()['result'] == expected


@pytest.mark.asyncio
async def test_kiruntime_2_absolute():
    gen_sig = GenerateEvent().get_signature()
    expression = {'isExpression': True, 'value': 'Steps.first.output.value'}
    result_obj = {'name': 'result', 'value': expression}

    fd = (
        FunctionDefinition('SingleCall')
        .set_namespace('Test')
        .set_parameters({'Value': Parameter('Value', Schema.of_integer('Value'))})
        .set_steps({
            'first': Statement('first', Namespaces.MATH, 'Absolute').set_parameter_map(_pmap(
                value=[ParameterReference.of_expression('Arguments.Value')],
            )),
            'second': Statement('second', gen_sig.get_namespace(), gen_sig.get_name()).set_parameter_map(_pmap(
                eventName=[ParameterReference.of_value('output')],
                results=[ParameterReference.of_value(result_obj)],
            )),
        })
    )

    out = (
        await KIRuntime(fd).execute(
            FunctionExecutionParameters(
                KIRunFunctionRepository(),
                KIRunSchemaRepository(),
            ).set_arguments({'Value': -10})
        )
    ).all_results()

    assert out[0].get_result()['result'] == 10


@pytest.mark.asyncio
async def test_kiruntime_3_cube_root():
    gen_sig = GenerateEvent().get_signature()
    expression = {'isExpression': True, 'value': 'Steps.first.output.value'}
    result_obj = {'name': 'result', 'value': expression}

    fd = (
        FunctionDefinition('SingleCall')
        .set_namespace('Test')
        .set_parameters({'Value': Parameter('Value', Schema.of_integer('Value'))})
        .set_steps({
            'first': Statement('first', Namespaces.MATH, 'CubeRoot').set_parameter_map(_pmap(
                value=[ParameterReference.of_expression('Arguments.Value')],
            )),
            'second': Statement('second', gen_sig.get_namespace(), gen_sig.get_name()).set_parameter_map(_pmap(
                eventName=[ParameterReference.of_value('output')],
                results=[ParameterReference.of_value(result_obj)],
            )),
        })
    )

    out = (
        await KIRuntime(fd).execute(
            FunctionExecutionParameters(
                KIRunFunctionRepository(),
                KIRunSchemaRepository(),
            ).set_arguments({'Value': 27})
        )
    ).all_results()

    assert out[0].get_result()['result'] == pytest.approx(3.0)


@pytest.mark.asyncio
async def test_kiruntime_4_custom_function():
    """Custom FibFunction injected via HybridRepository."""
    num = 7
    expected = [0] * num
    expected[0] = 0
    expected[1] = 1
    for i in range(2, num):
        expected[i] = expected[i - 2] + expected[i - 1]

    fib_sig = (
        FunctionSignature('FibFunction')
        .set_namespace('FibSpace')
        .set_parameters({'value': Parameter('value', Schema.of_integer('value'))})
        .set_events(dict([
            Event.output_event_map_entry({'value': Schema.of_array('value', Schema.of_integer('value'))})
        ]))
    )

    class FibFunction(AbstractFunction):
        def get_signature(self):
            return fib_sig

        async def internal_execute(self, context):
            count = context.get_arguments().get('value')
            a = [0] * count
            for i in range(count):
                a[i] = i if i < 2 else a[i - 1] + a[i - 2]
            return FunctionOutput([EventResult.output_of({'value': a})])

    fib_func = FibFunction()

    class FibRepo:
        async def find(self, namespace, name):
            return fib_func

        async def filter(self, name):
            full = fib_func.get_signature().get_full_name()
            return [full] if name.lower() in full.lower() else []

    hybrid = HybridRepository(KIRunFunctionRepository(), FibRepo())
    gen_sig = GenerateEvent().get_signature()
    expression = {'isExpression': True, 'value': 'Steps.fib.output.value'}
    result_obj = {'name': 'result', 'value': expression}

    fd = (
        FunctionDefinition('CustomFunction')
        .set_namespace('Test')
        .set_parameters({'Value': Parameter('Value', Schema.of_integer('Value'))})
        .set_steps({
            'fib': Statement('fib', fib_sig.get_namespace(), 'asdf').set_parameter_map(_pmap(
                value=[ParameterReference.of_expression('Arguments.Value')],
            )),
            'fiboutput': Statement('fiboutput', gen_sig.get_namespace(), gen_sig.get_name()).set_parameter_map(_pmap(
                eventName=[ParameterReference.of_value('output')],
                results=[ParameterReference.of_value(result_obj)],
            )),
        })
    )

    out = (
        await KIRuntime(fd).execute(
            FunctionExecutionParameters(hybrid, KIRunSchemaRepository())
            .set_arguments({'Value': num})
        )
    ).all_results()

    assert out[0].get_result()['result'] == expected


@pytest.mark.asyncio
async def test_kiruntime_fib_from_definition():
    """Fibonacci from JSON definition dict (like KIRuntimeFibTest.ts)."""
    def_dict = {
        'name': 'fibonaccii',
        'namespace': 'TestUI',
        'parameters': {
            'n': {
                'parameterName': 'n',
                'schema': {'type': ['INTEGER'], 'version': 1},
            }
        },
        'events': {
            'output': {
                'name': 'output',
                'parameters': {
                    'result': {
                        'type': ['ARRAY'],
                        'version': 1,
                        'items': {'type': ['INTEGER']},
                    }
                },
            }
        },
        'steps': {
            'create': {
                'statementName': 'create',
                'name': 'Create',
                'namespace': 'System.Context',
                'parameterMap': {
                    'name': {
                        'k1': {'key': 'k1', 'type': 'VALUE', 'expression': '', 'order': 1, 'value': 'a'},
                    },
                    'schema': {
                        'k2': {'key': 'k2', 'type': 'VALUE', 'expression': '', 'order': 1,
                               'value': {'type': 'ARRAY', 'items': {'type': 'INTEGER'}}},
                    },
                },
            },
            'set2': {
                'statementName': 'set2',
                'name': 'Set',
                'namespace': 'System.Context',
                'parameterMap': {
                    'name': {
                        'k3': {'key': 'k3', 'type': 'VALUE', 'expression': '', 'order': 1, 'value': 'Context.a'},
                    },
                    'value': {
                        'k4': {'key': 'k4', 'type': 'VALUE', 'expression': '', 'order': 1, 'value': []},
                    },
                },
                'dependentStatements': {'Steps.create.output': True},
            },
            'rangeLoop': {
                'statementName': 'rangeLoop',
                'name': 'RangeLoop',
                'namespace': 'System.Loop',
                'parameterMap': {
                    'to': {
                        'k5': {'key': 'k5', 'type': 'EXPRESSION', 'expression': 'Arguments.n', 'value': 1},
                    },
                },
                'dependentStatements': {'Steps.create.output': False, 'Steps.set2.output': True},
            },
            'if': {
                'statementName': 'if',
                'name': 'If',
                'namespace': 'System',
                'parameterMap': {
                    'condition': {
                        'k6': {'key': 'k6', 'type': 'EXPRESSION',
                               'expression': 'Steps.rangeLoop.iteration.index < 2', 'order': 1},
                    },
                },
            },
            'trueInsert': {
                'statementName': 'trueInsert',
                'name': 'InsertLast',
                'namespace': 'System.Array',
                'parameterMap': {
                    'source': {
                        'k7': {'key': 'k7', 'type': 'EXPRESSION', 'expression': 'Context.a', 'order': 1},
                    },
                    'element': {
                        'k8': {'key': 'k8', 'type': 'EXPRESSION',
                               'expression': 'Steps.rangeLoop.iteration.index', 'order': 1},
                    },
                },
                'dependentStatements': {'Steps.if.true': True},
            },
            'set': {
                'statementName': 'set',
                'name': 'Set',
                'namespace': 'System.Context',
                'parameterMap': {
                    'name': {
                        'k9': {'key': 'k9', 'type': 'VALUE', 'expression': '', 'order': 1, 'value': 'Context.a'},
                    },
                    'value': {
                        'k10': {'key': 'k10', 'type': 'EXPRESSION',
                                'expression': 'Steps.trueInsert.output.result', 'order': 1},
                    },
                },
                'dependentStatements': {'Steps.if.true': False},
            },
            'falseInsert': {
                'statementName': 'falseInsert',
                'name': 'InsertLast',
                'namespace': 'System.Array',
                'parameterMap': {
                    'source': {
                        'k11': {'key': 'k11', 'type': 'EXPRESSION', 'expression': 'Context.a', 'order': 1},
                    },
                    'element': {
                        'k12': {
                            'key': 'k12', 'type': 'EXPRESSION',
                            'expression': 'Context.a[Steps.rangeLoop.iteration.index - 1] + Context.a[Steps.rangeLoop.iteration.index - 2]',
                            'order': 1,
                        },
                    },
                },
                'dependentStatements': {'Steps.if.false': True},
            },
            'set1': {
                'statementName': 'set1',
                'name': 'Set',
                'namespace': 'System.Context',
                'parameterMap': {
                    'name': {
                        'k13': {'key': 'k13', 'type': 'VALUE', 'expression': '', 'order': 1, 'value': 'Context.a'},
                    },
                    'value': {
                        'k14': {'key': 'k14', 'type': 'EXPRESSION',
                                'expression': 'Steps.falseInsert.output.result', 'order': 1},
                    },
                },
                'dependentStatements': {'Steps.if.false': False},
            },
            'generateEvent': {
                'statementName': 'generateEvent',
                'name': 'GenerateEvent',
                'namespace': 'System',
                'parameterMap': {
                    'results': {
                        'k15': {'key': 'k15', 'type': 'VALUE',
                                'value': {'name': 'result', 'value': {'isExpression': True, 'value': 'Context.a'}},
                                'order': 1},
                    },
                },
                'dependentStatements': {'Steps.rangeLoop.output': True},
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)
    out = await KIRuntime(fd, True).execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({'n': 5})
    )

    result = out.next().get_result()
    assert result['result'] == [0, 1, 1, 2, 3]
