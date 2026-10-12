"""PrePad / PostPad with an empty pad string.

The padding loop only stops once the padded length passes ``length``, and an empty pad
string never grows it, so these used to loop forever (kirun-js and kirun-java had the
same bug). An empty pad now returns the input unchanged.
"""

from __future__ import annotations

import pytest

from kirun_py.function.system.string.post_pad import PostPad
from kirun_py.function.system.string.pre_pad import PrePad
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


def _fep(args: dict) -> FunctionExecutionParameters:
    return FunctionExecutionParameters(KIRunFunctionRepository(), KIRunSchemaRepository()).set_arguments(args)


@pytest.mark.asyncio
async def test_prepad_with_empty_pad_string_returns_the_input():
    fep = _fep({
        PrePad.PARAMETER_STRING_NAME: '5',
        PrePad.PARAMETER_PREPAD_STRING_NAME: '',
        PrePad.PARAMETER_LENGTH_NAME: 3,
    })
    assert (await PrePad().execute(fep)).all_results()[0].get_result()['result'] == '5'


@pytest.mark.asyncio
async def test_postpad_with_empty_pad_string_returns_the_input():
    fep = _fep({
        PostPad.PARAMETER_STRING_NAME: '5',
        PostPad.PARAMETER_POSTPAD_STRING_NAME: '',
        PostPad.PARAMETER_LENGTH_NAME: 3,
    })
    assert (await PostPad().execute(fep)).all_results()[0].get_result()['result'] == '5'
