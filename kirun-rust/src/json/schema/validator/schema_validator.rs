//! Port of kirun-js `json/schema/validator/SchemaValidator.ts`.

use std::sync::Arc;

use futures::future::BoxFuture;

use crate::json::schema::convertor::ConversionMode;
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::schema_util::SchemaUtil;
use crate::json::schema::validator::{AnyOfAllOfOneOfValidator, TypeValidator};
use crate::json::schema::Schema;
use crate::repository::Repository;
use crate::util::primitive::{js_number_value_to_string, js_stringify, js_to_string};
use crate::util::string::string_util::StringUtil;
use crate::{KIRunError, Result, Value};

/// Anything that can render the JS `SchemaValidator.path(parents)` string.
pub trait SchemaPath {
    fn schema_path(&self) -> String;
}

impl SchemaPath for [Schema] {
    fn schema_path(&self) -> String {
        SchemaValidator::path(Some(self))
    }
}

impl SchemaPath for Vec<Schema> {
    fn schema_path(&self) -> String {
        SchemaValidator::path(Some(self))
    }
}

/// The JS `parents: Schema[]` chain as the validators use it: the titles (for error paths) and
/// the first schema (the root that `#/` references resolve against). JS shares one array by
/// reference in some calls (ref, not, propertyNames) and copies it in others; Rust mirrors that
/// with `&mut SchemaParents` versus `.clone()`.
///
/// The titles are a shared linked list, so the copy made for every property and array item is a
/// reference-count bump, and the root is kept only when the schema tree has a `$ref` to resolve
/// (copying it for every validation was most of the cost of validating plain values).
#[derive(Debug, Clone, Default)]
pub struct SchemaParents {
    root: Option<Arc<Schema>>,
    /// Whether the first schema has been pushed (it is the root even when not kept).
    rooted: bool,
    tail: Option<Arc<ParentTitle>>,
}

#[derive(Debug)]
struct ParentTitle {
    title: String,
    prev: Option<Arc<ParentTitle>>,
}

impl SchemaParents {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn from_slice(parents: &[Schema]) -> Self {
        let mut p = Self::new();
        for s in parents {
            p.push(s);
        }
        p
    }

    pub fn push(&mut self, schema: &Schema) {
        if !self.rooted {
            self.rooted = true;
            if has_ref(schema) {
                self.root = Some(Arc::new(schema.clone()));
            }
        }
        self.push_title(schema);
    }

    fn push_arc(&mut self, schema: &Arc<Schema>) {
        if !self.rooted {
            self.rooted = true;
            self.root = Some(schema.clone());
        }
        self.push_title(schema);
    }

    fn push_title(&mut self, schema: &Schema) {
        self.tail = Some(Arc::new(ParentTitle { title: schema.get_title().unwrap_or_default(), prev: self.tail.take() }));
    }

    pub fn root(&self) -> Option<&Arc<Schema>> {
        self.root.as_ref()
    }

    pub fn path(&self) -> String {
        let mut titles = Vec::new();
        let mut node = self.tail.as_deref();
        while let Some(n) = node {
            if !n.title.is_empty() {
                titles.push(n.title.as_str());
            }
            node = n.prev.as_deref();
        }
        titles.reverse();
        titles.join(".")
    }
}

/// Whether `schema` or anything inside it has a `$ref` (only then is the root needed).
fn has_ref(schema: &Schema) -> bool {
    let list = |l: Option<&Vec<Schema>>| l.is_some_and(|l| l.iter().any(has_ref));
    let map = |m: Option<&crate::Map<Schema>>| m.is_some_and(|m| m.values().any(has_ref));
    let additional = |a: Option<&crate::json::schema::AdditionalType>| {
        a.and_then(|a| a.get_schema_value()).is_some_and(has_ref)
    };
    schema.get_ref().is_some_and(|r| !r.trim().is_empty())
        || list(schema.get_any_of())
        || list(schema.get_all_of())
        || list(schema.get_one_of())
        || schema.get_not().is_some_and(has_ref)
        || map(schema.get_properties())
        || map(schema.get_pattern_properties())
        || map(schema.get_defs())
        || additional(schema.get_additional_properties())
        || additional(schema.get_additional_items())
        || schema.get_property_names().is_some_and(has_ref)
        || schema.get_contains().is_some_and(has_ref)
        || schema.get_items().is_some_and(|i| {
            i.get_single_schema().is_some_and(has_ref) || list(i.get_tuple_schema())
        })
}

impl SchemaPath for SchemaParents {
    fn schema_path(&self) -> String {
        self.path()
    }
}

pub struct SchemaValidator;

/// JS `SchemaValidator.ORDER`: the order in which the allowed types of a schema are tried.
fn type_order(t: SchemaType) -> u8 {
    match t {
        SchemaType::Object => 0,
        SchemaType::Array => 1,
        SchemaType::Double => 2,
        SchemaType::Float => 3,
        SchemaType::Long => 4,
        SchemaType::Integer => 5,
        SchemaType::String => 6,
        SchemaType::Boolean => 7,
        SchemaType::Null => 8,
    }
}

impl SchemaValidator {
    /// JS `SchemaValidator.path(parents)`: dotted titles of the parent chain.
    pub fn path(parents: Option<&[Schema]>) -> String {
        let Some(parents) = parents else {
            return String::new();
        };
        parents
            .iter()
            .filter_map(|s| s.get_title())
            .filter(|t| !t.is_empty())
            .collect::<Vec<_>>()
            .join(".")
    }

    /// JS `SchemaValidator.validate(parents, schema, repository, element)`.
    /// Returns the (possibly defaulted) element, or a `SchemaValidation` error.
    pub async fn validate(
        parents: Option<Vec<Schema>>,
        schema: Option<&Schema>,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
    ) -> Result<Value> {
        Self::validate_with(parents, schema, repository, element, false, None).await
    }

    /// JS `SchemaValidator.validate(..., convert, mode)`.
    pub async fn validate_with(
        parents: Option<Vec<Schema>>,
        schema: Option<&Schema>,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<Value> {
        let mut p = parents.as_deref().map(SchemaParents::from_slice).unwrap_or_default();
        Self::validate_in(&mut p, schema, repository, element, convert, mode).await
    }

    /// The engine of `validate`, on a shared parent chain.
    pub(crate) fn validate_in<'a>(
        parents: &'a mut SchemaParents,
        schema: Option<&'a Schema>,
        repository: Option<&'a dyn Repository<Schema>>,
        element: Value,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> BoxFuture<'a, Result<Value>> {
        Box::pin(async move {
            let Some(schema) = schema else {
                return Err(KIRunError::schema_validation(parents.path(), "No schema found to validate"));
            };
            parents.push(schema);
            Self::validate_pushed(parents, schema, repository, element, convert, mode).await
        })
    }

    fn validate_arc<'a>(
        parents: &'a mut SchemaParents,
        schema: Option<Arc<Schema>>,
        repository: Option<&'a dyn Repository<Schema>>,
        element: Value,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> BoxFuture<'a, Result<Value>> {
        Box::pin(async move {
            let Some(schema) = schema else {
                return Err(KIRunError::schema_validation(parents.path(), "No schema found to validate"));
            };
            parents.push_arc(&schema);
            Self::validate_pushed(parents, &schema, repository, element, convert, mode).await
        })
    }

    async fn validate_pushed(
        parents: &mut SchemaParents,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        mut element: Value,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<Value> {
        if element.is_null() {
            if let Some(d) = schema.get_default_value().filter(|d| !d.is_null()) {
                return Ok(d.clone());
            }
        }

        if schema.get_constant().is_some_and(|c| !c.is_null()) {
            return Self::constant_validation_in(parents, schema, element);
        }

        if schema.get_enums().is_some_and(|e| !e.is_empty()) {
            return Self::enum_check_in(parents, schema, element);
        }

        if let (Some(format), None) = (schema.get_format(), schema.get_type()) {
            return Err(KIRunError::schema_validation(
                parents.path(),
                format!("Type is missing in schema for declared {} format.", format.name()),
            ));
        }

        if convert && schema.get_type().is_none() {
            return Err(KIRunError::schema_validation(
                parents.path(),
                format!(
                    "Type is missing in schema for declared {}",
                    mode.map(|m| m.name()).unwrap_or("undefined")
                ),
            ));
        }

        if schema.get_type().is_some() {
            element = Self::type_validation_in(parents, schema, repository, element, convert, mode).await?;
        }

        if !StringUtil::is_null_or_blank(schema.get_ref()) {
            let root = parents.root().cloned();
            let resolved = match root {
                Some(root) => SchemaUtil::get_schema_from_ref_arc(root, repository, schema.get_ref(), 0).await?,
                None => None,
            };
            return Self::validate_arc(parents, resolved, repository, element, convert, mode).await;
        }

        if schema.get_one_of().is_some() || schema.get_all_of().is_some() || schema.get_any_of().is_some() {
            element =
                AnyOfAllOfOneOfValidator::validate_in(parents, schema, repository, element, convert, mode).await?;
        }

        if let Some(not) = schema.get_not() {
            let flag = Self::validate_in(parents, Some(not), repository, element.clone(), convert, mode)
                .await
                .is_ok();
            if flag {
                return Err(KIRunError::schema_validation(
                    parents.path(),
                    "Schema validated value in not condition.",
                ));
            }
        }

        Ok(element)
    }

    /// JS `constantValidation`.
    pub fn constant_validation(parents: &[Schema], schema: &Schema, element: Value) -> Result<Value> {
        Self::constant_validation_in(&SchemaParents::from_slice(parents), schema, element)
    }

    fn constant_validation_in(parents: &SchemaParents, schema: &Schema, element: Value) -> Result<Value> {
        let constant = schema.get_constant().cloned().unwrap_or(Value::Null);
        if !js_deep_equal(&constant, &element) {
            return Err(KIRunError::schema_validation(
                parents.path(),
                format!("Expecting a constant value : {}", js_to_string(&element)),
            ));
        }
        Ok(element)
    }

    /// JS `enumCheck`: strict (`===`) membership, so objects and arrays never match.
    pub fn enum_check(parents: &[Schema], schema: &Schema, element: Value) -> Result<Value> {
        Self::enum_check_in(&SchemaParents::from_slice(parents), schema, element)
    }

    fn enum_check_in(parents: &SchemaParents, schema: &Schema, element: Value) -> Result<Value> {
        let enums = schema.get_enums().map(Vec::as_slice).unwrap_or(&[]);
        if enums.iter().any(|e| js_strict_equal(e, &element)) {
            return Ok(element);
        }
        Err(KIRunError::schema_validation(
            parents.path(),
            format!("Value is not one of {}", js_to_string(&Value::Array(enums.to_vec()))),
        ))
    }

    /// JS `typeValidation`: tries each allowed type in `ORDER`, returning the first success.
    pub async fn type_validation(
        parents: &[Schema],
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<Value> {
        let mut p = SchemaParents::from_slice(parents);
        Self::type_validation_in(&mut p, schema, repository, element, convert, mode).await
    }

    async fn type_validation_in(
        parents: &mut SchemaParents,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<Value> {
        let mut allowed: Vec<SchemaType> = schema
            .get_type()
            .map(|t| t.get_allowed_schema_types().into_iter().collect())
            .unwrap_or_default();
        allowed.sort_by_key(|t| type_order(*t));

        // One allowed type: no other attempt needs the value, and the loop below would report
        // this attempt's error alone, so validate it in place without copying the value.
        if allowed.len() == 1 {
            return TypeValidator::validate_in(parents, allowed[0], schema, repository, element, convert, mode)
                .await
                .map_err(|e| KIRunError::schema_validation(parents.path(), e.to_string()));
        }

        // Without conversion a type can only accept its own JSON kind, so try just those first
        // (same order, so the same type wins). Failed attempts build their error messages
        // (JSON-encoding the value) and each attempt clones it; skipping hopeless ones avoids
        // both. If none passes, the full loop below produces the usual errors.
        if !convert {
            for &t in allowed.iter().filter(|t| kind_matches(**t, &element)) {
                if let Ok(v) =
                    TypeValidator::validate_in(parents, t, schema, repository, element.clone(), convert, mode).await
                {
                    return Ok(v);
                }
            }
        }

        let mut errors: Vec<KIRunError> = Vec::new();

        for t in allowed {
            match TypeValidator::validate_in(parents, t, schema, repository, element.clone(), convert, mode).await {
                Ok(v) => return Ok(v),
                Err(e) => errors.push(e),
            }
        }

        if errors.len() == 1 {
            return Err(KIRunError::schema_validation(parents.path(), errors.remove(0).to_string()));
        }

        Err(KIRunError::schema_validation_with(
            parents.path(),
            format!("Value {} is not of valid type(s)", js_stringify(&element)),
            errors,
        ))
    }
}

/// Whether a value of this JSON kind could pass the type's validator without conversion.
fn kind_matches(t: SchemaType, v: &Value) -> bool {
    match t {
        SchemaType::Object => v.is_object(),
        SchemaType::Array => v.is_array(),
        SchemaType::Integer | SchemaType::Long | SchemaType::Float | SchemaType::Double => v.is_number(),
        SchemaType::String => v.is_string(),
        SchemaType::Boolean => v.is_boolean(),
        SchemaType::Null => v.is_null(),
    }
}

/// JS `===` on JSON values: primitives by value (numbers numerically), objects/arrays never
/// (they are distinct references once parsed).
pub(crate) fn js_strict_equal(a: &Value, b: &Value) -> bool {
    match (a, b) {
        (Value::Null, Value::Null) => true,
        (Value::Bool(x), Value::Bool(y)) => x == y,
        (Value::String(x), Value::String(y)) => x == y,
        (Value::Number(x), Value::Number(y)) => num_eq(x, y),
        _ => false,
    }
}

fn num_eq(x: &serde_json::Number, y: &serde_json::Number) -> bool {
    match (x.as_i64(), y.as_i64()) {
        (Some(p), Some(q)) => p == q,
        _ => x.as_f64() == y.as_f64(),
    }
}

/// JS `deepEqual` (util/deepEqual.ts): structural equality, numbers by value, an array never
/// equals an object. Local copy so the validators do not depend on the shared util module.
pub(crate) fn js_deep_equal(x: &Value, y: &Value) -> bool {
    let mut stack: Vec<(&Value, &Value)> = vec![(x, y)];
    while let Some((a, b)) = stack.pop() {
        match (a, b) {
            (Value::Array(p), Value::Array(q)) => {
                if p.len() != q.len() {
                    return false;
                }
                stack.extend(p.iter().zip(q));
            }
            (Value::Object(p), Value::Object(q)) => {
                if p.len() != q.len() {
                    return false;
                }
                for (k, v) in p {
                    stack.push((v, q.get(k).unwrap_or(&Value::Null)));
                }
            }
            _ => {
                if !js_strict_equal(a, b) {
                    return false;
                }
            }
        }
    }
    true
}

/// JS `ErrorMessageFormatter.formatValue(value)` (max length 200). Local copy so the validators
/// do not depend on the shared util module.
pub(crate) fn format_value(value: &Value) -> String {
    const MAX_LENGTH: usize = 200;
    match value {
        Value::Null => "null".into(),
        Value::String(s) => format!("\"{s}\""),
        Value::Number(n) => js_number_value_to_string(n),
        Value::Bool(b) => b.to_string(),
        Value::Array(_) | Value::Object(_) => {
            let json = js_stringify(value);
            let mut formatted = json.clone();
            if json.chars().count() > 2 {
                formatted = json.replace(',', ", ").replace(':', ": ");
                let mut collapsed = String::with_capacity(formatted.len());
                let mut in_ws = false;
                for c in formatted.chars() {
                    if c.is_whitespace() {
                        if !in_ws {
                            collapsed.push(' ');
                        }
                        in_ws = true;
                    } else {
                        collapsed.push(c);
                        in_ws = false;
                    }
                }
                formatted = collapsed;
            }
            if formatted.encode_utf16().count() > MAX_LENGTH {
                let units: Vec<u16> = formatted.encode_utf16().take(MAX_LENGTH).collect();
                return format!("{}...", String::from_utf16_lossy(&units));
            }
            formatted
        }
    }
}
