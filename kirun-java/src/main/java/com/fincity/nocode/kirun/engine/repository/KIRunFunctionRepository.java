package com.fincity.nocode.kirun.engine.repository;

import com.fincity.nocode.kirun.engine.HybridRepository;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.function.system.math.MathFunctionRepository;
import com.fincity.nocode.kirun.engine.function.system.math.RandomRepository;
import com.fincity.nocode.kirun.engine.function.system.string.StringFunctionRepository;

public class KIRunFunctionRepository extends HybridRepository<Function> {

	public KIRunFunctionRepository() {
		super(new PackageScanningFunctionRepository("com.fincity.nocode.kirun.engine.function"),
		        new MathFunctionRepository(), new StringFunctionRepository(), new RandomRepository());
	}
}
