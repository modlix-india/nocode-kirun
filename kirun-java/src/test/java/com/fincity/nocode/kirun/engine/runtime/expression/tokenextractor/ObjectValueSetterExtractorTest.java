package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // =========================================================================
    // Array with integer indexes
    // =========================================================================

    @Test
    void arraySetAndGetValueAtSpecificIndex() {
        JsonObject store = new JsonObject();
        JsonArray items = new JsonArray();
        items.add("a");
        items.add("b");
        items.add("c");
        items.add("d");
        items.add("e");
        store.add("items", items);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Page");

        assertEquals(new JsonPrimitive("c"), ext.getValue("Page.items[2]"));

        ext.setValue("Page.items[2]", new JsonPrimitive("updated"), null, null);
        assertEquals(new JsonPrimitive("updated"), ext.getValue("Page.items[2]"));
    }

    @Test
    void arraySetValueAtIndex0() {
        JsonObject store = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add(10);
        arr.add(20);
        arr.add(30);
        store.add("arr", arr);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.arr[0]", new JsonPrimitive(99), null, null);
        assertEquals(new JsonPrimitive(99), ext.getValue("Store.arr[0]"));
        assertEquals(new JsonPrimitive(20), ext.getValue("Store.arr[1]"));
    }

    @Test
    void arraySparseGrowBeyondCurrentLength() {
        JsonObject store = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add(2);
        store.add("arr", arr);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.arr[5]", new JsonPrimitive(100), null, null);

        JsonArray result = ext.getValue("Store.arr").getAsJsonArray();
        assertEquals(6, result.size());
        assertEquals(new JsonPrimitive(100), result.get(5));
        assertTrue(result.get(2).isJsonNull());
        assertTrue(result.get(3).isJsonNull());
        assertTrue(result.get(4).isJsonNull());
    }

    @Test
    void arraySetValueOnEmptyArray() {
        JsonObject store = new JsonObject();
        store.add("arr", new JsonArray());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.arr[0]", new JsonPrimitive("first"), null, null);
        assertEquals(new JsonPrimitive("first"), ext.getValue("Store.arr[0]"));
        assertEquals(1, ext.getValue("Store.arr").getAsJsonArray().size());
    }

    @Test
    void arraySetObjectAsElement() {
        JsonObject store = new JsonObject();
        store.add("arr", new JsonArray());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        JsonObject obj = new JsonObject();
        obj.addProperty("name", "Alice");
        obj.addProperty("age", 30);
        ext.setValue("Store.arr[0]", obj, null, null);

        assertEquals(new JsonPrimitive("Alice"), ext.getValue("Store.arr[0].name"));
        assertEquals(new JsonPrimitive(30), ext.getValue("Store.arr[0].age"));
    }

    @Test
    void arraySetArrayAsElement() {
        JsonObject store = new JsonObject();
        store.add("matrix", new JsonArray());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        JsonArray row0 = new JsonArray();
        row0.add(1);
        row0.add(2);
        row0.add(3);
        JsonArray row1 = new JsonArray();
        row1.add(4);
        row1.add(5);
        row1.add(6);

        ext.setValue("Store.matrix[0]", row0, null, null);
        ext.setValue("Store.matrix[1]", row1, null, null);

        assertEquals(new JsonPrimitive(2), ext.getValue("Store.matrix[0][1]"));
        assertEquals(new JsonPrimitive(6), ext.getValue("Store.matrix[1][2]"));
    }

    // =========================================================================
    // Nested property access on array elements
    // =========================================================================

    @Test
    void arraySetNestedPropertyOnObjectInsideArray() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"logs":[{"msg":"hello"},{"msg":"world"}]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Page");

        ext.setValue("Page.logs[1].msg", new JsonPrimitive("updated"), null, null);
        assertEquals(new JsonPrimitive("updated"), ext.getValue("Page.logs[1].msg"));
        assertEquals(new JsonPrimitive("hello"), ext.getValue("Page.logs[0].msg"));
    }

    @Test
    void arrayCreateIntermediateObjectOnNullElement() {
        JsonObject store = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add(JsonNull.INSTANCE);
        arr.add(JsonNull.INSTANCE);
        store.add("arr", arr);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.arr[0].deep.value", new JsonPrimitive(42), null, null);
        assertEquals(new JsonPrimitive(42), ext.getValue("Store.arr[0].deep.value"));
    }

    @Test
    void arrayCreateIntermediateArrayOnNestedPath() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"arr":[{"items":[]}]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.arr[0].items[2]", new JsonPrimitive("value"), null, null);

        JsonArray items = ext.getValue("Store.arr[0].items").getAsJsonArray();
        assertEquals(3, items.size());
        assertEquals(new JsonPrimitive("value"), items.get(2));
    }

    // =========================================================================
    // Array of objects - activityLogs pattern
    // =========================================================================

    @Test
    void activityLogsAccessPropertiesOfObjectsInArray() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"activityLogs":[
                    {"action":"login","timestamp":100},
                    {"action":"view","timestamp":200},
                    {"action":"edit","timestamp":300},
                    {"action":"save","timestamp":400},
                    {"action":"logout","timestamp":500}
                ]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Page");

        assertEquals(new JsonPrimitive("login"), ext.getValue("Page.activityLogs[0].action"));
        assertEquals(new JsonPrimitive("logout"), ext.getValue("Page.activityLogs[4].action"));
    }

    @Test
    void activityLogsSetPropertyOnObjectInArray() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"activityLogs":[
                    {"action":"login","timestamp":100},
                    {"action":"view","timestamp":200},
                    {"action":"edit","timestamp":300}
                ]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Page");

        ext.setValue("Page.activityLogs[1].action", new JsonPrimitive("updated-view"), null, null);
        assertEquals(new JsonPrimitive("updated-view"), ext.getValue("Page.activityLogs[1].action"));
    }

    @Test
    void activityLogsAppendNewObjectAtLength() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"activityLogs":[
                    {"action":"login","timestamp":100},
                    {"action":"view","timestamp":200}
                ]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Page");

        JsonObject newLog = new JsonObject();
        newLog.addProperty("action", "edit");
        newLog.addProperty("timestamp", 300);
        ext.setValue("Page.activityLogs[2]", newLog, null, null);

        assertEquals(3, ext.getValue("Page.activityLogs").getAsJsonArray().size());
        assertEquals(new JsonPrimitive("edit"), ext.getValue("Page.activityLogs[2].action"));
    }

    @Test
    void activityLogsSetNestedPropertyAtSpecificIndex() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"activityLogs":[
                    {"action":"a","details":{}},
                    {"action":"b","details":{}},
                    {"action":"c","details":{}}
                ]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Page");

        ext.setValue("Page.activityLogs[1].details.status", new JsonPrimitive("done"), null, null);
        assertEquals(new JsonPrimitive("done"), ext.getValue("Page.activityLogs[1].details.status"));
    }

    // =========================================================================
    // Multiple consecutive array indexes (2D / 3D)
    // =========================================================================

    @Test
    void array2DAccess() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"grid":[[1,2,3],[4,5,6],[7,8,9]]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        assertEquals(new JsonPrimitive(6), ext.getValue("Store.grid[1][2]"));

        ext.setValue("Store.grid[2][0]", new JsonPrimitive(99), null, null);
        assertEquals(new JsonPrimitive(99), ext.getValue("Store.grid[2][0]"));
    }

    @Test
    void array3DAccess() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"cube":[[[1,2],[3,4]],[[5,6],[7,8]]]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        assertEquals(new JsonPrimitive(6), ext.getValue("Store.cube[1][0][1]"));
    }

    // =========================================================================
    // Object with integer keys
    // =========================================================================

    @Test
    void objectSetAndGetValueWithNumericKey() {
        JsonObject store = new JsonObject();
        store.add("obj", new JsonObject());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.obj.1", new JsonPrimitive("value-one"), null, null);
        assertEquals(new JsonPrimitive("value-one"), ext.getValue("Store.obj.1"));

        JsonObject expected = new JsonObject();
        expected.addProperty("1", "value-one");
        assertEquals(expected, ext.getValue("Store.obj"));
    }

    @Test
    void objectSetMultipleNumericKeys() {
        JsonObject store = new JsonObject();
        store.add("obj", new JsonObject());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.obj.0", new JsonPrimitive("zero"), null, null);
        ext.setValue("Store.obj.1", new JsonPrimitive("one"), null, null);
        ext.setValue("Store.obj.2", new JsonPrimitive("two"), null, null);

        JsonObject expected = new JsonObject();
        expected.addProperty("0", "zero");
        expected.addProperty("1", "one");
        expected.addProperty("2", "two");
        assertEquals(expected, ext.getValue("Store.obj"));

        // Should remain an object, not an array
        assertFalse(ext.getValue("Store.obj").isJsonArray());
    }

    @Test
    void objectWithPreExistingNumericKeys() {
        JsonObject store = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty("0", "a");
        data.addProperty("1", "b");
        data.addProperty("2", "c");
        store.add("data", data);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        assertEquals(new JsonPrimitive("b"), ext.getValue("Store.data.1"));

        ext.setValue("Store.data.1", new JsonPrimitive("updated"), null, null);
        assertEquals(new JsonPrimitive("updated"), ext.getValue("Store.data.1"));

        // Should still be an object, not an array
        assertFalse(ext.getValue("Store.data").isJsonArray());
    }

    @Test
    void objectSetNestedValueThroughNumericKey() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"obj":{"1":{"name":"first"}}}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.obj.1.name", new JsonPrimitive("updated"), null, null);
        assertEquals(new JsonPrimitive("updated"), ext.getValue("Store.obj.1.name"));
    }

    @Test
    void objectCreateIntermediateObjectForNumericKey() {
        JsonObject store = new JsonObject();
        store.add("obj", new JsonObject());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        // When obj is {} and we set obj.1.name, intermediate "1" key should create an object
        ext.setValue("Store.obj.1.name", new JsonPrimitive("kiran"), null, null);
        assertEquals(new JsonPrimitive("kiran"), ext.getValue("Store.obj.1.name"));

        JsonObject inner = new JsonObject();
        inner.addProperty("name", "kiran");
        assertEquals(inner, ext.getValue("Store.obj.1"));

        assertFalse(ext.getValue("Store.obj").isJsonArray());
    }

    // =========================================================================
    // Object with quoted numeric keys (bracket notation)
    // =========================================================================

    @Test
    void objectQuotedNumericKeyDoublequotes() {
        JsonObject store = new JsonObject();
        store.add("obj", new JsonObject());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.obj[\"3\"]", new JsonPrimitive("quoted-three"), null, null);
        assertEquals(new JsonPrimitive("quoted-three"), ext.getValue("Store.obj[\"3\"]"));
    }

    @Test
    void objectQuotedNumericKeySingleQuotes() {
        JsonObject store = new JsonObject();
        store.add("obj", new JsonObject());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.obj['5']", new JsonPrimitive("quoted-five"), null, null);
        assertEquals(new JsonPrimitive("quoted-five"), ext.getValue("Store.obj['5']"));
    }

    @Test
    void objectQuotedNumericKeyForcesObjectNotArray() {
        JsonObject store = new JsonObject();
        store.add("data", new JsonObject());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.data[\"0\"]", new JsonPrimitive("zero"), null, null);
        ext.setValue("Store.data[\"1\"]", new JsonPrimitive("one"), null, null);

        JsonObject expected = new JsonObject();
        expected.addProperty("0", "zero");
        expected.addProperty("1", "one");
        assertEquals(expected, ext.getValue("Store.data"));
        assertFalse(ext.getValue("Store.data").isJsonArray());
    }

    // =========================================================================
    // Mixed numeric and string keys on objects
    // =========================================================================

    @Test
    void objectMixedNumericAndStringKeys() {
        JsonObject store = new JsonObject();
        store.add("obj", new JsonObject());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.obj.name", new JsonPrimitive("Alice"), null, null);
        ext.setValue("Store.obj.1", new JsonPrimitive("numeric-one"), null, null);
        ext.setValue("Store.obj.age", new JsonPrimitive(30), null, null);

        assertEquals(new JsonPrimitive("Alice"), ext.getValue("Store.obj.name"));
        assertEquals(new JsonPrimitive("numeric-one"), ext.getValue("Store.obj.1"));
        assertEquals(new JsonPrimitive(30), ext.getValue("Store.obj.age"));
        assertFalse(ext.getValue("Store.obj").isJsonArray());
    }

    @Test
    void objectNumericKeysWithNestedObjects() {
        JsonObject store = new JsonObject();
        store.add("obj", new JsonObject());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        JsonObject sub1 = new JsonObject();
        sub1.addProperty("sub", "value");
        JsonObject sub2 = new JsonObject();
        sub2.addProperty("sub", "another");

        ext.setValue("Store.obj.1", sub1, null, null);
        ext.setValue("Store.obj.2", sub2, null, null);

        assertEquals(new JsonPrimitive("value"), ext.getValue("Store.obj.1.sub"));
        assertEquals(new JsonPrimitive("another"), ext.getValue("Store.obj.2.sub"));
    }

    // =========================================================================
    // Array vs Object disambiguation
    // =========================================================================

    @Test
    void disambiguationNumericIndexOnArrayStaysArray() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"data":[10,20,30]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.data[1]", new JsonPrimitive(99), null, null);
        assertEquals(new JsonPrimitive(99), ext.getValue("Store.data[1]"));
        assertTrue(ext.getValue("Store.data").isJsonArray());
    }

    @Test
    void disambiguationNumericKeyOnObjectStaysObject() {
        JsonObject store = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty("0", "a");
        data.addProperty("1", "b");
        store.add("data", data);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.data.1", new JsonPrimitive("updated"), null, null);
        assertEquals(new JsonPrimitive("updated"), ext.getValue("Store.data.1"));
        assertFalse(ext.getValue("Store.data").isJsonArray());
    }

    @Test
    void disambiguationDotNotationNumericOnArray() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"arr":["first","second","third"]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.arr.1", new JsonPrimitive("UPDATED"), null, null);
        assertEquals(new JsonPrimitive("UPDATED"), ext.getValue("Store.arr.1"));
        assertEquals(3, ext.getValue("Store.arr").getAsJsonArray().size());
    }

    @Test
    void disambiguationDotNotationNumericOnObject() {
        JsonObject store = new JsonObject();
        store.add("obj", new JsonObject());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.obj.name", new JsonPrimitive("test"), null, null);
        ext.setValue("Store.obj.1", new JsonPrimitive("numeric"), null, null);

        assertEquals(new JsonPrimitive("test"), ext.getValue("Store.obj.name"));
        assertEquals(new JsonPrimitive("numeric"), ext.getValue("Store.obj.1"));
        assertFalse(ext.getValue("Store.obj").isJsonArray());
    }

    // =========================================================================
    // Intermediate creation: object vs array
    // =========================================================================

    @Test
    void intermediateCreateArrayWhenBracketIndex() {
        JsonObject store = new JsonObject();

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.data[0].name", new JsonPrimitive("Alice"), null, null);
        assertTrue(ext.getValue("Store.data").isJsonArray());
        assertEquals(new JsonPrimitive("Alice"), ext.getValue("Store.data[0].name"));
    }

    @Test
    void intermediateCreateObjectWhenStringSegment() {
        JsonObject store = new JsonObject();

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.data.name", new JsonPrimitive("Alice"), null, null);
        assertTrue(ext.getValue("Store.data").isJsonObject());
        assertFalse(ext.getValue("Store.data").isJsonArray());
        assertEquals(new JsonPrimitive("Alice"), ext.getValue("Store.data.name"));
    }

    @Test
    void intermediatePreserveArrayTypeOnNestedSet() {
        JsonObject store = new JsonObject();
        Gson gson = new Gson();
        JsonArray list = gson.fromJson("[{\"a\":1},{\"a\":2},{\"a\":3}]", JsonArray.class);
        store.add("list", list);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.list[1].a", new JsonPrimitive(99), null, null);
        assertEquals(new JsonPrimitive(99), ext.getValue("Store.list[1].a"));
        assertTrue(ext.getValue("Store.list").isJsonArray());
    }

    @Test
    void intermediatePreserveObjectTypeOnNestedSet() {
        JsonObject store = new JsonObject();
        JsonObject map = new JsonObject();
        JsonObject inner1 = new JsonObject();
        inner1.addProperty("a", 1);
        JsonObject inner2 = new JsonObject();
        inner2.addProperty("a", 2);
        map.add("1", inner1);
        map.add("2", inner2);
        store.add("map", map);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.map.1.a", new JsonPrimitive(99), null, null);
        assertEquals(new JsonPrimitive(99), ext.getValue("Store.map.1.a"));
        assertFalse(ext.getValue("Store.map").isJsonArray());
    }

    // =========================================================================
    // Edge cases with empty containers
    // =========================================================================

    @Test
    void edgeCaseSetOnEmptyArray() {
        JsonObject store = new JsonObject();
        store.add("arr", new JsonArray());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        JsonObject obj = new JsonObject();
        obj.addProperty("key", "value");
        ext.setValue("Store.arr[0]", obj, null, null);

        assertEquals(new JsonPrimitive("value"), ext.getValue("Store.arr[0].key"));
        assertEquals(1, ext.getValue("Store.arr").getAsJsonArray().size());
    }

    @Test
    void edgeCaseSetNumericKeyOnEmptyObject() {
        JsonObject store = new JsonObject();
        store.add("obj", new JsonObject());

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.obj.0", new JsonPrimitive("zero"), null, null);
        assertEquals(new JsonPrimitive("zero"), ext.getValue("Store.obj.0"));
        assertFalse(ext.getValue("Store.obj").isJsonArray());
    }

    @Test
    void edgeCaseReplaceEntireArray() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"items":[1,2,3]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        JsonArray newArr = new JsonArray();
        newArr.add(10);
        newArr.add(20);
        ext.setValue("Store.items", newArr, null, null);

        assertEquals(2, ext.getValue("Store.items").getAsJsonArray().size());
        assertEquals(new JsonPrimitive(10), ext.getValue("Store.items[0]"));
        assertEquals(new JsonPrimitive(20), ext.getValue("Store.items[1]"));
    }

    @Test
    void edgeCaseReplaceEntireObjectWithNumericKeys() {
        JsonObject store = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty("1", "old");
        store.add("data", data);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        JsonObject newData = new JsonObject();
        newData.addProperty("1", "new");
        newData.addProperty("2", "added");
        ext.setValue("Store.data", newData, null, null);

        assertEquals(new JsonPrimitive("new"), ext.getValue("Store.data.1"));
        assertEquals(new JsonPrimitive("added"), ext.getValue("Store.data.2"));
    }

    // =========================================================================
    // Complex mixed scenarios
    // =========================================================================

    @Test
    void complexArrayOfObjectsWithNumericStringKeys() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"rows":[{"0":"r0c0","1":"r0c1"},{"0":"r1c0","1":"r1c1"}]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        assertEquals(new JsonPrimitive("r0c0"), ext.getValue("Store.rows[0].0"));
        assertEquals(new JsonPrimitive("r1c1"), ext.getValue("Store.rows[1].1"));

        ext.setValue("Store.rows[0].1", new JsonPrimitive("updated"), null, null);
        assertEquals(new JsonPrimitive("updated"), ext.getValue("Store.rows[0].1"));
    }

    @Test
    void complexObjectContainingArraysAccessedByNumericKeys() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"data":{"1":[10,20],"2":[30,40]}}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        assertEquals(new JsonPrimitive(10), ext.getValue("Store.data.1[0]"));
        assertEquals(new JsonPrimitive(40), ext.getValue("Store.data.2[1]"));
    }

    @Test
    void complexDeeplyNestedMixOfArraysAndObjects() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"a":[{"b":{"1":{"c":[null,null,"deep"]}}}]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        assertEquals(new JsonPrimitive("deep"), ext.getValue("Store.a[0].b.1.c[2]"));
    }

    @Test
    void complexSetOnArrayElementThenAccessSubObjectWithNumericKey() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"items":[{"props":{"100":"hundred"}}]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        assertEquals(new JsonPrimitive("hundred"), ext.getValue("Store.items[0].props.100"));

        ext.setValue("Store.items[0].props.200", new JsonPrimitive("two-hundred"), null, null);
        assertEquals(new JsonPrimitive("two-hundred"), ext.getValue("Store.items[0].props.200"));

        // props should remain an object
        assertFalse(ext.getValue("Store.items[0].props").isJsonArray());
    }

    // =========================================================================
    // Direct setStoreData-equivalent tests
    // =========================================================================

    @Test
    void directSetOnArrayUsingBracketNotation() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"arr":["a","b","c"]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.arr[1]", new JsonPrimitive("B"), null, null);
        assertEquals(new JsonPrimitive("B"), ext.getValue("Store.arr[1]"));
        assertTrue(ext.getValue("Store.arr").isJsonArray());
    }

    @Test
    void directSetOnObjectUsingDotNotationWithNumericSegment() {
        JsonObject store = new JsonObject();
        JsonObject obj = new JsonObject();
        obj.addProperty("1", "old");
        store.add("obj", obj);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.obj.1", new JsonPrimitive("new"), null, null);
        assertEquals(new JsonPrimitive("new"), ext.getValue("Store.obj.1"));
        assertFalse(ext.getValue("Store.obj").isJsonArray());
    }

    @Test
    void directCreateArrayWhenBracketPath() {
        JsonObject store = new JsonObject();

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.newArr[0]", new JsonPrimitive("first"), null, null);
        assertTrue(ext.getValue("Store.newArr").isJsonArray());
        assertEquals(new JsonPrimitive("first"), ext.getValue("Store.newArr[0]"));
    }

    @Test
    void directCreateObjectWhenDotNumericPath() {
        JsonObject store = new JsonObject();

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.newObj.0", new JsonPrimitive("first"), null, null);
        // "0" accessed via dot notation on a new key - intermediate should be created
        assertTrue(ext.getValue("Store.newObj").isJsonObject() || ext.getValue("Store.newObj").isJsonArray());
    }

    // =========================================================================
    // Delete operations with arrays and objects
    // =========================================================================

    @Test
    void deleteArrayElement() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"arr":[10,20,30]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.arr[1]", null, true, true);

        JsonArray result = ext.getValue("Store.arr").getAsJsonArray();
        assertEquals(2, result.size());
        assertEquals(new JsonPrimitive(10), result.get(0));
        assertEquals(new JsonPrimitive(30), result.get(1));
    }

    @Test
    void deleteObjectNumericKey() {
        JsonObject store = new JsonObject();
        JsonObject obj = new JsonObject();
        obj.addProperty("1", "one");
        obj.addProperty("2", "two");
        obj.addProperty("3", "three");
        store.add("obj", obj);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.obj.2", null, true, true);

        JsonObject result = ext.getValue("Store.obj").getAsJsonObject();
        assertEquals(2, result.keySet().size());
        assertFalse(result.has("2"));
        assertTrue(result.has("1"));
        assertTrue(result.has("3"));
    }

    // =========================================================================
    // Overwrite flag with arrays and objects
    // =========================================================================

    @Test
    void overwriteFalseDoesNotReplaceExistingArrayElement() {
        Gson gson = new Gson();
        JsonObject store = gson.fromJson("""
                {"arr":[10,20,30]}
                """, JsonObject.class);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.arr[1]", new JsonPrimitive(99), false, null);
        // Should NOT overwrite since overwrite=false and value already exists
        assertEquals(new JsonPrimitive(20), ext.getValue("Store.arr[1]"));
    }

    @Test
    void overwriteFalseDoesNotReplaceExistingObjectNumericKey() {
        JsonObject store = new JsonObject();
        JsonObject obj = new JsonObject();
        obj.addProperty("1", "original");
        store.add("obj", obj);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.obj.1", new JsonPrimitive("replacement"), false, null);
        // Should NOT overwrite since overwrite=false and value already exists
        assertEquals(new JsonPrimitive("original"), ext.getValue("Store.obj.1"));
    }

    @Test
    void overwriteFalseAllowsSettingNullElement() {
        JsonObject store = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add(JsonNull.INSTANCE);
        arr.add(JsonNull.INSTANCE);
        store.add("arr", arr);

        ObjectValueSetterExtractor ext = new ObjectValueSetterExtractor(store, "Store");

        ext.setValue("Store.arr[0]", new JsonPrimitive("filled"), false, null);
        // Should set because the existing value is null
        assertEquals(new JsonPrimitive("filled"), ext.getValue("Store.arr[0]"));
    }
}
