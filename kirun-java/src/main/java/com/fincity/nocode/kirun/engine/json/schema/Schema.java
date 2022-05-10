package com.fincity.nocode.kirun.engine.json.schema;

import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.ARRAY;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.BOOLEAN;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.DOUBLE;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.FLOAT;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.INTEGER;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.LONG;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.NULL;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.OBJECT;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.STRING;
import static java.util.Map.entry;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.json.schema.string.StringFormat;
import com.fincity.nocode.kirun.engine.json.schema.string.StringSchema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Schema implements Serializable {

	private static final String ITEMS_STRING = "items";
	private static final String SHEMA_ROOT_PATH = "#/";
	private static final String REQUIRED_STRING = "required";
	private static final String VERSION_STRING = "version";
	private static final String NAMESPACE_STRING = "namespace";

	private static final long serialVersionUID = 4041990622586726910L;

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM).setVersion(1)
			.setType(Type.of(SchemaType.OBJECT)).setTitle("Schema").setName("Schema")
			.setProperties(Map.ofEntries(
					entry(NAMESPACE_STRING, Schema.of(NAMESPACE_STRING, STRING)),
					entry("name", Schema.of("name", STRING)),
					entry(VERSION_STRING, Schema.of(VERSION_STRING, INTEGER)),
					entry("ref", Schema.of("ref", STRING)),
					entry("type", Schema.ofArray("type", new Schema().setType(Type.of(STRING))
									.setEnums(List.of(new JsonPrimitive("INTEGER"), new JsonPrimitive("LONG"),
											new JsonPrimitive("FLOAT"), new JsonPrimitive("DOUBLE"),
											new JsonPrimitive("STRING"), new JsonPrimitive("OBJECT"),
											new JsonPrimitive("ARRAY"), new JsonPrimitive("BOOLEAN"),
											new JsonPrimitive("NULL"))))),
					entry("anyOf", Schema.ofArray("anyOf", Schema.ofRef(SHEMA_ROOT_PATH))),
					entry("allOf", Schema.ofArray("allOf", Schema.ofRef(SHEMA_ROOT_PATH))),
					entry("oneOf", Schema.ofArray("oneOf", Schema.ofRef(SHEMA_ROOT_PATH))),

					entry("not", Schema.ofRef(SHEMA_ROOT_PATH)),
					entry("title", Schema.of("title", STRING)),
					entry("description", Schema.of("description", STRING)),
					entry("id", Schema.of("id", STRING)),
					entry("examples",  Schema.ofArray("examples", Schema.of("example", INTEGER, LONG, FLOAT, DOUBLE, STRING, OBJECT, ARRAY, BOOLEAN, NULL))),
					entry("defaultValue", Schema.of("defaultValue", INTEGER, LONG, FLOAT, DOUBLE, STRING, OBJECT, ARRAY, BOOLEAN, NULL)),
					entry("comment", Schema.of("comment", STRING)),
					entry("enums", Schema.ofArray("enums", Schema.of("enum", STRING))),
					entry("constant", Schema.of("constant", INTEGER, LONG, FLOAT, DOUBLE, STRING, OBJECT, ARRAY, BOOLEAN, NULL)),
					
					entry("pattern", Schema.of("pattern", STRING)),
					entry("format", Schema.of("format", STRING).setEnums(List.of(new JsonPrimitive("DATETIME"),
							new JsonPrimitive("TIME"),
							new JsonPrimitive("DATE"),
							new JsonPrimitive("EMAIL"),
							new JsonPrimitive("REGEX")))),
					entry("minLength", Schema.of("minLength", INTEGER)),
					entry("maxLength", Schema.of("maxLength", INTEGER)),
					
					entry("multipleOf", Schema.of("multipleOf", LONG)),
					entry("minimum", Schema.of("minimum", INTEGER, LONG, DOUBLE, FLOAT)),
					entry("maximum", Schema.of("maximum", INTEGER, LONG, DOUBLE, FLOAT)),
					entry("exclusiveMinimum", Schema.of("exclusiveMinimum", INTEGER, LONG, DOUBLE, FLOAT)),
					entry("exclusiveMaximum", Schema.of("exclusiveMaximum", INTEGER, LONG, DOUBLE, FLOAT)),
					
					entry("properties", Schema.of("properties", OBJECT).setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(Schema.ofRef(SHEMA_ROOT_PATH)))),
					entry("additionalProperties", Schema.of("additionalProperties", BOOLEAN, OBJECT).setRef(SHEMA_ROOT_PATH)),
					entry(REQUIRED_STRING, Schema.ofArray(REQUIRED_STRING, Schema.of(REQUIRED_STRING, STRING))),
					entry("propertyNames", Schema.ofRef(SHEMA_ROOT_PATH)),
					entry("minProperties", Schema.of("minProperties", INTEGER)),
					entry("maxProperties", Schema.of("maxProperties", INTEGER)),
					entry("patternProperties", Schema.of("patternProperties", OBJECT).setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(Schema.ofRef(SHEMA_ROOT_PATH)))),
					
					entry(ITEMS_STRING, new Schema().setName(ITEMS_STRING).setTitle(ITEMS_STRING).setAnyOf(List.of(
							Schema.ofRef(SHEMA_ROOT_PATH).setName("item").setTitle("item"),
							Schema.ofArray("tuple", Schema.ofRef(SHEMA_ROOT_PATH))
							))),
					
					entry("contains", Schema.ofRef(SHEMA_ROOT_PATH)),
					entry("minItems", Schema.of("minItems", INTEGER)),
					entry("maxItems", Schema.of("maxItems", INTEGER)),
					entry("uniqueItems", Schema.of("uniqueItems", BOOLEAN)),
					
					entry("definitions", Schema.of("definitions", OBJECT).setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(Schema.ofRef(SHEMA_ROOT_PATH)))),
					
					entry("permission", Schema.of("permission", SchemaType.STRING))
			))
			.setRequired(List.of(NAMESPACE_STRING, VERSION_STRING));

	public static Schema of(String id, SchemaType... type) {
		return new Schema().setType(Type.of(type)).setName(id).setTitle(id);
	}

	public static Schema ofRef(String ref) {
		return new Schema().setRef(ref);
	}

	public static Schema ofArray(String id, Schema... itemSchemas) {
		return new Schema().setType(Type.of(SchemaType.ARRAY)).setName(id).setTitle(id)
				.setItems(ArraySchemaType.of(itemSchemas));
	}

	private String namespace;
	private String name;

	private int version;

	private String ref;

	private Type type;
	private List<Schema> anyOf;
	private List<Schema> allOf;
	private List<Schema> oneOf;
	private Schema not;

	private String title;
	private String description;
	private List<JsonElement> examples; // NOSONAR - JSON Element for some reason is not serialised.
	private JsonElement defaultValue; // NOSONAR - JSON Element for some reason is not serialised.
	private String comment;
	private List<JsonElement> enums; // NOSONAR - JSON Element for some reason is not serialised.
	private JsonElement constant; // NOSONAR - JSON Element for some reason is not serialised.

	// String
	private String pattern;
	private StringFormat format;
	private Integer minLength;
	private Integer maxLength;

	// Number
	private Long multipleOf;
	private Number minimum;
	private Number maximum;
	private Number exclusiveMinimum;
	private Number exclusiveMaximum;

	// Object
	private Map<String, Schema> properties;
	private AdditionalPropertiesType additionalProperties;
	private List<String> required;
	private StringSchema propertyNames;
	private Integer minProperties;
	private Integer maxProperties;
	private Map<String, Schema> patternProperties;

	// Array
	private ArraySchemaType items;
	private Schema contains;
	private Integer minItems;
	private Integer maxItems;
	private Boolean uniqueItems;

	private Map<String, Schema> def;
	private String permission;
}
