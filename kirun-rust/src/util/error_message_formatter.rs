//! Port of kirun-js `util/ErrorMessageFormatter.ts`.

use crate::json::schema::r#type::SchemaType;
use crate::json::schema::Schema;
use crate::util::primitive::{js_number_value_to_string, js_stringify};
use crate::{KIRunError, Value};

pub struct ErrorMessageFormatter;

/// Prefix JS puts on nested function execution errors.
const NESTED_ERROR_PREFIX: &str = "Error while executing the function ";

impl ErrorMessageFormatter {
    /// JS `formatValue(value, maxLength = 200)`: strings quoted, numbers and booleans via
    /// `String()`, objects and arrays as JSON with a space after `,` and `:`, truncated to
    /// `max_length` UTF-16 units plus `...`. (A `Value` is a tree, so `[Circular]` cannot occur.)
    pub fn format_value(value: &Value, max_length: usize) -> String {
        match value {
            Value::Null => "null".into(),
            Value::String(s) => format!("\"{s}\""),
            Value::Number(n) => js_number_value_to_string(n),
            Value::Bool(b) => b.to_string(),
            Value::Array(_) | Value::Object(_) => {
                let json = js_stringify(value);
                let mut formatted = json.clone();
                if json.encode_utf16().count() > 2 {
                    let spaced = json.replace(',', ", ").replace(':', ": ");
                    // JS `.replaceAll(/\s+/g, ' ')`
                    let mut collapsed = String::with_capacity(spaced.len());
                    let mut in_ws = false;
                    for c in spaced.chars() {
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
                if formatted.encode_utf16().count() > max_length {
                    let units: Vec<u16> = formatted.encode_utf16().take(max_length).collect();
                    return format!("{}...", String::from_utf16_lossy(&units));
                }
                formatted
            }
        }
    }

    /// JS `formatFunctionName(namespace, name)`.
    pub fn format_function_name(namespace: &str, name: &str) -> String {
        if namespace.is_empty() || namespace == "undefined" || namespace == "null" {
            return name.to_string();
        }
        format!("{namespace}.{name}")
    }

    /// JS `formatStatementName(statementName)`: `'name'`, or `None` when unknown.
    pub fn format_statement_name(statement_name: Option<&str>) -> Option<String> {
        match statement_name {
            None => None,
            Some(s) if s.is_empty() || s == "undefined" || s == "null" => None,
            Some(s) => Some(format!("'{s}'")),
        }
    }

    /// JS `formatSchemaDefinition(schema)`.
    pub fn format_schema_definition(schema: Option<&Schema>) -> String {
        let Some(schema) = schema else {
            return "any".into();
        };
        let Some(t) = schema.get_type() else {
            return "any".into();
        };
        let allowed = t.get_allowed_schema_types();
        match allowed.len() {
            0 => "any".into(),
            1 => Self::format_single_schema_type(schema, *allowed.iter().next().unwrap()),
            _ => allowed.iter().map(|t| t.as_str()).collect::<Vec<_>>().join(" | "),
        }
    }

    /// JS `formatSingleSchemaType(schema, schemaType)`.
    fn format_single_schema_type(schema: &Schema, schema_type: SchemaType) -> String {
        let type_name = schema_type.as_str();

        if schema_type == SchemaType::Array {
            if let Some(items) = schema.get_items() {
                if let Some(single) = items.get_single_schema() {
                    return format!("Array<{}>", Self::format_schema_definition(Some(single)));
                }
                if let Some(tuple) = items.get_tuple_schema() {
                    if !tuple.is_empty() {
                        let types = tuple
                            .iter()
                            .map(|s| Self::format_schema_definition(Some(s)))
                            .collect::<Vec<_>>()
                            .join(", ");
                        return format!("[{types}]");
                    }
                }
            }
            return "Array".into();
        }

        if let Some(enums) = schema.get_enums() {
            if !enums.is_empty() {
                let values = enums
                    .iter()
                    .take(5)
                    .map(|e| Self::format_value(e, 50))
                    .collect::<Vec<_>>()
                    .join(" | ");
                let more = if enums.len() > 5 { " | ..." } else { "" };
                return format!("{type_name}({values}{more})");
            }
        }

        type_name.to_string()
    }

    /// JS `buildFunctionExecutionError(functionName, statementName, errorMessage,
    /// parameterName?, parameterSchema?)`.
    pub fn build_function_execution_error(
        function_name: &str,
        statement_name: &Option<String>,
        error_message: &str,
        parameter_name: Option<&str>,
        schema: Option<&Schema>,
    ) -> String {
        let parameter_part = match parameter_name {
            Some(p) if !p.is_empty() => format!("'s parameter {p}"),
            _ => String::new(),
        };
        let statement_part = match statement_name {
            Some(s) if !s.is_empty() => format!(" in statement {s}"),
            _ => String::new(),
        };
        let definition_part = match schema {
            Some(s) => format!(" [Expected: {}]", Self::format_schema_definition(Some(s))),
            None => String::new(),
        };
        let separator = if error_message.starts_with(NESTED_ERROR_PREFIX) { "\n" } else { "" };

        format!(
            "{NESTED_ERROR_PREFIX}{function_name}{parameter_part}{statement_part}{definition_part}: {separator}{error_message}"
        )
    }

    /// JS `formatErrorMessage(error)` for an exception: its `message`, with any
    /// `[object Object]` replaced by the formatted error. JS falls back to
    /// `formatValue(error)` (`{}` for an Error without own enumerable fields) when the
    /// message is empty.
    pub fn format_error_message(error: &KIRunError) -> String {
        let message = error.get_message();
        if message.is_empty() {
            return "{}".into();
        }
        if message.contains("[object Object]") {
            return message.replace("[object Object]", "{}");
        }
        message
    }

    /// JS `formatErrorMessage(error: any)` for a non-exception value: `null` → `Unknown error`,
    /// a string → itself, an object with `message` → that message, else `format_value`.
    pub fn format_error_value(error: &Value) -> String {
        match error {
            Value::Null => "Unknown error".into(),
            // JS `!error` is also true for false, 0 and "".
            Value::Bool(false) => "Unknown error".into(),
            Value::Number(n) if n.as_f64() == Some(0.0) => "Unknown error".into(),
            Value::String(s) if s.is_empty() => "Unknown error".into(),
            Value::String(s) => s.clone(),
            Value::Object(m) => match m.get("message") {
                Some(msg) if crate::util::primitive::js_truthy(msg) => {
                    let message = crate::util::primitive::js_to_string(msg);
                    if message.contains("[object Object]") {
                        let formatted = Self::format_value(error, 200);
                        return message.replace("[object Object]", &formatted);
                    }
                    message
                }
                _ => Self::format_value(error, 200),
            },
            _ => Self::format_value(error, 200),
        }
    }
}
