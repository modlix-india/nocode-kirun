//! Port of kirun-js `util/string/StringFormatter.ts`.

pub struct StringFormatter;

impl StringFormatter {
    /// Replaces each `$` with the next param; `\$` is a literal `$`.
    pub fn format(format_string: &str, params: &[&str]) -> String {
        if params.is_empty() {
            return format_string.to_string();
        }
        let mut sb = String::new();
        let mut ind = 0;
        let mut prevchar = '\0';
        for (i, chr) in format_string.chars().enumerate() {
            if chr == '$' && prevchar == '\\' {
                // JS: sb = sb.substring(0, i - 1) + chr
                sb = sb.chars().take(i.saturating_sub(1)).collect();
                sb.push(chr);
            } else if chr == '$' && ind < params.len() {
                sb.push_str(params[ind]);
                ind += 1;
            } else {
                sb.push(chr);
            }
            prevchar = chr;
        }
        sb
    }
}
