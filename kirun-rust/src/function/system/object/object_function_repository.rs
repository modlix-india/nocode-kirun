//! Port of kirun-js `function/system/object/ObjectFunctionRepository.ts`.

use std::sync::Arc;

use async_trait::async_trait;

use super::{ObjectConvert, ObjectDeleteKey, ObjectEntries, ObjectKeys, ObjectPutValue, ObjectValues};
use crate::function::Function;
use crate::namespaces::Namespaces;
use crate::repository::Repository;
use crate::Map;

pub struct ObjectFunctionRepository {
    function_objects_index: Map<Arc<dyn Function>>,
    filterable_names: Vec<String>,
}

impl ObjectFunctionRepository {
    pub fn new() -> Self {
        let functions: Vec<(&str, Arc<dyn Function>)> = vec![
            ("ObjectValues", Arc::new(ObjectValues::new())),
            ("ObjectKeys", Arc::new(ObjectKeys::new())),
            ("ObjectEntries", Arc::new(ObjectEntries::new())),
            ("ObjectDeleteKey", Arc::new(ObjectDeleteKey::new())),
            ("ObjectPutValue", Arc::new(ObjectPutValue::new())),
            ("ObjectConvert", Arc::new(ObjectConvert::new())),
        ];
        let function_objects_index: Map<Arc<dyn Function>> =
            functions.into_iter().map(|(k, v)| (k.to_string(), v)).collect();
        let filterable_names =
            function_objects_index.values().map(|f| f.get_signature().get_full_name()).collect();
        Self { function_objects_index, filterable_names }
    }
}

impl Default for ObjectFunctionRepository {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl Repository<dyn Function> for ObjectFunctionRepository {
    async fn find(&self, namespace: &str, name: &str) -> Option<Arc<dyn Function>> {
        if namespace != Namespaces::SYSTEM_OBJECT {
            return None;
        }
        self.function_objects_index.get(name).cloned()
    }

    async fn filter(&self, name: &str) -> Vec<String> {
        let name = name.to_lowercase();
        self.filterable_names.iter().filter(|e| e.to_lowercase().contains(&name)).cloned().collect()
    }
}
