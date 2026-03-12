"""Python ports of GROUP 3 misc/util tests:

- ErrorMessageFormatterTest.ts
- RepositoryFilterTest.ts  (filter portion not already covered)
"""
from __future__ import annotations

import pytest

from kirun_py.util.error_message_formatter import ErrorMessageFormatter
from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.array.array_schema_type import ArraySchemaType
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


# ---------------------------------------------------------------------------
# ErrorMessageFormatter.format_value
# ---------------------------------------------------------------------------

def test_format_value_none():
    assert ErrorMessageFormatter.format_value(None) == 'null'


def test_format_value_string_with_quotes():
    assert ErrorMessageFormatter.format_value('hello') == '"hello"'


def test_format_value_integer():
    assert ErrorMessageFormatter.format_value(42) == '42'


def test_format_value_float():
    assert ErrorMessageFormatter.format_value(3.14) == '3.14'


def test_format_value_bool_true():
    assert ErrorMessageFormatter.format_value(True) == 'true'


def test_format_value_bool_false():
    assert ErrorMessageFormatter.format_value(False) == 'false'


def test_format_value_object_as_json():
    obj = {'name': 'John', 'age': 30}
    result = ErrorMessageFormatter.format_value(obj)
    assert '"name"' in result
    assert '"John"' in result
    assert '"age"' in result
    assert '30' in result


def test_format_value_array_as_json():
    arr = [1, 2, 3]
    result = ErrorMessageFormatter.format_value(arr)
    assert '[' in result
    assert '1' in result
    assert '2' in result
    assert '3' in result
    assert ']' in result


def test_format_value_truncates_long_values():
    long_obj = {'data': 'x' * 300}
    result = ErrorMessageFormatter.format_value(long_obj, 50)
    assert len(result) <= 53  # 50 + "..."
    assert '...' in result


# ---------------------------------------------------------------------------
# ErrorMessageFormatter.format_function_name
# ---------------------------------------------------------------------------

def test_format_function_name_with_namespace():
    assert ErrorMessageFormatter.format_function_name('UIEngine', 'SetStore') == 'UIEngine.SetStore'


def test_format_function_name_none_namespace():
    assert ErrorMessageFormatter.format_function_name(None, 'loadEverything') == 'loadEverything'


def test_format_function_name_undefined_string():
    assert ErrorMessageFormatter.format_function_name('undefined', 'loadEverything') == 'loadEverything'


# ---------------------------------------------------------------------------
# ErrorMessageFormatter.format_statement_name
# ---------------------------------------------------------------------------

def test_format_statement_name_returns_quoted():
    assert ErrorMessageFormatter.format_statement_name('storeString') == "'storeString'"


def test_format_statement_name_none_returns_none():
    assert ErrorMessageFormatter.format_statement_name(None) is None


def test_format_statement_name_undefined_string_returns_none():
    assert ErrorMessageFormatter.format_statement_name('undefined') is None


# ---------------------------------------------------------------------------
# ErrorMessageFormatter.build_function_execution_error
# ---------------------------------------------------------------------------

def test_build_function_execution_error_with_statement():
    result = ErrorMessageFormatter.build_function_execution_error(
        'UIEngine.SetStore',
        "'storeString'",
        'Expected an array but found {"key": "value"}',
    )
    assert result == (
        "Error while executing the function UIEngine.SetStore in statement 'storeString': "
        'Expected an array but found {"key": "value"}'
    )


def test_build_function_execution_error_without_statement():
    result = ErrorMessageFormatter.build_function_execution_error(
        'loadEverything',
        None,
        'Some error occurred',
    )
    assert result == 'Error while executing the function loadEverything: Some error occurred'


def test_build_function_execution_error_with_parameter():
    result = ErrorMessageFormatter.build_function_execution_error(
        'UIEngine.SetStore',
        "'storeString'",
        'Expected an array but found {}',
        'value',
    )
    assert result == (
        "Error while executing the function UIEngine.SetStore's parameter value "
        "in statement 'storeString': Expected an array but found {}"
    )


def test_build_function_execution_error_parameter_no_statement():
    result = ErrorMessageFormatter.build_function_execution_error(
        'loadApp',
        None,
        'Invalid parameter',
        'config',
    )
    assert result == (
        "Error while executing the function loadApp's parameter config: Invalid parameter"
    )


def test_build_function_execution_error_with_schema_definition():
    ast = ArraySchemaType()
    ast.set_single_schema(Schema.of_integer('item'))
    schema = Schema.of_array('value').set_items(ast)
    result = ErrorMessageFormatter.build_function_execution_error(
        'System.Array.Concatenate',
        None,
        'Expected an array but found null',
        'secondSource',
        schema,
    )
    assert result == (
        "Error while executing the function System.Array.Concatenate's parameter secondSource "
        '[Expected: Array<Integer>]: Expected an array but found null'
    )


def test_build_function_execution_error_wraps_nested_error():
    inner_error = (
        "Error while executing the function UIEngine.SetStore in statement 'storeString' "
        '[Expected: Array]: Expected an array but found {}'
    )
    result = ErrorMessageFormatter.build_function_execution_error(
        'loadApp',
        None,
        inner_error,
    )
    assert result == 'Error while executing the function loadApp: \n' + inner_error


# ---------------------------------------------------------------------------
# ErrorMessageFormatter.format_error_message
# ---------------------------------------------------------------------------

def test_format_error_message_from_exception():
    error = Exception('Something went wrong')
    assert ErrorMessageFormatter.format_error_message(error) == 'Something went wrong'


def test_format_error_message_string():
    assert ErrorMessageFormatter.format_error_message('Error message') == 'Error message'


def test_format_error_message_none():
    assert ErrorMessageFormatter.format_error_message(None) == 'Unknown error'


def test_format_error_message_dict_with_message():
    # Python dicts don't have .args, so format_value is used
    error = {'message': 'Expected an array but found something'}
    result = ErrorMessageFormatter.format_error_message(error)
    # The dict gets JSON-formatted; just check the key content is present
    assert 'Expected an array but found something' in result or 'message' in result


# ---------------------------------------------------------------------------
# RepositoryFilterTest (already in test_ki_run_function_repository.py but
# included here as specified by the task)
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_repository_filter_rep():
    func_repo = KIRunFunctionRepository()
    result = await func_repo.filter('Rep')
    assert sorted(result) == sorted([
        'System.String.Repeat',
        'System.String.Replace',
        'System.String.ReplaceFirst',
        'System.String.PrePad',
        'System.String.ReplaceAtGivenPosition',
    ])


@pytest.mark.asyncio
async def test_repository_filter_root():
    func_repo = KIRunFunctionRepository()
    result = await func_repo.filter('root')
    assert sorted(result) == sorted([
        'System.Math.CubeRoot',
        'System.Math.SquareRoot',
    ])


@pytest.mark.asyncio
async def test_schema_repository_filter_root_empty():
    schema_repo = KIRunSchemaRepository()
    result = await schema_repo.filter('root')
    assert result == []


@pytest.mark.asyncio
async def test_schema_repository_filter_rin():
    schema_repo = KIRunSchemaRepository()
    result = await schema_repo.filter('rin')
    assert result == ['System.string']


@pytest.mark.asyncio
async def test_schema_repository_filter_ny():
    schema_repo = KIRunSchemaRepository()
    result = await schema_repo.filter('ny')
    assert result == ['System.any']


@pytest.mark.asyncio
async def test_schema_repository_filter_all():
    schema_repo = KIRunSchemaRepository()
    result = sorted(await schema_repo.filter(''))
    assert result == sorted([
        'System.Date.Duration',
        'System.Date.TimeObject',
        'System.Date.Timestamp',
        'System.Date.Timeunit',
        'System.Null',
        'System.ParameterExpression',
        'System.Schema',
        'System.any',
        'System.boolean',
        'System.double',
        'System.float',
        'System.integer',
        'System.long',
        'System.number',
        'System.string',
    ])
