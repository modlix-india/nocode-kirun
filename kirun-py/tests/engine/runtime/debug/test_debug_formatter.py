from __future__ import annotations

import asyncio

import pytest

from kirun_py.runtime.debug import DebugCollector, DebugFormatter


@pytest.fixture(autouse=True)
def setup_collector():
    collector = DebugCollector.get_instance()
    collector.clear()
    collector.enable()
    yield
    collector.disable()
    collector.clear()


def test_should_format_execution_as_text():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'System.Test.Function')
    step1 = collector.start_step('test-eid', 'step1', 'System.Math.Add')
    collector.end_step('test-eid', step1, 'output', {'result': 3})
    step2 = collector.start_step('test-eid', 'step2', 'System.Math.Multiply')
    collector.end_step('test-eid', step2, 'output', {'result': 6})
    execution = collector.get_execution('test-eid')
    text = DebugFormatter.format_as_text(execution)
    assert 'test-eid' in text
    assert 'step1' in text
    assert 'System.Math.Add' in text
    assert 'step2' in text
    assert 'System.Math.Multiply' in text
    import re
    assert re.search(r'Duration: \d+ms', text)


def test_should_format_execution_with_error_status():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'System.Test.Function')
    step_id = collector.start_step('test-eid', 'failingStep', 'System.Test.Failing')
    collector.end_step('test-eid', step_id, 'error', None, 'Something went wrong')
    execution = collector.get_execution('test-eid')
    text = DebugFormatter.format_as_text(execution)
    assert '❌' in text
    assert 'Error: Something went wrong' in text


def test_should_get_execution_timeline_in_chronological_order():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'System.Test.Function')
    step1 = collector.start_step('test-eid', 'step1', 'Function1')
    collector.end_step('test-eid', step1, 'output')
    step2 = collector.start_step('test-eid', 'step2', 'Function2')
    collector.end_step('test-eid', step2, 'output')
    step3 = collector.start_step('test-eid', 'step3', 'Function3')
    collector.end_step('test-eid', step3, 'output')
    execution = collector.get_execution('test-eid')
    timeline = DebugFormatter.get_timeline(execution)
    assert len(timeline) == 3
    assert timeline[0].statement_name == 'step1'
    assert timeline[1].statement_name == 'step2'
    assert timeline[2].statement_name == 'step3'
    for i in range(1, len(timeline)):
        assert timeline[i].timestamp >= timeline[i - 1].timestamp


def test_should_get_timeline_including_nested_calls():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'System.Test.Function')
    step1 = collector.start_step('test-eid', 'step1', 'Function1')
    nested_step = collector.start_step('test-eid', 'nestedStep', 'Nested.Internal')
    collector.end_step('test-eid', nested_step, 'output')
    collector.end_step('test-eid', step1, 'output')
    execution = collector.get_execution('test-eid')
    timeline = DebugFormatter.get_timeline(execution)
    assert len(timeline) == 2
    assert any(s.statement_name == 'step1' for s in timeline)
    assert any(s.statement_name == 'nestedStep' for s in timeline)


@pytest.mark.asyncio
async def test_should_get_performance_summary():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'System.Test.Function')
    step1 = collector.start_step('test-eid', 'fastStep', 'Function1')
    await asyncio.sleep(0.005)
    collector.end_step('test-eid', step1, 'output')
    step2 = collector.start_step('test-eid', 'slowStep', 'Function2')
    await asyncio.sleep(0.05)
    collector.end_step('test-eid', step2, 'output')
    step3 = collector.start_step('test-eid', 'mediumStep', 'Function3')
    await asyncio.sleep(0.02)
    collector.end_step('test-eid', step3, 'output')
    execution = collector.get_execution('test-eid')
    summary = DebugFormatter.get_performance_summary(execution)
    assert summary.step_count == 3
    assert summary.total_duration > 0
    assert summary.average_duration > 0
    assert len(summary.slowest_steps) == 3
    # Slowest step should be first
    assert summary.slowest_steps[0].statement_name == 'slowStep'
    assert summary.slowest_steps[0].duration > summary.slowest_steps[1].duration


def test_should_format_hierarchical_logs_correctly():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'Parent.Function')
    parent_step = collector.start_step('test-eid', 'parentStep', 'Parent.Function')
    child_step = collector.start_step('test-eid', 'childStep', 'Child.Function')
    grandchild_step = collector.start_step('test-eid', 'grandchildStep', 'Grandchild.Function')
    collector.end_step('test-eid', grandchild_step, 'output')
    collector.end_step('test-eid', child_step, 'output')
    collector.end_step('test-eid', parent_step, 'output')
    execution = collector.get_execution('test-eid')
    text = DebugFormatter.format_as_text(execution)
    assert 'parentStep' in text
    assert 'childStep' in text
    assert 'grandchildStep' in text
    # Timeline should flatten all logs
    timeline = DebugFormatter.get_timeline(execution)
    assert len(timeline) == 3


def test_should_handle_empty_execution():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'System.Test.Function')
    execution = collector.get_execution('test-eid')
    text = DebugFormatter.format_as_text(execution)
    timeline = DebugFormatter.get_timeline(execution)
    summary = DebugFormatter.get_performance_summary(execution)
    assert 'test-eid' in text
    assert 'Steps: 0' in text
    assert len(timeline) == 0
    assert summary.step_count == 0
    assert summary.average_duration == 0
