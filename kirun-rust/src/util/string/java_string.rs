//! The kirun-java string functions that call a `java.lang.String` method taking a regular
//! expression (`replaceFirst`, `split`), where kirun-js treats the argument as plain text.
//! Stored server functions run on kirun-java, so these follow Java; every case in the tests was
//! measured on kirun-java 4.9.2 (`"T59".replaceFirst("^T", "")` is "59", `"a.b".split(".")` is
//! empty). Patterns compile with `fancy_regex`, whose syntax covers what stored functions use;
//! the message of a pattern that does not compile is fancy_regex's, not Java's.

use std::sync::Arc;

use fancy_regex::{Captures, Regex};

use crate::util::regex_cache::cached_regex;

fn compile(pattern: &str) -> Result<Arc<Regex>, String> {
    cached_regex(pattern).map_err(|e| e.to_string())
}

/// Java `String.replaceFirst(regex, replacement)`.
pub fn replace_first(s: &str, regex: &str, replacement: &str) -> Result<String, String> {
    let re = compile(regex)?;
    let Some(caps) = re.captures(s).map_err(|e| e.to_string())? else { return Ok(s.to_string()) };
    let Some(whole) = caps.get(0) else { return Ok(s.to_string()) };
    let mut out = String::with_capacity(s.len() + replacement.len());
    out.push_str(&s[..whole.start()]);
    append_replacement(&mut out, replacement, &caps, &re)?;
    out.push_str(&s[whole.end()..]);
    Ok(out)
}

/// Java `Matcher.appendReplacement`: `\x` is a literal x, `$n` and `${name}` insert a group. A
/// multi-digit `$12` takes as many digits as still name a group, as Java does.
fn append_replacement(out: &mut String, replacement: &str, caps: &Captures, re: &Regex) -> Result<(), String> {
    let chars: Vec<char> = replacement.chars().collect();
    let group_count = caps.len().saturating_sub(1);
    let mut i = 0;
    while i < chars.len() {
        match chars[i] {
            '\\' => {
                let Some(c) = chars.get(i + 1) else { return Err("character to be escaped is missing".to_string()) };
                out.push(*c);
                i += 2;
            }
            '$' => {
                i += 1;
                let Some(&next) = chars.get(i) else {
                    return Err("Illegal group reference: group index is missing".to_string());
                };
                if next == '{' {
                    i += 1;
                    let start = i;
                    while chars.get(i).is_some_and(char::is_ascii_alphanumeric) {
                        i += 1;
                    }
                    if i == start {
                        return Err("named capturing group has 0 length name".to_string());
                    }
                    if chars.get(i) != Some(&'}') {
                        return Err("named capturing group is missing trailing '}'".to_string());
                    }
                    let name: String = chars[start..i].iter().collect();
                    i += 1;
                    if !re.capture_names().any(|n| n == Some(name.as_str())) {
                        return Err(format!("No group with name {{{name}}}"));
                    }
                    if let Some(g) = caps.name(&name) {
                        out.push_str(g.as_str());
                    }
                } else {
                    let Some(first) = next.to_digit(10) else { return Err("Illegal group reference".to_string()) };
                    let mut group = first as usize;
                    i += 1;
                    while let Some(d) = chars.get(i).and_then(|c| c.to_digit(10)) {
                        let longer = group * 10 + d as usize;
                        if longer > group_count {
                            break;
                        }
                        group = longer;
                        i += 1;
                    }
                    if group > group_count {
                        return Err(format!("No group {group}"));
                    }
                    if let Some(g) = caps.get(group) {
                        out.push_str(g.as_str());
                    }
                }
            }
            c => {
                out.push(c);
                i += 1;
            }
        }
    }
    Ok(())
}

/// Java `String.split(regex)`: no leading empty part for a zero-width match at the start, and
/// trailing empty parts dropped (so `"a,b,,"` gives `a`, `b` and `","` gives nothing). An empty
/// string gives one empty part.
pub fn split(s: &str, regex: &str) -> Result<Vec<String>, String> {
    if s.is_empty() {
        return Ok(vec![String::new()]);
    }
    let re = compile(regex)?;
    let mut parts = Vec::new();
    let mut index = 0;
    let mut matched = false;
    for m in re.find_iter(s) {
        let m = m.map_err(|e| e.to_string())?;
        if m.end() == 0 {
            continue;
        }
        matched = true;
        parts.push(s[index..m.start()].to_string());
        index = m.end();
    }
    if !matched {
        return Ok(vec![s.to_string()]);
    }
    parts.push(s[index..].to_string());
    while parts.last().is_some_and(String::is_empty) {
        parts.pop();
    }
    Ok(parts)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn replace_first_follows_java() {
        assert_eq!(replace_first("T59", "^T", ""), Ok("59".to_string()));
        assert_eq!(replace_first("a1b2", r"([a-z])(\d)", "$2$1"), Ok("1ab2".to_string()));
        assert_eq!(replace_first("a.b.c", ".", "X"), Ok("X.b.c".to_string()));
        assert_eq!(replace_first("cost", "o", r"\$"), Ok("c$st".to_string()));
        assert_eq!(replace_first("abc", "z", "Q"), Ok("abc".to_string()));
        assert_eq!(
            replace_first("cost", "o", "$"),
            Err("Illegal group reference: group index is missing".to_string())
        );
        assert!(replace_first("abc", "(b", "x").is_err());
        assert_eq!(replace_first("ab", "(?<x>b)", "[${x}]"), Ok("a[b]".to_string()));
        assert_eq!(replace_first("ab", "(b)", "$1$10"), Ok("abb0".to_string()));
        assert_eq!(replace_first("ab", "b", "$1"), Err("No group 1".to_string()));
    }

    #[test]
    fn split_follows_java() {
        let v = |xs: &[&str]| Ok(xs.iter().map(|x| x.to_string()).collect::<Vec<_>>());
        assert_eq!(split("a,b,,", ","), v(&["a", "b"]));
        assert_eq!(split("a|b", "|"), v(&["a", "|", "b"]));
        assert_eq!(split("a.b", "."), v(&[]));
        assert_eq!(split("", ","), v(&[""]));
        assert_eq!(split(",a", ","), v(&["", "a"]));
        assert_eq!(split("abc", ""), v(&["a", "b", "c"]));
        assert_eq!(split("a1b22c", r"\d+"), v(&["a", "b", "c"]));
        assert_eq!(split("abc", ","), v(&["abc"]));
    }
}
