//! Port of kirun-js `json/schema/validator/StringValidator.ts`.

use std::sync::LazyLock;

use regex::Regex;

use crate::json::schema::string::StringFormat;
use crate::json::schema::validator::schema_validator::{format_value, SchemaPath};
use crate::json::schema::Schema;
use crate::{KIRunError, Result, Value};

static TIME: LazyLock<Regex> = LazyLock::new(|| {
    Regex::new(r"^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9](\.[0-9]{1,9})?)?(Z|[+-][01][0-9]:[0-5][0-9])?$").unwrap()
});

static DATE: LazyLock<Regex> =
    LazyLock::new(|| Regex::new(r"^[0-9]{4,4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[01])$").unwrap());

static DATETIME: LazyLock<Regex> = LazyLock::new(|| {
    Regex::new(
        r"^[0-9]{4,4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[01])T([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9](\.[0-9]{1,9})?)?(Z|[+-][01][0-9]:[0-5][0-9])?$",
    )
    .unwrap()
});

static EMAIL: LazyLock<Regex> = LazyLock::new(|| {
    Regex::new(r"^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$").unwrap()
});

static DECIMAL: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"^[+-]?[0-9]+(\.[0-9]+)?$").unwrap());

// A row id on either app data backend: a Mongo ObjectId (24 hex) or a ULID (26 Crockford base32).
static ID: LazyLock<Regex> =
    LazyLock::new(|| Regex::new(r"^([0-9a-fA-F]{24}|[0-9A-HJKMNP-TV-Za-hjkmnp-tv-z]{26})$").unwrap());

pub struct StringValidator;

impl StringValidator {
    /// JS `StringValidator.validate(parents, schema, element)`.
    pub fn validate(parents: &[Schema], schema: &Schema, element: Value) -> Result<Value> {
        Self::validate_in(parents, schema, element)
    }

    pub(crate) fn validate_in<P: SchemaPath + ?Sized>(parents: &P, schema: &Schema, element: Value) -> Result<Value> {
        let Value::String(s) = &element else {
            return Err(KIRunError::schema_validation(
                parents.schema_path(),
                format!("Expected a string but found {}", format_value(&element)),
            ));
        };

        match schema.get_format() {
            Some(StringFormat::Time) => Self::pattern_matcher(parents, schema, s, TIME.is_match(s), "time pattern")?,
            Some(StringFormat::Date) => Self::pattern_matcher(parents, schema, s, DATE.is_match(s), "date pattern")?,
            Some(StringFormat::DateTime) => {
                Self::pattern_matcher(parents, schema, s, DATETIME.is_match(s), "date time pattern")?
            }
            Some(StringFormat::Email) => {
                Self::pattern_matcher(parents, schema, s, EMAIL.is_match(s), "email pattern")?
            }
            Some(StringFormat::Decimal) => {
                Self::pattern_matcher(parents, schema, s, DECIMAL.is_match(s), "decimal pattern")?
            }
            Some(StringFormat::Id) => Self::pattern_matcher(parents, schema, s, ID.is_match(s), "id pattern")?,
            _ => {
                if let Some(pattern) = schema.get_pattern().filter(|p| !p.is_empty()) {
                    let matched = js_regex_test(pattern, s).map_err(|e| {
                        KIRunError::schema_validation(
                            parents.schema_path(),
                            format!("Invalid regular expression: /{pattern}/: {e}"),
                        )
                    })?;
                    Self::pattern_matcher(parents, schema, s, matched, &format!("pattern {pattern}"))?;
                }
            }
        }

        // JS `string.length` counts UTF-16 code units.
        let length = s.encode_utf16().count();
        if let Some(min) = schema.get_min_length().copied().filter(|m| *m != 0) {
            if length < min {
                return Err(KIRunError::schema_validation(
                    parents.schema_path(),
                    message(schema, "minLength").unwrap_or_else(|| format!("Expected a minimum of {min} characters")),
                ));
            }
        }
        if let Some(max) = schema.get_max_length().copied().filter(|m| *m != 0) {
            if length > max {
                return Err(KIRunError::schema_validation(
                    parents.schema_path(),
                    message(schema, "maxLength").unwrap_or_else(|| format!("Expected a maximum of {max} characters")),
                ));
            }
        }

        Ok(element)
    }

    fn pattern_matcher<P: SchemaPath + ?Sized>(
        parents: &P,
        schema: &Schema,
        element: &str,
        matched: bool,
        message_text: &str,
    ) -> Result<()> {
        if !matched {
            return Err(KIRunError::schema_validation(
                parents.schema_path(),
                message(schema, "pattern").unwrap_or_else(|| format!("{element} is not matched with the {message_text}")),
            ));
        }
        Ok(())
    }
}

/// JS `new RegExp(pattern).test(s)`. JS patterns may use lookaround, so this uses `fancy_regex`.
pub(crate) fn js_regex_test(pattern: &str, s: &str) -> std::result::Result<bool, String> {
    let re = crate::util::regex_cache::cached_regex(pattern).map_err(|e| e.to_string())?;
    re.is_match(s).map_err(|e| e.to_string())
}

fn message(schema: &Schema, key: &str) -> Option<String> {
    schema.get_details().and_then(|d| d.get_validation_message(key)).cloned()
}
