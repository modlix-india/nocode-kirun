package com.fincity.nocode.kirun.engine.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.PackageScanningFunctionRepository;

/**
 * The constructor of this repository runs a full Reflections classpath scan and reflectively
 * instantiates every function class it finds. On 2026-07-30 dev core was building one of these
 * per function execution, which cost ~450% CPU under load. The scan must be done once per
 * package per JVM.
 */
class PackageScanningFunctionRepositoryTest {

	private static final String FUNCTION_PACKAGE = "com.fincity.nocode.kirun.engine.function";

	@Test
	void scanIsSharedAcrossInstances() {

		var first = new PackageScanningFunctionRepository(FUNCTION_PACKAGE);
		var second = new PackageScanningFunctionRepository(FUNCTION_PACKAGE);

		ReactiveFunction fromFirst = first.find(Namespaces.SYSTEM, "If").block();
		ReactiveFunction fromSecond = second.find(Namespaces.SYSTEM, "If").block();

		assertNotNull(fromFirst, "System.If should resolve from a freshly scanned repository");

		// A second scan would have produced a distinct instance. Sharing proves the scan was
		// memoized rather than repeated.
		assertSame(fromFirst, fromSecond, "Both repositories must serve the same memoized function instance");
	}

	@Test
	void filterStillWorksOnAMemoizedScan() {

		var repo = new PackageScanningFunctionRepository(FUNCTION_PACKAGE);

		var names = repo.filter("if").collectList().block();

		assertNotNull(names);
		assertTrue(
				names.stream().anyMatch(e -> e.equalsIgnoreCase(Namespaces.SYSTEM + ".If")),
				"filter() must still see the scanned functions, got: " + names);
	}
}
