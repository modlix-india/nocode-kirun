from __future__ import annotations

import pytest
import pytest_asyncio
from kirun_py.function.system.json.json_parse import JSONParse
from kirun_py.function.system.json.json_stringify import JSONStringify
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


# ---------------------------------------------------------------------------
# JSONParse tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_json_parse_object():
    json_parse = JSONParse()

    result = await json_parse.execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({'source': '{"name":"John","age":30}'}),
    )

    assert result.all_results()[0].get_result()['value'] == {'name': 'John', 'age': 30}


@pytest.mark.asyncio
async def test_json_parse_array():
    json_parse = JSONParse()

    result = await json_parse.execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({'source': '[1,2,3,4,5]'}),
    )

    assert result.all_results()[0].get_result()['value'] == [1, 2, 3, 4, 5]


@pytest.mark.asyncio
async def test_json_parse_null_string():
    json_parse = JSONParse()

    result = await json_parse.execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({'source': 'null'}),
    )

    assert result.all_results()[0].get_result()['value'] is None


@pytest.mark.asyncio
async def test_json_parse_invalid_json():
    json_parse = JSONParse()

    result = await json_parse.execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({'source': 'invalid JSON'}),
    )

    # When parsing fails, the error event is first; check it has errorMessage
    error_result = result.all_results()[0]
    assert error_result.get_name() == 'error'
    assert 'errorMessage' in error_result.get_result()
    assert error_result.get_result()['errorMessage'] is not None


@pytest.mark.asyncio
async def test_json_parse_empty_string():
    json_parse = JSONParse()

    result = await json_parse.execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({'source': ''}),
    )

    assert result.all_results()[0].get_result()['value'] is None


# ---------------------------------------------------------------------------
# JSONStringify tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_json_stringify_object():
    json_stringify = JSONStringify()

    result = await json_stringify.execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({'source': {'name': 'John', 'age': 30}}),
    )

    # Python's json.dumps uses ', ' and ': ' separators by default
    import json
    expected = json.dumps({'name': 'John', 'age': 30})
    assert result.all_results()[0].get_result()['value'] == expected


@pytest.mark.asyncio
async def test_json_stringify_none():
    json_stringify = JSONStringify()

    result = await json_stringify.execute(
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        ).set_arguments({'source': None}),
    )

    assert result.all_results()[0].get_result()['value'] == 'null'
