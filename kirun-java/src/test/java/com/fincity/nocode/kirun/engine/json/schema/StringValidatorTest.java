package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.string.StringFormat;
import com.fincity.nocode.kirun.engine.json.schema.validator.StringValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

public class StringValidatorTest {
	
	@Test
	public void StringValidatorTestForValidation() {
		Schema schema = new Schema();
		JsonElement element = null;
		
		//Null Check
		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
				() -> StringValidator.validate(null, schema, element));

		assertEquals("Expected a string but found null", schemaValidationException.getMessage());
	}
	
	@Test
	public void StringValidatorTestForValidationIfString() {
		
		//Is not Json primitive
		Schema schema = new Schema();
		JsonObject elementJsonPrimitive = new JsonObject();
		elementJsonPrimitive.addProperty("value", 123);
		
		SchemaValidationException schemaValidationExceptionForJsonPrimitive = assertThrows(SchemaValidationException.class,
				() -> StringValidator.validate(null, schema, elementJsonPrimitive));

		assertEquals(elementJsonPrimitive.toString() + " is not String", schemaValidationExceptionForJsonPrimitive.getMessage());
		
	}
	
    @Test
    void StringValidatorTestForMinLengthIfStringException() {

        Schema schema = new Schema();
        schema.setMinLength(13);
        JsonObject stringObj = new JsonObject();
        stringObj.addProperty("value", "SURENdHar.S");

        SchemaValidationException schemaValidationExceptionOfMinString = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(null, schema, stringObj.get("value")));
        assertEquals("Expected a minimum of " + schema.getMinLength() + " characters",
                schemaValidationExceptionOfMinString.getMessage());
    }
    
    @Test
    void StringValidatorTestForMinLengthIfString() {

        Schema schema = new Schema();
        schema.setMinLength(7);
        JsonObject stringObj = new JsonObject();
        stringObj.addProperty("value", "Fincity");

        assertEquals(stringObj.get("value"),
                StringValidator.validate(null, schema, stringObj.get("value")));
    }
    
    @Test
    void StringValidatorTestForMaxLengthIfStringException() {

        Schema schema = new Schema();
        schema.setMaxLength(10);
        JsonObject stringObj = new JsonObject();
        stringObj.addProperty("value", "SURENdHar.S");

        SchemaValidationException schemaValidationExceptionOfMaxString = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(null, schema, stringObj.get("value")));
        assertEquals("Expected a maximum of " + schema.getMaxLength() + " characters",
                schemaValidationExceptionOfMaxString.getMessage());
    }
	
    @Test
    void StringValidatorTestForMaxLengthIfString() {

        Schema schema = new Schema();
        schema.setMaxLength(9);
        JsonObject stringObj = new JsonObject();
        stringObj.addProperty("value", "SURENdHar");

        assertEquals(stringObj.get("value"),
                 StringValidator.validate(null, schema, stringObj.get("value")));
    }
    
	@Test
	public void StringValidatorTestForValidationIfTimePatternMatched() {
		
		//String format is Time
		Schema schema = new Schema();
		schema.setFormat(StringFormat.TIME);
		
		JsonObject formatElement = new JsonObject();
		formatElement.addProperty("value", "10-Dec-198 10:19:59");
		
		SchemaValidationException schemaValidationExceptionTimeFormat = assertThrows(SchemaValidationException.class,
				() -> StringValidator.validate(null, schema, formatElement.get("value")));

		assertEquals(formatElement.get("value").toString() + " is not matched with the " + "time pattern", schemaValidationExceptionTimeFormat.getMessage());
		
	}
	
	@Test
	public void StringValidatorTestForValidationIfDatePatternMatched() {
		
		//String format is Time
		Schema schema = new Schema();
		schema.setFormat(StringFormat.DATE);
		
		JsonObject formatElement = new JsonObject();
		formatElement.addProperty("value", "1998-20-12");
		
		SchemaValidationException schemaValidationExceptionTimeFormat = assertThrows(SchemaValidationException.class,
				() -> StringValidator.validate(null, schema, formatElement.get("value")));

		assertEquals(formatElement.get("value").toString() + " is not matched with the " + "date pattern", schemaValidationExceptionTimeFormat.getMessage());
		
	}
	
	@Test
	public void StringValidatorTestForValidationIfDateTimePatternMatched() {
		
		//String format is Time
		Schema schema = new Schema();
		schema.setFormat(StringFormat.DATETIME);
		
		JsonObject formatElement = new JsonObject();
		formatElement.addProperty("value", "2018-12-25 23:50:55.999");
		
		SchemaValidationException schemaValidationExceptionTimeFormat = assertThrows(SchemaValidationException.class,
				() -> StringValidator.validate(null, schema, formatElement.get("value")));

		assertEquals(formatElement.get("value").toString() + " is not matched with the " + "date time pattern", schemaValidationExceptionTimeFormat.getMessage());

	}

	@Test
	public void StringValidatorTestForDateOnTenthAndTwentieth() {

		Schema schema = new Schema();
		schema.setFormat(StringFormat.DATE);

		for (String value : new String[] { "2026-01-10", "2026-01-20", "2026-01-19", "2026-01-29" }) {
			JsonPrimitive element = new JsonPrimitive(value);
			assertEquals(element, StringValidator.validate(null, schema, element));
		}
	}

	@Test
	public void StringValidatorTestRejectsMonthZero() {

		// The month alternation was ([0][0-9]|[1][0-2]), so 00 matched the first
		// branch. A date of 2026-00-15 validated on all three runtimes and then
		// failed wherever something tried to make a real date out of it.
		assertInvalid(StringFormat.DATE, "date pattern", "2026-00-15", "2026-00-01");
		assertInvalid(StringFormat.DATETIME, "date time pattern", "2026-00-15T10:00:00Z");

		assertValid(StringFormat.DATE, "2026-01-15", "2026-09-30", "2026-10-01", "2026-12-31");
	}

	@Test
	public void StringValidatorTestForFractionalSecondsAndZulu() {

		assertValid(StringFormat.DATETIME, "2026-01-15T10:00:00Z", "2026-01-15T10:00:00.123Z",
				"2026-01-15T10:00:00.123456789+05:30");
		assertInvalid(StringFormat.DATETIME, "date time pattern", "2026-01-15T10:00.123",
				"2026-01-15T10:00:00.1234567890Z", "2026-01-15T10:00:00.Z");

		assertValid(StringFormat.TIME, "10:00:00Z", "10:00:00.123", "10:00:00.123+05:30");
		assertInvalid(StringFormat.TIME, "time pattern", "10:00.5", "10:00:00.");
	}

	@Test
	public void StringValidatorTestForDecimal() {

		assertValid(StringFormat.DECIMAL, "0", "100", "-12.50", "+3.14159");
		assertInvalid(StringFormat.DECIMAL, "decimal pattern", "1e5", "12.", ".5", "1,000", "abc", "");
	}

	@Test
	public void StringValidatorTestForId() {

		assertValid(StringFormat.ID, "65f1c2a9e4b0a1b2c3d4e5f6", "01J9ZQ3V8K2M4N6P8R0S2T4V6W",
				"01j9zq3v8k2m4n6p8r0s2t4v6w");
		assertInvalid(StringFormat.ID, "id pattern", "65f1c2a9e4b0a1b2c3d4e5fg", "01J9ZQ3V8K2M4N6P8R0S2T4V6U",
				"01J9ZQ3V8K2M4N6P8R0S2T4V6", "abc");
	}

	private static void assertValid(StringFormat format, String... values) {

		Schema schema = new Schema();
		schema.setFormat(format);

		for (String value : values) {
			JsonPrimitive element = new JsonPrimitive(value);
			assertEquals(element, StringValidator.validate(null, schema, element));
		}
	}

	private static void assertInvalid(StringFormat format, String patternName, String... values) {

		Schema schema = new Schema();
		schema.setFormat(format);

		for (String value : values) {
			JsonPrimitive element = new JsonPrimitive(value);
			SchemaValidationException ex = assertThrows(SchemaValidationException.class,
					() -> StringValidator.validate(null, schema, element));
			assertEquals(element.toString() + " is not matched with the " + patternName, ex.getMessage());
		}
	}

	@Test
	public void StringValidatorTestForDateTimeOnTenthAndTwentieth() {

		Schema schema = new Schema();
		schema.setFormat(StringFormat.DATETIME);

		for (String value : new String[] { "2026-01-10T10:00:00", "2026-01-20T10:00:00", "2026-01-20T10:00:00+05:30" }) {
			JsonPrimitive element = new JsonPrimitive(value);
			assertEquals(element, StringValidator.validate(null, schema, element));
		}
	}
	
    @Test
    void StringValidatorTestForValidationIfEmailPatternNotMatched() {

        Schema schema = new Schema();
        schema.setFormat(StringFormat.EMAIL);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", "testemail fai%6&8ls@gmail.com");

        SchemaValidationException schemaValidationExceptionEx = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(null, schema, jsonObject.get("value")));

        assertEquals(jsonObject.get("value").toString() + " is not matched with the " + "email pattern",
                schemaValidationExceptionEx.getMessage());
    }

    @Test
    void StringValidatorTestForValidationIfEmailPatternMatched() {

        Schema schema = new Schema();
        schema.setFormat(StringFormat.EMAIL);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", "testemai_fai%6&8lworkings@gmagil.com");

        assertEquals(jsonObject.get("value").toString(),
                StringValidator.validate(null, schema, jsonObject.get("value")).toString());
    }

    @Test
    void customMessaageTest() {
        Schema schema = new Schema().setType(Type.of(SchemaType.STRING)).setMinLength(10).setDetails(new SchemaDetails().setValidationMessages(Map.of(SchemaDetails.MIN_LENGTH, "It is not a minimum of 10 characters")));

        assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(null, schema, new JsonPrimitive("Something")));
    }
}
