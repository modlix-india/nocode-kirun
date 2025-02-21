package com.fincity.nocode.kirun.engine.function.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class HashDataTest {

    private final HashData hashData = new HashData();

    @Test
    void testHashInteger() {
        var fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(HashData.PARAMETER_DATA, new JsonPrimitive(12345)));

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> assertEquals(
                        "5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5",
                        result.allResults().get(0).getResult().get(HashData.EVENT_RESULT_NAME).getAsString()))
                .verifyComplete();
    }

    @Test
    void testHashDouble() {
        var fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(HashData.PARAMETER_DATA, new JsonPrimitive(123.45)));

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> assertEquals(
                        "4ebc4a141b378980461430980948a55988fbf56f85d084ac33d8a8f61b9fab88",
                        result.allResults().get(0).getResult().get(HashData.EVENT_RESULT_NAME).getAsString()))
                .verifyComplete();
    }

    @Test
    void testHashString() {
        var fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(HashData.PARAMETER_DATA, new JsonPrimitive("test string")));

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> assertEquals(
                        "ee68a16fef8bf44a2b86b5614554b4079820f98dea14a67c3b507f59333cd591",
                        result.allResults().get(0).getResult().get(HashData.EVENT_RESULT_NAME).getAsString()))
                .verifyComplete();
    }

    @Test
    void testHashMD5Algorithm() {
        var fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(
                        HashData.PARAMETER_DATA, new JsonPrimitive("test string"),
                        HashData.PARAMETER_ALGORITHM, new JsonPrimitive("MD5")));

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> assertEquals(
                        "520a193597c1170fc7f00c6e77df571f",
                        result.allResults().get(0).getResult().get(HashData.EVENT_RESULT_NAME).getAsString()))
                .verifyComplete();
    }

    @Test
    void testHashNull() {
        var fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of());

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> assertEquals(
                        "null",
                        result.allResults().get(0).getResult().get(HashData.EVENT_RESULT_NAME).getAsString()))
                .verifyComplete();
    }

    @Test
    void testHashBoolean() {
        var fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(HashData.PARAMETER_DATA, new JsonPrimitive(true)));

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> assertEquals(
                        "b5bea41b6c623f7c09f1bf24dcae58ebab3c0cdd90ad966bc43a45b44867e12b",
                        result.allResults().get(0).getResult().get(HashData.EVENT_RESULT_NAME).getAsString()))
                .verifyComplete();
    }

    @Test
    void testHashEmptyString() {
        var fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(HashData.PARAMETER_DATA, new JsonPrimitive("")));

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> assertEquals(
                        "12ae32cb1ec02d01eda3581b127c1fee3b0dc53572ed6baf239721a03d82e126",
                        result.allResults().get(0).getResult().get(HashData.EVENT_RESULT_NAME).getAsString()))
                .verifyComplete();
    }

    @Test
    void testHashSpecialCharacters() {
        var fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(HashData.PARAMETER_DATA, new JsonPrimitive("!@#$%^&*()")));

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> assertEquals(
                        "1bacb430682c1bca76b72c8cd3f163a218816c43fa08b0e1e18175e34e96ef1c",
                        result.allResults().get(0).getResult().get(HashData.EVENT_RESULT_NAME).getAsString()))
                .verifyComplete();
    }

    @Test
    void testHashLongString() {
        String longString = "a".repeat(1000);
        var fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(HashData.PARAMETER_DATA, new JsonPrimitive(longString)));

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> assertEquals(
                        "955d04731f2adae1d94c940c2b9f72677431310d92f650dc581251045f4dbcdb",
                        result.allResults().get(0).getResult().get(HashData.EVENT_RESULT_NAME).getAsString()))
                .verifyComplete();
    }

    @Test
    void testHashUnicodeCharacters() {
        var fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(HashData.PARAMETER_DATA, new JsonPrimitive("Hello 世界")));

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> assertEquals(
                        "b7be42dc220b912e1970a421cb7fd053798a8d40b78ae6d05d55478b9004be78",
                        result.allResults().get(0).getResult().get(HashData.EVENT_RESULT_NAME).getAsString()))
                .verifyComplete();
    }

    @Test
    void shouldHashObjectWithPrimitiveLevelTrueAndDefaultAlgorithm() {
        JsonObject testObject = new JsonObject();
        testObject.addProperty("name", "Kailash");
        testObject.addProperty("age", 23);
        testObject.addProperty("city", "Bengaluru");

        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(
                        HashData.PARAMETER_DATA, testObject,
                        HashData.PARAMETER_PRIMITIVE_LEVEL, new JsonPrimitive(true)));

        JsonObject expectedObject = new JsonObject();

        expectedObject.addProperty(
                "6ffe58b85cad95f1e86f1afebc4ffba738ef6c5520811cb5f393f6c0234ffcb0",
                "587ba9b0b3222f5af3ad1c7495536e2d0356a9881f45059dcf619923e543b7cc");
        expectedObject.addProperty(
                "78eb1fc4755453033fe186d682406e53543f575fa2b98887eceb8736e7d24567",
                "535fa30d7e25dd8a49f1536779734ec8286108d115da5045d77f3b4185d8f790");
        expectedObject.addProperty(
                "b295162ccd7483bcf4f715b22b84d053efb490c5fd1beb6799751456d37c1ad1",
                "afd27606e80dfd77bac4f51dabbb2ecf0ac061dd07b0fe58fc1cdde9a70ad3be");

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> {
                    JsonObject actualResult = result.allResults().get(0)
                            .getResult()
                            .get(HashData.EVENT_RESULT_NAME).getAsJsonObject();
                    assertEquals(expectedObject, actualResult);
                })
                .verifyComplete();
    }

    @Test
    void shouldHashArrayWithPrimitiveLevelTrue() {

        JsonArray testArray = new JsonArray();
        testArray.add("test");
        testArray.add(123);
        testArray.add(true);
        testArray.add(JsonNull.INSTANCE);

        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(
                        HashData.PARAMETER_DATA, testArray,
                        HashData.PARAMETER_PRIMITIVE_LEVEL, new JsonPrimitive(true)));

        JsonArray expectedArray = new JsonArray();
        expectedArray.add("4d967a30111bf29f0eba01c448b375c1629b2fed01cdfcc3aed91f1b57d5dd5e");
        expectedArray.add("a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3");
        expectedArray.add("b5bea41b6c623f7c09f1bf24dcae58ebab3c0cdd90ad966bc43a45b44867e12b");
        expectedArray.add("null");

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> {
                    JsonElement actualResult = result.allResults().get(0)
                            .getResult()
                            .get(HashData.EVENT_RESULT_NAME);
                    assertEquals(expectedArray.toString(), actualResult.toString());
                })
                .verifyComplete();
    }

    @Test
    void shouldHashWithMD5Algorithm() {
        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(
                        HashData.PARAMETER_DATA, new JsonPrimitive("test string"),
                        HashData.PARAMETER_ALGORITHM, new JsonPrimitive("MD5")));

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> {
                    JsonElement actualResult = result.allResults().get(0)
                            .getResult()
                            .get(HashData.EVENT_RESULT_NAME);
                    assertEquals("520a193597c1170fc7f00c6e77df571f", actualResult.getAsString());
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleNullValues() {
        // Test with null value
        ReactiveFunctionExecutionParameters fepNull = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(
                        HashData.PARAMETER_DATA, JsonNull.INSTANCE));

        StepVerifier.create(hashData.execute(fepNull))
                .assertNext(result -> {
                    JsonElement actualResult = result.allResults().get(0)
                            .getResult()
                            .get(HashData.EVENT_RESULT_NAME);
                    assertEquals("null", actualResult.getAsString());
                })
                .verifyComplete();

        // Test with missing parameter (equivalent to undefined in TypeScript)
        ReactiveFunctionExecutionParameters fepMissing = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of());

        StepVerifier.create(hashData.execute(fepMissing))
                .assertNext(result -> {
                    JsonElement actualResult = result.allResults().get(0)
                            .getResult()
                            .get(HashData.EVENT_RESULT_NAME);
                    assertEquals("null", actualResult.getAsString());
                })
                .verifyComplete();
    }

    @Test
    void shouldHashNestedObjectsWithPrimitiveLevel() {
        // Create nested object structure
        JsonObject contactsObject = new JsonObject();
        contactsObject.addProperty("email", "john@example.com");
        contactsObject.addProperty("phone", "1234567890");

        JsonObject userObject = new JsonObject();
        userObject.addProperty("name", "John");
        userObject.add("contacts", contactsObject);

        JsonObject nestedObject = new JsonObject();
        nestedObject.add("user", userObject);
        nestedObject.addProperty("active", true);

        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(
                        HashData.PARAMETER_DATA, nestedObject,
                        HashData.PARAMETER_PRIMITIVE_LEVEL, new JsonPrimitive(true)));

        // Create expected result object
        JsonObject contactsHashObject = new JsonObject();
        contactsHashObject.addProperty(
                "d55b99efba68c4d57a901b891ddbaa9d1d8ad6c52dd62390b00204efc3170441",
                "aaa07cfdec182c9ab13a25f43b0a84356bfe3ef199658fd4e15f96e5af54b665");
        contactsHashObject.addProperty(
                "d5511d58ae89a5136544686b7e471128789e3309c2ec9b520c217067f6abb75d",
                "9f191b3167af0649edeb8888946bee1c523b87aff6219fa7765ac731bca74fb1");

        JsonObject userHashObject = new JsonObject();
        userHashObject.addProperty(
                "6ffe58b85cad95f1e86f1afebc4ffba738ef6c5520811cb5f393f6c0234ffcb0",
                "152f3ca566617488c2450ba9a88bdb8ef1593c860c496c39386d68cfe351df3f");
        userHashObject.add(
                "ffc121712a18c1513ded336647a0ccf7d369ba1927ac68c5d3272ac54535d633",
                contactsHashObject);

        JsonObject expectedObject = new JsonObject();
        expectedObject.add(
                "3190d261d186aeead3a8deec202737c7775af5c8d455a9e5ba958c48b5fd3f59",
                userHashObject);
        expectedObject.addProperty(
                "db0fb2bf5ebc424454c3e11b5ee8bfb43af24a52e561aa49e94143831dc6fd93",
                "b5bea41b6c623f7c09f1bf24dcae58ebab3c0cdd90ad966bc43a45b44867e12b");

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> {
                    JsonElement actualResult = result.allResults().get(0)
                            .getResult()
                            .get(HashData.EVENT_RESULT_NAME);
                    assertEquals(expectedObject, actualResult);
                })
                .verifyComplete();
    }

    @Test
    void testHashMetaAudienceWithPrimitiveLevel() {
        // Create nested array structure
        JsonArray nestedObject = new JsonArray();

        // First row
        JsonArray row1 = new JsonArray();
        row1.add("kunal");
        row1.add("jha");
        row1.add((JsonElement) null);  // NaN in Java
        row1.add(9922552168L);
        nestedObject.add(row1);

        // Second row
        JsonArray row2 = new JsonArray();
        row2.add("Saleem");
        row2.add((JsonElement) null);
        row2.add("gssaleem22@gmail.com");
        row2.add(9845367369L);
        nestedObject.add(row2);

        // Third row
        JsonArray row3 = new JsonArray();
        row3.add("Sunil");
        row3.add("Gayathri");
        row3.add((JsonElement) null);
        row3.add(9900511830L);
        nestedObject.add(row3);

        // Fourth row
        JsonArray row4 = new JsonArray();
        row4.add("Hemant");
        row4.add("Hemant");
        row4.add((JsonElement) null);
        row4.add(9619006766L);
        nestedObject.add(row4);

        // Fifth row
        JsonArray row5 = new JsonArray();
        row5.add("Hazarath");
        row5.add("Sarabu");
        row5.add("hazarath@yahoo.com");
        row5.add(9848514909L);
        nestedObject.add(row5);

        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(
                        HashData.PARAMETER_DATA, nestedObject,
                        HashData.PARAMETER_ALGORITHM, new JsonPrimitive("SHA-256"),
                        HashData.PARAMETER_PRIMITIVE_LEVEL, new JsonPrimitive(true)));

        // Create expected result array
        JsonArray expectedArray = new JsonArray();

        JsonArray expected1 = new JsonArray();
        expected1.add("5c5ec4214a4f90fc81d20698f3b58971a65f99be8f807167c0dc357dbfca6d64");
        expected1.add("1a14fb90285bbdc811ec90c31535f8cbfe7a190cdea4388ffe5bfb5149373c85");
        expected1.add("null");
        expected1.add("c73e1c882b48b71df3346a638b86a361d4ad58bd5119bed829c193192ea0bd12");
        expectedArray.add(expected1);

        JsonArray expected2 = new JsonArray();
        expected2.add("b0a368eae50806c434c5dc52ea1743c033e20e28b37e95da3f515d426c412f24");
        expected2.add("null");
        expected2.add("6aa7533f4df03ce2cdb94fbd3af0df01d3ad798997224b31bbd972c8f4110d98");
        expected2.add("55dd6fc4584ff7971599839a58ea554e0ceabf8ea54108c2afe96b9600ad22de");
        expectedArray.add(expected2);

        JsonArray expected3 = new JsonArray();
        expected3.add("d4ac1f574136969f501b11f8312613addf81a738bd32d318ff57b14c57074c46");
        expected3.add("d77c692372de396251e5a30d31f8f0d1e729fc2d2ddf38286d4f9133aa7849de");
        expected3.add("null");
        expected3.add("859aee372e6161388c67a2ebe39ea30da2f09d6e4dca715be79109d8c9c9a946");
        expectedArray.add(expected3);

        JsonArray expected4 = new JsonArray();
        expected4.add("8ff58050b37737f3b0d5b9541c1cb1f67394b41631bfdc927ab216ff580af445");
        expected4.add("8ff58050b37737f3b0d5b9541c1cb1f67394b41631bfdc927ab216ff580af445");
        expected4.add("null");
        expected4.add("3aaf978a21568f3132e586b5fab896a46930831d8362ae3a630f9b0e1ac44d5a");
        expectedArray.add(expected4);

        JsonArray expected5 = new JsonArray();
        expected5.add("ff1363632be96e64e74f8f6ce09e899dc93243a0aba24ae8d1e901df266e783b");
        expected5.add("60124d4663c3581f6d5ab01d3a8652e67c83a0eea14d2eebc617132386a63533");
        expected5.add("5df5828651d67e079dbaa41176ed84c87e1823a316abd2a55ade9c9045dadb0a");
        expected5.add("c6701bb10611df1b1ac0f1bc3d008200a81ccb78a2e3e71db8d0e6b6ad6bca83");
        expectedArray.add(expected5);

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> {
                    JsonElement actualResult = result.allResults().get(0)
                            .getResult()
                            .get(HashData.EVENT_RESULT_NAME);
                    assertEquals(expectedArray.toString(), actualResult.toString());
                })
                .verifyComplete();
    }

    @Test
    void shouldHash2DArrayWithPrimitiveLevel() {
        // Create 2D array structure
        JsonArray twoDArray = new JsonArray();

        // First row
        JsonArray row1 = new JsonArray();
        row1.add("a");
        row1.add(1);
        row1.add(true);
        twoDArray.add(row1);

        // Second row
        JsonArray row2 = new JsonArray();
        row2.add("b");
        row2.add(2);
        row2.add(false);
        twoDArray.add(row2);

        // Third row
        JsonArray row3 = new JsonArray();
        row3.add("c");
        row3.add(3);
        row3.add(JsonNull.INSTANCE);
        twoDArray.add(row3);

        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(
                        HashData.PARAMETER_DATA, twoDArray,
                        HashData.PARAMETER_PRIMITIVE_LEVEL, new JsonPrimitive(true)));

        // Create expected result array
        JsonArray expectedArray = new JsonArray();

        JsonArray expected1 = new JsonArray();
        expected1.add("ac8d8342bbb2362d13f0a559a3621bb407011368895164b628a54f7fc33fc43c"); // hash of "a"
        expected1.add("6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b"); // hash of 1
        expected1.add("b5bea41b6c623f7c09f1bf24dcae58ebab3c0cdd90ad966bc43a45b44867e12b"); // hash of true
        expectedArray.add(expected1);

        JsonArray expected2 = new JsonArray();
        expected2.add("c100f95c1913f9c72fc1f4ef0847e1e723ffe0bde0b36e5f36c13f81fe8c26ed"); // hash of "b"
        expected2.add("d4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35"); // hash of 2
        expected2.add("fcbcf165908dd18a9e49f7ff27810176db8e9f63b4352213741664245224f8aa"); // hash of false
        expectedArray.add(expected2);

        JsonArray expected3 = new JsonArray();
        expected3.add("879923da020d1533f4d8e921ea7bac61e8ba41d3c89d17a4d14e3a89c6780d5d"); // hash of "c"
        expected3.add("4e07408562bedb8b60ce05c1decfe3ad16b72230967de01f640b7e4729b49fce"); // hash of 3
        expected3.add("null"); // null value
        expectedArray.add(expected3);

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> {
                    JsonElement actualResult = result.allResults().get(0)
                            .getResult()
                            .get(HashData.EVENT_RESULT_NAME);
                    assertEquals(expectedArray.toString(), actualResult.toString());
                })
                .verifyComplete();
    }

    @Test
    void testHashLongValues() {
        // Test various Long values
        JsonArray longValues = new JsonArray();
        longValues.add(Long.MAX_VALUE);
        longValues.add(Long.MIN_VALUE);
        longValues.add(0L);
        longValues.add(9223372036854775807L); // Max long value
        longValues.add(-9223372036854775808L); // Min long value
        longValues.add(42L);

        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(
                        HashData.PARAMETER_DATA, longValues,
                        HashData.PARAMETER_ALGORITHM, new JsonPrimitive("SHA-256"),
                        HashData.PARAMETER_PRIMITIVE_LEVEL, new JsonPrimitive(true)));

        JsonArray expectedHashes = new JsonArray();
        // Hash of Long.MAX_VALUE (9223372036854775807)
        expectedHashes.add("b34a1c30a715f6bf8b7243afa7fab883ce3612b7231716bdcbbdc1982e1aed29");
        // Hash of Long.MIN_VALUE (-9223372036854775808)
        expectedHashes.add("85386477f3af47e4a0b308ee3b3a688df16e8b2228105dd7d4dcd42a9807cb78");
        // Hash of 0L
        expectedHashes.add("5feceb66ffc86f38d952786c6d696c79c2dbc239dd4e91b46729d73a27fb57e9");
        // Hash of max long value
        expectedHashes.add("b34a1c30a715f6bf8b7243afa7fab883ce3612b7231716bdcbbdc1982e1aed29");
        // Hash of min long value
        expectedHashes.add("85386477f3af47e4a0b308ee3b3a688df16e8b2228105dd7d4dcd42a9807cb78");
        // Hash of 42L
        expectedHashes.add("73475cb40a568e8da8a045ced110137e159f890ac4da883b6b17dc651b3a8049");

        StepVerifier.create(hashData.execute(fep))
                .assertNext(result -> {
                    JsonElement actualResult = result.allResults().get(0)
                            .getResult()
                            .get(HashData.EVENT_RESULT_NAME);
                    assertEquals(expectedHashes.toString(), actualResult.toString());
                })
                .verifyComplete();
    }
}