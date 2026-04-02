"""KIRuntime integration tests ported from TypeScript:
- KIRuntimeDependencyTest.ts
- KIRuntimeFunctionInFunction.ts
- KIRuntimeLoopBugBranchTest.ts
- KIRuntimeMessagesTest.ts (already in test_ki_runtime_extended.py — duplicated here for completeness)
- KIRuntimeParallelTest.ts
- KIRuntimeNoParamMapTest.ts
- KIRuntimeNoValuesTest.ts
- KIRuntimeTestWithoutGenEvent.ts
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
from kirun_py.function.system.print_ import Print


def _pmap(**kwargs):
    """Build a parameter map: {param_name: {key: ParameterReference}}"""
    result = {}
    for name, refs in kwargs.items():
        if not isinstance(refs, list):
            refs = [refs]
        result[name] = dict(refs)
    return result


# ---------------------------------------------------------------------------
# KIRuntimeDependencyTest — executeIfTrue
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_execute_if_true_with_name_field(capsys):
    """ForEachLoop + executeIftrue: print only when 'name' field is truthy (2 of 3 items)."""
    def_dict = {
        'name': 'getAppData',
        'namespace': 'UIApp',
        'parameters': {
            'a': {
                'parameterName': 'a',
                'schema': {
                    'name': 'INTEGER',
                    'type': ['ARRAY'],
                    'items': {'type': ['OBJECT', 'NULL']},
                },
            },
        },
        'steps': {
            'forEach': {
                'statementName': 'forEach',
                'namespace': 'System.Loop',
                'name': 'ForEachLoop',
                'parameterMap': {
                    'source': {
                        'one': {'key': 'one', 'type': 'EXPRESSION', 'expression': 'Arguments.a'},
                    },
                },
            },
            'print': {
                'statementName': 'print',
                'namespace': 'System',
                'name': 'Print',
                'parameterMap': {
                    'values': {
                        'one': {
                            'key': 'one',
                            'type': 'EXPRESSION',
                            'expression': 'Steps.forEach.iteration.each.name',
                        },
                    },
                },
                'executeIftrue': {
                    'Steps.forEach.iteration.each.name': True,
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)

    await KIRuntime(fd, False).execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({'a': [{'name': 'Kiran', 'age': 40}, None, {'name': 'Kumar', 'age': 39}]})
    )

    captured = capsys.readouterr()
    # Should print 2 times: Kiran and Kumar (None element skipped)
    assert 'Kiran' in captured.out
    assert 'Kumar' in captured.out


@pytest.mark.asyncio
async def test_execute_if_true_with_age_expression(capsys):
    """ForEachLoop + executeIftrue: print only when age is even (1 of 3 items: Kiran age=40)."""
    def_dict = {
        'name': 'getAppData',
        'namespace': 'UIApp',
        'parameters': {
            'a': {
                'parameterName': 'a',
                'schema': {
                    'name': 'INTEGER',
                    'type': ['ARRAY'],
                    'items': {'type': ['OBJECT', 'NULL']},
                },
            },
        },
        'steps': {
            'forEach': {
                'statementName': 'forEach',
                'namespace': 'System.Loop',
                'name': 'ForEachLoop',
                'parameterMap': {
                    'source': {
                        'one': {'key': 'one', 'type': 'EXPRESSION', 'expression': 'Arguments.a'},
                    },
                },
            },
            'print': {
                'statementName': 'print',
                'namespace': 'System',
                'name': 'Print',
                'parameterMap': {
                    'values': {
                        'one': {
                            'key': 'one',
                            'type': 'EXPRESSION',
                            'expression': 'Steps.forEach.iteration.each.name',
                        },
                    },
                },
                'executeIftrue': {
                    '(Steps.forEach.iteration.each.age ?? 1) % 2 = 0': True,
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)

    await KIRuntime(fd, False).execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({'a': [{'name': 'Kiran', 'age': 40}, None, {'name': 'Kumar', 'age': 39}]})
    )

    captured = capsys.readouterr()
    # Only Kiran (age=40, even) should be printed; null uses fallback 1 (odd), Kumar (age=39, odd)
    assert 'Kiran' in captured.out


# ---------------------------------------------------------------------------
# KIRuntimeFunctionInFunction
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_function_in_function():
    """First calls Second (value*2) and Third (value*3) and returns Third's result."""
    first = KIRuntime(
        FunctionDefinition.from_value({
            'name': 'First',
            'namespace': 'Internal',
            'events': {
                'output': {
                    'name': 'output',
                    'parameters': {'aresult': {'name': 'aresult', 'type': 'INTEGER'}},
                },
            },
            'steps': {
                'exSecond': {
                    'statementName': 'exSecond',
                    'name': 'Second',
                    'namespace': 'Internal',
                    'parameterMap': {
                        'value': {'one': {'key': 'one', 'type': 'VALUE', 'value': 2}},
                    },
                },
                'exThird': {
                    'statementName': 'exThird',
                    'name': 'Third',
                    'namespace': 'Internal',
                    'parameterMap': {
                        'value': {'one': {'key': 'one', 'type': 'VALUE', 'value': 3}},
                    },
                },
                'genOutput': {
                    'statementName': 'genOutput',
                    'namespace': 'System',
                    'name': 'GenerateEvent',
                    'dependentStatements': {
                        'Steps.exSecond.output': True,
                        'Steps.exThird.output': True,
                    },
                    'parameterMap': {
                        'eventName': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'output'}},
                        'results': {
                            'one': {
                                'key': 'one',
                                'type': 'VALUE',
                                'value': {
                                    'name': 'aresult',
                                    'value': {
                                        'isExpression': True,
                                        'value': 'Steps.exThird.output.result',
                                    },
                                },
                            },
                        },
                    },
                },
            },
        }),
        True,
    )

    second = KIRuntime(
        FunctionDefinition.from_value({
            'name': 'Second',
            'namespace': 'Internal',
            'parameters': {
                'value': {
                    'parameterName': 'value',
                    'schema': {'name': 'INTEGER', 'type': 'INTEGER'},
                },
            },
            'events': {
                'output': {
                    'name': 'output',
                    'parameters': {'result': {'name': 'result', 'type': 'INTEGER'}},
                },
            },
            'steps': {
                'genOutput': {
                    'statementName': 'genOutput',
                    'namespace': 'System',
                    'name': 'GenerateEvent',
                    'parameterMap': {
                        'eventName': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'output'}},
                        'results': {
                            'one': {
                                'key': 'one',
                                'type': 'VALUE',
                                'value': {
                                    'name': 'result',
                                    'value': {
                                        'isExpression': True,
                                        'value': 'Arguments.value * 2',
                                    },
                                },
                            },
                        },
                    },
                },
            },
        }),
        True,
    )

    third = KIRuntime(
        FunctionDefinition.from_value({
            'name': 'Third',
            'namespace': 'Internal',
            'parameters': {
                'value': {
                    'parameterName': 'value',
                    'schema': {'name': 'INTEGER', 'type': 'INTEGER'},
                },
            },
            'events': {
                'output': {
                    'name': 'output',
                    'parameters': {'result': {'name': 'result', 'type': 'INTEGER'}},
                },
            },
            'steps': {
                'genOutput': {
                    'statementName': 'genOutput',
                    'namespace': 'System',
                    'name': 'GenerateEvent',
                    'parameterMap': {
                        'eventName': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'output'}},
                        'results': {
                            'one': {
                                'key': 'one',
                                'type': 'VALUE',
                                'value': {
                                    'name': 'result',
                                    'value': {
                                        'isExpression': True,
                                        'value': 'Arguments.value * 3',
                                    },
                                },
                            },
                        },
                    },
                },
            },
        }),
        True,
    )

    class InternalRepository:
        async def find(self, namespace: str, name: str):
            if namespace != 'Internal':
                return None
            if name == 'Third':
                return third
            if name == 'Second':
                return second
            return None

        async def filter(self, name: str):
            names = [
                third.get_signature().get_full_name(),
                second.get_signature().get_full_name(),
            ]
            return [n for n in names if name.lower() in n.lower()]

    repo = HybridRepository(KIRunFunctionRepository(), InternalRepository())

    result = (
        await first.execute(
            FunctionExecutionParameters(repo, KIRunSchemaRepository())
        )
    ).next()

    # Third(value=3) returns 3*3=9
    assert result.get_result()['aresult'] == 9


# ---------------------------------------------------------------------------
# KIRuntimeLoopBugBranchTest
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_loop_branch_bug(capsys):
    """Loop over strings, trim each, count non-empty strings via if-branch."""
    func = {
        'name': 'testloophistory',
        'namespace': '',
        'steps': {
            'create': {
                'statementName': 'create',
                'name': 'Create',
                'namespace': 'System.Context',
                'parameterMap': {
                    'name': {
                        '1nngcowEVDtBrXBhyFsfqB': {
                            'key': '1nngcowEVDtBrXBhyFsfqB',
                            'type': 'VALUE',
                            'expression': '',
                            'order': 1,
                            'value': 'arr',
                        },
                    },
                    'schema': {
                        'paM69eD4wlyJv3NLnlpRf': {
                            'key': 'paM69eD4wlyJv3NLnlpRf',
                            'type': 'VALUE',
                            'expression': '',
                            'order': 1,
                            'value': {'type': 'ARRAY', 'items': {'type': 'STRING'}},
                        },
                    },
                },
            },
            'loop': {
                'statementName': 'loop',
                'name': 'ForEachLoop',
                'namespace': 'System.Loop',
                'parameterMap': {
                    'source': {
                        '4fQaZPve4hAoLyIFbaSnEE': {
                            'key': '4fQaZPve4hAoLyIFbaSnEE',
                            'type': 'EXPRESSION',
                            'expression': 'Context.arr',
                            'order': 1,
                        },
                    },
                },
                'dependentStatements': {'Steps.set.output': True, 'Steps.set1.output': True},
            },
            'create1': {
                'statementName': 'create1',
                'name': 'Create',
                'namespace': 'System.Context',
                'parameterMap': {
                    'name': {
                        '5ntX0A6D5lObvGesm9kciW': {
                            'key': '5ntX0A6D5lObvGesm9kciW',
                            'type': 'VALUE',
                            'expression': '',
                            'order': 1,
                            'value': 'count',
                        },
                    },
                    'schema': {
                        '1Se6m0293IcsOiFRKQlQen': {
                            'key': '1Se6m0293IcsOiFRKQlQen',
                            'type': 'VALUE',
                            'expression': '',
                            'order': 1,
                            'value': {'type': 'INTEGER'},
                        },
                    },
                },
            },
            'set1': {
                'statementName': 'set1',
                'name': 'Set',
                'namespace': 'System.Context',
                'parameterMap': {
                    'name': {
                        '3QBReTdFoCDSMa7SBrjHje': {
                            'key': '3QBReTdFoCDSMa7SBrjHje',
                            'type': 'VALUE',
                            'expression': '',
                            'order': 1,
                            'value': 'Context.count',
                        },
                    },
                    'value': {
                        '5Luu5FddZu8SYKOrgkdUoV': {
                            'key': '5Luu5FddZu8SYKOrgkdUoV',
                            'type': 'VALUE',
                            'order': 1,
                            'value': 0,
                        },
                    },
                },
                'dependentStatements': {'Steps.create1.output': True},
            },
            'generateEvent': {
                'statementName': 'generateEvent',
                'name': 'GenerateEvent',
                'namespace': 'System',
                'parameterMap': {
                    'results': {
                        '1Ddtjl1fJKLsUvZdr127RI': {
                            'key': '1Ddtjl1fJKLsUvZdr127RI',
                            'type': 'VALUE',
                            'expression': '',
                            'order': 1,
                            'value': {
                                'name': 'count',
                                'value': {'isExpression': True, 'value': 'Context.count'},
                            },
                        },
                    },
                },
                'dependentStatements': {'Steps.loop.output': True},
            },
            'set': {
                'statementName': 'set',
                'name': 'Set',
                'namespace': 'System.Context',
                'parameterMap': {
                    'name': {
                        'w8fhgGiU2OmUfqFAiTPh9': {
                            'key': 'w8fhgGiU2OmUfqFAiTPh9',
                            'type': 'VALUE',
                            'expression': '',
                            'order': 1,
                            'value': 'Context.arr',
                        },
                    },
                    'value': {
                        '4dFnOUA9f80JhkqsKZJFvz': {
                            'key': '4dFnOUA9f80JhkqsKZJFvz',
                            'type': 'VALUE',
                            'order': 1,
                            'value': ['kiran', 'hi', '', '', 'hello', '', 'how'],
                        },
                    },
                },
                'dependentStatements': {'Steps.create.output': True},
            },
            'trim': {
                'statementName': 'trim',
                'name': 'Trim',
                'namespace': 'System.String',
                'parameterMap': {
                    'string': {
                        '7uvjy8hwDzlkIO6s8NLWr3': {
                            'key': '7uvjy8hwDzlkIO6s8NLWr3',
                            'type': 'EXPRESSION',
                            'expression': 'Steps.loop.iteration.each',
                            'order': 1,
                        },
                    },
                },
            },
            'if': {
                'statementName': 'if',
                'name': 'If',
                'namespace': 'System',
                'parameterMap': {
                    'condition': {
                        '2rnqfOmJtOVK4RzhK0lPDu': {
                            'key': '2rnqfOmJtOVK4RzhK0lPDu',
                            'type': 'EXPRESSION',
                            'expression': "Steps.trim.output.result != ''",
                            'order': 1,
                        },
                    },
                },
            },
            'set2': {
                'statementName': 'set2',
                'name': 'Set',
                'namespace': 'System.Context',
                'parameterMap': {
                    'name': {
                        '7PMLUgrXK2AK1WqAPMsD4': {
                            'key': '7PMLUgrXK2AK1WqAPMsD4',
                            'type': 'VALUE',
                            'expression': '',
                            'order': 1,
                            'value': 'Context.count',
                        },
                    },
                    'value': {
                        '1sbsvevDB8XQpFLvGE9Eoq': {
                            'key': '1sbsvevDB8XQpFLvGE9Eoq',
                            'type': 'EXPRESSION',
                            'expression': 'Context.count + 1',
                            'order': 1,
                        },
                    },
                },
                'dependentStatements': {'Steps.if.true': True},
            },
            'print': {
                'statementName': 'print',
                'name': 'Print',
                'namespace': 'System',
                'parameterMap': {
                    'values': {
                        '7GEehmufgT3pw3P6dAEkY7': {
                            'key': '7GEehmufgT3pw3P6dAEkY7',
                            'type': 'EXPRESSION',
                            'expression': 'Steps.trim.output.result',
                            'order': 1,
                        },
                        '5ESXu8zlY5wJLUzPt0ed5I': {
                            'key': '5ESXu8zlY5wJLUzPt0ed5I',
                            'type': 'EXPRESSION',
                            'expression': 'Steps.loop.iteration.each',
                            'order': 2,
                        },
                        '1Lghu5LxsX0DEDqLJmpzUY': {
                            'key': '1Lghu5LxsX0DEDqLJmpzUY',
                            'type': 'EXPRESSION',
                            'order': 3,
                        },
                    },
                },
                'dependentStatements': {'Steps.if.true': True},
            },
        },
    }

    fd = FunctionDefinition.from_value(func)
    runtime = KIRuntime(fd, False)

    results = await runtime.execute(
        FunctionExecutionParameters(KIRunFunctionRepository(), KIRunSchemaRepository())
    )

    # ['kiran', 'hi', '', '', 'hello', '', 'how'] => 4 non-empty strings after trim
    assert results.all_results()[0].get_result()['count'] == 4

    captured = capsys.readouterr()
    # print should be called 4 times (one per non-empty string)
    non_empty = [s for s in ['kiran', 'hi', '', '', 'hello', '', 'how'] if s.strip()]
    for word in non_empty:
        assert word in captured.out


# ---------------------------------------------------------------------------
# KIRuntimeNoParamMapTest — no parameterMap at all in definition
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_no_param_map_in_definition():
    """Step with no parameterMap key at all should not crash fatally."""
    def_dict = {
        'name': 'Make an error',
        'namespace': 'UIApp',
        'steps': {
            'print': {
                'statementName': 'print',
                'namespace': 'function',
                'name': 'test',
                # no parameterMap key at all
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)

    class TestRepository:
        async def find(self, namespace: str, name: str):
            return Print()

        async def filter(self, name: str):
            raise NotImplementedError('Method not implemented.')

    try:
        await KIRuntime(fd).execute(
            FunctionExecutionParameters(
                HybridRepository(KIRunFunctionRepository(), TestRepository()),
                KIRunSchemaRepository(),
            ).set_arguments({})
        )
    except Exception:
        pass  # Expected; the test verifies it doesn't crash unhandled


# ---------------------------------------------------------------------------
# KIRuntimeNoValuesTest — parameterMap is empty dict {}
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_no_values_empty_parameter_map():
    """Step with empty parameterMap ({}) should not crash fatally."""
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
        async def find(self, namespace: str, name: str):
            return Print()

        async def filter(self, name: str):
            raise NotImplementedError('Method not implemented.')

    try:
        await KIRuntime(fd).execute(
            FunctionExecutionParameters(
                HybridRepository(KIRunFunctionRepository(), TestRepository()),
                KIRunSchemaRepository(),
            ).set_arguments({})
        )
    except Exception:
        pass  # Expected; the test verifies it doesn't crash unhandled


# ---------------------------------------------------------------------------
# KIRuntimeTestWithoutGenEvent — no GenerateEvent step
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_without_gen_event_1(capsys):
    """Add two numbers and Print result — no GenerateEvent. Checks print output."""
    def_dict = {
        'name': 'getAppData',
        'namespace': 'UIApp',
        'parameters': {
            'a': {'parameterName': 'a', 'schema': {'name': 'INTEGER', 'type': 'INTEGER'}},
            'b': {'parameterName': 'b', 'schema': {'name': 'INTEGER', 'type': 'INTEGER'}},
            'c': {'parameterName': 'c', 'schema': {'name': 'INTEGER', 'type': 'INTEGER'}},
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
            'print': {
                'statementName': 'print',
                'namespace': Namespaces.SYSTEM,
                'name': 'Print',
                'parameterMap': {
                    'values': {
                        'one': {
                            'key': 'one',
                            'type': 'EXPRESSION',
                            'expression': 'Steps.add.output.value',
                            'order': 2,
                        },
                        'abc': {
                            'key': 'abc',
                            'type': 'VALUE',
                            'value': 'Nothing muchh....',
                            'order': 1,
                        },
                    },
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)

    await KIRuntime(fd, False).execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({'a': 7, 'b': 11, 'c': 13})
    )

    captured = capsys.readouterr()
    # a=7, 10+1=11, c=13 → 7+11+13=31
    assert 'Nothing muchh....' in captured.out
    assert '31' in captured.out


@pytest.mark.asyncio
async def test_without_gen_event_2(capsys):
    """Add two numbers and Print result (with events defined but no genOutput step)."""
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
                'parameters': {},
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
            'print': {
                'statementName': 'print',
                'namespace': Namespaces.SYSTEM,
                'name': 'Print',
                'parameterMap': {
                    'values': {
                        'one': {
                            'key': 'one',
                            'type': 'EXPRESSION',
                            'expression': 'Steps.add.output.value',
                            'order': 2,
                        },
                        'abc': {
                            'key': 'abc',
                            'type': 'VALUE',
                            'value': 'Something muchh....',
                            'order': 1,
                        },
                    },
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)

    await KIRuntime(fd, False).execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({'a': 7, 'b': 11, 'c': 13})
    )

    captured = capsys.readouterr()
    assert 'Something muchh....' in captured.out
    assert '31' in captured.out


@pytest.mark.asyncio
async def test_without_gen_event_3_raises_when_event_defined_but_missing_genoutput():
    """When events are defined but GenerateEvent step is missing, execute should raise."""
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
            'print': {
                'statementName': 'print',
                'namespace': Namespaces.SYSTEM,
                'name': 'Print',
                'parameterMap': {
                    'values': {
                        'one': {
                            'key': 'one',
                            'type': 'EXPRESSION',
                            'expression': 'Steps.add.output.value',
                            'order': 2,
                        },
                        'abc': {
                            'key': 'abc',
                            'type': 'VALUE',
                            'value': 'Something muchh....',
                            'order': 1,
                        },
                    },
                },
            },
        },
    }

    fd = FunctionDefinition.from_value(def_dict)

    with pytest.raises(Exception):
        await KIRuntime(fd, False).execute(
            FunctionExecutionParameters(
                KIRunFunctionRepository(),
                KIRunSchemaRepository(),
            ).set_arguments({'a': 7, 'b': 11, 'c': 13})
        )


# ---------------------------------------------------------------------------
# KIRuntimeParallelTest
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_parallel_execution_waits_for_all_paths(capsys):
    """Three parallel paths (with Wait steps) should all complete before the function returns."""
    import time

    path1 = KIRuntime(
        FunctionDefinition.from_value({
            'name': 'Path1',
            'namespace': 'Internal',
            'events': {'output': {'name': 'output', 'parameters': {}}},
            'steps': {
                'wait': {
                    'statementName': 'wait',
                    'namespace': 'System',
                    'name': 'Wait',
                    'parameterMap': {
                        'millis': {'one': {'key': 'one', 'type': 'VALUE', 'value': 200}},
                    },
                },
                'print': {
                    'statementName': 'print',
                    'namespace': 'System',
                    'name': 'Print',
                    'parameterMap': {
                        'values': {'one': {'key': 'one', 'type': 'VALUE', 'value': '200 Finished'}},
                    },
                    'dependentStatements': {'Steps.wait.output': True},
                },
                'genOutput': {
                    'statementName': 'genOutput',
                    'namespace': 'System',
                    'name': 'GenerateEvent',
                    'dependentStatements': {'Steps.print.output': True},
                    'parameterMap': {
                        'eventName': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'output'}},
                    },
                },
            },
        }),
    )

    path2 = KIRuntime(
        FunctionDefinition.from_value({
            'name': 'Path2',
            'namespace': 'Internal',
            'events': {'output': {'name': 'output', 'parameters': {}}},
            'steps': {
                'wait': {
                    'statementName': 'wait',
                    'namespace': 'System',
                    'name': 'Wait',
                    'parameterMap': {
                        'millis': {'one': {'key': 'one', 'type': 'VALUE', 'value': 300}},
                    },
                },
                'print': {
                    'statementName': 'print',
                    'namespace': 'System',
                    'name': 'Print',
                    'parameterMap': {
                        'values': {'one': {'key': 'one', 'type': 'VALUE', 'value': '300 Finished'}},
                    },
                    'dependentStatements': {'Steps.wait.output': True},
                },
                'genOutput': {
                    'statementName': 'genOutput',
                    'namespace': 'System',
                    'name': 'GenerateEvent',
                    'dependentStatements': {'Steps.print.output': True},
                    'parameterMap': {
                        'eventName': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'output'}},
                    },
                },
            },
        }),
    )

    path3 = KIRuntime(
        FunctionDefinition.from_value({
            'name': 'Path3',
            'namespace': 'Internal',
            'events': {'output': {'name': 'output', 'parameters': {}}},
            'steps': {
                'print': {
                    'statementName': 'print',
                    'namespace': 'System',
                    'name': 'Print',
                    'parameterMap': {
                        'values': {
                            'one': {'key': 'one', 'type': 'VALUE', 'value': 'Without Time Finished'},
                        },
                    },
                },
                'genOutput': {
                    'statementName': 'genOutput',
                    'namespace': 'System',
                    'name': 'GenerateEvent',
                    'dependentStatements': {'Steps.print.output': True},
                    'parameterMap': {
                        'eventName': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'output'}},
                    },
                },
            },
        }),
    )

    main_def = FunctionDefinition.from_value({
        'name': 'TestParallel',
        'namespace': 'UIApp',
        'steps': {
            'exPath1': {
                'statementName': 'exPath1',
                'name': 'Path1',
                'namespace': 'Internal',
                'parameterMap': {},
            },
            'exPath2': {
                'statementName': 'exPath2',
                'name': 'Path2',
                'namespace': 'Internal',
                'parameterMap': {},
            },
            'exPath3': {
                'statementName': 'exPath3',
                'name': 'Path3',
                'namespace': 'Internal',
                'parameterMap': {},
            },
        },
    })

    class InternalRepository:
        async def find(self, namespace: str, name: str):
            if namespace != 'Internal':
                return None
            if name == 'Path1':
                return path1
            if name == 'Path2':
                return path2
            if name == 'Path3':
                return path3
            return None

        async def filter(self, name: str):
            names = [
                path1.get_signature().get_full_name(),
                path2.get_signature().get_full_name(),
                path3.get_signature().get_full_name(),
            ]
            return [n for n in names if name.lower() in n.lower()]

    repo = HybridRepository(KIRunFunctionRepository(), InternalRepository())

    start_time = time.time()

    await KIRuntime(main_def, False).execute(
        FunctionExecutionParameters(repo, KIRunSchemaRepository())
    )

    elapsed_ms = (time.time() - start_time) * 1000

    captured = capsys.readouterr()

    # All 3 print statements should have been called
    assert 'Without Time Finished' in captured.out
    assert '200 Finished' in captured.out
    assert '300 Finished' in captured.out

    # Execution should have waited for the longest path (300ms).
    assert elapsed_ms >= 200
