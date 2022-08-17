package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

class BinarySearchTest {

	@Test
	void test() {
		var src = new JsonArray();
		src.add(2);src.add(4);src.add(10);src.add(12);src.add(20);src.add(1233);
		
		var search =new JsonArray();
		search.add(20);
		
		FunctionExecutionParameters fep = new FunctionExecutionParameters().setArguments(Map.of(
				BinarySearch.PARAMETER_ARRAY_SOURCE.getParameterName(), src, BinarySearch.PARAMETER_INT_FIND_FROM.getParameterName() , new JsonPrimitive(1),
				BinarySearch.PARAMETER_ARRAY_FIND.getParameterName() , search , BinarySearch.PARAMETER_INT_LENGTH.getParameterName() , new JsonPrimitive(4)  
				));
		BinarySearch bs = new BinarySearch();
		
		assertEquals(bs.execute(fep).allResults().get(0).getResult().get(BinarySearch.EVENT_INDEX_NAME), new JsonPrimitive(4));
 	}

}
