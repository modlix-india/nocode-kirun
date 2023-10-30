package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetStartOfTimestampTest  {

	 GetStartOfTimestamp getstart = new GetStartOfTimestamp();
	    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
	            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());
	   	    

@Test
void test() {

	Map<String, JsonElement> arguments = new HashMap<>();
    arguments.put("isodate", new JsonPrimitive("1994-10-24T02:10:30.700+05:00")); 
    arguments.put("year", new JsonPrimitive(true));
    arguments.put("month", new JsonPrimitive(true));
    arguments.put("day", new JsonPrimitive(true));
    arguments.put("hour", new JsonPrimitive(true));
    arguments.put("minute", new JsonPrimitive(true));
    arguments.put("second", new JsonPrimitive(true));
    arguments.put("milli",new JsonPrimitive(true));

      rfep.setArguments(arguments);
    StepVerifier.create(getstart.execute(rfep))
    .expectNextMatches(result -> {
        String startOfTimestamp = result.allResults().get(0).getResult().get("startoftimestamp").getAsString();
        return startOfTimestamp.equals("1994-10-24T02:40:30.700");
    })
    .verifyComplete();

}
}