from __future__ import annotations

import asyncio

import pytest

from kirun_py.runtime.debug import DebugCollector


@pytest.fixture(autouse=True)
def setup_collector():
    collector = DebugCollector.get_instance()
    collector.clear()
    collector.enable()
    yield
    collector.disable()
    collector.clear()


def test_should_initialize_execution_correctly():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'System.Test.Function')
    debug_info = collector.get_execution('test-eid')
    assert debug_info is not None
    assert debug_info.execution_id == 'test-eid'
    assert len(debug_info.logs) == 0
    assert debug_info.start_time > 0


@pytest.mark.asyncio
async def test_should_track_step_start_and_end():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'System.Test.Function')
    step_id = collector.start_step('test-eid', 'step1', 'System.Math.Add', {'a': 1, 'b': 2})
    await asyncio.sleep(0.01)
    collector.end_step('test-eid', step_id, 'output', {'result': 3})
    debug_info = collector.get_execution('test-eid')
    assert len(debug_info.logs) == 1
    assert debug_info.logs[0].statement_name == 'step1'
    assert debug_info.logs[0].function_name == 'System.Math.Add'
    assert debug_info.logs[0].event_name == 'output'
    assert debug_info.logs[0].duration > 0
    assert debug_info.logs[0].arguments == {'a': 1, 'b': 2}
    assert debug_info.logs[0].result == {'result': 3}


def test_should_track_multiple_steps_in_order():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'System.Test.Function')
    step1 = collector.start_step('test-eid', 'step1', 'Function1')
    collector.end_step('test-eid', step1, 'output')
    step2 = collector.start_step('test-eid', 'step2', 'Function2')
    collector.end_step('test-eid', step2, 'output')
    step3 = collector.start_step('test-eid', 'step3', 'Function3')
    collector.end_step('test-eid', step3, 'output')
    debug_info = collector.get_execution('test-eid')
    assert len(debug_info.logs) == 3
    assert debug_info.logs[0].statement_name == 'step1'
    assert debug_info.logs[1].statement_name == 'step2'
    assert debug_info.logs[2].statement_name == 'step3'


def test_should_record_error_in_step():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'System.Test.Function')
    step_id = collector.start_step('test-eid', 'failingStep', 'System.Test.Failing')
    collector.end_step('test-eid', step_id, 'error', None, 'Test error message')
    debug_info = collector.get_execution('test-eid')
    assert debug_info.logs[0].error == 'Test error message'
    assert debug_info.logs[0].event_name == 'error'
    assert debug_info.errored is True


def test_should_track_nested_function_calls_hierarchically():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'Parent.Function')
    parent_step = collector.start_step('test-eid', 'parentStep', 'Parent.Function')
    child_step = collector.start_step('test-eid', 'childStep', 'Child.Function')
    collector.end_step('test-eid', child_step, 'output', {'value': 42})
    collector.end_step('test-eid', parent_step, 'output')
    debug_info = collector.get_execution('test-eid')
    assert len(debug_info.logs) == 1
    assert debug_info.logs[0].statement_name == 'parentStep'
    assert len(debug_info.logs[0].children) == 1
    assert debug_info.logs[0].children[0].statement_name == 'childStep'


def test_should_flatten_nested_logs_correctly():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'Parent.Function')
    parent_step = collector.start_step('test-eid', 'parentStep', 'Parent.Function')
    child_step = collector.start_step('test-eid', 'childStep', 'Child.Function')
    collector.end_step('test-eid', child_step, 'output')
    collector.end_step('test-eid', parent_step, 'output')
    flat_logs = collector.get_flat_logs('test-eid')
    assert len(flat_logs) == 2
    assert any(l.statement_name == 'parentStep' for l in flat_logs)
    assert any(l.statement_name == 'childStep' for l in flat_logs)


def test_should_handle_missing_step_id_gracefully():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'System.Test.Function')
    collector.end_step('test-eid', 'non-existent-step', 'output')  # should not raise
    debug_info = collector.get_execution('test-eid')
    assert len(debug_info.logs) == 0


@pytest.mark.asyncio
async def test_should_calculate_correct_duration():
    collector = DebugCollector.get_instance()
    collector.start_execution('test-eid', 'System.Test.Function')
    step_id = collector.start_step('test-eid', 'timedStep', 'System.Test.Timed')
    await asyncio.sleep(0.05)
    collector.end_step('test-eid', step_id, 'output')
    debug_info = collector.get_execution('test-eid')
    assert debug_info.logs[0].duration >= 45
    assert debug_info.logs[0].duration < 150


def test_should_store_definitions_via_start_execution():
    collector = DebugCollector.get_instance()
    definition = {'name': 'TestFunc', 'steps': {'step1': {}}}
    collector.start_execution('test-eid', 'MyNamespace.TestFunc', definition)
    step1 = collector.start_step('test-eid', 'step1', 'System.Math.Add')
    collector.end_step('test-eid', step1, 'output')
    debug_info = collector.get_execution('test-eid')
    assert len(debug_info.definitions) == 1
    assert debug_info.definitions.get('MyNamespace.TestFunc') == definition


def test_should_store_multiple_definitions_for_nested_function_calls():
    collector = DebugCollector.get_instance()
    parent_def = {'name': 'Parent', 'steps': {'callChild': {}}}
    child_def = {'name': 'Child', 'steps': {'doWork': {}}}
    collector.start_execution('test-eid', 'App.Parent', parent_def)
    parent_step = collector.start_step('test-eid', 'callChild', 'App.Child')
    collector.start_execution('test-eid', 'App.Child', child_def)
    child_step = collector.start_step('test-eid', 'doWork', 'System.Math.Add')
    collector.end_step('test-eid', child_step, 'output')
    collector.end_step('test-eid', parent_step, 'output')
    debug_info = collector.get_execution('test-eid')
    assert len(debug_info.definitions) == 2
    assert debug_info.definitions.get('App.Parent') == parent_def
    assert debug_info.definitions.get('App.Child') == child_def


# Event Listener tests

def test_should_emit_execution_start_event():
    collector = DebugCollector.get_instance()
    events = []
    unsubscribe = collector.add_event_listener(lambda e: events.append(e))
    collector.start_execution('test-eid', 'Test.Function')
    assert len(events) == 1
    assert events[0]['type'] == 'executionStart'
    assert events[0]['executionId'] == 'test-eid'
    assert events[0]['data']['functionName'] == 'Test.Function'
    unsubscribe()


def test_should_emit_step_start_and_step_end_events():
    collector = DebugCollector.get_instance()
    events = []
    collector.add_event_listener(lambda e: events.append(e))
    collector.start_execution('test-eid', 'Test.Function')
    step_id = collector.start_step('test-eid', 'step1', 'System.Math.Add', {'a': 1})
    collector.end_step('test-eid', step_id, 'output', {'result': 2})
    # executionStart, stepStart, stepEnd
    assert len(events) == 3
    assert events[1]['type'] == 'stepStart'
    assert events[1]['data']['statementName'] == 'step1'
    assert events[1]['data']['functionName'] == 'System.Math.Add'
    assert events[2]['type'] == 'stepEnd'
    assert events[2]['data']['log'].statement_name == 'step1'
    assert events[2]['data']['log'].result == {'result': 2}


def test_should_emit_execution_end_event():
    collector = DebugCollector.get_instance()
    events = []
    collector.add_event_listener(lambda e: events.append(e))
    collector.start_execution('test-eid', 'Test.Function')
    collector.end_execution('test-eid')
    end_event = next((e for e in events if e['type'] == 'executionEnd'), None)
    assert end_event is not None
    assert end_event['executionId'] == 'test-eid'
    assert end_event['data']['duration'] >= 0


def test_should_emit_execution_errored_event_on_error():
    collector = DebugCollector.get_instance()
    events = []
    collector.add_event_listener(lambda e: events.append(e))
    collector.start_execution('test-eid', 'Test.Function')
    step_id = collector.start_step('test-eid', 'step1', 'Failing.Function')
    collector.end_step('test-eid', step_id, 'error', None, 'Something failed')
    error_event = next((e for e in events if e['type'] == 'executionErrored'), None)
    assert error_event is not None
    assert error_event['executionId'] == 'test-eid'


def test_should_allow_unsubscribing_from_events():
    collector = DebugCollector.get_instance()
    events = []
    unsubscribe = collector.add_event_listener(lambda e: events.append(e))
    collector.start_execution('test-eid', 'Test.Function')
    assert len(events) == 1
    unsubscribe()
    collector.start_execution('test-eid2', 'Test.Function2')
    assert len(events) == 1  # no new events after unsubscribe


def test_should_emit_events_in_correct_order_for_nested_calls():
    collector = DebugCollector.get_instance()
    events = []
    collector.add_event_listener(lambda e: events.append(e))
    collector.start_execution('test-eid', 'Parent.Function')
    parent_step = collector.start_step('test-eid', 'parentStep', 'Child.Function')
    # Nested function
    collector.start_execution('test-eid', 'Child.Function')
    child_step = collector.start_step('test-eid', 'childStep', 'System.Math.Add')
    collector.end_step('test-eid', child_step, 'output')
    collector.end_step('test-eid', parent_step, 'output')
    collector.end_execution('test-eid')
    event_types = [e['type'] for e in events]
    assert event_types == [
        'executionStart',  # Parent starts
        'stepStart',       # parentStep starts
        'stepStart',       # childStep starts
        'stepEnd',         # childStep ends
        'stepEnd',         # parentStep ends
        'executionEnd',    # Execution ends
    ]
