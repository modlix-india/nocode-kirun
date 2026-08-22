package com.fincity.nocode.kirun.engine.runtime.suspend.store;

import com.fincity.nocode.kirun.engine.runtime.suspend.SuspendedExecution;

import reactor.core.publisher.Mono;

/**
 * Where a host parks suspended executions between a stop and a go.
 *
 * The runtime never calls this itself - it hands back a
 * {@link SuspendedExecution} and lets the host decide what to persist and when. The interface
 * exists so hosts have one shape to implement against, and so tests can run the whole stop/go
 * cycle against {@link InMemoryExecutionStateStore}.
 */
public interface ExecutionStateStore {

	/** Stores a snapshot, replacing any existing one for the same execution id. */
	Mono<Void> save(SuspendedExecution state);

	/** The snapshot for this execution id, or empty when there is none. */
	Mono<SuspendedExecution> load(String executionId);

	/** Forgets the snapshot. Called once an execution has resumed to completion, or is abandoned. */
	Mono<Void> delete(String executionId);
}
