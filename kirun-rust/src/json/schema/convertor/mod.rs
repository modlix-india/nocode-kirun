//! Port of kirun-js `json/schema/convertor/*`.

pub mod boolean_convertor;
pub mod null_convertor;
pub mod number_convertor;
pub mod string_convertor;

pub use boolean_convertor::BooleanConvertor;
pub use null_convertor::NullConvertor;
pub use number_convertor::NumberConvertor;
pub use string_convertor::StringConvertor;

use crate::{KIRunError, Result};

/// JS `ConversionMode`.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub enum ConversionMode {
    Strict,
    Lenient,
    UseDefault,
    Skip,
}

impl ConversionMode {
    pub fn name(&self) -> &'static str {
        match self {
            ConversionMode::Strict => "STRICT",
            ConversionMode::Lenient => "LENIENT",
            ConversionMode::UseDefault => "USE_DEFAULT",
            ConversionMode::Skip => "SKIP",
        }
    }

    /// JS `genericValueOf`.
    pub fn generic_value_of(mode: &str) -> Result<ConversionMode> {
        Self::get_conversion_modes()
            .into_iter()
            .find(|m| m.name().eq_ignore_ascii_case(mode))
            .ok_or_else(|| KIRunError::runtime(format!("Unknown conversion mode : {mode}")))
    }

    /// JS `getConversionModes`.
    pub fn get_conversion_modes() -> Vec<ConversionMode> {
        vec![
            ConversionMode::Strict,
            ConversionMode::Lenient,
            ConversionMode::UseDefault,
            ConversionMode::Skip,
        ]
    }
}
