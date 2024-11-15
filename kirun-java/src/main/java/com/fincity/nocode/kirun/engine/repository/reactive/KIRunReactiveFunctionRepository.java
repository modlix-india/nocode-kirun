package com.fincity.nocode.kirun.engine.repository.reactive;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.function.system.date.DateFunctionRepository;
import com.fincity.nocode.kirun.engine.function.system.math.MathFunctionRepository;
import com.fincity.nocode.kirun.engine.function.system.math.RandomRepository;
import com.fincity.nocode.kirun.engine.function.system.string.StringFunctionRepository;
import com.fincity.nocode.kirun.engine.reactive.ReactiveHybridRepository;

public class KIRunReactiveFunctionRepository extends ReactiveHybridRepository<ReactiveFunction> {

	public KIRunReactiveFunctionRepository() {
		super(new PackageScanningFunctionRepository("com.fincity.nocode.kirun.engine.function"),
				new MathFunctionRepository(), new StringFunctionRepository(), new RandomRepository(),
				new DateFunctionRepository());
	}

}
