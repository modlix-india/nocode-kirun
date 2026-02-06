package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class ObjectValueSetterExtractorTest {

    @Test
    void ObjectValueSetterExtractorTest1() {

        Gson gson = new Gson();

        var store = gson.fromJson(
                """
                        {"name":"Kiran","addresses":[{"city":"Bangalore","state":"Karnataka","country":"India"},{"city":"Kakinada","state":"Andhra Pradesh","country":"India"},{"city":"Beaverton","state":"Oregon"}],"phone":{"home":"080-23456789","office":"080-23456789","mobile":"080-23456789","mobile2":"503-23456789"},"plain":[1,2,3,4]}
                        """,
                JsonObject.class);

        ObjectValueSetterExtractor objExtractor = new ObjectValueSetterExtractor(store, "Store");

        assertEquals(objExtractor.getValue("Store.name"), new JsonPrimitive("Kiran"));

        objExtractor.setValue("Store.name", new JsonPrimitive("Surendhar"), null, null);

        assertEquals(objExtractor.getValue("Store.name"), new JsonPrimitive("Surendhar"));

        JsonObject newStore = objExtractor.getStore().getAsJsonObject();

        assertEquals(newStore.get("name"), new JsonPrimitive("Surendhar"));

        objExtractor.setValue("Store.addresses[0].city", new JsonPrimitive("Bengaluru"), null, null);

        assertEquals(objExtractor.getValue("Store.addresses[0].city"), new JsonPrimitive("Bengaluru"));

        // Test creating a new array when path has array access operator
        objExtractor.setValue("Store.otherAddress[1].country", new JsonPrimitive("USA"), null, null);
        assertEquals(objExtractor.getValue("Store.otherAddress[1].country"), new JsonPrimitive("USA"));

        // Test creating a new object when path has object access
        objExtractor.setValue("Store.otherSingleAddress.country", new JsonPrimitive("USA"), null, null);
        assertEquals(objExtractor.getValue("Store.otherSingleAddress.country"), new JsonPrimitive("USA"));

        objExtractor.setValue("Store.plain[0]", new JsonPrimitive("123"), null, null);

        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive("123"));
        arr.add(new JsonPrimitive(2));
        arr.add(new JsonPrimitive(3));
        arr.add(new JsonPrimitive(4));

        assertEquals(objExtractor.getValue("Store.plain"), arr);

        objExtractor.setValue("Store.plain[0]", new JsonPrimitive(1), false, null);

        assertEquals(objExtractor.getValue("Store.plain[0]"), arr.get(0));

        assertEquals(objExtractor.getValue("Store.plain"), arr);

        objExtractor.setValue("Store.plain", null, true, true);

        var storeObject = objExtractor.getValue("Store");

        Set<String> keys = objExtractor.getValue("Store").isJsonObject() ? storeObject.getAsJsonObject().keySet()
                : null;

        Set<String> expectedKeys = Set.of("name", "addresses", "phone", "otherAddress", "otherSingleAddress");

        assertEquals(keys, expectedKeys);

        objExtractor.setValue("Store.plain", new JsonPrimitive("plainString"), false, false);

        assertEquals(objExtractor.getValue("Store.plain"), new JsonPrimitive("plainString"));

    }

    @Test
    void setNewKeyInObjectTest() {

        Gson gson = new Gson();

        var store = gson.fromJson("""
                {}
                """, JsonObject.class);
        

        ObjectValueSetterExtractor objExtractor = new ObjectValueSetterExtractor(store, "Obj");


        objExtractor.setValue("Obj.plain", new JsonPrimitive("plainString"), false, false);

        assertEquals(objExtractor.getValue("Obj.plain"), new JsonPrimitive("plainString"));


    }

    @Test
    void setEmptyObjectThenSetNestedPropertyWithNumericKey() {

        JsonObject store = new JsonObject();

        ObjectValueSetterExtractor objExtractor = new ObjectValueSetterExtractor(store, "Store");

        objExtractor.setValue("Store.x", new JsonObject(), null, null);
        assertEquals(objExtractor.getValue("Store.x"), new JsonObject());

        objExtractor.setValue("Store.x.1", new JsonPrimitive("kiran"), null, null);
        assertEquals(objExtractor.getValue("Store.x.1"), new JsonPrimitive("kiran"));

        JsonObject expected = new JsonObject();
        expected.addProperty("1", "kiran");
        assertEquals(objExtractor.getValue("Store.x"), expected);
    }

    @Test
    void setEmptyArrayThenSetElementWithNumericIndex() {

        JsonObject store = new JsonObject();

        ObjectValueSetterExtractor objExtractor = new ObjectValueSetterExtractor(store, "Store");

        objExtractor.setValue("Store.arr", new JsonArray(), null, null);
        assertEquals(objExtractor.getValue("Store.arr"), new JsonArray());

        objExtractor.setValue("Store.arr.1", new JsonPrimitive("value"), null, null);
        assertEquals(objExtractor.getValue("Store.arr.1"), new JsonPrimitive("value"));

        JsonArray expectedArr = new JsonArray();
        expectedArr.add(JsonNull.INSTANCE);
        expectedArr.add(new JsonPrimitive("value"));
        assertEquals(objExtractor.getValue("Store.arr"), expectedArr);
    }

}
