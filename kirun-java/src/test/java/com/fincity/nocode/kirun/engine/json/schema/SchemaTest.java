package com.fincity.nocode.kirun.engine.json.schema;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

class SchemaTest {

	@Test
	void test() {

		var json = """
		      {
            "name" : "lead",
            "type" : [
                "OBJECT"
            ],
            "version" : 1,
            "properties" : {
                "name" : {
                    "type" : [
                        "STRING"
                    ],
                    "minLength" : 3
                },
                "mobileNumber" : {
                    "type" : [
                        "STRING"
                    ],
                    "minLength" : 3
                },
                "formType" : {
                    "type" : [
                        "STRING"
                    ],
                    "enums" : [
                        "LEAD_FORM",
                        "CONTACT_FORM"
                    ]
                },
                "email" : {
                    "type" : [
                        "STRING"
                    ]
                }
            }
        }
       
		        """;

		AdditionalTypeAdapter additional = new AdditionalTypeAdapter();
		ArraySchemaTypeAdapter arraySchema = new ArraySchemaTypeAdapter();

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .registerTypeAdapter(AdditionalType.class, additional)
		        .registerTypeAdapter(ArraySchemaType.class, arraySchema)
		        .create();

		additional.setGson(gson);
		arraySchema.setGson(gson);

		Schema sch = gson.fromJson(json, Schema.class);
		
		JsonObject element = new JsonObject();
		element.addProperty("name", "Kiran");
		element.addProperty("number", "12312312");
		
		ReactiveSchemaValidator.validate(null, sch, null, element);

		System.out.println(sch);
	}

}
