//! Compiled JS-style regexes (`fancy_regex`, which supports lookaround), cached by pattern.
//! Schema `pattern`s and the String `Matches` functions used to compile on every call.

use std::collections::HashMap;
use std::sync::{Arc, LazyLock};

use parking_lot::RwLock;

use crate::util::fx_hash::FxBuildHasher;

/// Patterns come from definitions and user input; past this many the cache starts over.
const MAX_ENTRIES: usize = 1024;

type Cache = HashMap<String, Arc<fancy_regex::Regex>, FxBuildHasher>;

static CACHE: LazyLock<RwLock<Cache>> = LazyLock::new(|| RwLock::new(HashMap::default()));

pub fn cached_regex(pattern: &str) -> Result<Arc<fancy_regex::Regex>, fancy_regex::Error> {
    if let Some(re) = CACHE.read().get(pattern) {
        return Ok(re.clone());
    }
    let re = Arc::new(fancy_regex::Regex::new(pattern)?);
    let mut cache = CACHE.write();
    if cache.len() >= MAX_ENTRIES {
        cache.clear();
    }
    cache.insert(pattern.to_string(), re.clone());
    Ok(re)
}
