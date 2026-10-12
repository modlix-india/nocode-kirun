//! Port of kirun-js `Repository.ts` and `repository/*`.

pub mod kirun_function_repository;
pub mod kirun_schema_repository;

use std::sync::Arc;

use async_trait::async_trait;

pub use kirun_function_repository::KIRunFunctionRepository;
pub use kirun_schema_repository::KIRunSchemaRepository;

/// JS `Repository<T>`. `T` is `dyn Function` for function repositories and `Schema` for schemas.
#[async_trait]
pub trait Repository<T: ?Sized + Send + Sync>: Send + Sync {
    async fn find(&self, namespace: &str, name: &str) -> Option<Arc<T>>;

    /// Full names (`Namespace.Name`) containing `name`, case-insensitively.
    async fn filter(&self, name: &str) -> Vec<String>;
}
