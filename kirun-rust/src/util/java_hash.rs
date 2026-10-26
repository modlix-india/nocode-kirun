//! Java's `String.hashCode` and the iteration order of a `java.util.HashSet<String>`, for the
//! messages kirun-java builds by printing a set.

/// Java `String.hashCode()`: `s[0]*31^(n-1) + ... + s[n-1]` over UTF-16 units, wrapping.
pub fn string_hash_code(s: &str) -> i32 {
    s.encode_utf16().fold(0i32, |h, u| h.wrapping_mul(31).wrapping_add(u as i32))
}

/// The order a `new HashSet<>(source)` iterates `items` in, where `items` are (a subset of) the
/// source's elements in the source's own order and `source_len` its size: buckets by
/// `(h ^ h >>> 16) & (capacity - 1)` with Java's initial capacity for the copy, and insertion
/// order within a bucket.
pub fn hash_set_order<'a>(items: impl IntoIterator<Item = &'a str>, source_len: usize) -> Vec<String> {
    let wanted = ((source_len as f32 / 0.75) as usize + 1).max(16);
    let capacity = wanted.next_power_of_two() as u32;
    let mut keyed: Vec<(u32, usize, &str)> = items
        .into_iter()
        .enumerate()
        .map(|(i, s)| {
            let h = string_hash_code(s) as u32;
            ((h ^ (h >> 16)) & (capacity - 1), i, s)
        })
        .collect();
    keyed.sort_by_key(|(bucket, i, _)| (*bucket, *i));
    keyed.into_iter().map(|(_, _, s)| s.to_string()).collect()
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn hash_codes_match_java() {
        assert_eq!(string_hash_code(""), 0);
        assert_eq!(string_hash_code("a"), 97);
        assert_eq!(string_hash_code("hello"), 99_162_322);
        assert_eq!(string_hash_code("polygenelubricants"), i32::MIN);
    }

    #[test]
    fn orders_like_a_java_hash_set() {
        // Measured on the JVM: new HashSet<>(List.of("other", "extra", "b", "a")) prints
        // [a, other, b, extra]; with two of eight keys left, [other, extra].
        assert_eq!(hash_set_order(["other", "extra", "b", "a"], 4), ["a", "other", "b", "extra"]);
        assert_eq!(hash_set_order(["extra", "other"], 8), ["other", "extra"]);
    }
}
