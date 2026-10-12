//! Port of kirun-js `model/Parameter.ts` and `model/ParameterType.ts`.

use std::sync::LazyLock;

use crate::json::schema::r#type::{SchemaType, TypeUtil};
use crate::json::schema::Schema;
use crate::namespaces::Namespaces;
use crate::{KIRunError, Map, Result, Value};

#[derive(Debug, Clone, Copy, PartialEq, Eq, Default)]
pub enum ParameterType {
    Constant,
    #[default]
    Expression,
}

impl ParameterType {
    pub fn name(&self) -> &'static str {
        match self {
            ParameterType::Constant => "CONSTANT",
            ParameterType::Expression => "EXPRESSION",
        }
    }

    pub fn parse(s: &str) -> Result<Self> {
        match s.to_ascii_uppercase().as_str() {
            "CONSTANT" => Ok(ParameterType::Constant),
            "EXPRESSION" => Ok(ParameterType::Expression),
            _ => Err(KIRunError::runtime(format!("Unknown parameter type : {s}"))),
        }
    }
}

#[derive(Debug, Clone, PartialEq)]
pub struct Parameter {
    pub schema: Schema,
    pub parameter_name: String,
    pub variable_argument: bool,
    pub r#type: ParameterType,
}

impl Parameter {
    pub fn new(parameter_name: impl Into<String>, schema: Schema) -> Self {
        Self {
            schema,
            parameter_name: parameter_name.into(),
            variable_argument: false,
            r#type: ParameterType::Expression,
        }
    }

    /// JS `Parameter.SCHEMA`.
    pub fn schema() -> &'static Schema {
        static SCHEMA: LazyLock<Schema> = LazyLock::new(|| {
            Schema::new()
                .set_namespace(Namespaces::SYSTEM)
                .set_name("Parameter")
                .set_properties(Map::from([
                    ("schema".to_string(), Schema::schema().clone()),
                    ("parameterName".to_string(), Schema::of_string("parameterName")),
                    (
                        "variableArgument".to_string(),
                        Schema::of("variableArgument", &[SchemaType::Boolean]).set_default_value(Value::Bool(false)),
                    ),
                    (
                        "type".to_string(),
                        Schema::of_string("type").set_enums(vec![Value::from("EXPRESSION"), Value::from("CONSTANT")]),
                    ),
                ]))
        });
        &SCHEMA
    }

    /// JS `Parameter.EXPRESSION`: the schema of a `{ isExpression, value }` object, the
    /// placeholder for an expression inside a VALUE parameter.
    pub fn expression() -> &'static Schema {
        static EXPRESSION: LazyLock<Schema> = LazyLock::new(|| {
            Schema::new()
                .set_namespace(Namespaces::SYSTEM)
                .set_name("ParameterExpression")
                .set_type(TypeUtil::of(&[SchemaType::Object]))
                .set_properties(Map::from([
                    (
                        "isExpression".to_string(),
                        Schema::of_boolean("isExpression").set_default_value(Value::Bool(true)),
                    ),
                    ("value".to_string(), Schema::of_any("value")),
                ]))
        });
        &EXPRESSION
    }

    pub fn get_schema(&self) -> &Schema {
        &self.schema
    }
    pub fn set_schema(mut self, schema: Schema) -> Self {
        self.schema = schema;
        self
    }
    pub fn get_parameter_name(&self) -> &str {
        &self.parameter_name
    }
    pub fn set_parameter_name(mut self, name: impl Into<String>) -> Self {
        self.parameter_name = name.into();
        self
    }
    pub fn is_variable_argument(&self) -> bool {
        self.variable_argument
    }
    pub fn set_variable_argument(mut self, v: bool) -> Self {
        self.variable_argument = v;
        self
    }
    pub fn get_type(&self) -> ParameterType {
        self.r#type
    }
    pub fn set_type(mut self, t: ParameterType) -> Self {
        self.r#type = t;
        self
    }

    /// JS `Parameter.ofEntry(name, schema, variableArgument)`; type defaults to EXPRESSION.
    pub fn of_entry(name: &str, schema: Schema, variable_argument: bool) -> (String, Parameter) {
        (name.to_string(), Parameter::of(name, schema, variable_argument))
    }

    pub fn of_entry_typed(
        name: &str,
        schema: Schema,
        variable_argument: bool,
        r#type: ParameterType,
    ) -> (String, Parameter) {
        (name.to_string(), Parameter::of(name, schema, variable_argument).set_type(r#type))
    }

    pub fn of(name: &str, schema: Schema, variable_argument: bool) -> Parameter {
        Parameter::new(name, schema).set_variable_argument(variable_argument)
    }

    pub fn from_value(json: &Value) -> Result<Parameter> {
        let schema = Schema::from_value(&json["schema"])?
            .ok_or_else(|| KIRunError::schema_reference("", "Parameter requires Schema"))?;
        let name = json["parameterName"].as_str().unwrap_or_default();
        let r#type = match json["type"].as_str() {
            Some(t) => ParameterType::parse(t)?,
            None => ParameterType::Expression,
        };
        Ok(Parameter::new(name, schema)
            .set_variable_argument(json["variableArgument"].as_bool().unwrap_or(false))
            .set_type(r#type))
    }

    pub fn to_json(&self) -> Value {
        serde_json::json!({
            "parameterName": self.parameter_name,
            "schema": self.schema.to_json(),
            "variableArgument": self.variable_argument,
            "type": self.r#type.name(),
        })
    }
}
