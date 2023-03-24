package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.HybridRepository;
import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.google.gson.JsonObject;

class SchemaRefValidatorTest {

    @Test
    void schemaValidatorForArray() {

        Map<String, Schema> detailsMap = new HashMap<>();
        detailsMap.put("firstName", Schema.ofString("firstName"));
        detailsMap.put("lastName", Schema.ofString("lastName"));
        Schema basic = new Schema().ofObject("basicDetails").setNamespace("Model").setName("BasicDetails")
                .setProperties(detailsMap);

        JsonObject realDetails = new JsonObject();
        realDetails.addProperty("firstName", "sruendhar");
        realDetails.addProperty("lastName", "sruendhar");

        assertEquals(realDetails, SchemaValidator.validate(null, basic, null, realDetails));
    }

    @Test
    void schemaValidatorTestForRefOfRef() {
        var locationMap = new HashMap<String, Schema>();
        var schemaMap = new HashMap<String, Schema>();
        locationMap.put("url", Schema.ofString("url"));
        var locationSchema = Schema.ofObject("Location").setNamespace("Test").setProperties(locationMap);
        var urlParamsSchema = Schema.ofObject("UrlParameters").setNamespace("Test")
                .setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(Schema.ofRef("Test.Location")));
        var testSchema = Schema.ofObject("TestSchema").setNamespace("Test")
                .setAdditionalProperties(
                        new AdditionalPropertiesType().setSchemaValue(Schema.ofRef("Test.UrlParameters")));
        schemaMap.put("Location", locationSchema);
        schemaMap.put("UrlParameters", urlParamsSchema);
        schemaMap.put("TestSchema", testSchema);
        class TestRepository implements Repository<Schema> {

            @Override
            public Schema find(String namespace, String name) {
                if (namespace == null) {
                    return null;
                }
                return schemaMap.get(name);
            }

        }
        var repo = new HybridRepository<Schema>(new TestRepository(), new KIRunSchemaRepository());
        var urlParams = new JsonObject();
        var testValue = new JsonObject();
        var location = new JsonObject();
        location.addProperty("url", "http://test/");
        urlParams.add("obj", location);
        testValue.add("obj", urlParams);

        assertEquals(SchemaValidator.validate(null, Schema.ofRef("Test.TestSchema"), repo, testValue), testValue);

    }
}
