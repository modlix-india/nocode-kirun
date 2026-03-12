"""Repository tests ported from:
- KIRunFunctionRepositoryTest.ts
- RepositoryFilterTest.ts
"""
from __future__ import annotations

import pytest

from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.namespaces.namespaces import Namespaces


# ---------------------------------------------------------------------------
# KIRunFunctionRepositoryTest
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_ki_run_function_repository_find():
    """Find known functions in KIRunFunctionRepository."""
    repo = KIRunFunctionRepository()

    fun = await repo.find(Namespaces.STRING, 'ToString')
    assert fun is not None
    assert fun.get_signature().get_name() == 'ToString'

    fun = await repo.find(Namespaces.STRING, 'IndexOfWithStartPoint')
    assert fun is not None
    assert fun.get_signature().get_name() == 'IndexOfWithStartPoint'

    fun = await repo.find(Namespaces.SYSTEM_ARRAY, 'Compare')
    assert fun is not None
    assert fun.get_signature().get_name() == 'Compare'

    fun = await repo.find(Namespaces.MATH, 'RandomInt')
    assert fun is not None
    assert fun.get_signature().get_name() == 'RandomInt'

    fun = await repo.find(Namespaces.MATH, 'Exponential')
    assert fun is not None
    assert fun.get_signature().get_name() == 'Exponential'

    fun = await repo.find(Namespaces.SYSTEM, 'If')
    assert fun is not None
    assert fun.get_signature().get_name() == 'If'


# ---------------------------------------------------------------------------
# RepositoryFilterTest
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_repository_filter():
    """Filter functions and schemas by name substring."""
    func_repo = KIRunFunctionRepository()
    schema_repo = KIRunSchemaRepository()

    result = await func_repo.filter('Rep')
    assert sorted(result) == sorted([
        'System.String.Repeat',
        'System.String.Replace',
        'System.String.ReplaceFirst',
        'System.String.PrePad',
        'System.String.ReplaceAtGivenPosition',
    ])

    result = await func_repo.filter('root')
    assert sorted(result) == sorted([
        'System.Math.CubeRoot',
        'System.Math.SquareRoot',
    ])

    result = await schema_repo.filter('root')
    assert result == []

    result = await schema_repo.filter('rin')
    assert result == ['System.string']

    result = await schema_repo.filter('ny')
    assert result == ['System.any']

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
