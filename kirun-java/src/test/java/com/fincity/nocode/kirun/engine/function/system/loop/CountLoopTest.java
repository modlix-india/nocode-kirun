package com.fincity.nocode.kirun.engine.function.system.loop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class CountLoopTest {

	@Test
	void test() {

		var loop = new CountLoop();
		
		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of());
		
		assertThrows(KIRuntimeException.class, () -> loop.execute(fep));
		
		FunctionExecutionParameters fep1 = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("count", new JsonPrimitive(10)));
		
		FunctionOutput fo = loop.execute(fep1);
		int i = 0;
		EventResult er;
		for (er = fo.next(); er.getName().equals("iteration");) {
			assertEquals(i++, er.getResult().get("index").getAsInt());
			er = fo.next();
		}
		
		assertEquals("output", er.getName());
		assertEquals(10, er.getResult().get("value").getAsInt());
		
		fep1 = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("count", new JsonPrimitive(0)));
		
		fo = loop.execute(fep1);
		i = 0;
		
		for (er = fo.next(); er.getName().equals("iteration");) {
			assertEquals(i++, er.getResult().get("index").getAsInt());
			er = fo.next();
		}
		
		assertEquals("output", er.getName());
		assertEquals(0, er.getResult().get("value").getAsInt());
		
		fep1 = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("count", new JsonPrimitive(-1)));
		
		fo = loop.execute(fep1);
		i = 0;
		
		for (er = fo.next(); er.getName().equals("iteration");) {
			assertEquals(i++, er.getResult().get("index").getAsInt());
			er = fo.next();
		}
		
		assertEquals("output", er.getName());
		assertEquals(-1, er.getResult().get("value").getAsInt());


//		StepVerifier
//		        .create(loop.execute(
//		                new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(CountLoop.COUNT, new JsonPrimitive(6)))))
//		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(1))))
//		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(2))))
//		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(3))))
//		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(4))))
//		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(5))))
//		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(6))))
//		        .expectNext(EventResult.outputOf(Map.of(RangeLoop.VALUE, new JsonPrimitive(6))))
//		        .expectComplete()
//		        .verify();
	}

}
