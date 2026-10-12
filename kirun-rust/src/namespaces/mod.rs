//! Port of kirun-js `namespaces/Namespaces.ts`.

pub struct Namespaces;

impl Namespaces {
    pub const SYSTEM: &'static str = "System";
    pub const SYSTEM_CTX: &'static str = "System.Context";
    pub const SYSTEM_LOOP: &'static str = "System.Loop";
    pub const SYSTEM_ARRAY: &'static str = "System.Array";
    pub const SYSTEM_OBJECT: &'static str = "System.Object";
    pub const SYSTEM_JSON: &'static str = "System.JSON";

    pub const MATH: &'static str = "System.Math";
    pub const STRING: &'static str = "System.String";
    pub const DATE: &'static str = "System.Date";
}
