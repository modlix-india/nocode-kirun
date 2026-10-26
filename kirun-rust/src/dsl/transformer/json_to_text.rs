//! Port of kirun-js `dsl/transformer/JSONToText.ts`: FunctionDefinition JSON → DSL text.
//!
//! Nesting is recovered from the steps' `Steps.<step>.<event>` references (the same rule
//! KIRuntime uses for dependencies), so a statement prints inside the block of the step whose
//! event it waits on.

use std::cell::RefCell;
use std::collections::{HashSet, VecDeque};
use std::sync::LazyLock;

use regex::Regex;

use crate::dsl::lexer::{DSLLexer, DSLTokenType as T};
use crate::dsl::transformer::expression_handler::ExpressionHandler;
use crate::dsl::transformer::schema_transformer::SchemaTransformer;
use crate::util::primitive::js_number_value_to_string;
use crate::util::string::js_string;
use crate::{Map, Result, Value};

/// Matches `Steps.stepName.eventName`.
static STEP_REGEX: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"Steps\.([a-zA-Z0-9_-]+)\.([a-zA-Z0-9_-]+)").unwrap());
static STEP_REF_REGEX: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"Steps\.([a-zA-Z_][a-zA-Z0-9_]*)").unwrap());
static NUMBER_REGEX: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"^-?\d+(\.\d+)?$").unwrap());
static NUMBER_OP_REGEX: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"^-?\d+(\.\d+)?\s*[+\-*/%]").unwrap());
static PLAIN_KEY: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"^[A-Za-z_][A-Za-z0-9_]*$").unwrap());
static VALUE_REF_START: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"^@[A-Za-z0-9_]").unwrap());

/// A step line longer than this puts its arguments one per line.
const LINE_WIDTH: usize = 120;
/// An object or array argument longer than this on one line moves to VALUES.
const INLINE_VALUE_WIDTH: usize = 100;

/// JS `String.prototype.length`: UTF-16 code units, so widths match kirun-js.
fn js_len(s: &str) -> usize {
    s.encode_utf16().count()
}

#[derive(Debug, Clone)]
struct Nesting {
    block_name: String,
    parent: String,
}

/// A ParameterReference's `type`: stored data often carries `["EXPRESSION"]` instead of the
/// scalar; read it through this, never directly.
fn ref_type(r: &Value) -> Option<&str> {
    match &r["type"] {
        Value::Array(a) => a.first().and_then(Value::as_str),
        Value::String(s) => Some(s),
        _ => None,
    }
}

/// A step's `dependentStatements` as a map of key → value, whatever shape it is stored in
/// (a list of keys is stored as often as a map; the kirun-py fix).
/// Each entry is (key, whether its value is `true`); borrowed from the step.
fn dependent_statements(step: &Value) -> Vec<(&str, bool)> {
    match &step["dependentStatements"] {
        Value::Object(o) => o.iter().map(|(k, v)| (k.as_str(), *v == Value::Bool(true))).collect(),
        Value::Array(a) => a.iter().filter_map(|k| k.as_str()).map(|k| (k, true)).collect(),
        _ => Vec::new(),
    }
}

/// An object's entries, borrowed (walking the definition used to copy every step).
fn object_entries(v: &Value) -> Vec<(&String, &Value)> {
    v.as_object().map(|o| o.iter().collect()).unwrap_or_default()
}

/// A field as JS template literals print it: a missing key is `undefined`.
fn js_field(v: &Value, key: &str) -> String {
    match v.get(key) {
        None => "undefined".to_string(),
        Some(x) => crate::util::primitive::js_to_string(x),
    }
}

fn decode_dots(s: &str) -> String {
    s.replace("__d-o-t__", ".")
}

/// A value moved to the VALUES section: its name, the value, and its expression key (see
/// `compact_json`).
type NamedValue = (String, Value, Option<&'static str>);

#[derive(Debug, Default)]
pub struct JSONToTextTransformer {
    /// Values moved to the VALUES section by the current transform, in order of use.
    named_values: RefCell<Vec<NamedValue>>,
    used_value_names: RefCell<HashSet<String>>,
}

impl JSONToTextTransformer {
    const INDENT: &'static str = "    ";

    pub fn new() -> Self {
        Self::default()
    }

    fn indent(level: usize) -> String {
        Self::INDENT.repeat(level)
    }

    pub fn transform(&self, json: &Value) -> Result<String> {
        self.named_values.borrow_mut().clear();
        self.used_value_names.borrow_mut().clear();
        let mut lines = Vec::new();
        lines.push(format!("FUNCTION {}", js_field(json, "name")));

        if let Some(ns) = json["namespace"].as_str().filter(|n| !n.is_empty() && *n != "_") {
            lines.push(format!("{}NAMESPACE {ns}", Self::indent(1)));
        }

        let parameters = object_entries(&json["parameters"]);
        if !parameters.is_empty() {
            lines.push(format!("{}PARAMETERS", Self::indent(1)));
            for (name, param) in parameters {
                lines.push(format!("{}{name} AS {}", Self::indent(2), SchemaTransformer::to_text(&param["schema"])));
            }
        }

        let events = object_entries(&json["events"]);
        if !events.is_empty() {
            lines.push(format!("{}EVENTS", Self::indent(1)));
            for (name, event) in events {
                lines.push(format!("{}{name}", Self::indent(2)));
                for (pname, schema) in object_entries(&event["parameters"]) {
                    lines.push(format!("{}{pname} AS {}", Self::indent(3), SchemaTransformer::to_text(schema)));
                }
            }
        }

        lines.push(format!("{}LOGIC", Self::indent(1)));
        let nested = self.build_nested_structure(json);
        lines.extend(self.steps_to_text(&json["steps"], &nested, 2));

        // Large objects and arrays, kept out of the step list
        let named = self.named_values.borrow();
        if !named.is_empty() {
            lines.push(format!("{}VALUES", Self::indent(1)));
            for (name, value, expr_key) in named.iter() {
                let head = format!("{}@{name} = ", Self::indent(2));
                let text = self.format_json(value, 2, js_len(&head), *expr_key);
                lines.push(head + &text);
            }
        }
        Ok(lines.join("\n"))
    }

    fn build_nested_structure(&self, json: &Value) -> Map<Nesting> {
        let mut structure: Map<Nesting> = Map::new();
        let steps = object_entries(&json["steps"]);
        let step_names: HashSet<String> = steps.iter().map(|(k, _)| k.to_string()).collect();

        let mut all_edges: Map<Vec<Nesting>> = Map::new();
        for (name, step) in &steps {
            all_edges.insert(name.to_string(), self.extract_all_dependencies(step, &step_names));
        }

        fn depth(structure: &Map<Nesting>, step: &str, visited: &mut HashSet<String>) -> usize {
            if !visited.insert(step.to_string()) {
                return 0;
            }
            match structure.get(step) {
                Some(n) => 1 + depth(structure, &n.parent, visited),
                None => 0,
            }
        }

        let sorted = topological_sort_for_nesting(&all_edges.keys().cloned().collect::<Vec<_>>(), &all_edges);

        let mut changed = true;
        while changed {
            changed = false;
            for step_name in &sorted {
                if structure.contains_key(step_name) {
                    continue;
                }
                let Some(edges) = all_edges.get(step_name).filter(|e| !e.is_empty()) else { continue };
                // Several parents: the deepest one wins.
                let mut deepest = &edges[0];
                let mut max = depth(&structure, &deepest.parent, &mut HashSet::new());
                for e in &edges[1..] {
                    let d = depth(&structure, &e.parent, &mut HashSet::new());
                    if d > max {
                        max = d;
                        deepest = e;
                    }
                }
                structure.insert(step_name.clone(), deepest.clone());
                changed = true;
            }
        }

        for s in detect_circular_dependencies(&structure) {
            structure.shift_remove(&s);
        }
        structure
    }

    /// Dependencies from EXPRESSION params, expressions inside VALUE params, dependentStatements
    /// and executeIftrue — the same sources KIRuntime reads.
    fn extract_all_dependencies(&self, step: &Value, valid: &HashSet<String>) -> Vec<Nesting> {
        let mut deps: Vec<Nesting> = Vec::new();
        let mut seen: HashSet<String> = HashSet::new();
        let mut add = |text: &str| {
            for c in STEP_REGEX.captures_iter(text) {
                let (parent, block) = (c[1].to_string(), c[2].to_string());
                if valid.contains(&parent) && seen.insert(format!("{parent}.{block}")) {
                    deps.push(Nesting { block_name: block, parent });
                }
            }
        };

        for (_, refs) in object_entries(&step["parameterMap"]) {
            for (_, r) in object_entries(refs) {
                if ref_type(r) == Some("EXPRESSION") {
                    if let Some(e) = r["expression"].as_str().filter(|e| !e.is_empty()) {
                        add(e);
                    }
                }
                if ref_type(r) == Some("VALUE") && !r["value"].is_null() {
                    extract_expressions_from_value(&r["value"], &mut add);
                }
            }
        }

        for (key, is_true) in dependent_statements(step) {
            if is_true {
                add(&decode_dots(key));
            }
        }

        for (cond, _) in object_entries(&step["executeIftrue"]) {
            add(cond);
        }
        deps
    }

    fn steps_to_text(&self, steps: &Value, nested: &Map<Nesting>, base_indent: usize) -> Vec<String> {
        let mut lines = Vec::new();
        let Value::Object(steps_obj) = steps else { return lines };

        // A step that follows another's `output` is printed in the same block as that step,
        // right after it, so a sequence reads top to bottom instead of moving right one level
        // per step. Only branches (true, false, iteration, error and other events) indent.
        // Indentation carries no meaning for the compiler: dependencies come from AFTER, IF and
        // Steps references.
        let block_of = |name: &str| -> Option<&Nesting> {
            let mut info = nested.get(name);
            let mut seen: HashSet<&str> = HashSet::from([name]);
            while let Some(n) = info.filter(|n| n.block_name == "output" && !seen.contains(n.parent.as_str())) {
                seen.insert(n.parent.as_str());
                info = nested.get(&n.parent);
            }
            info.filter(|n| n.block_name != "output")
        };

        let mut top_level: Vec<String> = Vec::new();
        let mut by_parent: Map<Map<Vec<String>>> = Map::new();
        for name in steps_obj.keys() {
            match block_of(name) {
                Some(n) if steps_obj.contains_key(&n.parent) => {
                    by_parent.entry(n.parent.clone()).or_default().entry(n.block_name.clone()).or_default().push(name.clone());
                }
                _ => top_level.push(name.clone()),
            }
        }

        let sorted_top = topological_sort(&top_level, steps_obj);
        for blocks in by_parent.values_mut() {
            for list in blocks.values_mut() {
                *list = topological_sort(list, steps_obj);
            }
        }

        fn block_order(name: &str) -> i32 {
            match name {
                "error" => 0,
                "iteration" => 1,
                "true" => 2,
                "false" => 3,
                "output" => 4,
                _ => 3,
            }
        }

        fn render(
            this: &JSONToTextTransformer,
            name: &str,
            steps: &serde_json::Map<String, Value>,
            by_parent: &Map<Map<Vec<String>>>,
            indent: usize,
            lines: &mut Vec<String>,
        ) {
            lines.extend(this.step_to_text(&steps[name], name, indent));
            if let Some(blocks) = by_parent.get(name) {
                let mut names: Vec<&String> = blocks.keys().collect();
                // Stable, like Array.prototype.sort.
                names.sort_by_key(|b| block_order(b));
                for block in names {
                    lines.push(format!("{}{block}", JSONToTextTransformer::indent(indent + 1)));
                    for nested_name in &blocks[block] {
                        render(this, nested_name, steps, by_parent, indent + 2, lines);
                    }
                }
            }
        }

        for name in &sorted_top {
            render(self, name, steps_obj, &by_parent, base_indent, &mut lines);
        }
        lines
    }

    fn step_to_text(&self, step: &Value, step_key: &str, indent: usize) -> Vec<String> {
        let step_name = step["statementName"].as_str().filter(|s| !s.is_empty()).unwrap_or(step_key);
        let is_generate_event = step["namespace"] == "System" && step["name"] == "GenerateEvent";
        let args = self.args_to_text(&step["parameterMap"], step_name, is_generate_event);
        let ind = Self::indent(indent);
        let head = format!("{ind}{step_name}: {}.{}(", js_field(step, "namespace"), js_field(step, "name"));

        let mut tail = String::new();
        let after: Vec<String> =
            dependent_statements(step).into_iter().filter(|(_, is_true)| *is_true).map(|(k, _)| decode_dots(k)).collect();
        if !after.is_empty() {
            tail.push_str(&format!(" AFTER {}", after.join(", ")));
        }

        let if_steps: Vec<&str> = object_entries(&step["executeIftrue"]).into_iter().map(|(k, _)| k.as_str()).collect();
        if !if_steps.is_empty() {
            tail.push_str(&format!(" IF {}", if_steps.join(", ")));
        }

        if let Some(c) = step["comment"].as_str().filter(|c| !c.trim().is_empty()) {
            tail.push_str(&format!(" /* {} */", c.replace("*/", "*\\/")));
        }

        let line = format!("{head}{}){tail}", args.join(", "));
        // A long call puts one argument per line, closing `)` back at the step's indent
        if js_len(&line) > LINE_WIDTH && args.len() > 1 {
            let arg_indent = Self::indent(indent + 1);
            let mut lines = vec![head];
            let last = args.len() - 1;
            for (i, arg) in args.iter().enumerate() {
                lines.push(format!("{arg_indent}{arg}{}", if i < last { "," } else { "" }));
            }
            lines.push(format!("{ind}){tail}"));
            return lines;
        }
        vec![line]
    }

    /// The `name = value` arguments of a step.
    fn args_to_text(&self, parameter_map: &Value, step_name: &str, is_generate_event: bool) -> Vec<String> {
        let mut args = Vec::new();
        for (param, refs) in object_entries(parameter_map) {
            let mut list: Vec<&Value> = object_entries(refs).into_iter().map(|(_, r)| r).collect();
            if list.is_empty() {
                continue;
            }
            list.sort_by(|a, b| {
                let o = |r: &&Value| r["order"].as_f64().unwrap_or(0.0);
                o(a).partial_cmp(&o(b)).unwrap_or(std::cmp::Ordering::Equal)
            });
            // GenerateEvent evaluates the `value` of each `results` entry; see compact_json
            let expr_key = (is_generate_event && param == "results").then_some("value");
            let multi = list.len() > 1;
            for (i, r) in list.into_iter().enumerate() {
                let value_name = if multi { format!("{step_name}.{param}.{}", i + 1) } else { format!("{step_name}.{param}") };
                args.push(format!("{param} = {}", self.param_ref_to_text(r, &value_name, expr_key)));
            }
        }
        args
    }

    /// An object or array value too long for one line is moved to the VALUES section under
    /// `value_name` and printed as `@value_name`.
    fn param_ref_to_text(&self, r: &Value, value_name: &str, expr_key: Option<&'static str>) -> String {
        if ref_type(r) != Some("EXPRESSION") {
            if let Some(value) = r.get("value").filter(|v| (v.is_object() || v.is_array()) && !ExpressionHandler::is_expression(v)) {
                let text = self.compact_json(value, expr_key);
                if js_len(&text) <= INLINE_VALUE_WIDTH {
                    return text;
                }
                let name = self.unique_value_name(value_name);
                self.named_values.borrow_mut().push((name.clone(), value.clone(), expr_key));
                return format!("@{name}");
            }
        }
        if ref_type(r) == Some("EXPRESSION") {
            let expr = match &r["expression"] {
                Value::String(s) if !s.is_empty() => s.clone(),
                Value::Null => return "``".to_string(),
                Value::String(_) => return "``".to_string(),
                other => crate::util::primitive::js_to_string(other),
            };
            if expression_needs_backticks(&expr) {
                return format!("`{}`", expr.replace('\\', "\\\\").replace('`', "\\`"));
            }
            return expr;
        }
        // A ref with no `value` key held JS `undefined`.
        match r.get("value") {
            None => "undefined".to_string(),
            Some(v) => value_to_text_preserve_type(v),
        }
    }
}

fn extract_expressions_from_value(value: &Value, add: &mut impl FnMut(&str)) {
    match value {
        Value::String(s) => add(s),
        Value::Array(a) => a.iter().for_each(|v| extract_expressions_from_value(v, add)),
        Value::Object(o) => {
            if value["isExpression"] == Value::Bool(true) && value["value"].is_string() {
                add(value["value"].as_str().unwrap());
            } else if ref_type(value) == Some("EXPRESSION") && value["expression"].is_string() {
                add(value["expression"].as_str().unwrap());
            } else {
                o.values().for_each(|v| extract_expressions_from_value(v, add));
            }
        }
        _ => {}
    }
}

/// Kahn's algorithm over the parent edges; steps left in cycles keep their original order.
fn topological_sort_for_nesting(names: &[String], edges: &Map<Vec<Nesting>>) -> Vec<String> {
    let set: HashSet<&String> = names.iter().collect();
    let deps: Map<HashSet<String>> = names
        .iter()
        .map(|n| {
            let d = edges.get(n).map(|e| e.iter().filter(|x| set.contains(&x.parent)).map(|x| x.parent.clone()).collect());
            (n.clone(), d.unwrap_or_default())
        })
        .collect();
    kahn(names, &deps)
}

fn kahn(names: &[String], deps: &Map<HashSet<String>>) -> Vec<String> {
    let mut in_degree: Map<usize> = names.iter().map(|n| (n.clone(), deps[n].len())).collect();
    let mut queue: VecDeque<String> = names.iter().filter(|n| in_degree[*n] == 0).cloned().collect();
    let mut sorted = Vec::new();
    while let Some(current) = queue.pop_front() {
        sorted.push(current.clone());
        for (name, d) in deps {
            if d.contains(&current) {
                let deg = in_degree.get_mut(name).unwrap();
                *deg -= 1;
                if *deg == 0 {
                    queue.push_back(name.clone());
                }
            }
        }
    }
    for n in names {
        if !sorted.contains(n) {
            sorted.push(n.clone());
        }
    }
    sorted
}

/// Orders steps so explicit (dependentStatements) and expression dependencies come first.
fn topological_sort(names: &[String], steps: &serde_json::Map<String, Value>) -> Vec<String> {
    let set: HashSet<&String> = names.iter().collect();
    let mut deps: Map<HashSet<String>> = Map::new();
    for name in names {
        let mut d = HashSet::new();
        let step = &steps[name];
        for (key, _) in dependent_statements(step) {
            let decoded = decode_dots(key);
            if let Some(rest) = decoded.strip_prefix("Steps.") {
                let dep = rest.split('.').next().unwrap_or("");
                if !dep.is_empty() && set.contains(&dep.to_string()) {
                    d.insert(dep.to_string());
                }
            }
        }
        for dep in extract_dependencies_from_params(&step["parameterMap"]) {
            if set.contains(&dep) && dep != *name {
                d.insert(dep);
            }
        }
        deps.insert(name.clone(), d);
    }
    kahn(names, &deps)
}

fn extract_dependencies_from_params(parameter_map: &Value) -> Vec<String> {
    let mut deps = Vec::new();
    let refs_of = |text: &str, deps: &mut Vec<String>| {
        for c in STEP_REF_REGEX.captures_iter(text) {
            deps.push(c[1].to_string());
        }
    };
    for (_, refs) in object_entries(parameter_map) {
        for (_, r) in object_entries(refs) {
            if ref_type(r) == Some("EXPRESSION") {
                if let Some(e) = r["expression"].as_str().filter(|e| !e.is_empty()) {
                    refs_of(e, &mut deps);
                }
            }
            if ref_type(r) == Some("VALUE") && crate::util::primitive::js_truthy(&r["value"]) {
                refs_of(&crate::util::primitive::js_stringify(&r["value"]), &mut deps);
            }
        }
    }
    deps
}

fn detect_circular_dependencies(structure: &Map<Nesting>) -> HashSet<String> {
    let mut in_cycle = HashSet::new();
    for name in structure.keys() {
        let mut visited: Vec<String> = Vec::new();
        let mut current = Some(name.clone());
        while let Some(c) = current.filter(|c| structure.contains_key(c)) {
            if visited.contains(&c) {
                in_cycle.extend(visited.iter().cloned());
                break;
            }
            visited.push(c.clone());
            current = structure.get(&c).map(|n| n.parent.clone());
        }
    }
    in_cycle
}

/// Expressions that would otherwise read as literals (or contain quotes) go in backticks.
fn expression_needs_backticks(expr: &str) -> bool {
    if expr.is_empty() {
        return false;
    }
    let t = expr.trim();
    // Whitespace at the end would be lost before a `)` on the next line; `@name` is a named value
    js_string::trim_end(expr) != expr
        || VALUE_REF_START.is_match(&js_string::trim(expr))
        || expr.contains('"')
        || expr.contains('\'')
        || t == "true"
        || t == "false"
        || t.starts_with("true ")
        || t.starts_with("false ")
        || t == "null"
        || t == "undefined"
        || NUMBER_REGEX.is_match(t)
        || NUMBER_OP_REGEX.is_match(t)
        || t == "[]"
        || t == "{}"
}

fn value_to_text_preserve_type(value: &Value) -> String {
    match value {
        Value::Null => "null".to_string(),
        Value::String(s) => format!("\"{}\"", s.replace('\\', "\\\\").replace('"', "\\\"")),
        Value::Bool(b) => b.to_string(),
        Value::Number(n) => number_text(n),
        Value::Array(a) if a.is_empty() => "[]".to_string(),
        Value::Array(a) => format!("[{}]", a.iter().map(value_to_text_preserve_type).collect::<Vec<_>>().join(", ")),
        Value::Object(_) => {
            if let Some(e) = ExpressionHandler::extract_expression_text(value) {
                return e.to_string();
            }
            let mut out = String::new();
            pretty_json(value, 0, &mut out);
            out
        }
    }
}

/// Integers print as integers and floats keep a fraction, so the round trip preserves int vs
/// float (JS prints `1.0` as `1`; serde_json, like kirun-py, keeps the distinction).
fn number_text(n: &serde_json::Number) -> String {
    match n.as_f64() {
        Some(f) if n.is_f64() && f.fract() == 0.0 && f.is_finite() && f.abs() < 1e21 => format!("{f:.1}"),
        _ => js_number_value_to_string(n),
    }
}

/// JS `JSON.stringify(value, null, 4)`.
fn pretty_json(value: &Value, level: usize, out: &mut String) {
    let pad = |l: usize| "    ".repeat(l);
    match value {
        Value::Array(a) if !a.is_empty() => {
            out.push_str("[\n");
            for (i, v) in a.iter().enumerate() {
                out.push_str(&pad(level + 1));
                pretty_json(v, level + 1, out);
                if i + 1 < a.len() {
                    out.push(',');
                }
                out.push('\n');
            }
            out.push_str(&pad(level));
            out.push(']');
        }
        Value::Object(o) if !o.is_empty() => {
            out.push_str("{\n");
            for (i, (k, v)) in o.iter().enumerate() {
                out.push_str(&pad(level + 1));
                out.push_str(&serde_json::to_string(k).unwrap_or_default());
                out.push_str(": ");
                pretty_json(v, level + 1, out);
                if i + 1 < o.len() {
                    out.push(',');
                }
                out.push('\n');
            }
            out.push_str(&pad(level));
            out.push('}');
        }
        Value::Number(n) => out.push_str(&number_text(n)),
        other => out.push_str(&serde_json::to_string(other).unwrap_or_default()),
    }
}

impl JSONToTextTransformer {
    /// One-line JSON in DSL form, with plain keys unquoted.
    ///
    /// In the object's `expr_key` entry, `{isExpression: true, value}` is written as the
    /// expression itself, which the parser turns back into that object. Only GenerateEvent
    /// evaluates such an object (the `value` of each `results` entry); anywhere else the runtime
    /// passes it through as data, so it stays written out in full rather than look like an
    /// expression that runs.
    fn compact_json(&self, value: &Value, expr_key: Option<&str>) -> String {
        match value {
            Value::Array(a) => format!("[{}]", a.iter().map(|v| self.compact_json(v, None)).collect::<Vec<_>>().join(", ")),
            Value::Object(o) if o.is_empty() => "{}".to_string(),
            Value::Object(o) => {
                let entries: Vec<String> =
                    o.iter().map(|(k, v)| format!("{}: {}", json_key(k), self.entry_text(k, v, expr_key))).collect();
                format!("{{{}}}", entries.join(", "))
            }
            Value::Number(n) => number_text(n),
            other => serde_json::to_string(other).unwrap_or_default(),
        }
    }

    fn entry_text(&self, key: &str, value: &Value, expr_key: Option<&str>) -> String {
        match value["value"].as_str() {
            Some(e) if Some(key) == expr_key && is_plain_expression(value) => json_expression_text(e),
            _ if Some(key) == expr_key && is_plain_literal(value) => self.compact_json(&value["value"], None),
            _ => self.compact_json(value, None),
        }
    }

    /// JSON that breaks over lines, one entry per line, only where the one-line form would run
    /// past LINE_WIDTH starting at `column`.
    fn format_json(&self, value: &Value, level: usize, column: usize, expr_key: Option<&str>) -> String {
        let compact = self.compact_json(value, expr_key);
        let is_empty = match value {
            Value::Array(a) => a.is_empty(),
            Value::Object(o) => o.is_empty(),
            _ => true,
        };
        if column + js_len(&compact) <= LINE_WIDTH || is_empty {
            return compact;
        }
        let inner = Self::indent(level + 1);
        let (open, close, items): (char, char, Vec<String>) = match value {
            Value::Array(a) => {
                ('[', ']', a.iter().map(|v| inner.clone() + &self.format_json(v, level + 1, inner.len(), None)).collect())
            }
            Value::Object(o) => (
                '{',
                '}',
                o.iter()
                    .map(|(k, v)| {
                        let head = format!("{inner}{}: ", json_key(k));
                        match v["value"].as_str() {
                            Some(e) if Some(k.as_str()) == expr_key && is_plain_expression(v) => head + &json_expression_text(e),
                            _ if Some(k.as_str()) == expr_key && is_plain_literal(v) => {
                                let text = self.format_json(&v["value"], level + 1, js_len(&head), None);
                                head + &text
                            }
                            _ => {
                                let text = self.format_json(v, level + 1, js_len(&head), None);
                                head + &text
                            }
                        }
                    })
                    .collect(),
            ),
            _ => return compact,
        };
        format!("{open}\n{}\n{}{close}", items.join(",\n"), Self::indent(level))
    }

    /// A VALUES name not used yet in this function: `step.param`, then `step.param_2`, ...
    fn unique_value_name(&self, base: &str) -> String {
        let chars: Vec<char> =
            base.chars().map(|c| if c.is_ascii_alphanumeric() || c == '_' || c == '.' { c } else { '_' }).collect();
        // No empty segment: a leading dot, or a dot before another dot or the end, becomes `_`.
        let clean: String = chars
            .iter()
            .enumerate()
            .map(|(i, &c)| {
                let empty_segment = c == '.' && (i == 0 || chars.get(i + 1).is_none_or(|n| *n == '.'));
                if empty_segment {
                    '_'
                } else {
                    c
                }
            })
            .collect();
        let mut used = self.used_value_names.borrow_mut();
        let mut name = clean.clone();
        let mut n = 2;
        while used.contains(&name) {
            name = format!("{clean}_{n}");
            n += 1;
        }
        used.insert(name.clone());
        name
    }
}

/// `{isExpression: true, value: "<text>"}` and nothing else.
fn is_plain_expression(value: &Value) -> bool {
    ExpressionHandler::is_expression(value) && value.as_object().is_some_and(|o| o.len() == 2)
}

/// `{isExpression: false, value}` and nothing else: a GenerateEvent result given as a literal.
fn is_plain_literal(value: &Value) -> bool {
    value.as_object().is_some_and(|o| o.len() == 2 && o.get("isExpression") == Some(&Value::Bool(false)) && o.contains_key("value"))
}

fn json_key(key: &str) -> String {
    // `null` lexes as a null literal, which the parser does not take as a key
    if PLAIN_KEY.is_match(key) && key != "null" {
        key.to_string()
    } else {
        serde_json::to_string(key).unwrap_or_default()
    }
}

/// An expression inside a JSON value: bare when the parser reads it back as the same
/// expression, otherwise in backticks.
fn json_expression_text(expr: &str) -> String {
    if is_bare_json_expression(expr) {
        expr.to_string()
    } else {
        format!("`{}`", expr.replace('\\', "\\\\").replace('`', "\\`"))
    }
}

/// Inside JSON the parser reads a string, number, boolean, null, undefined, object or array
/// literal as that literal, and an expression runs to the first `,` or closing bracket at its
/// own depth. So an expression can go bare only if it starts with none of those literals, its
/// brackets balance, it has no top-level comma, and it has no whitespace at either end to lose.
fn is_bare_json_expression(expr: &str) -> bool {
    if expr.is_empty() || js_string::trim(expr) != expr {
        return false;
    }
    let Ok(tokens) = DSLLexer::new(expr).tokenize() else { return false };
    let tokens: Vec<_> = tokens.into_iter().filter(|t| t.r#type != T::Eof).collect();
    let Some(first) = tokens.first() else { return false };
    let literal_start =
        matches!(first.r#type, T::String | T::Number | T::Boolean | T::Null | T::BacktickString | T::LeftBrace | T::LeftBracket)
            || (first.r#type == T::Operator && first.value == "@")
            || (first.r#type == T::Keyword && ["true", "false", "null"].contains(&first.value.as_str()))
            || (first.r#type == T::Identifier && first.value == "undefined");
    if literal_start {
        return false;
    }
    let mut depth = 0usize;
    for t in &tokens {
        match t.r#type {
            T::Comment => return false,
            T::LeftParen | T::LeftBracket | T::LeftBrace => depth += 1,
            T::RightParen | T::RightBracket | T::RightBrace => {
                if depth == 0 {
                    return false;
                }
                depth -= 1;
            }
            T::Comma if depth == 0 => return false,
            _ => {}
        }
    }
    depth == 0
}

