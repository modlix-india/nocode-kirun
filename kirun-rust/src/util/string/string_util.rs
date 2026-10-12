//! Port of kirun-js `util/string/StringUtil.ts`.

pub struct StringUtil;

impl StringUtil {
    pub fn is_null_or_blank(s: Option<&str>) -> bool {
        s.map_or(true, |s| s.trim().is_empty())
    }
}
