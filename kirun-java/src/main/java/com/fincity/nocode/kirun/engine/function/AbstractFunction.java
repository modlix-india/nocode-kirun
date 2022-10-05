package com.fincity.nocode.kirun.engine.function;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public abstract class AbstractFunction implements Function {

	protected Map<String, JsonElement> validateArguments(final Map<String, JsonElement> arguments, Repository<Schema> repository) {

		return this.getSignature()
		        .getParameters()
		        .entrySet()
		        .stream()
		        .map(e ->
			        {
				        Parameter param = e.getValue();
				        JsonElement jsonElement = arguments.get(e.getKey());

				        if ((jsonElement == null || jsonElement.isJsonNull()) && !param.isVariableArgument()) {
					        return Map.entry(e.getKey(), SchemaValidator.validate(null, param.getSchema(), repository, null));
				        }

				        if (!param.isVariableArgument())
					        return Map.entry(e.getKey(),
					                SchemaValidator.validate(null, param.getSchema(), repository, jsonElement));

				        JsonArray array = null;

				        if (jsonElement.isJsonArray())
					        array = jsonElement.getAsJsonArray();
				        else {
					        array = new JsonArray();
					        if (jsonElement != null && !jsonElement.isJsonNull()) array.add(jsonElement);
		                    else if (param.getSchema().getDefaultValue() != null)
		                        array.add(param.getSchema().getDefaultValue());
				        }

				        for (int i=0; i<array.size();i++) {
					        JsonElement je = SchemaValidator.validate(null, param.getSchema(), repository, array.get(i));
					        array.set(i, je);
				        }

				        return Map.entry(e.getKey(), array);
			        })
		        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public FunctionOutput execute(FunctionExecutionParameters context) {
		
		context.setArguments(this.validateArguments(context.getArguments(), context.getSchemaRepository()));

		return this.internalExecute(context);
	}

	protected abstract FunctionOutput internalExecute(FunctionExecutionParameters context);

	@Override
	public Map<String, Event> getProbableEventSignature(Map<String, List<Schema>> probableParameters) {
		return this.getSignature()
		        .getEvents();
	}
}
