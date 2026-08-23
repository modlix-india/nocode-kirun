package com.fincity.nocode.kirun.engine.runtime.suspend;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterReference;
import com.fincity.nocode.kirun.engine.model.ParameterReferenceType;
import com.fincity.nocode.kirun.engine.model.Statement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Function definitions used by the stop-and-go tests, kept out of the tests themselves so each test
 * reads as the behaviour it is checking rather than as a wall of graph construction.
 */
final class SuspendTestDefinitions {

	private SuspendTestDefinitions() {
	}

	// -------------------------------------------------------------------------------------------
	// Definitions
	// -------------------------------------------------------------------------------------------

	/** first -> wait -> afterWait -> output. The plain "stop in the middle" shape. */
	static FunctionDefinition threeStepsWithWaitInTheMiddle() {

		Statement first = concatenate("first", value("first"));

		Statement wait = waitUntil("wait", 60_000).setDependentStatements(Map.of("Steps.first.output", true));

		Statement afterWait = concatenate("afterWait", expression("Steps.first.output.value", 0),
		        value("-then-after", 1)).setDependentStatements(Map.of("Steps.wait.output", true));

		return definition("StopsInTheMiddle", first, wait, afterWait,
		        generateOutput("outputStep", "Steps.afterWait.output.value", "Steps.afterWait.output"));
	}

	/** wait -> echo whatever the resume payload carried. */
	static FunctionDefinition waitThenEchoThePayload() {

		Statement wait = waitUntil("wait", 60_000);

		return definition("EchoesThePayload", wait,
		        generateOutput("outputStep", "Steps.wait.output.token", "Steps.wait.output"));
	}

	/** wait, then echo a function argument - so the definition has a required parameter. */
	static FunctionDefinition waitThenEchoAnArgument() {

		Statement wait = waitUntil("wait", 60_000);

		FunctionDefinition fd = definition("EchoesAnArgument", wait,
		        generateOutput("outputStep", "Arguments.who", "Steps.wait.output"));

		return (FunctionDefinition) fd.setParameters(Map.of("who", new Parameter().setParameterName("who")
		        .setSchema(Schema.ofString("who"))));
	}

	/** A signal wait with both ways out wired to different outputs. */
	static FunctionDefinition signalWaitWithTimeoutBranch() {

		Statement wait = waitForSignal("approval", "manager-approved", 86_400_000);

		Statement approved = generateOutputValue("approved", "approved", "Steps.approval.output");
		Statement expired = generateOutputValue("expired", "expired", "Steps.approval.timeout");

		return definition("ApprovalGate", wait, approved, expired);
	}

	/** Raises a non-output event, then stops - so the resume has earlier history to preserve. */
	static FunctionDefinition raisesAProgressEventThenWaits() {

		Statement progress = generateNamedEvent("progress", "progress", "started", null);

		Statement wait = waitUntil("wait", 60_000).setDependentStatements(Map.of("Steps.progress.output", true));

		return definition("ReportsProgress", progress, wait,
		        generateOutputValue("done", "finished", "Steps.wait.output"));
	}

	/** Two waits with nothing ordering them, so both get dispatched in the same pass. */
	static FunctionDefinition twoIndependentWaits() {

		return definition("TwoWaits", waitUntil("waitA", 60_000), waitUntil("waitB", 60_000));
	}

	// -------------------------------------------------------------------------------------------
	// Builders
	// -------------------------------------------------------------------------------------------

	static Statement waitUntil(String name, long durationMillis) {

		return new Statement(name).setNamespace("System")
		        .setName("WaitUntil")
		        .setParameterMap(
		                Map.of("durationMillis", Map.ofEntries(ParameterReference.of(new JsonPrimitive(durationMillis)))));
	}

	static Statement waitForSignal(String name, String signalName, long timeoutMillis) {

		return new Statement(name).setNamespace("System")
		        .setName("WaitForSignal")
		        .setParameterMap(Map.of("signalName",
		                Map.ofEntries(ParameterReference.of(new JsonPrimitive(signalName))), "timeoutMillis",
		                Map.ofEntries(ParameterReference.of(new JsonPrimitive(timeoutMillis)))));
	}

	static Statement concatenate(String name, Map.Entry<String, ParameterReference>... values) {

		Map<String, ParameterReference> refs = new LinkedHashMap<>();
		for (Map.Entry<String, ParameterReference> v : values)
			refs.put(v.getKey(), v.getValue());

		return new Statement(name).setNamespace("System.String")
		        .setName("Concatenate")
		        .setParameterMap(Map.of("value", refs));
	}

	/** A GenerateEvent step whose output value comes from an expression. */
	static Statement generateOutput(String name, String valueExpression, String dependsOn) {

		JsonObject expressionValue = new JsonObject();
		expressionValue.addProperty("isExpression", true);
		expressionValue.addProperty("value", valueExpression);

		return generateEvent(name, expressionValue, dependsOn);
	}

	/** A GenerateEvent step whose output value is a fixed string. */
	static Statement generateOutputValue(String name, String literal, String dependsOn) {

		JsonObject fixedValue = new JsonObject();
		fixedValue.addProperty("isExpression", false);
		fixedValue.addProperty("value", literal);

		return generateEvent(name, fixedValue, dependsOn);
	}

	/** A GenerateEvent step raising an event other than output. */
	static Statement generateNamedEvent(String name, String eventName, String literal, String dependsOn) {

		JsonObject fixedValue = new JsonObject();
		fixedValue.addProperty("isExpression", false);
		fixedValue.addProperty("value", literal);

		JsonObject result = new JsonObject();
		result.add("name", new JsonPrimitive("value"));
		result.add("value", fixedValue);

		Statement s = new Statement(name).setNamespace("System")
		        .setName("GenerateEvent")
		        .setParameterMap(Map.of("eventName", Map.ofEntries(ParameterReference.of(new JsonPrimitive(eventName))),
		                "results", Map.ofEntries(ParameterReference.of(result))));

		return dependsOn == null ? s : s.setDependentStatements(Map.of(dependsOn, true));
	}

	private static Statement generateEvent(String name, JsonObject value, String dependsOn) {

		JsonObject result = new JsonObject();
		result.add("name", new JsonPrimitive("value"));
		result.add("value", value);

		Statement s = new Statement(name).setNamespace("System")
		        .setName("GenerateEvent")
		        .setParameterMap(Map.of("eventName", Map.ofEntries(ParameterReference.of(new JsonPrimitive("output"))),
		                "results", Map.ofEntries(ParameterReference.of(result))));

		return dependsOn == null ? s : s.setDependentStatements(Map.of(dependsOn, true));
	}

	static Map.Entry<String, ParameterReference> value(String literal) {
		return value(literal, 0);
	}

	static Map.Entry<String, ParameterReference> value(String literal, int order) {

		ParameterReference ref = new ParameterReference().setType(ParameterReferenceType.VALUE)
		        .setValue(new JsonPrimitive(literal))
		        .setOrder(order);

		return Map.entry(ref.getKey(), ref);
	}

	static Map.Entry<String, ParameterReference> expression(String expression, int order) {

		ParameterReference ref = new ParameterReference().setType(ParameterReferenceType.EXPRESSION)
		        .setExpression(expression)
		        .setOrder(order);

		return Map.entry(ref.getKey(), ref);
	}

	static FunctionDefinition definition(String name, Statement... steps) {

		Map<String, Statement> stepMap = new HashMap<>();
		for (Statement s : steps)
			stepMap.put(s.getStatementName(), s);

		return (FunctionDefinition) new FunctionDefinition().setSteps(stepMap)
		        .setNamespace("Test")
		        .setName(name)
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of("value", Schema.ofString("value")))));
	}

	// -------------------------------------------------------------------------------------------
	// Assertions helper
	// -------------------------------------------------------------------------------------------

	/** Drains a function output looking for one event by name. */
	static EventResult eventNamed(FunctionOutput output, String eventName) {

		if (output == null)
			return null;

		EventResult er;
		while ((er = output.next()) != null)
			if (eventName.equals(er.getName()))
				return er;

		return null;
	}
}
