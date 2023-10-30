package com.fincity.nocode.kirun.engine.function.system.date;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetTimeAsObjectTest {


	GetTimeAsObject gettimeobject = new GetTimeAsObject();
	ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
			new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

	
	 @Test
	    public void test() {
	       

					fep.setArguments(Map.of("date", new JsonPrimitive("2023-09-07T17:35:17.000Z")));
			
							
					StepVerifier.create(gettimeobject.execute(fep))
					.expectNextMatches(
	                        r -> {
	                            JsonObject timeObject = r.allResults().get(0).getResult().get("output").getAsJsonObject();
	                            assertEquals(17, timeObject.get("hour").getAsInt());
	                            assertEquals(35, timeObject.get("minute").getAsInt());
	                            assertEquals(17, timeObject.get("second").getAsInt());
	                            
	                        return true;
	                        			      
})
					.expectComplete()
					.verify();
}

	 @Test
	    public void test1() {
	       

					fep.setArguments(Map.of("date", new JsonPrimitive("2023-09-07T12:25:10.112+11:00")));
			
							
					StepVerifier.create(gettimeobject.execute(fep))
					.expectNextMatches(
	                        r -> {
	                            JsonObject timeObject = r.allResults().get(0).getResult().get("output").getAsJsonObject();
	                            assertEquals(12, timeObject.get("hour").getAsInt());
	                            assertEquals(25, timeObject.get("minute").getAsInt());
	                            assertEquals(10, timeObject.get("second").getAsInt());
	                            
	                        return true;
	                        			      
})
					.expectComplete()
					.verify();
}

	
}
