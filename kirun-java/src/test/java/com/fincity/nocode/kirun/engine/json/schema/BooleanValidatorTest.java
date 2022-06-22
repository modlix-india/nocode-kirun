package com.fincity.nocode.kirun.engine.json.schema;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.validator.BooleanValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;

class BooleanValidatorTest {

	
	@Test
	void booleanValidatorValidateTest() {
		
		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
				() -> BooleanValidator.validate(null, null, null));

		assertEquals("Expected a boolean but found null",schemaValidationException.getMessage());
	}
	
	
}
