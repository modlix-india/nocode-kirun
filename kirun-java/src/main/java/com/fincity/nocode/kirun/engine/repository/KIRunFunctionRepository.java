package com.fincity.nocode.kirun.engine.repository;

import com.fincity.nocode.kirun.engine.HybridRepository;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.function.system.MathFunctionRepository;

public class KIRunFunctionRepository extends HybridRepository<Function> {

	public KIRunFunctionRepository() {
		super(new PackageScanningFunctionRepository("com.fincity.nocode.kirun.engine.function"),
				new MathFunctionRepository());
	}
}
