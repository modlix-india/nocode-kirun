//! Port of kirun-js `json/schema/string/StringFormat.ts`.

use crate::{KIRunError, Result};

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub enum StringFormat {
    DateTime,
    Time,
    Date,
    Email,
    Regex,
    Decimal,
    Id,
}

impl StringFormat {
    pub fn name(&self) -> &'static str {
        match self {
            StringFormat::DateTime => "DATETIME",
            StringFormat::Time => "TIME",
            StringFormat::Date => "DATE",
            StringFormat::Email => "EMAIL",
            StringFormat::Regex => "REGEX",
            StringFormat::Decimal => "DECIMAL",
            StringFormat::Id => "ID",
        }
    }

    pub fn parse(s: &str) -> Result<StringFormat> {
        [
            StringFormat::DateTime,
            StringFormat::Time,
            StringFormat::Date,
            StringFormat::Email,
            StringFormat::Regex,
            StringFormat::Decimal,
            StringFormat::Id,
        ]
        .into_iter()
        .find(|f| f.name().eq_ignore_ascii_case(s))
        .ok_or_else(|| KIRunError::runtime(format!("Unknown string format : {s}")))
    }
}
