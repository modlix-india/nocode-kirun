//! Port of kirun-js `repository/KIRunFunctionRepository.ts`.

use std::sync::Arc;

use async_trait::async_trait;

use crate::function::system::array::array_function_repository::ArrayFunctionRepository;
use crate::function::system::array::join::Join;
use crate::function::system::context::create::Create;
use crate::function::system::context::get::Get;
use crate::function::system::context::set_function::SetFunction;
use crate::function::system::date::date_function_repository::DateFunctionRepository;
use crate::function::system::generate_event::GenerateEvent;
use crate::function::system::json::{json_parse::JSONParse, json_stringify::JSONStringify};
use crate::function::system::make::Make;
use crate::function::system::math::math_function_repository::MathFunctionRepository;
use crate::function::system::print::Print;
use crate::function::system::object::object_function_repository::ObjectFunctionRepository;
use crate::function::system::r#if::If;
use crate::function::system::string::string_function_repository::StringFunctionRepository;
use crate::function::system::r#loop::{Break, CountLoop, ForEachLoop, RangeLoop};
use crate::function::system::validate_schema::ValidateSchema;
use crate::function::system::wait::Wait;
use crate::function::Function;
use crate::hybrid_repository::HybridRepository;
use crate::namespaces::Namespaces;
use crate::repository::Repository;
use crate::Map;

/// JS `SystemFunctionRepository` (module-private class in KIRunFunctionRepository.ts).
struct SystemFunctionRepository {
    map: Map<Map<Arc<dyn Function>>>,
    filterable_names: Vec<String>,
}

fn entries(functions: Vec<Arc<dyn Function>>) -> Map<Arc<dyn Function>> {
    functions.into_iter().map(|f| (f.get_signature().get_name().to_string(), f)).collect()
}

impl SystemFunctionRepository {
    fn new() -> Self {
        let mut map: Map<Map<Arc<dyn Function>>> = Map::new();
        map.insert(
            Namespaces::SYSTEM_JSON.to_string(),
            entries(vec![Arc::new(JSONParse::new()), Arc::new(JSONStringify::new())]),
        );
        map.insert(
            Namespaces::SYSTEM_CTX.to_string(),
            entries(vec![Arc::new(Create::new()), Arc::new(Get::new()), Arc::new(SetFunction::new())]),
        );
        map.insert(
            Namespaces::SYSTEM_LOOP.to_string(),
            entries(vec![
                Arc::new(RangeLoop::new()),
                Arc::new(CountLoop::new()),
                Arc::new(Break::new()),
                Arc::new(ForEachLoop::new()),
            ]),
        );
        map.insert(
            Namespaces::SYSTEM.to_string(),
            entries(vec![
                Arc::new(If::new()),
                Arc::new(GenerateEvent::new()),
                Arc::new(Print::new()),
                Arc::new(Wait::new()),
                Arc::new(Join::new()),
                Arc::new(ValidateSchema::new()),
                Arc::new(Make::new()),
            ]),
        );

        let filterable_names = map
            .values()
            .flat_map(|m| m.values())
            .map(|f| f.get_signature().get_full_name())
            .collect();

        SystemFunctionRepository { map, filterable_names }
    }
}

#[async_trait]
impl Repository<dyn Function> for SystemFunctionRepository {
    async fn find(&self, namespace: &str, name: &str) -> Option<Arc<dyn Function>> {
        self.map.get(namespace).and_then(|m| m.get(name)).cloned()
    }

    async fn filter(&self, name: &str) -> Vec<String> {
        let name = name.to_lowercase();
        self.filterable_names.iter().filter(|e| e.to_lowercase().contains(&name)).cloned().collect()
    }
}

/// JS `KIRunFunctionRepository`: a hybrid of the System, Math, String, Array, Object
/// and Date function repositories.
pub struct KIRunFunctionRepository {
    hybrid: HybridRepository<dyn Function>,
}

impl KIRunFunctionRepository {
    pub fn new() -> Self {
        KIRunFunctionRepository {
            hybrid: HybridRepository::new(vec![
                Arc::new(SystemFunctionRepository::new()),
                Arc::new(MathFunctionRepository::new()),
                Arc::new(StringFunctionRepository::new()),
                Arc::new(ArrayFunctionRepository::new()),
                Arc::new(ObjectFunctionRepository::new()),
                Arc::new(DateFunctionRepository::new()),
            ]),
        }
    }
}

impl Default for KIRunFunctionRepository {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl Repository<dyn Function> for KIRunFunctionRepository {
    async fn find(&self, namespace: &str, name: &str) -> Option<Arc<dyn Function>> {
        self.hybrid.find(namespace, name).await
    }

    async fn filter(&self, name: &str) -> Vec<String> {
        self.hybrid.filter(name).await
    }
}
