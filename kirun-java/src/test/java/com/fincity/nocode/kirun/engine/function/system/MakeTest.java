package com.fincity.nocode.kirun.engine.function.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class MakeTest {

	private Make make = new Make();

	private Map<String, Map<String, Map<String, JsonElement>>> createOutputMap(Map<String, JsonElement> data) {
		return Map.of("step1", Map.of("output", data));
	}

	@Test
	void testSimpleObjectWithExpression() {
		JsonObject source = new JsonObject();
		source.addProperty("name", "John");
		source.addProperty("age", 30);

		JsonObject resultShape = new JsonObject();
		resultShape.add("fullName", new JsonPrimitive("{{Steps.step1.output.source.name}}"));
		resultShape.add("userAge", new JsonPrimitive("{{Steps.step1.output.source.age}}"));

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("resultShape", resultShape))
				.setContext(Map.of())
				.setSteps(createOutputMap(Map.of("source", source)));

		StepVerifier.create(make.execute(fep)
				.map(output -> output.allResults().getFirst().getResult().get("value")))
				.expectNextMatches(result -> {
					assertNotNull(result);
					assertTrue(result.isJsonObject());
					JsonObject obj = result.getAsJsonObject();
					assertEquals("John", obj.get("fullName").getAsString());
					assertEquals(30, obj.get("userAge").getAsInt());
					return true;
				})
				.verifyComplete();
	}

	@Test
	void testNestedObjectWithExpressions() {
		JsonObject user = new JsonObject();
		user.addProperty("firstName", "John");
		user.addProperty("lastName", "Doe");

		JsonObject address = new JsonObject();
		address.addProperty("city", "NYC");
		address.addProperty("zip", "10001");

		JsonObject source = new JsonObject();
		source.add("user", user);
		source.add("address", address);

		JsonObject personShape = new JsonObject();
		personShape.add("name", new JsonPrimitive("{{Steps.step1.output.source.user.firstName}}"));
		personShape.add("surname", new JsonPrimitive("{{Steps.step1.output.source.user.lastName}}"));

		JsonObject locationShape = new JsonObject();
		locationShape.add("cityName", new JsonPrimitive("{{Steps.step1.output.source.address.city}}"));
		locationShape.add("postalCode", new JsonPrimitive("{{Steps.step1.output.source.address.zip}}"));

		JsonObject resultShape = new JsonObject();
		resultShape.add("person", personShape);
		resultShape.add("location", locationShape);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("resultShape", resultShape))
				.setContext(Map.of())
				.setSteps(createOutputMap(Map.of("source", source)));

		StepVerifier.create(make.execute(fep)
				.map(output -> output.allResults().getFirst().getResult().get("value")))
				.expectNextMatches(result -> {
					assertNotNull(result);
					assertTrue(result.isJsonObject());
					JsonObject obj = result.getAsJsonObject();
					assertEquals("John", obj.getAsJsonObject("person").get("name").getAsString());
					assertEquals("Doe", obj.getAsJsonObject("person").get("surname").getAsString());
					assertEquals("NYC", obj.getAsJsonObject("location").get("cityName").getAsString());
					assertEquals("10001", obj.getAsJsonObject("location").get("postalCode").getAsString());
					return true;
				})
				.verifyComplete();
	}

	@Test
	void testArrayWithExpressions() {
		JsonArray items = new JsonArray();
		items.add("apple");
		items.add("banana");
		items.add("cherry");

		JsonObject source = new JsonObject();
		source.add("items", items);

		JsonArray fruitsShape = new JsonArray();
		fruitsShape.add(new JsonPrimitive("{{Steps.step1.output.source.items[0]}}"));
		fruitsShape.add(new JsonPrimitive("{{Steps.step1.output.source.items[1]}}"));
		fruitsShape.add(new JsonPrimitive("{{Steps.step1.output.source.items[2]}}"));

		JsonObject resultShape = new JsonObject();
		resultShape.add("fruits", fruitsShape);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("resultShape", resultShape))
				.setContext(Map.of())
				.setSteps(createOutputMap(Map.of("source", source)));

		StepVerifier.create(make.execute(fep)
				.map(output -> output.allResults().getFirst().getResult().get("value")))
				.expectNextMatches(result -> {
					assertNotNull(result);
					assertTrue(result.isJsonObject());
					JsonArray fruits = result.getAsJsonObject().getAsJsonArray("fruits");
					assertEquals("apple", fruits.get(0).getAsString());
					assertEquals("banana", fruits.get(1).getAsString());
					assertEquals("cherry", fruits.get(2).getAsString());
					return true;
				})
				.verifyComplete();
	}

	@Test
	void testDeeplyNestedStructureWithArrays() {
		JsonObject user1 = new JsonObject();
		user1.addProperty("id", 1);
		user1.addProperty("name", "Alice");

		JsonObject user2 = new JsonObject();
		user2.addProperty("id", 2);
		user2.addProperty("name", "Bob");

		JsonArray users = new JsonArray();
		users.add(user1);
		users.add(user2);

		JsonObject data = new JsonObject();
		data.add("users", users);

		JsonObject source = new JsonObject();
		source.add("data", data);

		JsonObject userShape1 = new JsonObject();
		userShape1.add("userId", new JsonPrimitive("{{Steps.step1.output.source.data.users[0].id}}"));
		userShape1.add("userName", new JsonPrimitive("{{Steps.step1.output.source.data.users[0].name}}"));

		JsonObject userShape2 = new JsonObject();
		userShape2.add("userId", new JsonPrimitive("{{Steps.step1.output.source.data.users[1].id}}"));
		userShape2.add("userName", new JsonPrimitive("{{Steps.step1.output.source.data.users[1].name}}"));

		JsonArray userListShape = new JsonArray();
		userListShape.add(userShape1);
		userListShape.add(userShape2);

		JsonObject level3 = new JsonObject();
		level3.add("userList", userListShape);

		JsonObject level2 = new JsonObject();
		level2.add("level3", level3);

		JsonObject level1 = new JsonObject();
		level1.add("level2", level2);

		JsonObject resultShape = new JsonObject();
		resultShape.add("level1", level1);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("resultShape", resultShape))
				.setContext(Map.of())
				.setSteps(createOutputMap(Map.of("source", source)));

		StepVerifier.create(make.execute(fep)
				.map(output -> output.allResults().getFirst().getResult().get("value")))
				.expectNextMatches(result -> {
					assertNotNull(result);
					JsonArray userList = result.getAsJsonObject()
							.getAsJsonObject("level1")
							.getAsJsonObject("level2")
							.getAsJsonObject("level3")
							.getAsJsonArray("userList");
					assertEquals(1, userList.get(0).getAsJsonObject().get("userId").getAsInt());
					assertEquals("Alice", userList.get(0).getAsJsonObject().get("userName").getAsString());
					assertEquals(2, userList.get(1).getAsJsonObject().get("userId").getAsInt());
					assertEquals("Bob", userList.get(1).getAsJsonObject().get("userName").getAsString());
					return true;
				})
				.verifyComplete();
	}

	@Test
	void testMixedStaticAndDynamicValues() {
		JsonObject source = new JsonObject();
		source.addProperty("dynamicValue", "from source");

		JsonObject nestedShape = new JsonObject();
		nestedShape.addProperty("staticNum", 42);
		nestedShape.add("dynamicNum", new JsonPrimitive("{{Steps.step1.output.source.dynamicValue}}"));

		JsonArray arrayShape = new JsonArray();
		arrayShape.add("static");
		arrayShape.add(new JsonPrimitive("{{Steps.step1.output.source.dynamicValue}}"));
		arrayShape.add(123);

		JsonObject resultShape = new JsonObject();
		resultShape.addProperty("static", "static string");
		resultShape.add("dynamic", new JsonPrimitive("{{Steps.step1.output.source.dynamicValue}}"));
		resultShape.add("nested", nestedShape);
		resultShape.add("array", arrayShape);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("resultShape", resultShape))
				.setContext(Map.of())
				.setSteps(createOutputMap(Map.of("source", source)));

		StepVerifier.create(make.execute(fep)
				.map(output -> output.allResults().getFirst().getResult().get("value")))
				.expectNextMatches(result -> {
					assertNotNull(result);
					JsonObject obj = result.getAsJsonObject();
					assertEquals("static string", obj.get("static").getAsString());
					assertEquals("from source", obj.get("dynamic").getAsString());
					assertEquals(42, obj.getAsJsonObject("nested").get("staticNum").getAsInt());
					assertEquals("from source", obj.getAsJsonObject("nested").get("dynamicNum").getAsString());
					JsonArray arr = obj.getAsJsonArray("array");
					assertEquals("static", arr.get(0).getAsString());
					assertEquals("from source", arr.get(1).getAsString());
					assertEquals(123, arr.get(2).getAsInt());
					return true;
				})
				.verifyComplete();
	}

	@Test
	void testNullHandling() {
		JsonObject nestedShape = new JsonObject();
		nestedShape.add("inner", JsonNull.INSTANCE);

		JsonObject resultShape = new JsonObject();
		resultShape.add("nullValue", JsonNull.INSTANCE);
		resultShape.add("nested", nestedShape);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("resultShape", resultShape))
				.setContext(Map.of())
				.setSteps(Map.of());

		StepVerifier.create(make.execute(fep)
				.map(output -> output.allResults().getFirst().getResult().get("value")))
				.expectNextMatches(result -> {
					assertNotNull(result);
					JsonObject obj = result.getAsJsonObject();
					assertTrue(obj.get("nullValue").isJsonNull());
					assertTrue(obj.getAsJsonObject("nested").get("inner").isJsonNull());
					return true;
				})
				.verifyComplete();
	}

	@Test
	void testArrayOfArrays() {
		JsonArray row1 = new JsonArray();
		row1.add(1);
		row1.add(2);

		JsonArray row2 = new JsonArray();
		row2.add(3);
		row2.add(4);

		JsonArray matrix = new JsonArray();
		matrix.add(row1);
		matrix.add(row2);

		JsonObject source = new JsonObject();
		source.add("matrix", matrix);

		JsonArray gridRow1 = new JsonArray();
		gridRow1.add(new JsonPrimitive("{{Steps.step1.output.source.matrix[0][0]}}"));
		gridRow1.add(new JsonPrimitive("{{Steps.step1.output.source.matrix[0][1]}}"));

		JsonArray gridRow2 = new JsonArray();
		gridRow2.add(new JsonPrimitive("{{Steps.step1.output.source.matrix[1][0]}}"));
		gridRow2.add(new JsonPrimitive("{{Steps.step1.output.source.matrix[1][1]}}"));

		JsonArray gridShape = new JsonArray();
		gridShape.add(gridRow1);
		gridShape.add(gridRow2);

		JsonObject resultShape = new JsonObject();
		resultShape.add("grid", gridShape);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("resultShape", resultShape))
				.setContext(Map.of())
				.setSteps(createOutputMap(Map.of("source", source)));

		StepVerifier.create(make.execute(fep)
				.map(output -> output.allResults().getFirst().getResult().get("value")))
				.expectNextMatches(result -> {
					assertNotNull(result);
					JsonArray grid = result.getAsJsonObject().getAsJsonArray("grid");
					assertEquals(1, grid.get(0).getAsJsonArray().get(0).getAsInt());
					assertEquals(2, grid.get(0).getAsJsonArray().get(1).getAsInt());
					assertEquals(3, grid.get(1).getAsJsonArray().get(0).getAsInt());
					assertEquals(4, grid.get(1).getAsJsonArray().get(1).getAsInt());
					return true;
				})
				.verifyComplete();
	}

	@Test
	void testPrimitiveResultShape() {
		JsonObject source = new JsonObject();
		source.addProperty("value", "hello");

		JsonPrimitive resultShape = new JsonPrimitive("{{Steps.step1.output.source.value}}");

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("resultShape", resultShape))
				.setContext(Map.of())
				.setSteps(createOutputMap(Map.of("source", source)));

		StepVerifier.create(make.execute(fep)
				.map(output -> output.allResults().getFirst().getResult().get("value")))
				.expectNextMatches(result -> {
					assertEquals("hello", result.getAsString());
					return true;
				})
				.verifyComplete();
	}

	@Test
	void testArrayAsRootResultShape() {
		JsonObject source = new JsonObject();
		source.addProperty("a", 1);
		source.addProperty("b", 2);
		source.addProperty("c", 3);

		JsonArray resultShape = new JsonArray();
		resultShape.add(new JsonPrimitive("{{Steps.step1.output.source.a}}"));
		resultShape.add(new JsonPrimitive("{{Steps.step1.output.source.b}}"));
		resultShape.add(new JsonPrimitive("{{Steps.step1.output.source.c}}"));

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("resultShape", resultShape))
				.setContext(Map.of())
				.setSteps(createOutputMap(Map.of("source", source)));

		StepVerifier.create(make.execute(fep)
				.map(output -> output.allResults().getFirst().getResult().get("value")))
				.expectNextMatches(result -> {
					assertTrue(result.isJsonArray());
					JsonArray arr = result.getAsJsonArray();
					assertEquals(1, arr.get(0).getAsInt());
					assertEquals(2, arr.get(1).getAsInt());
					assertEquals(3, arr.get(2).getAsInt());
					return true;
				})
				.verifyComplete();
	}
}
