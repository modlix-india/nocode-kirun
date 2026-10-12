//! Port of kirun-js `runtime/ContextElement.ts`.

use crate::json::schema::Schema;
use crate::Value;

#[derive(Debug, Clone, PartialEq, Default)]
pub struct ContextElement {
    pub schema: Option<Schema>,
    pub element: Value,
}

impl ContextElement {
    pub fn new(schema: Option<Schema>, element: Value) -> Self {
        Self { schema, element }
    }
    /// JS `ContextElement.NULL`.
    pub fn null() -> Self {
        Self { schema: Some(Schema::null().clone()), element: Value::Null }
    }
    pub fn get_schema(&self) -> Option<&Schema> {
        self.schema.as_ref()
    }
    pub fn set_schema(mut self, schema: Schema) -> Self {
        self.schema = Some(schema);
        self
    }
    pub fn get_element(&self) -> &Value {
        &self.element
    }
    pub fn set_element(mut self, element: Value) -> Self {
        self.element = element;
        self
    }
}
