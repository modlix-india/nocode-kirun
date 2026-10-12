//! Port of kirun-js `function/system/object/*`.

pub mod abstract_object_function;
pub use abstract_object_function::AbstractObjectFunction;
pub mod object_convert;
pub use object_convert::ObjectConvert;
pub mod object_delete_key;
pub use object_delete_key::ObjectDeleteKey;
pub mod object_entries;
pub use object_entries::ObjectEntries;
pub mod object_function_repository;
pub use object_function_repository::ObjectFunctionRepository;
pub mod object_keys;
pub use object_keys::ObjectKeys;
pub mod object_put_value;
pub use object_put_value::ObjectPutValue;
pub mod object_values;
pub use object_values::ObjectValues;
