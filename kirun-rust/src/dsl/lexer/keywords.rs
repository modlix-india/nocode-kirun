//! Port of kirun-js `dsl/lexer/Keywords.ts`.

pub const KEYWORDS: &[&str] = &[
    "FUNCTION", "NAMESPACE", "PARAMETERS", "EVENTS", "LOGIC", "AS", "OF", "WITH", "DEFAULT", "VALUE", "AFTER", "IF",
    "INTEGER", "LONG", "FLOAT", "DOUBLE", "STRING", "BOOLEAN", "NULL", "ANY", "ARRAY", "OBJECT", "true", "false",
    "null",
];

pub const BLOCK_NAMES: [&str; 5] = ["iteration", "true", "false", "output", "error"];

pub const PRIMITIVE_TYPES: [&str; 8] = ["INTEGER", "LONG", "FLOAT", "DOUBLE", "STRING", "BOOLEAN", "NULL", "ANY"];

pub fn is_keyword(s: &str) -> bool {
    KEYWORDS.contains(&s)
}

pub fn is_block_name(s: &str) -> bool {
    BLOCK_NAMES.contains(&s)
}

pub fn is_primitive_type(s: &str) -> bool {
    PRIMITIVE_TYPES.contains(&s)
}
