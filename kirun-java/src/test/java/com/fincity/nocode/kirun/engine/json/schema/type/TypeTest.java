package com.fincity.nocode.kirun.engine.json.schema.type;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

class TypeTest {

	@Test
	void test() {
		assertEquals(Type.of(SchemaType.BOOLEAN).getAllowedSchemaTypes(), Set.of(SchemaType.BOOLEAN));
		assertEquals(Type.of(SchemaType.BOOLEAN, SchemaType.DOUBLE).getAllowedSchemaTypes(),
				Set.of(SchemaType.DOUBLE, SchemaType.BOOLEAN));
	}

}
