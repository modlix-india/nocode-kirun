package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.google.gson.JsonObject;

class SchemaPatternPropertiesTest {

    @Test
    void test() {

        Map<String, Schema> props = new HashMap<>();

        props.put("name", Schema.ofString("name").setMinLength(3));
        props.put("age", Schema.ofInteger("age"));

        Map<String, Schema> patProps = new HashMap<>();
        patProps.put("^g([a-z]|[A-Z]|[\\d])*$", Schema.ofBoolean("pat1"));
        patProps.put("^c([a-z]|[A-Z]|[\\d])*$", Schema.ofString("pat2"));

        Schema objSchema = Schema.ofObject("checkingPattern").setProperties(props).setPatternProperties(patProps)
                .setAdditionalProperties(new AdditionalPropertiesType().setBooleanValue(false));

        JsonObject job = new JsonObject();
        job.addProperty("name", "xyz");
        job.addProperty("age", 24);

        // adding pattern props
        job.addProperty("gender", true);
        job.addProperty("city", "Yanam");
//        job.addProperty("prop", "dont add this");

        assertEquals(job, SchemaValidator.validate(null, objSchema, null, job));
    }

}
