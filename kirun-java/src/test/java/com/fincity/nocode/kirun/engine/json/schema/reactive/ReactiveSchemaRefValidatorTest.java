package com.fincity.nocode.kirun.engine.json.schema.reactive;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.fincity.nocode.kirun.engine.reactive.ReactiveHybridRepository;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.google.gson.JsonObject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveSchemaRefValidatorTest {

	@Test
	void schemaValidatorForArray() {

		Map<String, Schema> detailsMap = new HashMap<>();
		detailsMap.put("firstName", Schema.ofString("firstName"));
		detailsMap.put("lastName", Schema.ofString("lastName"));
		Schema basic = Schema.ofObject("basicDetails")
		        .setNamespace("Model")
		        .setName("BasicDetails")
		        .setProperties(detailsMap);

		JsonObject realDetails = new JsonObject();
		realDetails.addProperty("firstName", "sruendhar");
		realDetails.addProperty("lastName", "sruendhar");

		StepVerifier
		        .create(ReactiveSchemaValidator.validate(null, basic, new KIRunReactiveSchemaRepository(), realDetails))
		        .expectNext(realDetails)
		        .verifyComplete();
	}

	@Test
	void schemaValidatorTestForRefOfRef() {
		var locationMap = new HashMap<String, Schema>();
		var schemaMap = new HashMap<String, Schema>();
		locationMap.put("url", Schema.ofString("url"));
		var locationSchema = Schema.ofObject("Location")
		        .setNamespace("Test")
		        .setProperties(locationMap);
		var urlParamsSchema = Schema.ofObject("UrlParameters")
		        .setNamespace("Test")
		        .setAdditionalProperties(new AdditionalType().setSchemaValue(Schema.ofRef("Test.Location")));
		var testSchema = Schema.ofObject("TestSchema")
		        .setNamespace("Test")
		        .setAdditionalProperties(new AdditionalType().setSchemaValue(Schema.ofRef("Test.UrlParameters")));
		schemaMap.put("Location", locationSchema);
		schemaMap.put("UrlParameters", urlParamsSchema);
		schemaMap.put("TestSchema", testSchema);
		class TestRepository implements ReactiveRepository<Schema> {

			@Override
			public Mono<Schema> find(String namespace, String name) {
				if (namespace == null) {
					return Mono.empty();
				}
				return Mono.just(schemaMap.get(name));
			}

			@Override
			public Flux<String> filter(String name) {

				return Flux.fromIterable(schemaMap.values())
				        .map(Schema::getFullName)
				        .filter(e -> e.toLowerCase()
				                .contains(name.toLowerCase()));
			}
		}

		var repo = new ReactiveHybridRepository<>(new TestRepository(), new KIRunReactiveSchemaRepository());
		var urlParams = new JsonObject();
		var testValue = new JsonObject();
		var location = new JsonObject();
		location.addProperty("url", "http://test/");
		urlParams.add("obj", location);
		testValue.add("obj", urlParams);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, Schema.ofRef("Test.TestSchema"), repo, testValue))
		        .expectNext(testValue)
		        .verifyComplete();

	}
}
