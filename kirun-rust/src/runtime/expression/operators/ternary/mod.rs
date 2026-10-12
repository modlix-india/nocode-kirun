//! Port of kirun-js `runtime/expression/operators/ternary/*`.

pub mod conditional_ternary_operator;
pub mod ternary_operator;

pub use conditional_ternary_operator::ConditionalTernaryOperator;
pub use ternary_operator::TernaryOperator;
