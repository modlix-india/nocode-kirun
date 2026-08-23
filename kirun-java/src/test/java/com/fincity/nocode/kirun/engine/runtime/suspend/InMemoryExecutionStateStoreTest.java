package com.fincity.nocode.kirun.engine.runtime.suspend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.suspend.store.InMemoryExecutionStateStore;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

/**
 * The reference store. Hosts will copy its shape, so its contract is worth pinning down.
 */
class InMemoryExecutionStateStoreTest {

	private SuspendedExecution state(String executionId) {

		return new SuspendedExecution().setExecutionId(executionId)
		        .setNamespace("Test")
		        .setName("Journey")
		        .setSuspendedStepName("wait")
		        .setWakeCondition(new WakeCondition.TimerWake(Instant.parse("2026-09-01T00:00:00Z")))
		        .setArguments(Map.of("who", new JsonPrimitive("someone")));
	}

	@Test
	void savesAndLoadsBackTheSameExecution() {

		InMemoryExecutionStateStore store = new InMemoryExecutionStateStore();

		store.save(state("exec-1"))
		        .block();

		SuspendedExecution loaded = store.load("exec-1")
		        .block();

		assertNotNull(loaded);
		assertEquals("Journey", loaded.getName());
		assertEquals("wait", loaded.getSuspendedStepName());
		assertEquals(Instant.parse("2026-09-01T00:00:00Z"),
		        ((WakeCondition.TimerWake) loaded.getWakeCondition()).wakeAt());
		assertEquals("someone", loaded.getArguments()
		        .get("who")
		        .getAsString());
	}

	@Test
	void loadingAnUnknownExecutionIsEmptyRatherThanAnError() {

		StepVerifier.create(new InMemoryExecutionStateStore().load("nope"))
		        .verifyComplete();
	}

	@Test
	void savingTwiceReplacesRatherThanDuplicates() {

		InMemoryExecutionStateStore store = new InMemoryExecutionStateStore();

		store.save(state("exec-1"))
		        .block();
		store.save(state("exec-1").setSuspendedStepName("laterWait"))
		        .block();

		assertEquals(1, store.size());
		assertEquals("laterWait", store.load("exec-1")
		        .block()
		        .getSuspendedStepName());
	}

	@Test
	void deleteForgetsTheExecution() {

		InMemoryExecutionStateStore store = new InMemoryExecutionStateStore();

		store.save(state("exec-1"))
		        .block();
		store.delete("exec-1")
		        .block();

		assertEquals(0, store.size());
		assertNull(store.load("exec-1")
		        .block());
	}

	@Test
	void savingWithoutAnExecutionIdIsRejected() {

		StepVerifier.create(new InMemoryExecutionStateStore().save(new SuspendedExecution()))
		        .expectError(IllegalArgumentException.class)
		        .verify();
	}
}
