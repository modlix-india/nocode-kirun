//! JS `String.prototype` methods with JS semantics. JS indexes strings by UTF-16 code unit, so
//! every index here is a UTF-16 index (it equals the char index for BMP-only text).

fn units(s: &str) -> Vec<u16> {
    s.encode_utf16().collect()
}

fn from_units(u: &[u16]) -> String {
    String::from_utf16_lossy(u)
}

/// Clamps a JS numeric argument to `[0, len]` (NaN → 0, like `ToIntegerOrInfinity`).
fn clamp(n: f64, len: usize) -> usize {
    if n.is_nan() || n <= 0.0 {
        0
    } else if n >= len as f64 {
        len
    } else {
        n.trunc() as usize
    }
}

/// `s.length`
pub fn length(s: &str) -> usize {
    s.encode_utf16().count()
}

/// `s.indexOf(search, from)`
pub fn index_of(s: &str, search: &str, from: f64) -> i64 {
    let (s, search) = (units(s), units(search));
    let start = clamp(from, s.len());
    if search.is_empty() {
        return start as i64;
    }
    if search.len() > s.len() {
        return -1;
    }
    (start..=s.len() - search.len())
        .find(|&i| s[i..i + search.len()] == search[..])
        .map_or(-1, |i| i as i64)
}

/// `s.lastIndexOf(search, from)`; pass `f64::INFINITY` for no `from`.
pub fn last_index_of(s: &str, search: &str, from: f64) -> i64 {
    let (s, search) = (units(s), units(search));
    let from = if from.is_nan() { f64::INFINITY } else { from };
    let start = clamp(from, s.len());
    if search.len() > s.len() {
        return -1;
    }
    let start = start.min(s.len() - search.len());
    (0..=start)
        .rev()
        .find(|&i| s[i..i + search.len()] == search[..])
        .map_or(-1, |i| i as i64)
}

/// `s.substring(start, end)`; pass `f64::INFINITY` for no `end`.
pub fn substring(s: &str, start: f64, end: f64) -> String {
    let u = units(s);
    let (mut a, mut b) = (clamp(start, u.len()), clamp(end, u.len()));
    if a > b {
        std::mem::swap(&mut a, &mut b);
    }
    from_units(&u[a..b])
}

/// `s.charAt(i)`
pub fn char_at(s: &str, i: usize) -> String {
    units(s).get(i).map(|c| from_units(&[*c])).unwrap_or_default()
}

/// The UTF-16 units of `s` in reverse, like building a string from `charAt` backwards.
pub fn reverse(s: &str) -> String {
    let mut u = units(s);
    u.reverse();
    from_units(&u)
}

/// `s.repeat(count)`; JS throws a RangeError for a negative or infinite count.
pub fn repeat(s: &str, count: f64) -> Result<String, String> {
    if count < 0.0 || count.is_infinite() {
        return Err(format!("Invalid count value: {}", crate::util::primitive::js_number_to_string(count)));
    }
    Ok(s.repeat(if count.is_nan() { 0 } else { count.trunc() as usize }))
}

/// JS whitespace for `trim`: Unicode White_Space plus the BOM.
fn is_js_space(c: char) -> bool {
    c.is_whitespace() || c == '\u{FEFF}'
}

pub fn trim(s: &str) -> String {
    s.trim_matches(is_js_space).to_string()
}

pub fn trim_start(s: &str) -> String {
    s.trim_start_matches(is_js_space).to_string()
}

pub fn trim_end(s: &str) -> String {
    s.trim_end_matches(is_js_space).to_string()
}

/// `s.split(separator)` with a string separator.
pub fn split(s: &str, separator: &str) -> Vec<String> {
    if separator.is_empty() {
        return units(s).iter().map(|c| from_units(&[*c])).collect();
    }
    s.split(separator).map(str::to_string).collect()
}

/// `s.replace(search, replacement)` with a string search: first occurrence only. The
/// replacement is inserted literally (JS `$` patterns are not interpreted).
pub fn replace_first(s: &str, search: &str, replacement: &str) -> String {
    match index_of(s, search, 0.0) {
        -1 => s.to_string(),
        i => {
            let u = units(s);
            let i = i as usize;
            let end = i + length(search);
            format!("{}{}{}", from_units(&u[..i]), replacement, from_units(&u[end..]))
        }
    }
}

/// `s.replaceAll(search, replacement)` with a string search (literal replacement).
pub fn replace_all(s: &str, search: &str, replacement: &str) -> String {
    if search.is_empty() {
        let mut out = String::from(replacement);
        for c in units(s) {
            out.push_str(&from_units(&[c]));
            out.push_str(replacement);
        }
        return out;
    }
    s.replace(search, replacement)
}
