//! Port of kirun-js `dsl/*`: DSL text <-> FunctionDefinition JSON.

pub mod dsl_compiler;
pub mod lexer;
pub mod parser;
pub mod transformer;

pub use dsl_compiler::{DSLCompiler, FormattedError, ValidationResult};
pub use transformer::{ASTToJSONTransformer, JSONToTextTransformer, SchemaTransformer};
