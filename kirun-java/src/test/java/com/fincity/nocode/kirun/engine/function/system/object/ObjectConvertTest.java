package com.fincity.nocode.kirun.engine.function.system.object;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ObjectConvertTest {

	private final ReactiveRepository<Schema> mockSchemaRepo = new ReactiveRepository<>() {

		private final Schema booleanArraySchema = new Schema()
				.setName("BooleanArray")
				.setNamespace("testNamespace")
				.setType(Type.of(SchemaType.ARRAY))
				.setItems(ArraySchemaType.of(Schema.ofBoolean("boolean")));

		@Override
		public Mono<Schema> find(String namespace, String target) {
			if ("testNamespace".equals(namespace) && "BooleanArray".equals(target)) {
				return Mono.just(booleanArraySchema);
			}
			return Mono.empty();
		}

		@Override
		public Flux<String> filter(String name) {
			return Flux.empty();
		}
	};

	@Test
	void testBooleanArrayConversion() {
		JsonArray booleanArray = new JsonArray();
		booleanArray.add(new JsonPrimitive(true));
		booleanArray.add(new JsonPrimitive(false));
		booleanArray.add(new JsonPrimitive("yes"));
		booleanArray.add(new JsonPrimitive("no"));
		booleanArray.add(new JsonPrimitive("y"));
		booleanArray.add(new JsonPrimitive("n"));
		booleanArray.add(new JsonPrimitive(1));
		booleanArray.add(new JsonPrimitive(0));

		JsonArray expectedArray = new JsonArray();
		expectedArray.add(new JsonPrimitive(true));
		expectedArray.add(new JsonPrimitive(false));
		expectedArray.add(new JsonPrimitive(true));
		expectedArray.add(new JsonPrimitive(false));
		expectedArray.add(new JsonPrimitive(true));
		expectedArray.add(new JsonPrimitive(false));
		expectedArray.add(new JsonPrimitive(true));
		expectedArray.add(new JsonPrimitive(false));

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				mockSchemaRepo)
				.setArguments(Map.of(
						"source", booleanArray,
						"targetNamespace", new JsonPrimitive("testNamespace"),
						"target", new JsonPrimitive("BooleanArray"),
						"conversionMode", new JsonPrimitive(ConversionMode.STRICT.name())))
				.setContext(Map.of())
				.setSteps(Map.of());

		ObjectConvert oc = new ObjectConvert();

		StepVerifier.create(oc.execute(fep).map(e -> e.next().getResult().get("value")))
				.expectNext(expectedArray)
				.verifyComplete();
	}
}
