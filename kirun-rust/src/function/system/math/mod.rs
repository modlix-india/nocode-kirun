//! Port of kirun-js `function/system/math/*`.

pub mod add;
pub use add::Add;
pub mod generic_math_function;
pub use generic_math_function::GenericMathFunction;
pub mod hypotenuse;
pub use hypotenuse::Hypotenuse;
pub mod math_function_repository;
pub use math_function_repository::MathFunctionRepository;
pub mod maximum;
pub use maximum::Maximum;
pub mod minimum;
pub use minimum::Minimum;
pub mod random;
pub use random::Random;
pub mod random_any;
pub use random_any::RandomAny;
