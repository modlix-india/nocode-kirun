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
