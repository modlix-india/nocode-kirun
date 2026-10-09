package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;

/**
 * The copy constructors of Schema, AdditionalType and ArraySchemaType.
 *
 * A boolean {@code additionalProperties} (or {@code additionalItems}) used to make
 * {@code new Schema(schema)} throw a NullPointerException, because the AdditionalType
 * copy built a Schema from its null schemaValue. {@code not}, {@code contains} and a
 * tuple {@code items} were copied from the object under construction instead of the
 * source, so they were dropped or threw.
 */
class SchemaCopyTest {

	@Test
	void aBooleanAdditionalPropertiesIsCopied() {

		Schema source = Schema.ofObject("row")
		        .setProperties(Map.of("name", Schema.ofString("name")))
		        .setAdditionalProperties(new AdditionalType(false));

		Schema copy = new Schema(source);

		assertNotNull(copy.getAdditionalProperties());
		assertEquals(Boolean.FALSE, copy.getAdditionalProperties().getBooleanValue());
		assertNull(copy.getAdditionalProperties().getSchemaValue());
		assertNotSame(source.getAdditionalProperties(), copy.getAdditionalProperties());
		assertFalse(AdditionalType.canHaveAddtionalItems(copy.getAdditionalProperties()));
	}

	@Test
	void aBooleanAdditionalPropertiesDeepInsideIsCopied() {

		Schema nested = Schema.ofObject("address")
		        .setAdditionalProperties(new AdditionalType(true));

		Schema source = Schema.ofObject("row")
		        .setProperties(Map.of("address", nested))
		        .setAdditionalItems(new AdditionalType(false));

		Schema copy = new Schema(source);

		assertEquals(Boolean.TRUE, copy.getProperties()
		        .get("address")
		        .getAdditionalProperties()
		        .getBooleanValue());
		assertEquals(Boolean.FALSE, copy.getAdditionalItems()
		        .getBooleanValue());
	}

	@Test
	void aSchemaAdditionalPropertiesIsStillDeepCopied() {

		Schema source = Schema.ofObject("map")
		        .setAdditionalProperties(new AdditionalType().setSchemaValue(Schema.ofString("value")));

		Schema copy = new Schema(source);

		assertNull(copy.getAdditionalProperties().getBooleanValue());
		assertEquals("value", copy.getAdditionalProperties().getSchemaValue().getName());
		assertNotSame(source.getAdditionalProperties().getSchemaValue(),
		        copy.getAdditionalProperties().getSchemaValue());
	}

	@Test
	void notAndContainsAreCopiedFromTheSource() {

		Schema source = Schema.ofArray("list", Schema.ofString("item"))
		        .setNot(Schema.ofString("forbidden"))
		        .setContains(Schema.ofString("needed"));

		Schema copy = new Schema(source);

		assertNotNull(copy.getNot());
		assertEquals("forbidden", copy.getNot().getName());
		assertNotSame(source.getNot(), copy.getNot());
		assertNotNull(copy.getContains());
		assertEquals("needed", copy.getContains().getName());
	}

	@Test
	void aTupleItemsIsCopied() {

		ArraySchemaType tuple = ArraySchemaType.of(Schema.ofString("first"), Schema.ofInteger("second"));
		assertFalse(tuple.isSingleType());

		ArraySchemaType copy = new ArraySchemaType(tuple);

		assertEquals(2, copy.getTupleSchema().size());
		assertEquals("second", copy.getTupleSchema().get(1).getName());

		Schema arr = new Schema(Schema.ofArray("pair").setItems(tuple));
		assertTrue(arr.getItems().getTupleSchema() != null && arr.getItems().getTupleSchema().size() == 2);
	}
}
