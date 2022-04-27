package com.fincity.nocode.kirun.engine.json.schema;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.json.schema.string.StringFormat;
import com.fincity.nocode.kirun.engine.json.schema.string.StringSchema;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.google.gson.JsonElement;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Schema implements Serializable{

	private static final long serialVersionUID = 4041990622586726910L;

	private String namespace;
	
	private int version;
	
	private String ref;
	
	private Type type;
	private List<Schema> anyOf;
	private List<Schema> allOf;
	private List<Schema> oneOf;
	private Schema not;

	private String title;
	private String description;
	private String id;
	private List<JsonElement> examples;
	private JsonElement defaultValue;
	private String comment;
	private List<JsonElement> enums;
	private JsonElement constant;

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
	private Map<String, List<String>> dependencies;
	private Map<String, Schema> patternProperties;

	// Array
	private ArraySchemaType items;
	private Schema contains;
	private Integer minItems;
	private Integer maxItems;
	private Boolean uniqueItems;
	private List<FunctionSignature> methods;
}
