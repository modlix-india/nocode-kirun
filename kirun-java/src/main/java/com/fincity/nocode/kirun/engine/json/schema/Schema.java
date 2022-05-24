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

	public static final Schema STRING = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setTitle("String")
	        .setType(Type.of(SchemaType.STRING));
	public static final Schema NUMBER = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setTitle("Number")
	        .setType(Type.of(SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE));
	public static final Schema INTEGER = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setTitle("Integer")
	        .setType(Type.of(SchemaType.INTEGER));
	public static final Schema LONG = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setTitle("Long")
	        .setType(Type.of(SchemaType.LONG));
	public static final Schema FLOAT = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setTitle("Float")
	        .setType(Type.of(SchemaType.FLOAT));
	public static final Schema DOUBLE = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setTitle("Double")
	        .setType(Type.of(SchemaType.DOUBLE));
	public static final Schema BOOLEAN = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setTitle("Boolean")
	        .setType(Type.of(SchemaType.BOOLEAN));
	public static final Schema ANY = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setTitle("Any")
	        .setType(Type.of(SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE,
	                SchemaType.STRING, SchemaType.BOOLEAN, SchemaType.ARRAY, SchemaType.NULL, SchemaType.OBJECT));

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setType(Type.of(SchemaType.OBJECT))
	        .setTitle("Schema")
	        .setName("Schema")
	        .setProperties(Map.ofEntries(
	                entry(NAMESPACE_STRING, STRING), entry("name", STRING), entry(VERSION_STRING,
	                        Schema.of(VERSION_STRING, SchemaType.INTEGER)
	                                .setDefaultValue(new JsonPrimitive(1))),
	                entry("ref", STRING),
	                entry("type", Schema.ofArray("type", new Schema().setType(Type.of(SchemaType.STRING))
	                        .setEnums(List.of(new JsonPrimitive("INTEGER"), new JsonPrimitive("LONG"),
	                                new JsonPrimitive("FLOAT"), new JsonPrimitive("DOUBLE"),
	                                new JsonPrimitive("STRING"), new JsonPrimitive("OBJECT"),
	                                new JsonPrimitive("ARRAY"), new JsonPrimitive("BOOLEAN"),
	                                new JsonPrimitive("NULL"))))),
	                entry("anyOf", Schema.ofArray("anyOf", Schema.ofRef(SHEMA_ROOT_PATH))),
	                entry("allOf", Schema.ofArray("allOf", Schema.ofRef(SHEMA_ROOT_PATH))),
	                entry("oneOf", Schema.ofArray("oneOf", Schema.ofRef(SHEMA_ROOT_PATH))),

	                entry("not", Schema.ofRef(SHEMA_ROOT_PATH)), entry("title", STRING), entry("description", STRING),
	                entry("id", STRING), entry("examples", ANY), entry("defaultValue", ANY), entry("comment", STRING),
	                entry("enums", Schema.ofArray("enums", STRING)), entry("constant", ANY),

	                entry("pattern", STRING), entry("format", Schema.of("format", SchemaType.STRING)
	                        .setEnums(List.of(new JsonPrimitive("DATETIME"), new JsonPrimitive("TIME"),
	                                new JsonPrimitive("DATE"), new JsonPrimitive("EMAIL"),
	                                new JsonPrimitive("REGEX")))),
	                entry("minLength", INTEGER), entry("maxLength", INTEGER),

	                entry("multipleOf", LONG), entry("minimum", NUMBER), entry("maximum", NUMBER),
	                entry("exclusiveMinimum", NUMBER), entry("exclusiveMaximum", NUMBER),

	                entry("properties", Schema.of("properties", SchemaType.OBJECT)
	                        .setAdditionalProperties(
	                                new AdditionalPropertiesType().setSchemaValue(Schema.ofRef(SHEMA_ROOT_PATH)))),
	                entry("additionalProperties",
	                        Schema.of("additionalProperties", SchemaType.BOOLEAN, SchemaType.OBJECT)
	                                .setRef(SHEMA_ROOT_PATH)),
	                entry(REQUIRED_STRING, Schema.ofArray(REQUIRED_STRING, STRING)),
	                entry("propertyNames", Schema.ofRef(SHEMA_ROOT_PATH)), entry("minProperties", INTEGER),
	                entry("maxProperties", INTEGER), entry("patternProperties",
	                        Schema.of("patternProperties", SchemaType.OBJECT)
	                                .setAdditionalProperties(new AdditionalPropertiesType()
	                                        .setSchemaValue(Schema.ofRef(SHEMA_ROOT_PATH)))),

	                entry(ITEMS_STRING, new Schema().setName(ITEMS_STRING)
	                        .setTitle(ITEMS_STRING)
	                        .setAnyOf(List.of(Schema.ofRef(SHEMA_ROOT_PATH)
	                                .setName("item")
	                                .setTitle("item"), Schema.ofArray("tuple", Schema.ofRef(SHEMA_ROOT_PATH))))),

	                entry("contains", Schema.ofRef(SHEMA_ROOT_PATH)), entry("minItems", INTEGER),
	                entry("maxItems", INTEGER), entry("uniqueItems", BOOLEAN),

	                entry("definitions", Schema.of("definitions", SchemaType.OBJECT)
	                        .setAdditionalProperties(
	                                new AdditionalPropertiesType().setSchemaValue(Schema.ofRef(SHEMA_ROOT_PATH)))),

	                entry("permission", STRING)))
	        .setRequired(List.of(NAMESPACE_STRING, VERSION_STRING));

	public static Schema of(String id, SchemaType... types) {
		return new Schema().setType(Type.of(types))
		        .setName(id)
		        .setTitle(id);
	}

	public static Schema ofObject(String id) {
		return new Schema().setType(Type.of(SchemaType.OBJECT))
		        .setName(id)
		        .setTitle(id);
	}

	public static Schema ofRef(String ref) {
		return new Schema().setRef(ref);
	}

	public static Schema ofArray(String id, Schema... itemSchemas) {
		return new Schema().setType(Type.of(SchemaType.ARRAY))
		        .setName(id)
		        .setTitle(id)
		        .setItems(ArraySchemaType.of(itemSchemas));
	}

	private String namespace;
	private String name;

	private int version = 1;

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

	public String getTitle() {

		if (title != null)
			return this.title;

		if (this.namespace == null)
			return this.name;

		return this.namespace + "." + this.name;
	}
}
