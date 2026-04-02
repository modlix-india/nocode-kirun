"""Python ports of GROUP 1 JS string-function tests:

- DeleteForGivenLengthTest.ts
- InsertAtGivenPositionTest.ts
- MatchesTest.ts
- PostPadTest.ts
- PrePadTest.ts
- RegionMatchesTest.ts
- ReverseTest.ts
- SplitTest.ts
- ToStringTest.ts
- TrimToTest.ts
- StringFunctionRepoTest2.ts
- StringFunctionRepoTest3.ts
- StringFunctionRepoTest4.ts
"""
from __future__ import annotations

import pytest

from kirun_py.function.system.string.delete_for_given_length import DeleteForGivenLength
from kirun_py.function.system.string.insert_at_given_position import InsertAtGivenPosition
from kirun_py.function.system.string.matches import Matches
from kirun_py.function.system.string.post_pad import PostPad
from kirun_py.function.system.string.pre_pad import PrePad
from kirun_py.function.system.string.region_matches import RegionMatches
from kirun_py.function.system.string.reverse import Reverse
from kirun_py.function.system.string.split import Split
from kirun_py.function.system.string.to_string import ToString
from kirun_py.function.system.string.trim_to import TrimTo
from kirun_py.function.system.string.abstract_string_function import AbstractStringFunction
from kirun_py.function.system.string.string_function_repository import StringFunctionRepository
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.namespaces.namespaces import Namespaces


# ---------------------------------------------------------------------------
# DeleteForGivenLength tests
# ---------------------------------------------------------------------------

delete_fn = DeleteForGivenLength()


@pytest.mark.asyncio
async def test_delete_for_given_length_1():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        DeleteForGivenLength.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        DeleteForGivenLength.PARAMETER_AT_START_NAME: 10,
        DeleteForGivenLength.PARAMETER_AT_END_NAME: 18,
    })

    result = (await delete_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == ' THIScompaNOcoDE plATFNORM'


@pytest.mark.asyncio
async def test_delete_for_given_length_2():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        DeleteForGivenLength.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        DeleteForGivenLength.PARAMETER_AT_START_NAME: 4,
        DeleteForGivenLength.PARAMETER_AT_END_NAME: 10,
    })

    result = (await delete_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == ' THItY IS A NOcoDE plATFNORM'


# ---------------------------------------------------------------------------
# InsertAtGivenPosition tests
# ---------------------------------------------------------------------------

insert_fn = InsertAtGivenPosition()


@pytest.mark.asyncio
async def test_insert_at_given_position_1():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        InsertAtGivenPosition.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        InsertAtGivenPosition.PARAMETER_AT_POSITION_NAME: 6,
        InsertAtGivenPosition.PARAMETER_INSERT_STRING_NAME: 'surendhar',
    })

    result = (await insert_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == ' THIScsurendharompatY IS A NOcoDE plATFNORM'


@pytest.mark.asyncio
async def test_insert_at_given_position_2():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        InsertAtGivenPosition.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        InsertAtGivenPosition.PARAMETER_AT_POSITION_NAME: 6,
        InsertAtGivenPosition.PARAMETER_INSERT_STRING_NAME: 'surendhar',
    })

    result = (await insert_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == ' THIScsurendharompatY IS A NOcoDE plATFNORM'


@pytest.mark.asyncio
async def test_insert_at_given_position_3():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        InsertAtGivenPosition.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        InsertAtGivenPosition.PARAMETER_AT_POSITION_NAME: 29,
        InsertAtGivenPosition.PARAMETER_INSERT_STRING_NAME: 'surendhar',
    })

    result = (await insert_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == ' THIScompatY IS A NOcoDE plATsurendharFNORM'


# ---------------------------------------------------------------------------
# Matches tests
# ---------------------------------------------------------------------------

matches_fn = Matches()


@pytest.mark.asyncio
async def test_matches_simple_date_match():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'regex': r'(\d{2}).(\d{2}).(\d{4})',
        'string': '10.12.1222',
    })
    result = (await matches_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result is True


@pytest.mark.asyncio
async def test_matches_date_with_end_anchor_trailing_space():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'regex': r'(\d{2}).(\d{2}).(\d{4})$',
        'string': '10.12.1222 ',
    })
    result = (await matches_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result is False


@pytest.mark.asyncio
async def test_matches_date_not_matching_text():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'regex': r'(\d{2}).(\d{2}).(\d{4})',
        'string': 'fdsgjhg10.12.122 2',
    })
    result = (await matches_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result is False


@pytest.mark.asyncio
async def test_matches_name_matches():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'regex': r'(\w+),\s(Mr|Ms|Mrs|Dr)\.\s?(\w+)',
        'string': 'smith, Mr.John',
    })
    result = (await matches_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result is True


@pytest.mark.asyncio
async def test_matches_name_in_sentence():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'regex': r'(\w+),\s(Mr|Ms|Mrs|Dr)\.\s?(\w+)',
        'string': 'How are you doing smith, Mr.John??',
    })
    result = (await matches_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result is True


@pytest.mark.asyncio
async def test_matches_name_anchored_false():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'regex': r'$(\w+),\s(Mr|Ms|Mrs|Dr)\.\s?(\w+)$',
        'string': 'smith, Mr.Johnadf',
    })
    result = (await matches_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result is False


@pytest.mark.asyncio
async def test_matches_time_matches():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'regex': r'^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?$',
        'string': '03:33:33',
    })
    result = (await matches_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result is True


@pytest.mark.asyncio
async def test_matches_time_end_anchor_false():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'regex': r'([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?$',
        'string': 'How are you d 03:33:33oing smith, Mr.John??',
    })
    result = (await matches_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result is False


@pytest.mark.asyncio
async def test_matches_time_in_string():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'regex': r'([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?',
        'string': 'surendhar12:12:12-02:54asd',
    })
    result = (await matches_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result is True


@pytest.mark.asyncio
async def test_matches_time_start_anchor_false():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'regex': r'^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?',
        'string': 'surendhar12:12:12-02:54asd',
    })
    result = (await matches_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result is False


# ---------------------------------------------------------------------------
# PostPad tests
# ---------------------------------------------------------------------------

postpad_fn = PostPad()


@pytest.mark.asyncio
async def test_postpad_1():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'string': ' THIScompatY IS A NOcoDE plATFNORM',
        'postpadString': 'hiran',
        'length': 12,
    })

    result = (await postpad_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == ' THIScompatY IS A NOcoDE plATFNORMhiranhiranhi'


@pytest.mark.asyncio
async def test_postpad_2():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'string': ' THIScompatY IS A NOcoDE plATFNORM',
        'postpadString': ' h ',
        'length': 15,
    })

    result = (await postpad_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == ' THIScompatY IS A NOcoDE plATFNORM h  h  h  h  h '


@pytest.mark.asyncio
async def test_postpad_3():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'string': ' THIScompatY IS A NOcoDE plATFNORM',
        'postpadString': ' surendhar ',
        'length': 100,
    })

    expected = (
        ' THIScompatY IS A NOcoDE plATFNORM'
        ' surendhar  surendhar  surendhar  surendhar  surendhar '
        ' surendhar  surendhar  surendhar  surendhar  '
    )
    result = (await postpad_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == expected


# ---------------------------------------------------------------------------
# PrePad tests
# ---------------------------------------------------------------------------

prepad_fn = PrePad()


@pytest.mark.asyncio
async def test_prepad_1():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        PrePad.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        PrePad.PARAMETER_PREPAD_STRING_NAME: 'hiran',
        PrePad.PARAMETER_LENGTH_NAME: 12,
    })

    result = (await prepad_fn.execute(fep)).all_results()[0].get_result()[PrePad.EVENT_RESULT_NAME]
    assert result == 'hiranhiranhi THIScompatY IS A NOcoDE plATFNORM'


@pytest.mark.asyncio
async def test_prepad_2():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        PrePad.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        PrePad.PARAMETER_PREPAD_STRING_NAME: ' h ',
        PrePad.PARAMETER_LENGTH_NAME: 11,
    })

    result = (await prepad_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == ' h  h  h  h THIScompatY IS A NOcoDE plATFNORM'


@pytest.mark.asyncio
async def test_prepad_3():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        PrePad.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        PrePad.PARAMETER_PREPAD_STRING_NAME: 'hiran',
        PrePad.PARAMETER_LENGTH_NAME: 4,
    })

    result = (await prepad_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == 'hira THIScompatY IS A NOcoDE plATFNORM'


# ---------------------------------------------------------------------------
# RegionMatches tests
# ---------------------------------------------------------------------------

region_fn = RegionMatches()


@pytest.mark.asyncio
async def test_region_matches_1():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        RegionMatches.PARAMETER_BOOLEAN_NAME: True,
        RegionMatches.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        RegionMatches.PARAMETER_FIRST_OFFSET_NAME: 5,
        RegionMatches.PARAMETER_OTHER_STRING_NAME: ' fincitY compatY ',
        RegionMatches.PARAMETER_SECOND_OFFSET_NAME: 9,
        RegionMatches.PARAMETER_INTEGER_NAME: 7,
    })

    result = (await region_fn.execute(fep)).all_results()[0].get_result()[RegionMatches.EVENT_RESULT_NAME]
    assert result is True


@pytest.mark.asyncio
async def test_region_matches_2():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        RegionMatches.PARAMETER_BOOLEAN_NAME: False,
        RegionMatches.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        RegionMatches.PARAMETER_FIRST_OFFSET_NAME: 5,
        RegionMatches.PARAMETER_OTHER_STRING_NAME: ' fincitY compatY ',
        RegionMatches.PARAMETER_SECOND_OFFSET_NAME: 1,
        RegionMatches.PARAMETER_INTEGER_NAME: 7,
    })

    result = (await region_fn.execute(fep)).all_results()[0].get_result()[RegionMatches.EVENT_RESULT_NAME]
    assert result is False


@pytest.mark.asyncio
async def test_region_matches_3():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        RegionMatches.PARAMETER_BOOLEAN_NAME: True,
        RegionMatches.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        RegionMatches.PARAMETER_FIRST_OFFSET_NAME: 10,
        RegionMatches.PARAMETER_OTHER_STRING_NAME: ' fincitY compatY ',
        RegionMatches.PARAMETER_SECOND_OFFSET_NAME: 6,
        RegionMatches.PARAMETER_INTEGER_NAME: 3,
    })

    result = (await region_fn.execute(fep)).all_results()[0].get_result()[RegionMatches.EVENT_RESULT_NAME]
    assert result is True


# ---------------------------------------------------------------------------
# Reverse tests
# ---------------------------------------------------------------------------

reverse_fn = Reverse()


@pytest.mark.asyncio
async def test_reverse_1():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': ' mr"ofta"lp edoc on a si sihT'})

    result = (await reverse_fn.execute(fep)).all_results()[0].get_result()['value']
    assert result == 'This is a no code pl"atfo"rm '


@pytest.mark.asyncio
async def test_reverse_2():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': ' '})

    result = (await reverse_fn.execute(fep)).all_results()[0].get_result()['value']
    assert result == ' '


@pytest.mark.asyncio
async def test_reverse_empty_string():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': ''})

    result = (await reverse_fn.execute(fep)).all_results()[0].get_result()['value']
    assert result == ''


@pytest.mark.asyncio
async def test_reverse_single_character():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': 'a'})

    result = (await reverse_fn.execute(fep)).all_results()[0].get_result()['value']
    assert result == 'a'


@pytest.mark.asyncio
async def test_reverse_palindrome():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': 'racecar'})

    result = (await reverse_fn.execute(fep)).all_results()[0].get_result()['value']
    assert result == 'racecar'


@pytest.mark.asyncio
async def test_reverse_special_characters():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': '!@#$%'})

    result = (await reverse_fn.execute(fep)).all_results()[0].get_result()['value']
    assert result == '%$#@!'


@pytest.mark.asyncio
async def test_reverse_numbers_in_string():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': '12345'})

    result = (await reverse_fn.execute(fep)).all_results()[0].get_result()['value']
    assert result == '54321'


# ---------------------------------------------------------------------------
# Split tests (from SplitTest.ts — already covered in test_string_misc.py,
# but ported again here for completeness matching the JS test files)
# ---------------------------------------------------------------------------

split_fn = Split()


@pytest.mark.asyncio
async def test_split_by_space():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'string': 'I am using eclipse to test the changes with test Driven developement',
        'searchString': ' ',
    })

    expected = [
        'I', 'am', 'using', 'eclipse', 'to', 'test',
        'the', 'changes', 'with', 'test', 'Driven', 'developement',
    ]
    result = (await split_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == expected


@pytest.mark.asyncio
async def test_split_by_e():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'string': 'I am using eclipse to test the changes with test Driven developement',
        'searchString': 'e',
    })

    expected = [
        'I am using ',
        'clips',
        ' to t',
        'st th',
        ' chang',
        's with t',
        'st Driv',
        'n d',
        'v',
        'lop',
        'm',
        'nt',
    ]
    result = (await split_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == expected


# ---------------------------------------------------------------------------
# ToString tests
# ---------------------------------------------------------------------------

tostring_fn = ToString()


@pytest.mark.asyncio
async def test_tostring_integer():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'anytype': 123})

    result = (await tostring_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == '123'


@pytest.mark.asyncio
async def test_tostring_array():
    array = ['I', 'am', 'using', 'eclipse', 'to', 'test',
             'the', 'changes', 'with', 'test', 'Driven', 'developement']

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'anytype': array})

    expected = (
        '[\n'
        '  "I",\n'
        '  "am",\n'
        '  "using",\n'
        '  "eclipse",\n'
        '  "to",\n'
        '  "test",\n'
        '  "the",\n'
        '  "changes",\n'
        '  "with",\n'
        '  "test",\n'
        '  "Driven",\n'
        '  "developement"\n'
        ']'
    )
    result = (await tostring_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == expected


@pytest.mark.asyncio
async def test_tostring_boolean():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'anytype': True})

    result = (await tostring_fn.execute(fep)).all_results()[0].get_result()['result']
    assert result == 'True'


# ---------------------------------------------------------------------------
# TrimTo tests (from TrimToTest.ts — also partially covered in test_string_misc.py)
# ---------------------------------------------------------------------------

trimto_fn = TrimTo()


@pytest.mark.asyncio
async def test_trim_to_14():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        TrimTo.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        TrimTo.PARAMETER_LENGTH_NAME: 14,
    })

    result = (await trimto_fn.execute(fep)).all_results()[0].get_result()[TrimTo.EVENT_RESULT_NAME]
    assert result == ' THIScompatY I'


@pytest.mark.asyncio
async def test_trim_to_0():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        TrimTo.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        TrimTo.PARAMETER_LENGTH_NAME: 0,
    })

    result = (await trimto_fn.execute(fep)).all_results()[0].get_result()[TrimTo.EVENT_RESULT_NAME]
    assert result == ''


# ---------------------------------------------------------------------------
# StringFunctionRepository tests (StringFunctionRepoTest2.ts)
# ---------------------------------------------------------------------------

string_repo = StringFunctionRepository()


@pytest.mark.asyncio
async def test_string_repo_contains_true():
    fun = await string_repo.find(Namespaces.STRING, 'Contains')
    assert fun is not None, 'Contains function should exist'

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: '\t\t\tno code  Kirun  PLATform\t\t',
        AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME: 'no code',
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is True


@pytest.mark.asyncio
async def test_string_repo_contains_space():
    fun = await string_repo.find(Namespaces.STRING, 'Contains')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: '   ',
        AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME: ' ',
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is True


@pytest.mark.asyncio
async def test_string_repo_contains_special_chars():
    fun = await string_repo.find(Namespaces.STRING, 'Contains')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: '{20934 123 1[[23 245-0 34\\\\" 3434 \\\\" 123]]}',
        AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME: '4 123 1[[23 245-0 34',
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is True


@pytest.mark.asyncio
async def test_string_repo_contains_regex_pattern_as_literal_false():
    fun = await string_repo.find(Namespaces.STRING, 'Contains')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: '{20934 123 1[[23 245-0 34\\\\" 3434 \\\\" 123]]}',
        AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME: '2093(.*)',
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is False


@pytest.mark.asyncio
async def test_string_repo_ends_with_true():
    fun = await string_repo.find(Namespaces.STRING, 'EndsWith')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: '\t\t\tno code  Kirun  PLATform\t\t',
        AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME: 'PLATform\t\t',
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is True


@pytest.mark.asyncio
async def test_string_repo_ends_with_tab():
    fun = await string_repo.find(Namespaces.STRING, 'EndsWith')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: 'this is a new job\t',
        AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME: 'job\t',
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is True


@pytest.mark.asyncio
async def test_string_repo_ends_with_special_chars():
    fun = await string_repo.find(Namespaces.STRING, 'EndsWith')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: '{20934 123 1[[23 245-0 34\\\\" 3434 \\\\" 123]]}',
        AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME: '" 123]]}',
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is True


@pytest.mark.asyncio
async def test_string_repo_ends_with_false():
    fun = await string_repo.find(Namespaces.STRING, 'EndsWith')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: '{20934 123 1[[23 245-0 34\\\\" 3434 \\\\" 123]]}',
        AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME: ']]20934}',
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is False


# ---------------------------------------------------------------------------
# StringFunctionRepoTest3.ts — EqualsIgnoreCase
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_string_repo_equals_ignore_case_true():
    fun = await string_repo.find(Namespaces.STRING, 'EqualsIgnoreCase')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: ' THIS IS A NOcoDE plATFORM\t\t',
        AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME: ' THIS IS A NOCODE PLATFORM\t\t',
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is True


@pytest.mark.asyncio
async def test_string_repo_equals_ignore_case_false():
    fun = await string_repo.find(Namespaces.STRING, 'EqualsIgnoreCase')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: '    20934 123 123 245-0 34" 3434 " 123',
        AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME: ' w20934 123 123 245-0 34" 3434 " 123   ',
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is False


@pytest.mark.asyncio
async def test_string_repo_equals_ignore_case_padded():
    fun = await string_repo.find(Namespaces.STRING, 'EqualsIgnoreCase')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: '         no code  Kirun  PLATform   ',
        AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME: '         NO CODE  KIRUN  PLATFORM   ',
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is True


# ---------------------------------------------------------------------------
# StringFunctionRepoTest4.ts — Replace
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_string_repo_replace_removes_spaces():
    fun = await string_repo.find(Namespaces.STRING, 'Replace')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: '  new elemenet  ',
        AbstractStringFunction.PARAMETER_SECOND_STRING_NAME: ' ',
        AbstractStringFunction.PARAMETER_THIRD_STRING_NAME: '',
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result == 'newelemenet'


@pytest.mark.asyncio
async def test_string_repo_replace_no_match():
    fun = await string_repo.find(Namespaces.STRING, 'Replace')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: 'thereisnospace',
        AbstractStringFunction.PARAMETER_SECOND_STRING_NAME: '   ',
        AbstractStringFunction.PARAMETER_THIRD_STRING_NAME: '  ',
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result == 'thereisnospace'
