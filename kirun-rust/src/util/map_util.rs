//! Port of kirun-js `util/MapUtil.ts`.

use crate::Map;

pub struct MapUtil;

impl MapUtil {
    /// JS `MapUtil.of(k1, v1, k2, v2, ...)`.
    pub fn of<V>(entries: Vec<(&str, V)>) -> Map<V> {
        entries.into_iter().map(|(k, v)| (k.to_string(), v)).collect()
    }

    /// JS `MapUtil.ofEntriesArray(...[k, v])` / `ofArrayEntries`.
    pub fn of_entries_array<V>(entries: Vec<(String, V)>) -> Map<V> {
        entries.into_iter().collect()
    }
}
