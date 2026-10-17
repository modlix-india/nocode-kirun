//! Port of kirun-js `exception/*` plus the schema validator/convertor exceptions.
//! `Display` reproduces the `error.message` of each exception class. The schema exceptions
//! follow kirun-java's `getMessage()` (`<schema path> - <message>`), which stored server
//! functions answer with; kirun-js leaves the path out (and writes `path-message` for a
//! reference error). `Uncaught` is kirun-java's own unchecked exceptions.

use thiserror::Error;

#[derive(Debug, Clone, PartialEq, Error)]
pub enum KIRunError {
    /// JS `KIRuntimeException`.
    #[error("{0}")]
    KIRuntime(String),

    /// JS `ExecutionException`.
    #[error("{0}")]
    Execution(String),

    /// An unchecked Java exception kirun-java lets out of a function as it is: a
    /// `NullPointerException` from Reactor (`value` for `Mono.just(null)`, `t2` for
    /// `Tuples.of(k, null)`) or from Gson (the JVM's helpful message), an `IllegalStateException`
    /// from `Collectors.toMap`. Its message is the Java message, and no wrapper adds the function
    /// or parameter to it (kirun-java's `AbstractReactiveFunction` and argument validation do
    /// not catch these), so it reaches the caller exactly as kirun-java's does.
    #[error("{0}")]
    Uncaught(String),

    /// JS `ExpressionEvaluationException`: `expression + ' : ' + message`.
    #[error("{expression} : {message}")]
    ExpressionEvaluation { expression: String, message: String },

    /// kirun-java `SchemaValidationException`: `path - ` when the path is not blank, the message,
    /// then each cause's message (with its own path) on its own line.
    #[error("{}", with_path(.schema_path, &with_causes(.message, .causes)))]
    SchemaValidation {
        schema_path: String,
        message: String,
        causes: Vec<KIRunError>,
    },

    /// kirun-java `SchemaReferenceException`: `path - message` when the path is not blank.
    #[error("{}", with_path(.schema_path, .message))]
    SchemaReference { schema_path: String, message: String },

    /// kirun-java `SchemaConversionException`: as `SchemaValidation`.
    #[error("{}", with_path(.schema_path, &with_causes(.message, .causes)))]
    SchemaConversion {
        schema_path: String,
        source_value: String,
        message: String,
        mode: Option<String>,
        causes: Vec<KIRunError>,
    },
}

/// kirun-java's `getMessage()` for the schema exceptions: the path first, unless it is blank
/// (Java `String.isBlank`).
fn with_path(schema_path: &str, message: &str) -> String {
    if schema_path.trim().is_empty() {
        message.to_string()
    } else {
        format!("{schema_path} - {message}")
    }
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

    /// See [`KIRunError::Uncaught`].
    pub fn uncaught(message: impl Into<String>) -> Self {
        KIRunError::Uncaught(message.into())
    }

    /// Whether wrappers must pass this error on unchanged ([`KIRunError::Uncaught`]).
    pub fn is_uncaught(&self) -> bool {
        matches!(self, KIRunError::Uncaught(_))
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
