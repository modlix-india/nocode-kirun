package com.fincity.nocode.kirun.engine.runtime.suspend.store;

import com.fincity.nocode.kirun.engine.runtime.suspend.WakeCondition;

import reactor.core.publisher.Mono;

/**
 * Arranges for a suspended execution to be woken up.
 *
 * The runtime deliberately owns no clock and no scheduler: a day-scale wait cannot be a live
 * subscription. A host implements this over whatever durable scheduling it already has - a cron
 * table, a due-time column swept on an interval, a job scheduler - and calls back into
 * {@link com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime#resume} when the
 * condition is met.
 */
public interface ResumeScheduler {

	/** Registers a wake-up. Replaces any existing registration for the same execution id. */
	Mono<Void> scheduleWake(String executionId, WakeCondition condition);

	/** Drops a pending wake-up, for an execution that resumed early or was abandoned. */
	Mono<Void> cancelWake(String executionId);
}
