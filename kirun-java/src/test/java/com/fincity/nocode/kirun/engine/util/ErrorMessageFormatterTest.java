package com.fincity.nocode.kirun.engine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.google.gson.JsonPrimitive;

class ErrorMessageFormatterTest {

    @Test
    void testFormatValue_Null() {
        assertEquals("null", ErrorMessageFormatter.formatValue(null));
    }

    @Test
    void testFormatValue_Strings() {
        assertEquals("\"hello\"", ErrorMessageFormatter.formatValue("hello"));
    }

    @Test
    void testFormatValue_Numbers() {
        assertEquals("42", ErrorMessageFormatter.formatValue(42));
        assertEquals("3.14", ErrorMessageFormatter.formatValue(3.14));
    }

    @Test
    void testFormatValue_Booleans() {
        assertEquals("true", ErrorMessageFormatter.formatValue(true));
        assertEquals("false", ErrorMessageFormatter.formatValue(false));
    }

    @Test
    void testFormatValue_Objects() {
        Map<String, Object> obj = new HashMap<>();
        obj.put("name", "John");
        obj.put("age", 30);

        String result = ErrorMessageFormatter.formatValue(obj);
        assertTrue(result.contains("\"name\""));
        assertTrue(result.contains("\"John\""));
        assertTrue(result.contains("\"age\""));
        assertTrue(result.contains("30"));
    }

    @Test
    void testFormatValue_Arrays() {
        List<Integer> arr = List.of(1, 2, 3);
        String result = ErrorMessageFormatter.formatValue(arr);

        assertTrue(result.contains("["));
        assertTrue(result.contains("1"));
        assertTrue(result.contains("2"));
        assertTrue(result.contains("3"));
        assertTrue(result.contains("]"));
    }

    @Test
    void testFormatValue_CircularReferences() {
        Map<String, Object> obj = new HashMap<>();
        obj.put("name", "test");
        obj.put("self", obj);  // Circular reference

        String result = ErrorMessageFormatter.formatValue(obj);
        assertTrue(result.contains("[Circular]"));
    }

    @Test
    void testFormatValue_Truncation() {
        Map<String, String> longObj = new HashMap<>();
        longObj.put("data", "x".repeat(300));

        String result = ErrorMessageFormatter.formatValue(longObj, 50);
        assertTrue(result.length() <= 53); // 50 + "..."
        assertTrue(result.endsWith("..."));
    }

    @Test
    void testFormatFunctionName_WithNamespace() {
        assertEquals("UIEngine.SetStore",
                ErrorMessageFormatter.formatFunctionName("UIEngine", "SetStore"));
    }

    @Test
    void testFormatFunctionName_WithoutNamespace() {
        assertEquals("loadEverything",
                ErrorMessageFormatter.formatFunctionName(null, "loadEverything"));
    }

    @Test
    void testFormatFunctionName_EmptyNamespace() {
        assertEquals("loadApp",
                ErrorMessageFormatter.formatFunctionName("", "loadApp"));
    }

    @Test
    void testFormatFunctionName_UndefinedString() {
        assertEquals("loadEverything",
                ErrorMessageFormatter.formatFunctionName("undefined", "loadEverything"));
    }

    @Test
    void testFormatFunctionName_NullString() {
        assertEquals("loadEverything",
                ErrorMessageFormatter.formatFunctionName("null", "loadEverything"));
    }

    @Test
    void testFormatStatementName_Valid() {
        assertEquals("'storeString'",
                ErrorMessageFormatter.formatStatementName("storeString"));
    }

    @Test
    void testFormatStatementName_Null() {
        assertNull(ErrorMessageFormatter.formatStatementName(null));
    }

    @Test
    void testFormatStatementName_Empty() {
        assertNull(ErrorMessageFormatter.formatStatementName(""));
    }

    @Test
    void testFormatStatementName_UndefinedString() {
        assertNull(ErrorMessageFormatter.formatStatementName("undefined"));
    }

    @Test
    void testFormatSchemaDefinition_SingleType() {
        Schema schema = Schema.ofInteger("test");
        assertEquals("Integer", ErrorMessageFormatter.formatSchemaDefinition(schema));
    }

    @Test
    void testFormatSchemaDefinition_ArrayType() {
        ArraySchemaType ast = new ArraySchemaType();
        ast.setSingleSchema(Schema.ofInteger("item"));

        Schema schema = Schema.ofArray("test").setItems(ast);
        assertEquals("Array<Integer>", ErrorMessageFormatter.formatSchemaDefinition(schema));
    }

    @Test
    void testFormatSchemaDefinition_TupleType() {
        ArraySchemaType ast = new ArraySchemaType();
        ast.setTupleSchema(List.of(
            Schema.ofString("first"),
            Schema.ofInteger("second")
        ));

        Schema schema = Schema.ofArray("test").setItems(ast);
        assertEquals("[String, Integer]", ErrorMessageFormatter.formatSchemaDefinition(schema));
    }

    @Test
    void testFormatSchemaDefinition_EnumType() {
        Schema schema = Schema.ofString("test").setEnums(List.of(
            new JsonPrimitive("option1"),
            new JsonPrimitive("option2")
        ));

        String result = ErrorMessageFormatter.formatSchemaDefinition(schema);
        assertTrue(result.contains("String"));
        assertTrue(result.contains("option1"));
        assertTrue(result.contains("option2"));
    }

    @Test
    void testFormatSchemaDefinition_MultipleTypes() {
        Schema schema = Schema.of("test", SchemaType.STRING, SchemaType.INTEGER);
        String result = ErrorMessageFormatter.formatSchemaDefinition(schema);

        assertTrue(result.contains("String") || result.contains("Integer"));
        assertTrue(result.contains("|"));
    }

    @Test
    void testBuildFunctionExecutionError_WithStatementName() {
        String result = ErrorMessageFormatter.buildFunctionExecutionError(
            "UIEngine.SetStore",
            "'storeString'",
            "Expected an array but found {\"key\": \"value\"}"
        );

        assertEquals("Error while executing the function UIEngine.SetStore in statement 'storeString': "
            + "Expected an array but found {\"key\": \"value\"}", result);
    }

    @Test
    void testBuildFunctionExecutionError_WithoutStatementName() {
        String result = ErrorMessageFormatter.buildFunctionExecutionError(
            "loadEverything",
            null,
            "Some error occurred"
        );

        assertEquals("Error while executing the function loadEverything: Some error occurred", result);
    }

    @Test
    void testBuildFunctionExecutionError_WithParameterName() {
        String result = ErrorMessageFormatter.buildFunctionExecutionError(
            "UIEngine.SetStore",
            "'storeString'",
            "Expected an array but found {}",
            "value"
        );

        assertEquals("Error while executing the function UIEngine.SetStore's parameter value in statement 'storeString': "
            + "Expected an array but found {}", result);
    }

    @Test
    void testBuildFunctionExecutionError_WithParameterNoStatement() {
        String result = ErrorMessageFormatter.buildFunctionExecutionError(
            "loadApp",
            null,
            "Invalid parameter",
            "config"
        );

        assertEquals("Error while executing the function loadApp's parameter config: Invalid parameter", result);
    }

    @Test
    void testBuildFunctionExecutionError_WithParameterSchema() {
        ArraySchemaType ast = new ArraySchemaType();
        ast.setSingleSchema(Schema.ofInteger("item"));
        Schema schema = Schema.ofArray("value").setItems(ast);

        String result = ErrorMessageFormatter.buildFunctionExecutionError(
            "System.Array.Concatenate",
            null,
            "Expected an array but found null",
            "secondSource",
            schema
        );

        assertEquals("Error while executing the function System.Array.Concatenate's parameter secondSource "
            + "[Expected: Array<Integer>]: Expected an array but found null", result);
    }

    @Test
    void testBuildFunctionExecutionError_NestedErrors() {
        String innerError = "Error while executing the function UIEngine.SetStore in statement 'storeString' "
            + "[Expected: Array]: Expected an array but found {}";

        String result = ErrorMessageFormatter.buildFunctionExecutionError(
            "loadApp",
            null,
            innerError
        );

        assertEquals("Error while executing the function loadApp: \n" + innerError, result);
    }

    @Test
    void testFormatErrorMessage_ThrowableWithMessage() {
        Throwable error = new RuntimeException("Something went wrong");
        assertEquals("Something went wrong", ErrorMessageFormatter.formatErrorMessage(error));
    }

    @Test
    void testFormatErrorMessage_String() {
        assertEquals("Error message", ErrorMessageFormatter.formatErrorMessage("Error message"));
    }

    @Test
    void testFormatErrorMessage_Null() {
        assertEquals("Unknown error", ErrorMessageFormatter.formatErrorMessage((Throwable) null));
    }

    @Test
    void testFormatErrorMessage_Object() {
        Map<String, String> error = Map.of("message", "Expected an array but found something");
        String result = ErrorMessageFormatter.formatErrorMessage(error);

        assertTrue(result.contains("message"));
        assertTrue(result.contains("Expected an array but found something"));
    }
}
