//! Port of kirun-js `exception/*` plus the schema validator/convertor exceptions.
//! `Display` reproduces the JS `error.message` of each exception class exactly.

use thiserror::Error;

#[derive(Debug, Clone, PartialEq, Error)]
pub enum KIRunError {
    /// JS `KIRuntimeException`.
    #[error("{0}")]
    KIRuntime(String),

    /// JS `ExecutionException`.
    #[error("{0}")]
    Execution(String),

    /// JS `ExpressionEvaluationException`: `expression + ' : ' + message`.
    #[error("{expression} : {message}")]
    ExpressionEvaluation { expression: String, message: String },

    /// JS `SchemaValidationException`: message, then each cause's message on its own line.
    #[error("{}", with_causes(.message, .causes))]
    SchemaValidation {
        schema_path: String,
        message: String,
        causes: Vec<KIRunError>,
    },

    /// JS `SchemaReferenceException`: `path-message` when the path is not blank.
    #[error("{}", if .schema_path.trim().is_empty() { .message.clone() } else { format!("{}-{}", .schema_path, .message) })]
    SchemaReference { schema_path: String, message: String },

    /// JS `SchemaConversionException`.
    #[error("{}", with_causes(.message, .causes))]
    SchemaConversion {
        schema_path: String,
        source_value: String,
        message: String,
        mode: Option<String>,
        causes: Vec<KIRunError>,
    },
}

fn with_causes(message: &str, causes: &[KIRunError]) -> String {
    causes
        .iter()
        .fold(message.to_string(), |acc, c| format!("{acc}\n{c}"))
}

impl KIRunError {
    pub fn runtime(message: impl Into<String>) -> Self {
        KIRunError::KIRuntime(message.into())
    }

    pub fn execution(message: impl Into<String>) -> Self {
        KIRunError::Execution(message.into())
    }

    pub fn expression_evaluation(expression: impl Into<String>, message: impl Into<String>) -> Self {
        KIRunError::ExpressionEvaluation {
            expression: expression.into(),
            message: message.into(),
        }
    }

    pub fn schema_validation(schema_path: impl Into<String>, message: impl Into<String>) -> Self {
        KIRunError::SchemaValidation {
            schema_path: schema_path.into(),
            message: message.into(),
            causes: Vec::new(),
        }
    }

    pub fn schema_validation_with(
        schema_path: impl Into<String>,
        message: impl Into<String>,
        causes: Vec<KIRunError>,
    ) -> Self {
        KIRunError::SchemaValidation {
            schema_path: schema_path.into(),
            message: message.into(),
            causes,
        }
    }

    pub fn schema_reference(schema_path: impl Into<String>, message: impl Into<String>) -> Self {
        KIRunError::SchemaReference {
            schema_path: schema_path.into(),
            message: message.into(),
        }
    }

    /// JS `err.message`.
    pub fn get_message(&self) -> String {
        self.to_string()
    }

    pub fn get_schema_path(&self) -> Option<&str> {
        match self {
            KIRunError::SchemaValidation { schema_path, .. }
            | KIRunError::SchemaReference { schema_path, .. }
            | KIRunError::SchemaConversion { schema_path, .. } => Some(schema_path),
            _ => None,
        }
    }
}

pub type Result<T> = std::result::Result<T, KIRunError>;
