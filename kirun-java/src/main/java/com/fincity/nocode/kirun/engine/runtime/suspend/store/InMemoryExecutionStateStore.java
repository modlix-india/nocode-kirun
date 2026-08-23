package com.fincity.nocode.kirun.engine.runtime.suspend.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fincity.nocode.kirun.engine.runtime.suspend.SuspendedExecution;
import com.fincity.nocode.kirun.engine.runtime.suspend.SuspendedExecutionSerializer;

import reactor.core.publisher.Mono;

/**
 * An {@link ExecutionStateStore} held in a map, for tests and for hosts that want the stop/go
 * mechanics without durability.
 *
 * Snapshots are stored as serialised JSON rather than as live objects, so a test that saves and
 * loads goes through the same round-trip a real database would and cannot accidentally pass by
 * sharing mutable state with the runtime.
 */
public class InMemoryExecutionStateStore implements ExecutionStateStore {

	private final Map<String, String> states = new ConcurrentHashMap<>();

	@Override
	public Mono<Void> save(SuspendedExecution state) {

		if (state == null || state.getExecutionId() == null)
			return Mono.error(new IllegalArgumentException("A suspended execution needs an execution id to be saved"));

		states.put(state.getExecutionId(), SuspendedExecutionSerializer.serialize(state));

		return Mono.empty();
	}

	@Override
	public Mono<SuspendedExecution> load(String executionId) {

		String json = executionId == null ? null : states.get(executionId);

		return json == null ? Mono.empty() : Mono.just(SuspendedExecutionSerializer.deserialize(json));
	}

	@Override
	public Mono<Void> delete(String executionId) {

		if (executionId != null)
			states.remove(executionId);

		return Mono.empty();
	}

	/** How many snapshots are currently held. Handy in tests. */
	public int size() {
		return states.size();
	}
}
