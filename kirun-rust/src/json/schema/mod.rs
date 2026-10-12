//! Port of kirun-js `json/schema/Schema.ts` (Schema, AdditionalType, SchemaDetails).

pub mod array;
pub mod convertor;
pub mod schema_util;
pub mod string;
pub mod r#type;
pub mod validator;

use std::sync::LazyLock;

use crate::namespaces::Namespaces;
use crate::util::primitive::{js_to_string, js_truthy, number_value};
use crate::{Map, Result, Value};

use self::array::ArraySchemaType;
use self::r#type::{SchemaType, Type, TypeUtil};
use self::string::StringFormat;

pub const TEMPORARY: &str = "_";
pub const SCHEMA_ROOT_PATH: &str = "System.Schema";

/// JS `AdditionalType`: `additionalProperties` / `additionalItems` is a boolean or a schema.
#[derive(Debug, Clone, PartialEq, Default)]
pub struct AdditionalType {
    pub boolean_value: Option<bool>,
    pub schema_value: Option<Box<Schema>>,
}

impl AdditionalType {
    pub fn new() -> Self {
        Self::default()
    }
    pub fn get_boolean_value(&self) -> Option<bool> {
        self.boolean_value
    }
    pub fn get_schema_value(&self) -> Option<&Schema> {
        self.schema_value.as_deref()
    }
    pub fn set_boolean_value(mut self, v: bool) -> Self {
        self.boolean_value = Some(v);
        self
    }
    pub fn set_schema_value(mut self, v: Schema) -> Self {
        self.schema_value = Some(Box::new(v));
        self
    }
    /// JS `AdditionalType.from`.
    pub fn from_value(obj: &Value) -> Result<Option<Self>> {
        if obj.is_null() {
            return Ok(None);
        }
        let mut ad = AdditionalType::new();
        match obj {
            Value::Bool(b) => ad.boolean_value = Some(*b),
            Value::Object(m) if m.contains_key("booleanValue") => {
                ad.boolean_value = m.get("booleanValue").and_then(Value::as_bool)
            }
            Value::Object(m) if m.contains_key("schemaValue") => {
                ad.schema_value = Schema::from_value(&m["schemaValue"])?.map(Box::new)
            }
            _ => ad.schema_value = Schema::from_value(obj)?.map(Box::new),
        }
        Ok(Some(ad))
    }
    /// The JS `JSON.stringify` shape: `{ booleanValue }` and/or `{ schemaValue }`.
    pub fn to_json(&self) -> Value {
        let mut m = serde_json::Map::new();
        if let Some(b) = self.boolean_value {
            m.insert("booleanValue".into(), Value::Bool(b));
        }
        if let Some(s) = &self.schema_value {
            m.insert("schemaValue".into(), s.to_json());
        }
        Value::Object(m)
    }
}

/// JS `SchemaDetails` (UI hints carried on a schema).
#[derive(Debug, Clone, PartialEq, Default)]
pub struct SchemaDetails {
    pub preferred_component: Option<String>,
    pub validation_messages: Option<Map<String>>,
    pub properties: Option<Map<Value>>,
    pub style_properties: Option<Map<Value>>,
    pub order: Option<f64>,
    pub label: Option<String>,
}

impl SchemaDetails {
    pub fn from_value(detail: &Value) -> Result<Option<Self>> {
        if !js_truthy(detail) {
            return Ok(None);
        }
        let entries = |v: &Value| -> Option<Map<Value>> {
            v.as_object().map(|o| o.iter().map(|(k, v)| (k.clone(), v.clone())).collect())
        };
        Ok(Some(SchemaDetails {
            preferred_component: detail["preferredComponent"].as_str().map(str::to_string),
            validation_messages: detail["validationMessages"].as_object().map(|o| {
                o.iter()
                    .map(|(k, v)| (k.clone(), v.as_str().map(str::to_string).unwrap_or_else(|| js_to_string(v))))
                    .collect()
            }),
            properties: entries(&detail["properties"]),
            style_properties: entries(&detail["styleProperties"]),
            order: detail["order"].as_f64(),
            label: detail["label"].as_str().map(str::to_string),
        }))
    }
    pub fn to_json(&self) -> Value {
        let mut m = serde_json::Map::new();
        if let Some(v) = &self.preferred_component {
            m.insert("preferredComponent".into(), Value::String(v.clone()));
        }
        if let Some(v) = &self.validation_messages {
            m.insert(
                "validationMessages".into(),
                Value::Object(v.iter().map(|(k, v)| (k.clone(), Value::String(v.clone()))).collect()),
            );
        }
        if let Some(v) = &self.properties {
            m.insert("properties".into(), Value::Object(v.iter().map(|(k, v)| (k.clone(), v.clone())).collect()));
        }
        if let Some(v) = &self.style_properties {
            m.insert(
                "styleProperties".into(),
                Value::Object(v.iter().map(|(k, v)| (k.clone(), v.clone())).collect()),
            );
        }
        if let Some(v) = self.order {
            m.insert("order".into(), number_value(v));
        }
        if let Some(v) = &self.label {
            m.insert("label".into(), Value::String(v.clone()));
        }
        Value::Object(m)
    }
    pub fn get_validation_message(&self, key: &str) -> Option<&String> {
        self.validation_messages.as_ref().and_then(|m| m.get(key))
    }
}

/// JS `Schema`. Numeric constraints are `f64` (JS numbers); counts are `usize`.
#[derive(Debug, Clone, PartialEq)]
pub struct Schema {
    pub namespace: String,
    pub name: Option<String>,
    pub version: i64,
    pub r#ref: Option<String>,
    pub r#type: Option<Type>,
    pub any_of: Option<Vec<Schema>>,
    pub all_of: Option<Vec<Schema>>,
    pub one_of: Option<Vec<Schema>>,
    pub not: Option<Box<Schema>>,
    pub description: Option<String>,
    pub examples: Option<Vec<Value>>,
    pub default_value: Option<Value>,
    pub comment: Option<String>,
    pub enums: Option<Vec<Value>>,
    pub constant: Option<Value>,
    pub pattern: Option<String>,
    pub format: Option<StringFormat>,
    pub min_length: Option<usize>,
    pub max_length: Option<usize>,
    pub multiple_of: Option<f64>,
    pub minimum: Option<f64>,
    pub maximum: Option<f64>,
    pub exclusive_minimum: Option<f64>,
    pub exclusive_maximum: Option<f64>,
    pub properties: Option<Map<Schema>>,
    pub additional_properties: Option<AdditionalType>,
    pub additional_items: Option<AdditionalType>,
    pub required: Option<Vec<String>>,
    pub property_names: Option<Box<Schema>>,
    pub min_properties: Option<usize>,
    pub max_properties: Option<usize>,
    pub pattern_properties: Option<Map<Schema>>,
    pub items: Option<ArraySchemaType>,
    pub contains: Option<Box<Schema>>,
    pub min_contains: Option<usize>,
    pub max_contains: Option<usize>,
    pub min_items: Option<usize>,
    pub max_items: Option<usize>,
    pub unique_items: Option<bool>,
    pub defs: Option<Map<Schema>>,
    pub permission: Option<String>,
    pub details: Option<SchemaDetails>,
    pub view_details: Option<SchemaDetails>,
}

impl Default for Schema {
    fn default() -> Self {
        Schema {
            namespace: TEMPORARY.to_string(),
            name: None,
            version: 1,
            r#ref: None,
            r#type: None,
            any_of: None,
            all_of: None,
            one_of: None,
            not: None,
            description: None,
            examples: None,
            default_value: None,
            comment: None,
            enums: None,
            constant: None,
            pattern: None,
            format: None,
            min_length: None,
            max_length: None,
            multiple_of: None,
            minimum: None,
            maximum: None,
            exclusive_minimum: None,
            exclusive_maximum: None,
            properties: None,
            additional_properties: None,
            additional_items: None,
            required: None,
            property_names: None,
            min_properties: None,
            max_properties: None,
            pattern_properties: None,
            items: None,
            contains: None,
            min_contains: None,
            max_contains: None,
            min_items: None,
            max_items: None,
            unique_items: None,
            defs: None,
            permission: None,
            details: None,
            view_details: None,
        }
    }
}

/// `get_x(&self) -> Option<&T>` and builder `set_x(self, T) -> Self` for plain optional fields.
macro_rules! optional_accessors {
    ($($field:ident : $ty:ty => $get:ident, $set:ident;)*) => {
        $(
            pub fn $get(&self) -> Option<&$ty> {
                self.$field.as_ref()
            }
            pub fn $set(mut self, v: $ty) -> Self {
                self.$field = Some(v);
                self
            }
        )*
    };
}

/// Same, for `Option<Box<Schema>>` fields.
macro_rules! boxed_schema_accessors {
    ($($field:ident => $get:ident, $set:ident;)*) => {
        $(
            pub fn $get(&self) -> Option<&Schema> {
                self.$field.as_deref()
            }
            pub fn $set(mut self, v: Schema) -> Self {
                self.$field = Some(Box::new(v));
                self
            }
        )*
    };
}

static NULL_SCHEMA: LazyLock<Schema> = LazyLock::new(|| {
    Schema::new()
        .set_namespace(Namespaces::SYSTEM)
        .set_name("Null")
        .set_type(TypeUtil::of(&[SchemaType::Null]))
});


const ADDITIONAL_PROPERTY: &str = "additionalProperty";
const ADDITIONAL_ITEMS: &str = "additionalItems";
const ENUMS: &str = "enums";
const ITEMS_STRING: &str = "items";
const REQUIRED_STRING: &str = "required";
const VERSION_STRING: &str = "version";
const NAMESPACE_STRING: &str = "namespace";

fn string_enums(values: &[&str]) -> Vec<Value> {
    values.iter().map(|v| Value::String((*v).to_string())).collect()
}

/// JS `Schema.TYPE_SCHEMA`.
fn type_schema() -> Schema {
    Schema::new().set_type(TypeUtil::of(&[SchemaType::String])).set_enums(string_enums(&[
        "INTEGER", "LONG", "FLOAT", "DOUBLE", "STRING", "OBJECT", "ARRAY", "BOOLEAN", "NULL",
    ]))
}

fn ref_root() -> Schema {
    Schema::of_ref(SCHEMA_ROOT_PATH)
}

fn map_of_root(id: &str) -> Schema {
    Schema::of(id, &[SchemaType::Object]).set_additional_properties(AdditionalType::new().set_schema_value(ref_root()))
}

/// JS `Schema.SCHEMA`.
static SCHEMA_SCHEMA: LazyLock<Schema> = LazyLock::new(|| {
    let props: Vec<(&str, Schema)> = vec![
        (NAMESPACE_STRING, Schema::of(NAMESPACE_STRING, &[SchemaType::String]).set_default_value(Value::from(TEMPORARY))),
        ("name", Schema::of_string("name")),
        (VERSION_STRING, Schema::of(VERSION_STRING, &[SchemaType::Integer]).set_default_value(Value::from(1))),
        ("ref", Schema::of_string("ref")),
        ("type", Schema::new().set_any_of(vec![type_schema(), Schema::of_array("type", vec![type_schema()])])),
        ("anyOf", Schema::of_array("anyOf", vec![ref_root()])),
        ("allOf", Schema::of_array("allOf", vec![ref_root()])),
        ("oneOf", Schema::of_array("oneOf", vec![ref_root()])),
        ("not", ref_root()),
        ("title", Schema::of_string("title")),
        ("description", Schema::of_string("description")),
        ("id", Schema::of_string("id")),
        ("examples", Schema::of_any("examples")),
        ("defaultValue", Schema::of_any("defaultValue")),
        ("comment", Schema::of_string("comment")),
        (ENUMS, Schema::of_array(ENUMS, vec![Schema::of_string(ENUMS)])),
        ("constant", Schema::of_any("constant")),
        ("pattern", Schema::of_string("pattern")),
        (
            "format",
            Schema::of("format", &[SchemaType::String])
                .set_enums(string_enums(&["DATETIME", "TIME", "DATE", "EMAIL", "REGEX", "DECIMAL", "ID"])),
        ),
        ("minLength", Schema::of_integer("minLength")),
        ("maxLength", Schema::of_integer("maxLength")),
        ("multipleOf", Schema::of_long("multipleOf")),
        ("minimum", Schema::of_number("minimum")),
        ("maximum", Schema::of_number("maximum")),
        ("exclusiveMinimum", Schema::of_number("exclusiveMinimum")),
        ("exclusiveMaximum", Schema::of_number("exclusiveMaximum")),
        ("properties", map_of_root("properties")),
        (
            "additionalProperties",
            Schema::new()
                .set_name(ADDITIONAL_PROPERTY)
                .set_namespace(Namespaces::SYSTEM)
                .set_any_of(vec![
                    Schema::of_boolean(ADDITIONAL_PROPERTY),
                    Schema::of_object(ADDITIONAL_PROPERTY).set_ref(SCHEMA_ROOT_PATH),
                ])
                .set_default_value(Value::Bool(true)),
        ),
        (
            REQUIRED_STRING,
            Schema::of_array(REQUIRED_STRING, vec![Schema::of_string(REQUIRED_STRING)])
                .set_default_value(Value::Array(vec![])),
        ),
        ("propertyNames", ref_root()),
        ("minProperties", Schema::of_integer("minProperties")),
        ("maxProperties", Schema::of_integer("maxProperties")),
        ("patternProperties", map_of_root("patternProperties")),
        (
            ITEMS_STRING,
            Schema::new()
                .set_name(ITEMS_STRING)
                .set_any_of(vec![ref_root().set_name("item"), Schema::of_array("tuple", vec![ref_root()])]),
        ),
        ("contains", ref_root()),
        ("minContains", Schema::of_integer("minContains")),
        ("maxContains", Schema::of_integer("maxContains")),
        ("minItems", Schema::of_integer("minItems")),
        ("maxItems", Schema::of_integer("maxItems")),
        ("uniqueItems", Schema::of_boolean("uniqueItems")),
        (
            "additionalItems",
            Schema::new()
                .set_name(ADDITIONAL_ITEMS)
                .set_namespace(Namespaces::SYSTEM)
                .set_any_of(vec![
                    Schema::of_boolean(ADDITIONAL_ITEMS),
                    Schema::of_object(ADDITIONAL_ITEMS).set_ref(SCHEMA_ROOT_PATH),
                ]),
        ),
        ("$defs", map_of_root("$defs")),
        ("permission", Schema::of_string("permission")),
        ("details", Schema::of_object("details")),
        ("viewDetails", Schema::of_object("viewDetails")),
    ];
    Schema::new()
        .set_namespace(Namespaces::SYSTEM)
        .set_name("Schema")
        .set_type(TypeUtil::of(&[SchemaType::Object]))
        .set_properties(props.into_iter().map(|(k, v)| (k.to_string(), v)).collect())
        .set_required(vec![])
});

impl Schema {
    pub fn new() -> Self {
        Self::default()
    }

    /// JS `Schema.NULL`.
    pub fn null() -> &'static Schema {
        &NULL_SCHEMA
    }

    /// JS `Schema.SCHEMA`: the meta-schema describing a schema.
    pub fn schema() -> &'static Schema {
        &SCHEMA_SCHEMA
    }

    pub fn of_string(id: &str) -> Schema {
        Schema::of(id, &[SchemaType::String])
    }
    pub fn of_integer(id: &str) -> Schema {
        Schema::of(id, &[SchemaType::Integer])
    }
    pub fn of_float(id: &str) -> Schema {
        Schema::of(id, &[SchemaType::Float])
    }
    pub fn of_long(id: &str) -> Schema {
        Schema::of(id, &[SchemaType::Long])
    }
    pub fn of_double(id: &str) -> Schema {
        Schema::of(id, &[SchemaType::Double])
    }
    pub fn of_boolean(id: &str) -> Schema {
        Schema::of(id, &[SchemaType::Boolean])
    }
    pub fn of_object(id: &str) -> Schema {
        Schema::of(id, &[SchemaType::Object])
    }
    pub fn of_number(id: &str) -> Schema {
        Schema::of(
            id,
            &[SchemaType::Integer, SchemaType::Long, SchemaType::Float, SchemaType::Double],
        )
    }
    pub fn of_any(id: &str) -> Schema {
        Schema::of(
            id,
            &[
                SchemaType::Integer,
                SchemaType::Long,
                SchemaType::Float,
                SchemaType::Double,
                SchemaType::String,
                SchemaType::Boolean,
                SchemaType::Array,
                SchemaType::Null,
                SchemaType::Object,
            ],
        )
    }
    pub fn of_any_not_null(id: &str) -> Schema {
        Schema::of(
            id,
            &[
                SchemaType::Integer,
                SchemaType::Long,
                SchemaType::Float,
                SchemaType::Double,
                SchemaType::String,
                SchemaType::Boolean,
                SchemaType::Array,
                SchemaType::Object,
            ],
        )
    }
    pub fn of(id: &str, types: &[SchemaType]) -> Schema {
        Schema::new().set_type(TypeUtil::of(types)).set_name(id)
    }
    pub fn of_ref(r: &str) -> Schema {
        Schema::new().set_ref(r)
    }
    pub fn of_array(id: &str, item_schemas: Vec<Schema>) -> Schema {
        Schema::new()
            .set_type(TypeUtil::of(&[SchemaType::Array]))
            .set_name(id)
            .set_items(ArraySchemaType::of(item_schemas))
    }

    /// JS `Schema.from(obj, isStringSchema)`. `Ok(None)` for null input.
    pub fn from_value(obj: &Value) -> Result<Option<Schema>> {
        Schema::from_value_with(obj, false)
    }
    pub fn from_value_with(obj: &Value, is_string_schema: bool) -> Result<Option<Schema>> {
        if obj.is_null() {
            return Ok(None);
        }

        let string = |k: &str| obj[k].as_str().map(str::to_string);
        let count = |k: &str| obj[k].as_f64().filter(|n| *n >= 0.0).map(|n| n as usize);
        let number = |k: &str| obj[k].as_f64();
        let array = |k: &str| match &obj[k] {
            Value::Array(a) => Some(a.clone()),
            _ => None,
        };
        let non_null = |k: &str| Some(obj[k].clone()).filter(|v| !v.is_null());

        let schema = Schema {
            namespace: string("namespace").unwrap_or_else(|| TEMPORARY.to_string()),
            name: string("name"),
            version: obj["version"].as_f64().map(|v| v as i64).unwrap_or(1),
            r#ref: string("ref"),
            r#type: if is_string_schema {
                Some(Type::Single(SchemaType::String))
            } else {
                TypeUtil::from_value(&obj["type"])?
            },
            any_of: Schema::from_list_of_schemas(&obj["anyOf"])?,
            all_of: Schema::from_list_of_schemas(&obj["allOf"])?,
            one_of: Schema::from_list_of_schemas(&obj["oneOf"])?,
            not: Schema::from_value(&obj["not"])?.map(Box::new),
            description: string("description"),
            examples: array("examples"),
            default_value: non_null("defaultValue"),
            comment: string("comment"),
            enums: array("enums"),
            constant: non_null("constant"),
            // String
            pattern: string("pattern"),
            format: obj["format"].as_str().and_then(|f| StringFormat::parse(f).ok()),
            min_length: count("minLength"),
            max_length: count("maxLength"),
            // Number
            multiple_of: number("multipleOf"),
            minimum: number("minimum"),
            maximum: number("maximum"),
            exclusive_minimum: number("exclusiveMinimum"),
            exclusive_maximum: number("exclusiveMaximum"),
            // Object
            properties: Schema::from_map_of_schemas(&obj["properties"])?,
            additional_properties: AdditionalType::from_value(&obj["additionalProperties"])?,
            required: obj["required"]
                .as_array()
                .map(|a| a.iter().map(|v| v.as_str().map(str::to_string).unwrap_or_else(|| js_to_string(v))).collect()),
            property_names: Schema::from_value_with(&obj["propertyNames"], true)?.map(Box::new),
            min_properties: count("minProperties"),
            max_properties: count("maxProperties"),
            pattern_properties: Schema::from_map_of_schemas(&obj["patternProperties"])?,
            // Array
            items: ArraySchemaType::from_value(&obj["items"])?,
            additional_items: AdditionalType::from_value(&obj["additionalItems"])?,
            contains: Schema::from_value(&obj["contains"])?.map(Box::new),
            min_contains: count("minContains"),
            max_contains: count("maxContains"),
            min_items: count("minItems"),
            max_items: count("maxItems"),
            unique_items: obj["uniqueItems"].as_bool(),
            defs: Schema::from_map_of_schemas(&obj["$defs"])?,
            permission: string("permission"),
            details: SchemaDetails::from_value(&obj["details"])?,
            view_details: SchemaDetails::from_value(&obj["viewDetails"])?,
        };

        Ok(Some(schema))
    }
    /// JS `fromListOfSchemas`: `None` for null; null entries are skipped.
    pub fn from_list_of_schemas(list: &Value) -> Result<Option<Vec<Schema>>> {
        match list {
            Value::Null => Ok(None),
            Value::Array(a) => {
                let mut x = Vec::with_capacity(a.len());
                for e in a {
                    if let Some(s) = Schema::from_value(e)? {
                        x.push(s);
                    }
                }
                Ok(Some(x))
            }
            // JS `Array.from` of a non-iterable is an empty array.
            _ => Ok(Some(Vec::new())),
        }
    }
    /// JS `fromMapOfSchemas`: `None` for null; null values are skipped.
    pub fn from_map_of_schemas(map: &Value) -> Result<Option<Map<Schema>>> {
        match map {
            Value::Null => Ok(None),
            Value::Object(o) => {
                let mut ret = Map::new();
                for (k, v) in o {
                    if let Some(s) = Schema::from_value(v)? {
                        ret.insert(k.clone(), s);
                    }
                }
                Ok(Some(ret))
            }
            _ => Ok(Some(Map::new())),
        }
    }
    /// Serialises back to the JSON shape `from_value` accepts: the JS field names, absent
    /// fields left out, `type` as `"STRING"` or `["STRING", ...]`.
    pub fn to_json(&self) -> Value {
        let mut m = serde_json::Map::new();
        let schemas = |v: &Vec<Schema>| Value::Array(v.iter().map(Schema::to_json).collect());
        let schema_map =
            |v: &Map<Schema>| Value::Object(v.iter().map(|(k, s)| (k.clone(), s.to_json())).collect());
        let count = |n: usize| Value::from(n as u64);

        m.insert("namespace".into(), Value::String(self.namespace.clone()));
        if let Some(v) = &self.name {
            m.insert("name".into(), Value::String(v.clone()));
        }
        m.insert("version".into(), Value::from(self.version));
        if let Some(v) = &self.r#ref {
            m.insert("ref".into(), Value::String(v.clone()));
        }
        if let Some(v) = &self.r#type {
            m.insert("type".into(), v.to_json());
        }
        if let Some(v) = &self.any_of {
            m.insert("anyOf".into(), schemas(v));
        }
        if let Some(v) = &self.all_of {
            m.insert("allOf".into(), schemas(v));
        }
        if let Some(v) = &self.one_of {
            m.insert("oneOf".into(), schemas(v));
        }
        if let Some(v) = &self.not {
            m.insert("not".into(), v.to_json());
        }
        if let Some(v) = &self.description {
            m.insert("description".into(), Value::String(v.clone()));
        }
        if let Some(v) = &self.examples {
            m.insert("examples".into(), Value::Array(v.clone()));
        }
        if let Some(v) = &self.default_value {
            m.insert("defaultValue".into(), v.clone());
        }
        if let Some(v) = &self.comment {
            m.insert("comment".into(), Value::String(v.clone()));
        }
        if let Some(v) = &self.enums {
            m.insert("enums".into(), Value::Array(v.clone()));
        }
        if let Some(v) = &self.constant {
            m.insert("constant".into(), v.clone());
        }
        if let Some(v) = &self.pattern {
            m.insert("pattern".into(), Value::String(v.clone()));
        }
        if let Some(v) = &self.format {
            m.insert("format".into(), Value::String(v.name().to_string()));
        }
        if let Some(v) = self.min_length {
            m.insert("minLength".into(), count(v));
        }
        if let Some(v) = self.max_length {
            m.insert("maxLength".into(), count(v));
        }
        for (k, v) in [
            ("multipleOf", self.multiple_of),
            ("minimum", self.minimum),
            ("maximum", self.maximum),
            ("exclusiveMinimum", self.exclusive_minimum),
            ("exclusiveMaximum", self.exclusive_maximum),
        ] {
            if let Some(v) = v {
                m.insert(k.into(), number_value(v));
            }
        }
        if let Some(v) = &self.properties {
            m.insert("properties".into(), schema_map(v));
        }
        if let Some(v) = &self.additional_properties {
            m.insert("additionalProperties".into(), v.to_json());
        }
        if let Some(v) = &self.required {
            m.insert("required".into(), Value::Array(v.iter().cloned().map(Value::String).collect()));
        }
        if let Some(v) = &self.property_names {
            m.insert("propertyNames".into(), v.to_json());
        }
        if let Some(v) = self.min_properties {
            m.insert("minProperties".into(), count(v));
        }
        if let Some(v) = self.max_properties {
            m.insert("maxProperties".into(), count(v));
        }
        if let Some(v) = &self.pattern_properties {
            m.insert("patternProperties".into(), schema_map(v));
        }
        if let Some(v) = &self.items {
            m.insert("items".into(), v.to_json());
        }
        if let Some(v) = &self.additional_items {
            m.insert("additionalItems".into(), v.to_json());
        }
        if let Some(v) = &self.contains {
            m.insert("contains".into(), v.to_json());
        }
        for (k, v) in [
            ("minContains", self.min_contains),
            ("maxContains", self.max_contains),
            ("minItems", self.min_items),
            ("maxItems", self.max_items),
        ] {
            if let Some(v) = v {
                m.insert(k.into(), count(v));
            }
        }
        if let Some(v) = self.unique_items {
            m.insert("uniqueItems".into(), Value::Bool(v));
        }
        if let Some(v) = &self.defs {
            m.insert("$defs".into(), schema_map(v));
        }
        if let Some(v) = &self.permission {
            m.insert("permission".into(), Value::String(v.clone()));
        }
        if let Some(v) = &self.details {
            m.insert("details".into(), v.to_json());
        }
        if let Some(v) = &self.view_details {
            m.insert("viewDetails".into(), v.to_json());
        }
        Value::Object(m)
    }

    pub fn get_title(&self) -> Option<String> {
        if self.namespace.is_empty() || self.namespace == TEMPORARY {
            return self.name.clone();
        }
        Some(format!("{}.{}", self.namespace, self.name.as_deref().unwrap_or("undefined")))
    }

    pub fn get_full_name(&self) -> String {
        format!("{}.{}", self.namespace, self.name.as_deref().unwrap_or("undefined"))
    }

    pub fn get_namespace(&self) -> &str {
        &self.namespace
    }
    pub fn set_namespace(mut self, namespace: impl Into<String>) -> Self {
        self.namespace = namespace.into();
        self
    }
    pub fn get_name(&self) -> Option<&str> {
        self.name.as_deref()
    }
    pub fn set_name(mut self, name: impl Into<String>) -> Self {
        self.name = Some(name.into());
        self
    }
    pub fn get_version(&self) -> i64 {
        self.version
    }
    pub fn set_version(mut self, version: i64) -> Self {
        self.version = version;
        self
    }
    pub fn get_ref(&self) -> Option<&str> {
        self.r#ref.as_deref()
    }
    pub fn set_ref(mut self, r: impl Into<String>) -> Self {
        self.r#ref = Some(r.into());
        self
    }
    pub fn get_type(&self) -> Option<&Type> {
        self.r#type.as_ref()
    }
    pub fn set_type(mut self, t: Type) -> Self {
        self.r#type = Some(t);
        self
    }
    pub fn get_defs(&self) -> Option<&Map<Schema>> {
        self.defs.as_ref()
    }
    pub fn set_defs(mut self, defs: Map<Schema>) -> Self {
        self.defs = Some(defs);
        self
    }
    pub fn get_default_value(&self) -> Option<&Value> {
        self.default_value.as_ref()
    }
    pub fn set_default_value(mut self, v: Value) -> Self {
        self.default_value = Some(v);
        self
    }
    pub fn get_constant(&self) -> Option<&Value> {
        self.constant.as_ref()
    }
    pub fn set_constant(mut self, v: Value) -> Self {
        self.constant = Some(v);
        self
    }

    optional_accessors! {
        any_of: Vec<Schema> => get_any_of, set_any_of;
        all_of: Vec<Schema> => get_all_of, set_all_of;
        one_of: Vec<Schema> => get_one_of, set_one_of;
        description: String => get_description, set_description;
        examples: Vec<Value> => get_examples, set_examples;
        comment: String => get_comment, set_comment;
        enums: Vec<Value> => get_enums, set_enums;
        pattern: String => get_pattern, set_pattern;
        format: StringFormat => get_format, set_format;
        min_length: usize => get_min_length, set_min_length;
        max_length: usize => get_max_length, set_max_length;
        multiple_of: f64 => get_multiple_of, set_multiple_of;
        minimum: f64 => get_minimum, set_minimum;
        maximum: f64 => get_maximum, set_maximum;
        exclusive_minimum: f64 => get_exclusive_minimum, set_exclusive_minimum;
        exclusive_maximum: f64 => get_exclusive_maximum, set_exclusive_maximum;
        properties: Map<Schema> => get_properties, set_properties;
        additional_properties: AdditionalType => get_additional_properties, set_additional_properties;
        additional_items: AdditionalType => get_additional_items, set_additional_items;
        required: Vec<String> => get_required, set_required;
        min_properties: usize => get_min_properties, set_min_properties;
        max_properties: usize => get_max_properties, set_max_properties;
        pattern_properties: Map<Schema> => get_pattern_properties, set_pattern_properties;
        items: ArraySchemaType => get_items, set_items;
        min_contains: usize => get_min_contains, set_min_contains;
        max_contains: usize => get_max_contains, set_max_contains;
        min_items: usize => get_min_items, set_min_items;
        max_items: usize => get_max_items, set_max_items;
        unique_items: bool => get_unique_items, set_unique_items;
        permission: String => get_permission, set_permission;
        details: SchemaDetails => get_details, set_details;
        view_details: SchemaDetails => get_view_details, set_view_details;
    }

    boxed_schema_accessors! {
        not => get_not, set_not;
        property_names => get_property_names, set_property_names;
        contains => get_contains, set_contains;
    }
}
