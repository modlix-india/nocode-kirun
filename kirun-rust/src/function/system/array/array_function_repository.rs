//! Port of kirun-js `function/system/array/ArrayFunctionRepository.ts`.

use std::sync::Arc;

use async_trait::async_trait;

use super::add_first::AddFirst;
use super::array_to_array_of_objects::ArrayToArrayOfObjects;
use super::array_to_object::ArrayToObject;
use super::binary_search::BinarySearch;
use super::compare::Compare;
use super::concatenate::Concatenate;
use super::copy::Copy;
use super::delete::Delete;
use super::delete_first::DeleteFirst;
use super::delete_from::DeleteFrom;
use super::delete_last::DeleteLast;
use super::disjoint::Disjoint;
use super::equals::Equals;
use super::fill::Fill;
use super::frequency::Frequency;
use super::index_of::IndexOf;
use super::index_of_array::IndexOfArray;
use super::insert::Insert;
use super::insert_last::InsertLast;
use super::join::Join;
use super::last_index_of::LastIndexOf;
use super::last_index_of_array::LastIndexOfArray;
use super::max::Max;
use super::min::Min;
use super::mis_match::MisMatch;
use super::remove_duplicates::RemoveDuplicates;
use super::reverse::Reverse;
use super::rotate::Rotate;
use super::shuffle::Shuffle;
use super::sort::Sort;
use super::sub_array::SubArray;
use crate::function::Function;
use crate::namespaces::Namespaces;
use crate::repository::Repository;
use crate::Map;

pub struct ArrayFunctionRepository {
    repo_map: Map<Arc<dyn Function>>,
    filterable_names: Vec<String>,
}

impl ArrayFunctionRepository {
    pub fn new() -> Self {
        let functions: Vec<Arc<dyn Function>> = vec![
            Arc::new(Concatenate::new()),
            Arc::new(AddFirst::new()),
            Arc::new(BinarySearch::new()),
            Arc::new(Compare::new()),
            Arc::new(Copy::new()),
            Arc::new(Delete::new()),
            Arc::new(DeleteFirst::new()),
            Arc::new(DeleteFrom::new()),
            Arc::new(DeleteLast::new()),
            Arc::new(Disjoint::new()),
            Arc::new(Equals::new()),
            Arc::new(Fill::new()),
            Arc::new(Frequency::new()),
            Arc::new(IndexOf::new()),
            Arc::new(IndexOfArray::new()),
            Arc::new(LastIndexOf::new()),
            Arc::new(LastIndexOfArray::new()),
            Arc::new(Max::new()),
            Arc::new(Min::new()),
            Arc::new(MisMatch::new()),
            Arc::new(Reverse::new()),
            Arc::new(Rotate::new()),
            Arc::new(Shuffle::new()),
            Arc::new(Sort::new()),
            Arc::new(SubArray::new()),
            Arc::new(ArrayToArrayOfObjects::new()),
            Arc::new(Insert::new()),
            Arc::new(InsertLast::new()),
            Arc::new(RemoveDuplicates::new()),
            Arc::new(ArrayToObject::new()),
            Arc::new(Join::new()),
        ];

        // JS `mapEntry(fn)`: keyed by the signature name.
        let repo_map: Map<Arc<dyn Function>> =
            functions.into_iter().map(|f| (f.get_signature().get_name().to_string(), f)).collect();
        let filterable_names = repo_map.values().map(|f| f.get_signature().get_full_name()).collect();

        ArrayFunctionRepository { repo_map, filterable_names }
    }
}

impl Default for ArrayFunctionRepository {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl Repository<dyn Function> for ArrayFunctionRepository {
    async fn find(&self, namespace: &str, name: &str) -> Option<Arc<dyn Function>> {
        if namespace != Namespaces::SYSTEM_ARRAY {
            return None;
        }
        self.repo_map.get(name).cloned()
    }

    async fn filter(&self, name: &str) -> Vec<String> {
        let name = name.to_lowercase();
        self.filterable_names.iter().filter(|e| e.to_lowercase().contains(&name)).cloned().collect()
    }
}
