package com.fincity.nocode.kirun.engine.function.system.math;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;

import reactor.test.StepVerifier;

public class RandomTest {

	@Test
	void test1() {

		Random ran = new Random();
		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());
				

		  StepVerifier.create(ran.execute(fep))
          .assertNext(output -> {
              double value = output.next().getResult().get("value").getAsDouble();
             
          })
          .verifyComplete();
	}
}
