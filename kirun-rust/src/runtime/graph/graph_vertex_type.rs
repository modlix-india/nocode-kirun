//! Port of kirun-js `runtime/graph/GraphVertexType.ts`. Keys are statement names (`String`).

use indexmap::IndexSet;

use crate::runtime::statement_execution::StatementExecution;

pub trait GraphVertexType {
    fn get_unique_key(&self) -> &str;
    /// JS `getDepenedencies` (sic).
    fn get_dependencies(&self) -> &IndexSet<String>;
}

impl GraphVertexType for StatementExecution {
    fn get_unique_key(&self) -> &str {
        StatementExecution::get_unique_key(self)
    }
    fn get_dependencies(&self) -> &IndexSet<String> {
        StatementExecution::get_dependencies(self)
    }
}

impl<T: GraphVertexType> GraphVertexType for std::sync::Arc<T> {
    fn get_unique_key(&self) -> &str {
        (**self).get_unique_key()
    }
    fn get_dependencies(&self) -> &IndexSet<String> {
        (**self).get_dependencies()
    }
}
