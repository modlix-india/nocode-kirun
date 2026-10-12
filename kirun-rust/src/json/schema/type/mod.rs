//! Port of kirun-js `json/schema/type/*` (SchemaType, Type, SingleType, MultipleType, TypeUtil).

use indexmap::IndexSet;

use crate::{KIRunError, Result, Value};

/// JS `SchemaType`. `as_str()` is the JS enum *value* ("Integer"), `name()` the key ("INTEGER").
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, PartialOrd, Ord)]
pub enum SchemaType {
    Integer,
    Long,
    Float,
    Double,
    String,
    Object,
    Array,
    Boolean,
    Null,
}

impl SchemaType {
    pub const ALL: [SchemaType; 9] = [
        SchemaType::Integer,
        SchemaType::Long,
        SchemaType::Float,
        SchemaType::Double,
        SchemaType::String,
        SchemaType::Object,
        SchemaType::Array,
        SchemaType::Boolean,
        SchemaType::Null,
    ];

    /// JS enum value, e.g. `SchemaType.INTEGER === 'Integer'`.
    pub fn as_str(&self) -> &'static str {
        match self {
            SchemaType::Integer => "Integer",
            SchemaType::Long => "Long",
            SchemaType::Float => "Float",
            SchemaType::Double => "Double",
            SchemaType::String => "String",
            SchemaType::Object => "Object",
            SchemaType::Array => "Array",
            SchemaType::Boolean => "Boolean",
            SchemaType::Null => "Null",
        }
    }

    /// JS enum key, e.g. `INTEGER` (the spelling used in schema JSON).
    pub fn name(&self) -> &'static str {
        match self {
            SchemaType::Integer => "INTEGER",
            SchemaType::Long => "LONG",
            SchemaType::Float => "FLOAT",
            SchemaType::Double => "DOUBLE",
            SchemaType::String => "STRING",
            SchemaType::Object => "OBJECT",
            SchemaType::Array => "ARRAY",
            SchemaType::Boolean => "BOOLEAN",
            SchemaType::Null => "NULL",
        }
    }

    /// Accepts either the key (`INTEGER`) or the value (`Integer`), case-insensitively.
    pub fn parse(s: &str) -> Result<SchemaType> {
        SchemaType::ALL
            .iter()
            .copied()
            .find(|t| t.name().eq_ignore_ascii_case(s))
            .ok_or_else(|| KIRunError::runtime(format!("Unknown schema type : {s}")))
    }
}

/// JS `Type` with its two subclasses `SingleType` and `MultipleType`.
#[derive(Debug, Clone, PartialEq)]
pub enum Type {
    Single(SchemaType),
    Multiple(IndexSet<SchemaType>),
}

impl Type {
    pub fn get_allowed_schema_types(&self) -> IndexSet<SchemaType> {
        match self {
            Type::Single(t) => IndexSet::from([*t]),
            Type::Multiple(set) => set.clone(),
        }
    }

    pub fn contains(&self, t: SchemaType) -> bool {
        match self {
            Type::Single(s) => *s == t,
            Type::Multiple(set) => set.contains(&t),
        }
    }

    pub fn to_json(&self) -> Value {
        match self {
            Type::Single(t) => Value::String(t.name().to_string()),
            Type::Multiple(set) => {
                Value::Array(set.iter().map(|t| Value::String(t.name().into())).collect())
            }
        }
    }
}

/// JS `TypeUtil`.
pub struct TypeUtil;

impl TypeUtil {
    pub fn of(types: &[SchemaType]) -> Type {
        if types.len() == 1 {
            Type::Single(types[0])
        } else {
            Type::Multiple(types.iter().copied().collect())
        }
    }

    /// JS `TypeUtil.fromJSONType`.
    fn from_json_type(jtype: &str) -> &str {
        if jtype.eq_ignore_ascii_case("NUMBER") {
            "DOUBLE"
        } else {
            jtype
        }
    }

    /// JS `TypeUtil.from`: a string, or an array of strings. JSON-Schema `number` is DOUBLE.
    pub fn from_value(types: &Value) -> Result<Option<Type>> {
        match types {
            Value::Null => Ok(None),
            Value::String(s) => Ok(Some(Type::Single(SchemaType::parse(Self::from_json_type(s))?))),
            Value::Array(arr) => {
                let mut set = IndexSet::new();
                for t in arr {
                    if let Value::String(s) = t {
                        set.insert(SchemaType::parse(Self::from_json_type(s))?);
                    }
                }
                Ok(Some(Type::Multiple(set)))
            }
            _ => Ok(None),
        }
    }
}
