//! Port of kirun-js `function/system/string/StringFunctionRepository.ts`.

use std::sync::Arc;

use async_trait::async_trait;

use crate::function::system::string::abstract_string_function::{js_regex, AbstractStringFunction as A};
use crate::function::system::string::{
    concatenate::Concatenate, delete_for_given_length::DeleteForGivenLength,
    insert_at_given_position::InsertAtGivenPosition, matches::Matches, post_pad::PostPad, pre_pad::PrePad,
    region_matches::RegionMatches, replace_at_given_position::ReplaceAtGivenPosition, reverse::Reverse, split::Split,
    to_string::ToString, trim_to::TrimTo,
};
use crate::function::Function;
use crate::namespaces::Namespaces;
use crate::repository::Repository;
use crate::util::string::js_string;
use crate::{KIRunError, Map};

pub struct StringFunctionRepository {
    repo_map: Map<Arc<dyn Function>>,
    filterable_names: Vec<String>,
}

fn entry(f: Arc<dyn Function>) -> (String, Arc<dyn Function>) {
    (f.get_signature().get_name().to_string(), f)
}

impl StringFunctionRepository {
    pub fn new() -> Self {
        let entries: Vec<(String, Arc<dyn Function>)> = vec![
            A::of_entry_string_and_string_output("Trim", js_string::trim),
            A::of_entry_string_and_string_output("TrimStart", js_string::trim_start),
            A::of_entry_string_and_string_output("TrimEnd", js_string::trim_end),
            A::of_entry_string_and_integer_output("Length", |e| js_string::length(e) as f64),
            A::of_entry_string_string_and_integer_output("Frequency", |a, b| {
                let mut count = 0.0;
                let mut index = js_string::index_of(a, b, 0.0);
                while index != -1 {
                    count += 1.0;
                    index = js_string::index_of(a, b, (index + 1) as f64);
                }
                count
            }),
            A::of_entry_string_and_string_output("LowerCase", |e| e.to_lowercase()),
            A::of_entry_string_and_string_output("UpperCase", |e| e.to_uppercase()),
            A::of_entry_string_and_boolean_output("IsBlank", |e| js_string::trim(e).is_empty()),
            A::of_entry_string_and_boolean_output("IsEmpty", |e| e.is_empty()),
            A::of_entry_string_string_and_boolean_output("Contains", |a, b| Ok(js_string::index_of(a, b, 0.0) != -1)),
            A::of_entry_string_string_and_boolean_output("EndsWith", |a, b| Ok(a.ends_with(b))),
            A::of_entry_string_string_and_boolean_output("StartsWith", |a, b| Ok(a.starts_with(b))),
            A::of_entry_string_string_and_boolean_output("EqualsIgnoreCase", |a, b| {
                Ok(a.to_uppercase() == b.to_uppercase())
            }),
            A::of_entry_string_string_and_boolean_output("Matches", |a, b| {
                Ok(js_regex(b)?.is_match(a).unwrap_or(false))
            }),
            A::of_entry_string_string_and_integer_output("IndexOf", |a, b| js_string::index_of(a, b, 0.0) as f64),
            A::of_entry_string_string_and_integer_output("LastIndexOf", |a, b| {
                js_string::last_index_of(a, b, f64::INFINITY) as f64
            }),
            A::of_entry_string_integer_and_string_output("Repeat", |a, b| {
                js_string::repeat(a, b).map_err(KIRunError::runtime)
            }),
            A::of_entry_string_string_integer_and_integer_output("IndexOfWithStartPoint", |a, b, c| {
                js_string::index_of(a, b, c) as f64
            }),
            A::of_entry_string_string_integer_and_integer_output("LastIndexOfWithStartPoint", |a, b, c| {
                js_string::last_index_of(a, b, c) as f64
            }),
            A::of_entry_string_string_string_and_string_output("Replace", js_string::replace_all),
            A::of_entry_string_string_string_and_string_output("ReplaceFirst", js_string::replace_first),
            A::of_entry_string_integer_integer_and_string_output("SubString", js_string::substring),
            entry(Arc::new(Concatenate::new())),
            entry(Arc::new(DeleteForGivenLength::new())),
            entry(Arc::new(InsertAtGivenPosition::new())),
            entry(Arc::new(PostPad::new())),
            entry(Arc::new(PrePad::new())),
            entry(Arc::new(RegionMatches::new())),
            entry(Arc::new(ReplaceAtGivenPosition::new())),
            entry(Arc::new(Reverse::new())),
            entry(Arc::new(Split::new())),
            entry(Arc::new(ToString::new())),
            entry(Arc::new(TrimTo::new())),
            // Replaces the generic `Matches` above but keeps its position, like a JS Map.
            entry(Arc::new(Matches::new())),
        ];
        let repo_map: Map<Arc<dyn Function>> = entries.into_iter().collect();
        let filterable_names = repo_map.values().map(|f| f.get_signature().get_full_name()).collect();
        Self { repo_map, filterable_names }
    }
}

impl Default for StringFunctionRepository {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl Repository<dyn Function> for StringFunctionRepository {
    async fn find(&self, namespace: &str, name: &str) -> Option<Arc<dyn Function>> {
        if namespace != Namespaces::STRING {
            return None;
        }
        self.repo_map.get(name).cloned()
    }

    async fn filter(&self, name: &str) -> Vec<String> {
        let name = name.to_lowercase();
        self.filterable_names.iter().filter(|e| e.to_lowercase().contains(&name)).cloned().collect()
    }
}
