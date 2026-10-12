//! Port of kirun-js `json/schema/array/ArraySchemaType.ts`.

use crate::json::schema::Schema;
use crate::util::primitive::js_truthy;
use crate::{Result, Value};

#[derive(Debug, Clone, PartialEq, Default)]
pub struct ArraySchemaType {
    pub single_schema: Option<Box<Schema>>,
    pub tuple_schema: Option<Vec<Schema>>,
}

impl ArraySchemaType {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn set_single_schema(mut self, schema: Schema) -> Self {
        self.single_schema = Some(Box::new(schema));
        self
    }

    pub fn set_tuple_schema(mut self, schemas: Vec<Schema>) -> Self {
        self.tuple_schema = Some(schemas);
        self
    }

    pub fn get_single_schema(&self) -> Option<&Schema> {
        self.single_schema.as_deref()
    }

    pub fn get_tuple_schema(&self) -> Option<&Vec<Schema>> {
        self.tuple_schema.as_ref()
    }

    pub fn is_single_type(&self) -> bool {
        self.single_schema.is_some()
    }

    /// JS `ArraySchemaType.of(...schemas)`: one schema is a single type, more is a tuple.
    pub fn of(schemas: Vec<Schema>) -> Self {
        if schemas.len() == 1 {
            Self::new().set_single_schema(schemas.into_iter().next().unwrap())
        } else {
            Self::new().set_tuple_schema(schemas)
        }
    }

    /// JS `ArraySchemaType.from(obj)`.
    pub fn from_value(obj: &Value) -> Result<Option<Self>> {
        if !js_truthy(obj) {
            return Ok(None);
        }
        if obj.is_array() {
            return Ok(Some(
                ArraySchemaType::new().set_tuple_schema(Schema::from_list_of_schemas(obj)?.unwrap_or_default()),
            ));
        }

        if let Value::Object(m) = obj {
            if m.contains_key("singleSchema") {
                let mut ast = ArraySchemaType::new();
                ast.single_schema = Schema::from_value(&m["singleSchema"])?.map(Box::new);
                return Ok(Some(ast));
            } else if m.contains_key("tupleSchema") {
                let mut ast = ArraySchemaType::new();
                ast.tuple_schema = Schema::from_list_of_schemas(&m["tupleSchema"])?;
                return Ok(Some(ast));
            }
        }

        Ok(Schema::from_value(obj)?.map(|x| ArraySchemaType::new().set_single_schema(x)))
    }

    /// The JS `JSON.stringify` shape: `{ singleSchema }` or `{ tupleSchema }`.
    pub fn to_json(&self) -> Value {
        let mut m = serde_json::Map::new();
        if let Some(s) = &self.single_schema {
            m.insert("singleSchema".into(), s.to_json());
        }
        if let Some(t) = &self.tuple_schema {
            m.insert("tupleSchema".into(), Value::Array(t.iter().map(Schema::to_json).collect()));
        }
        Value::Object(m)
    }
}
