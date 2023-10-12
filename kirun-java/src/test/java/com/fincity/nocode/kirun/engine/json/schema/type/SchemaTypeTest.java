package com.fincity.nocode.kirun.engine.json.schema.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.Event;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class SchemaTypeTest {

	private static Gson gson;

	@BeforeAll
	static void makeGsonBuilder() {
		ArraySchemaTypeAdapter arraySchemaTypeAdapter = new ArraySchemaTypeAdapter();

		AdditionalTypeAdapter additionalTypeAdapter = new AdditionalTypeAdapter();

		gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .registerTypeAdapter(AdditionalType.class, additionalTypeAdapter)
		        .registerTypeAdapter(ArraySchemaType.class, arraySchemaTypeAdapter)
		        .create();

		arraySchemaTypeAdapter.setGson(gson);
		additionalTypeAdapter.setGson(gson);
	}

	@Test
	void test() {

		var schemaMap = Map.of("type", List.of("INTEGER"), "version", 2);

		Schema sch = gson.fromJson(gson.toJsonTree(schemaMap), Schema.class);

		assertEquals(new MultipleType().setType(Set.of(SchemaType.INTEGER)), sch.getType());
	}

	@Test
	void bigTest() {

		

		String eventDef = """
		          {

		                "name" : "output",
		                "parameters" : {
		                    "returnValue" : {

		                            "type" : [
		                                "INTEGER"
		                            ],
		                            "version" : 1

		                    }
		                }
		        }
		          """;

		Event fd = gson.fromJson(eventDef, Event.class);
		assertEquals(new MultipleType().setType(Set.of(SchemaType.INTEGER)), fd.getParameters().get("returnValue").getType());
	}

}
