package com.fincity.nocode.kirun.engine.json.schema;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Schema implements Serializable {

	private static final String ADDITIONAL_PROPERTY = "additionalProperty";
	private static final String ENUMS = "enums";
	private static final String ITEMS_STRING = "items";
	private static final String SCHEMA_ROOT_PATH = "#/";
	private static final String REQUIRED_STRING = "required";
	private static final String VERSION_STRING = "version";
	private static final String NAMESPACE_STRING = "namespace";
	private static final String TEMPORARY = "_";

	private static final long serialVersionUID = 4041990622586726910L;

	public static final Schema NULL = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName("Null")
	        .setType(Type.of(SchemaType.NULL))
	        .setConstant(JsonNull.INSTANCE);

	private static final Schema TYPE_SCHEMA = new Schema().setType(Type.of(SchemaType.STRING))
	        .setEnums(List.of(new JsonPrimitive("INTEGER"), new JsonPrimitive("LONG"), new JsonPrimitive("FLOAT"),
	                new JsonPrimitive("DOUBLE"), new JsonPrimitive("STRING"), new JsonPrimitive("OBJECT"),
	                new JsonPrimitive("ARRAY"), new JsonPrimitive("BOOLEAN"), new JsonPrimitive("NULL")));

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName("Schema")
	        .setType(Type.of(SchemaType.OBJECT))
	        .setProperties(Map.ofEntries(entry(NAMESPACE_STRING, Schema.of(NAMESPACE_STRING, SchemaType.STRING)
	                .setDefaultValue(new JsonPrimitive(TEMPORARY))), entry("name", ofString("name")),
	                entry(VERSION_STRING, Schema.of(VERSION_STRING, SchemaType.INTEGER)
	                        .setDefaultValue(new JsonPrimitive(1))),
	                entry("ref", ofString("ref")),
	                entry("type", new Schema().setAnyOf(List.of(TYPE_SCHEMA, Schema.ofArray("type", TYPE_SCHEMA)))),
	                entry("anyOf", Schema.ofArray("anyOf", Schema.ofRef(SCHEMA_ROOT_PATH))),
	                entry("allOf", Schema.ofArray("allOf", Schema.ofRef(SCHEMA_ROOT_PATH))),
	                entry("oneOf", Schema.ofArray("oneOf", Schema.ofRef(SCHEMA_ROOT_PATH))),

	                entry("not", Schema.ofRef(SCHEMA_ROOT_PATH)), entry("title", ofString("title")),
	                entry("description", ofString("description")), entry("id", ofString("id")),
	                entry("examples", ofAny("examples")), entry("defaultValue", ofAny("defaultValue")),
	                entry("comment", ofString("comment")), entry(ENUMS, Schema.ofArray(ENUMS, ofString(ENUMS))),
	                entry("constant", ofAny("constant")),

	                entry("pattern", ofString("pattern")), entry("format", Schema.of("format", SchemaType.STRING)
	                        .setEnums(List.of(new JsonPrimitive("DATETIME"), new JsonPrimitive("TIME"),
	                                new JsonPrimitive("DATE"), new JsonPrimitive("EMAIL"),
	                                new JsonPrimitive("REGEX")))),
	                entry("minLength", ofInteger("minLength")), entry("maxLength", ofInteger("maxLength")),

	                entry("multipleOf", ofLong("multipleOf")), entry("minimum", ofNumber("minimum")),
	                entry("maximum", ofNumber("maximum")), entry("exclusiveMinimum", ofNumber("exclusiveMinimum")),
	                entry("exclusiveMaximum", ofNumber("exclusiveMaximum")),

	                entry("properties", Schema.of("properties", SchemaType.OBJECT)
	                        .setAdditionalProperties(
	                                new AdditionalPropertiesType().setSchemaValue(Schema.ofRef(SCHEMA_ROOT_PATH)))),
	                entry("additionalProperties", new Schema().setName(ADDITIONAL_PROPERTY)
	                        .setNamespace(Namespaces.SYSTEM)
	                        .setAnyOf(List.of(ofBoolean(ADDITIONAL_PROPERTY), Schema.ofObject(ADDITIONAL_PROPERTY)
	                                .setRef(SCHEMA_ROOT_PATH)))
	                        .setDefaultValue(new JsonPrimitive(true))),
	                entry(REQUIRED_STRING, Schema.ofArray(REQUIRED_STRING, ofString(REQUIRED_STRING))
	                        .setDefaultValue(new JsonArray())),
	                entry("propertyNames", Schema.ofRef(SCHEMA_ROOT_PATH)),
	                entry("minProperties", ofInteger("minProperties")),
	                entry("maxProperties", ofInteger("maxProperties")), entry("patternProperties",
	                        Schema.of("patternProperties", SchemaType.OBJECT)
	                                .setAdditionalProperties(new AdditionalPropertiesType()
	                                        .setSchemaValue(Schema.ofRef(SCHEMA_ROOT_PATH)))),

	                entry(ITEMS_STRING, new Schema().setName(ITEMS_STRING)
	                        .setAnyOf(List.of(Schema.ofRef(SCHEMA_ROOT_PATH)
	                                .setName("item"), Schema.ofArray("tuple", Schema.ofRef(SCHEMA_ROOT_PATH))))),

	                entry("contains", Schema.ofRef(SCHEMA_ROOT_PATH)), entry("minItems", ofInteger("minItems")),
	                entry("maxItems", ofInteger("maxItems")), entry("uniqueItems", ofBoolean("uniqueItems")),

	                entry("$defs", Schema.of("$defs", SchemaType.OBJECT)
	                        .setAdditionalProperties(
	                                new AdditionalPropertiesType().setSchemaValue(Schema.ofRef(SCHEMA_ROOT_PATH)))),

	                entry("permission", ofString("permission"))))
	        .setRequired(List.of());

	public static Schema ofString(String id) {
		return new Schema().setType(Type.of(SchemaType.STRING))
		        .setName(id);
	}

	public static Schema ofInteger(String id) {
		return new Schema().setType(Type.of(SchemaType.INTEGER))
		        .setName(id);
	}

	public static Schema ofFloat(String id) {
		return new Schema().setType(Type.of(SchemaType.FLOAT))
		        .setName(id);
	}

	public static Schema ofLong(String id) {
		return new Schema().setType(Type.of(SchemaType.LONG))
		        .setName(id);
	}

	public static Schema ofDouble(String id) {
		return new Schema().setType(Type.of(SchemaType.DOUBLE))
		        .setName(id);
	}

	public static Schema ofAny(String id) {
		return new Schema()
		        .setType(Type.of(SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE,
		                SchemaType.STRING, SchemaType.BOOLEAN, SchemaType.ARRAY, SchemaType.NULL, SchemaType.OBJECT))
		        .setName(id);
	}

	public static Schema ofNumber(String id) {
		return new Schema().setType(Type.of(SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE))
		        .setName(id);
	}

	public static Schema ofBoolean(String id) {
		return new Schema().setType(Type.of(SchemaType.BOOLEAN))
		        .setName(id);
	}

	public static Schema of(String id, SchemaType... types) {
		return new Schema().setType(Type.of(types))
		        .setName(id);
	}

	public static Schema ofObject(String id) {
		return new Schema().setType(Type.of(SchemaType.OBJECT))
		        .setName(id);
	}

	public static Schema ofRef(String ref) {
		return new Schema().setRef(ref);
	}

	public static Schema ofArray(String id, Schema... itemSchemas) {
		return new Schema().setType(Type.of(SchemaType.ARRAY))
		        .setName(id)
		        .setItems(ArraySchemaType.of(itemSchemas));
	}

	private String namespace = TEMPORARY;
	private String name;

	private int version = 1;

	private String ref;

	private Type type;
	private List<Schema> anyOf;
	private List<Schema> allOf;
	private List<Schema> oneOf;
	private Schema not;

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

	private Map<String, Schema> $defs; // NOSONAR - needed as per json schema
	private String permission;

	public String getTitle() {

		return this.getFullName();
	}

	private String getFullName() {

		if (this.namespace == null || this.namespace.equals(TEMPORARY))
			return this.name;

		return this.namespace + "." + this.name;
	}

	public Map<String, Schema> get$defs() { // NOSONAR - needed as per json schema

		return this.$defs;
	}

	public Schema set$defs(Map<String, Schema> $defs) { // NOSONAR - needed as per json schema

		this.$defs = $defs;
		return this;
	}
}
