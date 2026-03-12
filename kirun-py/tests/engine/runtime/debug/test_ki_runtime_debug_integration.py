from __future__ import annotations

import json

import pytest

from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.hybrid_repository import HybridRepository
from kirun_py.model.function_definition import FunctionDefinition
from kirun_py.repository import Repository
from kirun_py.runtime.debug import DebugCollector
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.runtime.ki_runtime import KIRuntime
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


@pytest.fixture(autouse=True)
def setup_collector():
    collector = DebugCollector.get_instance()
    collector.clear()
    collector.enable()
    yield
    collector.disable()
    collector.clear()


@pytest.mark.asyncio
async def test_should_track_nested_ki_runtime_execution_hierarchy():
    first = KIRuntime(
        FunctionDefinition.from_value(
            json.loads("""{
                "name": "First",
                "namespace": "Internal",
                "events": {
                    "output": {
                        "name": "output",
                        "parameters": { "aresult": { "name": "aresult", "type": "INTEGER" } }
                    }
                },
                "steps": {
                    "exSecond": {
                        "statementName": "exSecond",
                        "name": "Second",
                        "namespace": "Internal",
                        "parameterMap": {
                            "value": { "one": { "key": "one", "type": "VALUE", "value": 2 } }
                        }
                    },
                    "exThird": {
                        "statementName": "exThird",
                        "name": "Third",
                        "namespace": "Internal",
                        "dependentStatements": { "Steps.exSecond.output": true },
                        "parameterMap": {
                            "value": { "one": { "key": "one", "type": "VALUE", "value": 3 } }
                        }
                    },
                    "genOutput": {
                        "statementName": "genOutput",
                        "namespace": "System",
                        "name": "GenerateEvent",
                        "dependentStatements": { "Steps.exThird.output": true },
                        "parameterMap": {
                            "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } },
                            "results": {
                                "one": {
                                    "key": "one",
                                    "type": "VALUE",
                                    "value": {
                                        "name": "aresult",
                                        "value": { "isExpression": true, "value": "Steps.exThird.output.result" }
                                    }
                                }
                            }
                        }
                    }
                }
            }""")
        ),
        True,  # debug_mode enabled
    )

    second = KIRuntime(
        FunctionDefinition.from_value(
            json.loads("""{
                "name": "Second",
                "namespace": "Internal",
                "parameters": {
                    "value": { "parameterName": "value", "schema": { "name": "INTEGER", "type": "INTEGER" } }
                },
                "events": {
                    "output": {
                        "name": "output",
                        "parameters": { "result": { "name": "result", "type": "INTEGER" } }
                    }
                },
                "steps": {
                    "genOutput": {
                        "statementName": "genOutput",
                        "namespace": "System",
                        "name": "GenerateEvent",
                        "parameterMap": {
                            "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } },
                            "results": {
                                "one": {
                                    "key": "one",
                                    "type": "VALUE",
                                    "value": {
                                        "name": "result",
                                        "value": { "isExpression": true, "value": "Arguments.value * 2" }
                                    }
                                }
                            }
                        }
                    }
                }
            }""")
        ),
        True,
    )

    third = KIRuntime(
        FunctionDefinition.from_value(
            json.loads("""{
                "name": "Third",
                "namespace": "Internal",
                "parameters": {
                    "value": { "parameterName": "value", "schema": { "name": "INTEGER", "type": "INTEGER" } }
                },
                "events": {
                    "output": {
                        "name": "output",
                        "parameters": { "result": { "name": "result", "type": "INTEGER" } }
                    }
                },
                "steps": {
                    "genOutput": {
                        "statementName": "genOutput",
                        "namespace": "System",
                        "name": "GenerateEvent",
                        "parameterMap": {
                            "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } },
                            "results": {
                                "one": {
                                    "key": "one",
                                    "type": "VALUE",
                                    "value": {
                                        "name": "result",
                                        "value": { "isExpression": true, "value": "Arguments.value * 3" }
                                    }
                                }
                            }
                        }
                    }
                }
            }""")
        ),
        True,
    )

    class InternalRepository(Repository):
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

    output = await first.execute(
        FunctionExecutionParameters(repo, KIRunSchemaRepository(), 'test-nested')
    )
    results = output.next()

    # Verify result: Third(3) * 3 = 9
    assert results.get_result().get('aresult') == 9

    # Verify debug info
    collector = DebugCollector.get_instance()
    execution = collector.get_execution('test-nested')
    assert execution is not None

    # All 3 function definitions should be stored
    assert len(execution.definitions) == 3
    assert 'Internal.First' in execution.definitions
    assert 'Internal.Second' in execution.definitions
    assert 'Internal.Third' in execution.definitions

    # Root logs should be First's steps
    assert len(execution.logs) == 3  # exSecond, exThird, genOutput

    # exSecond should have Second's genOutput as child
    ex_second_log = next((l for l in execution.logs if l.statement_name == 'exSecond'), None)
    assert ex_second_log is not None
    assert len(ex_second_log.children) == 1
    assert ex_second_log.children[0].statement_name == 'genOutput'

    # exThird should have Third's genOutput as child
    ex_third_log = next((l for l in execution.logs if l.statement_name == 'exThird'), None)
    assert ex_third_log is not None
    assert len(ex_third_log.children) == 1
    assert ex_third_log.children[0].statement_name == 'genOutput'

    flat_logs = collector.get_flat_logs('test-nested')
    assert len(flat_logs) > 0
