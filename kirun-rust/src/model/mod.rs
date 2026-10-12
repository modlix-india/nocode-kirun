//! Port of kirun-js `model/*`.

pub mod event;
pub mod event_result;
pub mod function_definition;
pub mod function_output;
pub mod function_signature;
pub mod parameter;
pub mod parameter_reference;
pub mod position;
pub mod statement;
pub mod statement_group;

pub use event::Event;
pub use event_result::EventResult;
pub use function_definition::FunctionDefinition;
pub use function_output::{FunctionOutput, FunctionOutputGenerator};
pub use function_signature::FunctionSignature;
pub use parameter::{Parameter, ParameterType};
pub use parameter_reference::{ParameterReference, ParameterReferenceType};
pub use position::Position;
pub use statement::Statement;
pub use statement_group::StatementGroup;
