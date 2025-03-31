package com.fincity.nocode.kirun.engine.json.schema;

import static java.util.Map.entry;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.string.StringFormat;
import com.fincity.nocode.kirun.engine.json.schema.string.StringSchema;
import com.fincity.nocode.kirun.engine.json.schema.type.MultipleType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.SingleType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Schema implements Serializable {

	private static final String ADDITIONAL_PROPERTY = "additionalProperty";
	private static final String ADDITIONAL_ITEM = "additionalItem";
	private static final String ENUMS = "enums";
	private static final String ITEMS_STRING = "items";
	private static final String SCHEMA_ROOT_PATH = "System.Schema";
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
									new AdditionalType().setSchemaValue(Schema.ofRef(SCHEMA_ROOT_PATH)))),
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
									.setAdditionalProperties(new AdditionalType()
											.setSchemaValue(Schema.ofRef(SCHEMA_ROOT_PATH)))),

					entry(ITEMS_STRING, new Schema().setName(ITEMS_STRING)
							.setAnyOf(List.of(Schema.ofRef(SCHEMA_ROOT_PATH)
									.setName("item"), Schema.ofArray("tuple", Schema.ofRef(SCHEMA_ROOT_PATH))))),

					entry("contains", Schema.ofRef(SCHEMA_ROOT_PATH)),
					entry("minContains", Schema.ofInteger("minContains")),
					entry("maxContains", Schema.ofInteger("maxContains")),
					entry("minItems", ofInteger("minItems")),
					entry("maxItems", ofInteger("maxItems")), entry("uniqueItems", ofBoolean("uniqueItems")),
					entry("additionalItems", new Schema().setName(ADDITIONAL_ITEM)
							.setNamespace(Namespaces.SYSTEM)
							.setAnyOf(List.of(ofBoolean(ADDITIONAL_ITEM),
									Schema.ofObject(ADDITIONAL_ITEM).setRef(
											SCHEMA_ROOT_PATH)))),
					entry("$defs", Schema.of("$defs", SchemaType.OBJECT)
							.setAdditionalProperties(
									new AdditionalType().setSchemaValue(Schema.ofRef(SCHEMA_ROOT_PATH)))),

					entry("permission", ofString("permission")),
					entry("uiHelper", Schema.ofObject("uiHelper")),

					entry("details", ofObject("details"))))
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

	public static Schema ofAnyNotNull(String id) {
		return new Schema()
				.setType(Type.of(SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE,
						SchemaType.STRING, SchemaType.BOOLEAN, SchemaType.ARRAY, SchemaType.OBJECT))
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
	private AdditionalType additionalProperties;
	private List<String> required;
	private StringSchema propertyNames;
	private Integer minProperties;
	private Integer maxProperties;
	private Map<String, Schema> patternProperties;

	// Array
	private ArraySchemaType items;
	private Schema contains;
	private Integer minContains;
	private Integer maxContains;
	private Integer minItems;
	private Integer maxItems;
	private Boolean uniqueItems;
	private AdditionalType additionalItems;

	private Map<String, Schema> $defs; // NOSONAR - needed as per json schema
	private String permission;

	private Map<String, Object> details; // NOSONAR - needed as per json schema

	private Map<String, Object> uiHelper;

	public String getTitle() {

		if (this.namespace == null || this.namespace.equals(TEMPORARY))
			return this.name;

		return this.namespace + "." + this.name;
	}

	public String getFullName() {

		return this.namespace + "." + this.name;
	}

	public Map<String, Schema> get$defs() { // NOSONAR - needed as per json schema

		return this.$defs;
	}

	public Schema set$defs(Map<String, Schema> $defs) { // NOSONAR - needed as per json schema

		this.$defs = $defs;
		return this;
	}

	public Schema(Schema schema) { // NOSONAR
		// Need to put all the fields in this constructor

		this.namespace = schema.namespace;
		this.name = schema.name;

		this.version = schema.version;
		this.ref = schema.ref;

		if (schema.type != null) {
			this.type = schema.type instanceof SingleType st ? new SingleType(st)
					: new MultipleType((MultipleType) schema.type);
		}

		this.anyOf = schema.anyOf == null ? null
				: schema.anyOf.stream()
						.map(Schema::new)
						.toList();
		this.allOf = schema.allOf == null ? null
				: schema.allOf.stream()
						.map(Schema::new)
						.toList();
		this.oneOf = schema.oneOf == null ? null
				: schema.oneOf.stream()
						.map(Schema::new)
						.toList();

		this.not = this.not == null ? null : new Schema(this.not);

		this.description = schema.description;
		this.examples = schema.examples == null ? null
				: schema.examples.stream()
						.map(JsonElement::deepCopy)
						.toList();
		this.defaultValue = schema.defaultValue == null ? null : schema.defaultValue.deepCopy();
		this.comment = schema.comment;
		this.enums = schema.enums == null ? null
				: schema.enums.stream()
						.map(JsonElement::deepCopy)
						.toList();
		this.constant = schema.constant == null ? null : schema.constant.deepCopy();

		this.pattern = schema.pattern;
		this.format = schema.format;

		this.minLength = schema.minLength;
		this.maxLength = schema.maxLength;

		this.multipleOf = schema.multipleOf;
		this.minimum = schema.minimum;
		this.maximum = schema.maximum;
		this.exclusiveMinimum = schema.exclusiveMinimum;
		this.exclusiveMaximum = schema.exclusiveMaximum;

		this.properties = schema.properties == null ? null
				: schema.properties.entrySet()
						.stream()
						.collect(Collectors.toMap(Entry::getKey, e -> new Schema(e.getValue())));

		this.additionalProperties = schema.additionalProperties == null ? null
				: new AdditionalType(schema.additionalProperties);

		this.required = schema.required == null ? null
				: schema.required.stream()
						.toList();

		this.propertyNames = schema.propertyNames == null ? null : new StringSchema(schema.propertyNames);
		this.minProperties = schema.minProperties;
		this.maxProperties = schema.maxProperties;

		this.patternProperties = schema.patternProperties == null ? null
				: schema.patternProperties.entrySet()
						.stream()
						.collect(Collectors.toMap(Entry::getKey, e -> new Schema(e.getValue())));

		this.items = schema.items == null ? null : new ArraySchemaType(schema.items);
		this.contains = schema.contains == null ? null : new Schema(this.contains);
		this.minContains = schema.minContains;
		this.maxContains = schema.maxContains;
		this.minItems = schema.minItems;
		this.maxItems = schema.maxItems;
		this.additionalItems = schema.additionalItems == null ? null : new AdditionalType(schema.additionalItems);
		this.uniqueItems = schema.uniqueItems;

		this.$defs = schema.$defs == null ? null
				: schema.$defs.entrySet()
						.stream()
						.collect(Collectors.toMap(Entry::getKey, e -> new Schema(e.getValue())));

		this.permission = schema.permission;

		this.details = schema.details == null ? null : Map.copyOf(schema.details);

		this.uiHelper = schema.uiHelper == null ? null
                : schema.uiHelper.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
}
