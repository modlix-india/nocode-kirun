//! Port of kirun-js `runtime/expression/ExpressionEvaluator.ts`.
//!
//! JS gives every extractor the values map through `setValuesMap` before evaluating; Rust passes
//! it to `TokenValueExtractor::get_value_with`. The JS instance-level
//! `internalTokenValueExtractor` is created per `evaluate` call (and shared with the nested
//! `{{ }}` evaluators of that call), which is all JS relies on.

use std::collections::HashMap;
use std::sync::atomic::{AtomicUsize, Ordering};
use std::sync::{Arc, OnceLock};

use parking_lot::RwLock;
use regex::Regex;

use crate::runtime::expression::exception::expression_evaluation_exception;
use crate::runtime::expression::expression_token::ExpressionToken;
use crate::runtime::expression::expression_token_value::ExpressionTokenValue;
use crate::runtime::expression::js;
use crate::runtime::expression::operation::Operation;
use crate::runtime::expression::operators::binary::*;
use crate::runtime::expression::operators::ternary::{ConditionalTernaryOperator, TernaryOperator};
use crate::runtime::expression::operators::unary::{
    ArithmeticUnaryMinusOperator, ArithmeticUnaryPlusOperator, BitwiseComplementOperator, LogicalNotOperator,
    UnaryOperator,
};
use crate::runtime::expression::tokenextractor::expression_internal_value_extractor::ExpressionInternalValueExtractor;
use crate::runtime::expression::tokenextractor::literal_token_value_extractor::{java_number_or_text, LiteralTokenValueExtractor};
use crate::runtime::expression::tokenextractor::token_value_extractor::{
    normalize_root_bracket, TokenValueExtractor, ValuesMap,
};
use crate::runtime::expression::Expression;
use crate::json::schema::validator::schema_validator::java_json;
use crate::util::string::string_formatter::StringFormatter;
use crate::{KIRunError, Result, Value};

const PATTERN_UNKNOWN: u8 = 0;
const PATTERN_LITERAL: u8 = 1;
const PATTERN_SIMPLE_PATH: u8 = 2;
#[allow(dead_code)]
const PATTERN_SIMPLE_ARRAY_ACCESS: u8 = 3;
const PATTERN_SIMPLE_COMPARISON: u8 = 4;
const PATTERN_SIMPLE_TERNARY: u8 = 5;

/// A parsed expression and its fast-path pattern, cached by expression text (JS keeps two
/// caches; one entry here means one lookup per evaluation).
struct CachedExpression {
    exp: Arc<Expression>,
    pattern: u8,
}

type ExpressionCache = HashMap<String, Arc<CachedExpression>, crate::util::fx_hash::FxBuildHasher>;

static EXPRESSION_CACHE: OnceLock<RwLock<ExpressionCache>> = OnceLock::new();
static KEY_COUNTER: AtomicUsize = AtomicUsize::new(0);

fn regex(cell: &'static OnceLock<Regex>, pattern: &str) -> &'static Regex {
    cell.get_or_init(|| Regex::new(pattern).expect("valid regex"))
}

fn number_regex() -> &'static Regex {
    static R: OnceLock<Regex> = OnceLock::new();
    regex(&R, r"^-?\d+(\.\d+)?$")
}

fn single_quote_regex() -> &'static Regex {
    static R: OnceLock<Regex> = OnceLock::new();
    regex(&R, r"^'([^'\\]|\\.)*'$")
}

fn double_quote_regex() -> &'static Regex {
    static R: OnceLock<Regex> = OnceLock::new();
    regex(&R, r#"^"([^"\\]|\\.)*"$"#)
}

fn get_cached(expression_string: &str) -> Result<Arc<CachedExpression>> {
    let cache = EXPRESSION_CACHE.get_or_init(|| RwLock::new(HashMap::default()));
    if let Some(e) = cache.read().get(expression_string) {
        return Ok(e.clone());
    }
    let exp = Arc::new(Expression::new(expression_string)?);
    let pattern = detect_pattern(&exp);
    let e = Arc::new(CachedExpression { exp, pattern });
    cache.write().insert(expression_string.to_string(), e.clone());
    Ok(e)
}

fn get_cached_expression(expression_string: &str) -> Result<Arc<Expression>> {
    Ok(get_cached(expression_string)?.exp.clone())
}

fn is_unary_map_key(op: Operation) -> bool {
    op == Operation::UNARY_BITWISE_COMPLEMENT
        || op == Operation::UNARY_LOGICAL_NOT
        || op == Operation::UNARY_MINUS
        || op == Operation::UNARY_PLUS
}

fn unary_operator(op: Operation) -> Option<Box<dyn UnaryOperator>> {
    Some(if op == Operation::UNARY_BITWISE_COMPLEMENT {
        Box::new(BitwiseComplementOperator)
    } else if op == Operation::UNARY_LOGICAL_NOT {
        Box::new(LogicalNotOperator)
    } else if op == Operation::UNARY_MINUS {
        Box::new(ArithmeticUnaryMinusOperator)
    } else if op == Operation::UNARY_PLUS {
        Box::new(ArithmeticUnaryPlusOperator)
    } else {
        return None;
    })
}

fn binary_operator(op: Operation) -> Option<Box<dyn BinaryOperator>> {
    let table: [(Operation, fn() -> Box<dyn BinaryOperator>); 22] = [
        (Operation::ADDITION, || Box::new(ArithmeticAdditionOperator)),
        (Operation::DIVISION, || Box::new(ArithmeticDivisionOperator)),
        (Operation::INTEGER_DIVISION, || Box::new(ArithmeticInetgerDivisionOperator)),
        (Operation::MOD, || Box::new(ArithmeticModulusOperator)),
        (Operation::MULTIPLICATION, || Box::new(ArithmeticMultiplicationOperator)),
        (Operation::SUBTRACTION, || Box::new(ArithmeticSubtractionOperator)),
        (Operation::BITWISE_AND, || Box::new(BitwiseAndOperator)),
        (Operation::BITWISE_LEFT_SHIFT, || Box::new(BitwiseLeftShiftOperator)),
        (Operation::BITWISE_OR, || Box::new(BitwiseOrOperator)),
        (Operation::BITWISE_RIGHT_SHIFT, || Box::new(BitwiseRightShiftOperator)),
        (Operation::BITWISE_UNSIGNED_RIGHT_SHIFT, || Box::new(BitwiseUnsignedRightShiftOperator)),
        (Operation::BITWISE_XOR, || Box::new(BitwiseXorOperator)),
        (Operation::AND, || Box::new(LogicalAndOperator)),
        (Operation::EQUAL, || Box::new(LogicalEqualOperator)),
        (Operation::GREATER_THAN, || Box::new(LogicalGreaterThanOperator)),
        (Operation::GREATER_THAN_EQUAL, || Box::new(LogicalGreaterThanEqualOperator)),
        (Operation::LESS_THAN, || Box::new(LogicalLessThanOperator)),
        (Operation::LESS_THAN_EQUAL, || Box::new(LogicalLessThanEqualOperator)),
        (Operation::OR, || Box::new(LogicalOrOperator)),
        (Operation::NOT_EQUAL, || Box::new(LogicalNotEqualOperator)),
        (Operation::NULLISH_COALESCING_OPERATOR, || Box::new(LogicalNullishCoalescingOperator)),
        (Operation::ARRAY_RANGE_INDEX_OPERATOR, || Box::new(ArrayRangeOperator)),
    ];
    if op == Operation::ARRAY_OPERATOR {
        return Some(Box::new(ArrayOperator));
    }
    if op == Operation::OBJECT_OPERATOR {
        return Some(Box::new(ObjectOperator));
    }
    table.iter().find(|(o, _)| *o == op).map(|(_, f)| f())
}

// ---------------------------------------------------------------- fast path detection

fn is_literal_string(s: &str) -> bool {
    s == "true"
        || s == "false"
        || s == "null"
        || s == "undefined"
        || number_regex().is_match(s)
        || single_quote_regex().is_match(s)
        || double_quote_regex().is_match(s)
}

fn detect_pattern(exp: &Expression) -> u8 {
    let exp_str = exp.get_expression();
    if is_literal_string(exp_str) {
        return PATTERN_LITERAL;
    }
    let pattern = if exp_str.contains('.') && !exp_str.contains("{{") && !exp_str.contains("..") {
        let ops = exp.get_operations();
        let mut is_simple_path = !ops.is_empty()
            && ops.iter().all(|o| *o == Operation::OBJECT_OPERATOR || *o == Operation::ARRAY_OPERATOR);
        if is_simple_path && exp.get_tokens().iter().any(|t| matches!(t, ExpressionToken::Expression(_))) {
            is_simple_path = false;
        }
        if is_simple_path {
            PATTERN_SIMPLE_PATH
        } else {
            detect_ternary_or_comparison(exp)
        }
    } else if exp_str.contains("..") {
        PATTERN_UNKNOWN
    } else if !exp_str.contains("{{") {
        detect_ternary_or_comparison(exp)
    } else {
        PATTERN_UNKNOWN
    };

    pattern
}

fn detect_ternary_or_comparison(exp: &Expression) -> u8 {
    if exp.get_tokens().iter().any(|t| matches!(t, ExpressionToken::Expression(_))) {
        return PATTERN_UNKNOWN;
    }
    let ops = exp.get_operations();
    if ops.len() == 1 && ops[0] == Operation::CONDITIONAL_TERNARY_OPERATOR {
        return PATTERN_SIMPLE_TERNARY;
    }
    if ops.len() == 1 && (ops[0] == Operation::EQUAL || ops[0] == Operation::NOT_EQUAL) {
        return PATTERN_SIMPLE_COMPARISON;
    }
    PATTERN_UNKNOWN
}

/// JS `evaluateLiteral` (`Value::Null` for both `null`/`undefined` and "not a literal").
fn evaluate_literal(exp_str: &str) -> Value {
    match exp_str {
        "true" => return Value::Bool(true),
        "false" => return Value::Bool(false),
        "null" | "undefined" => return Value::Null,
        _ => {}
    }
    if number_regex().is_match(exp_str) {
        // kirun-java `Long.parseLong` for a whole number: exact, not through a double.
        if !exp_str.contains('.') {
            if let Ok(i) = exp_str.parse::<i64>() {
                return Value::from(i);
            }
        }
        // A decimal stays a decimal, `5.0` included, as the literal extractor reads it.
        return java_number_or_text(exp_str);
    }
    if single_quote_regex().is_match(exp_str) || double_quote_regex().is_match(exp_str) {
        return Value::String(exp_str[1..exp_str.len() - 1].to_string());
    }
    Value::Null
}

/// JS `==` (loose equality), used only by the simple-comparison fast path.
fn loose_equal(a: &Value, b: &Value) -> bool {
    match (a, b) {
        (Value::Null, Value::Null) => true,
        (Value::Null, _) | (_, Value::Null) => false,
        (Value::String(x), Value::String(y)) => x == y,
        (Value::Bool(x), Value::Bool(y)) => x == y,
        (Value::Number(_), Value::Number(_)) => js::to_number(a) == js::to_number(b),
        (Value::Array(_) | Value::Object(_), Value::Array(_) | Value::Object(_)) => a == b,
        (Value::Bool(_), _) => loose_equal(&js::num(js::to_number(a)), b),
        (_, Value::Bool(_)) => loose_equal(a, &js::num(js::to_number(b))),
        (Value::Array(_) | Value::Object(_), _) => loose_equal(&Value::String(js::to_js_string(a)), b),
        (_, Value::Array(_) | Value::Object(_)) => loose_equal(a, &Value::String(js::to_js_string(b))),
        _ => js::to_number(a) == js::to_number(b),
    }
}

fn value_of_extractor_path(path: &str, values_map: &ValuesMap) -> Option<Result<Value>> {
    let dot = path.find('.')?;
    let prefix = &path[..=dot];
    values_map.get(prefix).map(|e| e.get_value_with(path, Some(values_map)))
}

/// A token on the evaluation stack: borrowed from the parsed expression or produced while
/// evaluating.
enum Tok<'a> {
    Ref(&'a ExpressionToken),
    Own(ExpressionToken),
}

impl Tok<'_> {
    fn get(&self) -> &ExpressionToken {
        match self {
            Tok::Ref(t) => t,
            Tok::Own(t) => t,
        }
    }
}

struct State<'a> {
    working: Vec<Tok<'a>>,
    op_idx: usize,
    src_idx: usize,
}

pub struct ExpressionEvaluator {
    expression: String,
    exp: Option<Arc<Expression>>,
}

impl ExpressionEvaluator {
    pub fn new(expression: &str) -> Self {
        Self { expression: expression.to_string(), exp: None }
    }

    pub fn from_expression(exp: Expression) -> Self {
        Self { expression: exp.get_expression().to_string(), exp: Some(Arc::new(exp)) }
    }

    pub fn evaluate(&self, values_map: &ValuesMap) -> Result<Value> {
        // Lives on the stack and stays empty (no allocation) unless an object path needs it.
        let internal = ExpressionInternalValueExtractor::new();
        self.evaluate_with_internal(values_map, &internal)
    }

    /// `evaluate`, but `None` where kirun-java's evaluator returns Java `null` (a plain path to
    /// a missing value) rather than JSON `null`; see [`super::java_null`].
    pub fn evaluate_or_missing(&self, values_map: &ValuesMap) -> Result<Option<Value>> {
        let v = self.evaluate(values_map)?;
        if v.is_null() && super::java_null::is_missing(&self.expression, values_map) {
            return Ok(None);
        }
        Ok(Some(v))
    }

    pub fn get_expression(&self) -> Result<Expression> {
        match &self.exp {
            Some(e) => Ok((**e).clone()),
            None => Ok((*get_cached_expression(&self.expression)?).clone()),
        }
    }

    pub fn get_expression_string(&self) -> &str {
        &self.expression
    }

    fn evaluate_with_internal(
        &self,
        values_map: &ValuesMap,
        internal: &ExpressionInternalValueExtractor,
    ) -> Result<Value> {
        let (expanded_expression, cached) = self.process_nesting_expression(&self.expression, values_map, internal)?;
        let expanded_exp = &cached.exp;

        match cached.pattern {
            PATTERN_LITERAL => Ok(evaluate_literal(expanded_expression.trim())),
            PATTERN_SIMPLE_PATH => self.evaluate_simple_path(expanded_exp, values_map),
            PATTERN_SIMPLE_COMPARISON => self.evaluate_simple_comparison(expanded_exp, values_map),
            PATTERN_SIMPLE_TERNARY => self.evaluate_simple_ternary(expanded_exp, values_map),
            // JS adds the internal extractor to a copy of the values map; `get_value` resolves
            // `_internal.` paths against it directly instead.
            _ => self.evaluate_expression(expanded_exp, values_map, internal),
        }
    }

    // ------------------------------------------------------------ fast paths

    fn evaluate_simple_path(&self, exp: &Expression, values_map: &ValuesMap) -> Result<Value> {
        value_of_extractor_path(exp.get_expression(), values_map).unwrap_or(Ok(Value::Null))
    }

    fn evaluate_simple_comparison(&self, exp: &Expression, values_map: &ValuesMap) -> Result<Value> {
        let ops = exp.get_operations();
        let tokens = exp.get_tokens();
        if tokens.len() != 2 || ops.len() != 1 {
            return Ok(Value::Null);
        }
        let v1 = self.get_token_value(&tokens[1], values_map)?;
        let v2 = self.get_token_value(&tokens[0], values_map)?;
        if ops[0] == Operation::EQUAL {
            Ok(Value::Bool(loose_equal(&v1, &v2)))
        } else if ops[0] == Operation::NOT_EQUAL {
            Ok(Value::Bool(!loose_equal(&v1, &v2)))
        } else {
            Ok(Value::Null)
        }
    }

    fn evaluate_simple_ternary(&self, exp: &Expression, values_map: &ValuesMap) -> Result<Value> {
        let tokens = exp.get_tokens();
        if tokens.len() != 3 {
            return Ok(Value::Null);
        }
        let condition = self.get_token_value(&tokens[2], values_map)?;
        let true_value = self.get_token_value(&tokens[1], values_map)?;
        let false_value = self.get_token_value(&tokens[0], values_map)?;
        Ok(if js::truthy(&condition) { true_value } else { false_value })
    }

    fn get_token_value(&self, token: &ExpressionToken, values_map: &ValuesMap) -> Result<Value> {
        if let ExpressionToken::Value(v) = token {
            return Ok(v.get_element().clone());
        }
        let token_str = token.get_expression();
        let literal = evaluate_literal(token_str);
        if !literal.is_null() || token_str == "undefined" || token_str == "null" {
            return Ok(literal);
        }
        if let Some(v) = value_of_extractor_path(token_str, values_map) {
            return v;
        }
        LiteralTokenValueExtractor::INSTANCE.get_value_with(token_str, Some(values_map))
    }

    // ------------------------------------------------------------ {{ }} nesting

    fn process_nesting_expression<'e>(
        &self,
        expression: &'e str,
        values_map: &ValuesMap,
        internal: &ExpressionInternalValueExtractor,
    ) -> Result<(std::borrow::Cow<'e, str>, Arc<CachedExpression>)> {
        if !expression.contains("{{") {
            return Ok((std::borrow::Cow::Borrowed(expression), get_cached(expression)?));
        }
        let mut current = expression.to_string();
        while current.contains("{{") {
            let Some(innermost) = find_innermost_pair(&current) else { break };
            current = self.replace_one_nesting(&current, innermost, values_map, internal)?;
        }
        let exp = get_cached(&current)?;
        Ok((std::borrow::Cow::Owned(current), exp))
    }

    fn replace_one_nesting(
        &self,
        expression: &str,
        (start_pos, end_pos, inner_expr): (usize, usize, String),
        values_map: &ValuesMap,
        internal: &ExpressionInternalValueExtractor,
    ) -> Result<String> {
        let nested = ExpressionEvaluator::new(&inner_expr);
        let evaluated_value = nested.evaluate_with_internal(values_map, internal)?;
        Ok(format!("{}{}{}", &expression[..start_pos], js::to_js_string(&evaluated_value), &expression[end_pos..]))
    }

    // ------------------------------------------------------------ full evaluation

    fn pop_token<'a>(&self, exp: &'a Expression, ops_len: usize, st: &mut State<'a>) -> Result<Tok<'a>> {
        if let Some(t) = st.working.pop() {
            return Ok(t);
        }
        let src = exp.get_tokens();
        if st.src_idx >= src.len() {
            let msg = if st.op_idx < ops_len {
                "Not enough tokens to evaluate expression"
            } else {
                "Expression evaluation incomplete: missing token"
            };
            return Err(expression_evaluation_exception(exp.get_expression(), msg));
        }
        st.src_idx += 1;
        Ok(Tok::Ref(&src[st.src_idx - 1]))
    }

    fn evaluate_expression(
        &self,
        exp: &Expression,
        values_map: &ValuesMap,
        internal: &ExpressionInternalValueExtractor,
    ) -> Result<Value> {
        let ops = exp.get_operations();
        let src = exp.get_tokens();
        let mut st = State { working: Vec::with_capacity(4), op_idx: 0, src_idx: 0 };

        while st.op_idx < ops.len() {
            let operator = ops[st.op_idx];
            st.op_idx += 1;
            let token = self.pop_token(exp, ops.len(), &mut st)?;

            if is_unary_map_key(operator) {
                let v = self.get_value_from_token(values_map, token.get(), internal)?;
                let r = self.apply_unary_operation(operator, v)?;
                st.working.push(Tok::Own(r));
            } else if operator == Operation::OBJECT_OPERATOR || operator == Operation::ARRAY_OPERATOR {
                self.process_object_or_array_operator_indexed(values_map, exp, ops, &mut st, operator, token, internal)?;
            } else if operator == Operation::CONDITIONAL_TERNARY_OPERATOR {
                let token2 = self.pop_token(exp, ops.len(), &mut st)?;
                let token3 = self.pop_token(exp, ops.len(), &mut st)?;
                let v1 = self.get_value_from_token(values_map, token3.get(), internal)?;
                let v2 = self.get_value_from_token(values_map, token2.get(), internal)?;
                let v3 = self.get_value_from_token(values_map, token.get(), internal)?;
                let r = self.apply_ternary_operation(operator, &v1, &v2, &v3)?;
                st.working.push(Tok::Own(r));
            } else {
                let token2 = self.pop_token(exp, ops.len(), &mut st)?;
                let v1 = self.get_value_from_token(values_map, token2.get(), internal)?;
                let v2 = self.get_value_from_token(values_map, token.get(), internal)?;
                let r = self.apply_binary_operation(operator, v1, v2, values_map)?;
                st.working.push(Tok::Own(r));
            }
        }

        while st.src_idx < src.len() {
            st.working.push(Tok::Ref(&src[st.src_idx]));
            st.src_idx += 1;
        }

        if st.working.is_empty() {
            return Err(KIRunError::execution(StringFormatter::format(
                "Expression : $ evaluated to null",
                &[&exp.to_string()],
            )));
        }

        if st.working.len() != 1 {
            // kirun-java evaluates the tokens left over, then refuses more than one, printing its
            // deque of `ExpressionTokenValue("", value)`s (`[: "a", : "b"]`, in text order).
            let mut values = Vec::with_capacity(st.working.len());
            for t in &st.working {
                values.push(match t.get() {
                    ExpressionToken::Value(v) => v.get_element().clone(),
                    other => self.get_value_from_token(values_map, other, internal)?,
                });
            }
            let list: Vec<String> = values.iter().rev().map(|v| format!(": {}", java_json(v))).collect();
            return Err(KIRunError::execution(format!(
                "Expression : {exp} evaluated multiple values [{}]",
                list.join(", ")
            )));
        }

        let token = st.working.pop().expect("one token");
        match token.get() {
            ExpressionToken::Value(v) => Ok(v.get_element().clone()),
            ExpressionToken::Expression(e) => self.evaluate_expression(e, values_map, internal),
            t => self.get_value(t.get_expression(), values_map, internal),
        }
    }

    /// Collects one object/array access chain into a path string and resolves it.
    #[allow(clippy::too_many_arguments)]
    fn process_object_or_array_operator_indexed<'a>(
        &self,
        values_map: &ValuesMap,
        exp: &'a Expression,
        ops: &std::collections::VecDeque<Operation>,
        st: &mut State<'a>,
        operator: Operation,
        token: Tok<'a>,
        internal: &ExpressionInternalValueExtractor,
    ) -> Result<()> {
        let src = exp.get_tokens();
        let mut obj_tokens: Vec<ExpressionToken> = Vec::new();
        let mut obj_operations: Vec<Operation> = Vec::new();

        let mut operator: Option<Operation> = Some(operator);
        let mut token: Option<Tok<'a>> = Some(token);

        loop {
            let op = operator.expect("operator");
            obj_operations.push(op);
            if let Some(t) = token.take() {
                match t.get() {
                    ExpressionToken::Expression(e) => {
                        let should_evaluate = op == Operation::ARRAY_OPERATOR || !is_path_expression(e);
                        if should_evaluate {
                            let v = self.evaluate_expression(e, values_map, internal)?;
                            obj_tokens.push(ExpressionToken::Value(ExpressionTokenValue::new(e.to_string(), v)));
                        } else {
                            obj_tokens.push(ExpressionToken::new(build_path_string(e)));
                        }
                    }
                    other => obj_tokens.push(other.clone()),
                }
            }

            token = if let Some(w) = st.working.pop() {
                Some(w)
            } else if st.src_idx < src.len() {
                st.src_idx += 1;
                Some(Tok::Ref(&src[st.src_idx - 1]))
            } else {
                None
            };
            operator = if st.op_idx < ops.len() {
                st.op_idx += 1;
                Some(ops[st.op_idx - 1])
            } else {
                None
            };
            if !(operator == Some(Operation::OBJECT_OPERATOR) || operator == Some(Operation::ARRAY_OPERATOR)) {
                break;
            }
        }

        if let Some(t) = token {
            match t.get() {
                ExpressionToken::Expression(e) => {
                    if is_path_expression(e) {
                        obj_tokens.push(ExpressionToken::new(build_path_string(e)));
                    } else {
                        let v = self.evaluate_expression(e, values_map, internal)?;
                        obj_tokens.push(ExpressionToken::Value(ExpressionTokenValue::new(e.to_string(), v)));
                    }
                }
                other => obj_tokens.push(other.clone()),
            }
        }

        if operator.is_some() {
            st.op_idx -= 1;
        }

        let mut obj_token_idx = obj_tokens.len() as isize - 1;
        let mut obj_op_idx = obj_operations.len() as isize - 1;

        let mut obj_token = obj_tokens[obj_token_idx as usize].clone();
        obj_token_idx -= 1;

        if let ExpressionToken::Value(tv) = &obj_token {
            let v = tv.get_token_value();
            if js::is_object(v) || v.is_null() {
                let key = format!("_k{}", KEY_COUNTER.fetch_add(1, Ordering::Relaxed));
                internal.add_value(key.clone(), v.clone());
                obj_token = ExpressionToken::new(format!("{}{}", ExpressionInternalValueExtractor::PREFIX, key));
            }
        }

        let preserve = |tv: &ExpressionTokenValue| {
            let original_expr = tv.get_expression();
            (original_expr.starts_with('"') || original_expr.starts_with('\''))
                && tv.get_token_value().as_str().is_some_and(|s| s.contains('.'))
        };

        let mut str = match &obj_token {
            ExpressionToken::Value(tv) => {
                if preserve(tv) {
                    tv.get_expression().to_string()
                } else {
                    js::to_js_string(tv.get_token_value())
                }
            }
            other => other.to_string(),
        };

        while obj_token_idx >= 0 {
            let obj_token = &obj_tokens[obj_token_idx as usize];
            obj_token_idx -= 1;
            let operator = obj_operations[obj_op_idx as usize];
            obj_op_idx -= 1;
            let token_val = match obj_token {
                ExpressionToken::Value(tv) => {
                    if operator == Operation::ARRAY_OPERATOR && preserve(tv) {
                        tv.get_expression().to_string()
                    } else {
                        js::to_js_string(tv.get_token_value())
                    }
                }
                other => other.to_string(),
            };
            str.push_str(operator.get_operator());
            str.push_str(&token_val);
            if operator == Operation::ARRAY_OPERATOR {
                str.push(']');
            }
        }

        let str = normalize_root_bracket(&str);
        let key = str.find('.').map_or("", |d| &str[..=d]);
        let v = if key.chars().count() > 2
            && (key == ExpressionInternalValueExtractor::PREFIX || values_map.contains_key(key))
        {
            self.get_value(&str, values_map, internal)?
        } else {
            match LiteralTokenValueExtractor::INSTANCE.try_get_value(&str) {
                Some(v) => v,
                None => evaluate_literal_property_access(&str),
            }
        };
        st.working.push(Tok::Own(ExpressionToken::Value(ExpressionTokenValue::new(str.into_owned(), v))));
        Ok(())
    }

    fn apply_ternary_operation(&self, operator: Operation, v1: &Value, v2: &Value, v3: &Value) -> Result<ExpressionToken> {
        if operator != Operation::CONDITIONAL_TERNARY_OPERATOR {
            return Err(expression_evaluation_exception(
                &self.expression,
                &StringFormatter::format("No operator found to evaluate $", &[&self.expression]),
            ));
        }
        let r = ConditionalTernaryOperator.apply(v1, v2, v3)?;
        Ok(ExpressionToken::Value(ExpressionTokenValue::new(operator.get_operator(), r)))
    }

    fn apply_binary_operation(
        &self,
        operator: Operation,
        v1: Value,
        v2: Value,
        _values_map: &ValuesMap,
    ) -> Result<ExpressionToken> {
        // kirun-java refuses only when both sides are arrays or objects (kirun-js: either side);
        // one primitive side is left to the operator, which names the other.
        if (js::is_object(&v1) && js::is_object(&v2))
            && operator != Operation::EQUAL
            && operator != Operation::NOT_EQUAL
            && operator != Operation::NULLISH_COALESCING_OPERATOR
            && operator != Operation::AND
            && operator != Operation::OR
        {
            return Err(expression_evaluation_exception(
                &self.expression,
                &StringFormatter::format(
                    "Cannot evaluate expression $ $ $",
                    &[&java_json(&v1), operator.get_operator(), &java_json(&v2)],
                ),
            ));
        }

        let Some(op) = binary_operator(operator) else {
            return Err(expression_evaluation_exception(
                &self.expression,
                &StringFormatter::format(
                    "No operator found to evaluate $ $ $",
                    &[&java_json(&v1), operator.get_operator(), &java_json(&v2)],
                ),
            ));
        };

        // kirun-js evaluates a `??` result again when it looks like an expression; kirun-java
        // returns it as it is.
        let result = op.apply(&v1, &v2)?;

        Ok(ExpressionToken::Value(ExpressionTokenValue::new(operator.get_operator(), result)))
    }

    fn apply_unary_operation(&self, operator: Operation, value: Value) -> Result<ExpressionToken> {
        if operator.get_operator() != Operation::NOT.get_operator()
            && operator.get_operator() != Operation::UNARY_LOGICAL_NOT.get_operator()
            && js::is_object(&value)
        {
            return Err(expression_evaluation_exception(
                &self.expression,
                &StringFormatter::format("The operator $ cannot be applied to $", &[operator.get_operator(), &java_json(&value)]),
            ));
        }

        let Some(op) = unary_operator(operator) else {
            return Err(expression_evaluation_exception(
                &self.expression,
                &StringFormatter::format("No Unary operator $ is found to apply on $", &[operator.get_operator(), &java_json(&value)]),
            ));
        };

        let r = op.apply(&value)?;
        Ok(ExpressionToken::Value(ExpressionTokenValue::new(operator.get_operator(), r)))
    }

    fn get_value_from_token(
        &self,
        values_map: &ValuesMap,
        token: &ExpressionToken,
        internal: &ExpressionInternalValueExtractor,
    ) -> Result<Value> {
        match token {
            ExpressionToken::Expression(e) => self.evaluate_expression(e, values_map, internal),
            ExpressionToken::Value(v) => Ok(v.get_element().clone()),
            ExpressionToken::Token(t) => self.get_value(t, values_map, internal),
        }
    }

    fn get_value(&self, path: &str, values_map: &ValuesMap, internal: &ExpressionInternalValueExtractor) -> Result<Value> {
        let path = normalize_root_bracket(path);
        let path_prefix = path.find('.').map_or("", |d| &path[..=d]);
        if path_prefix == ExpressionInternalValueExtractor::PREFIX {
            return internal.get_value_with(&path, Some(values_map));
        }
        if let Some(extractor) = values_map.get(path_prefix) {
            return extractor.get_value_with(&path, Some(values_map));
        }
        LiteralTokenValueExtractor::INSTANCE.get_value_from_extractors(&path, values_map)
    }
}

/// Finds the innermost `{{ }}` pair (one whose content has no `{{`): `(start, end, content)`,
/// `start` at the `{{` and `end` just after the `}}`.
fn find_innermost_pair(expr: &str) -> Option<(usize, usize, String)> {
    let b = expr.as_bytes();
    let n = b.len();
    let mut i = 0;
    while i + 1 < n {
        if b[i] != b'{' || b[i + 1] != b'{' {
            i += 1;
            continue;
        }
        let open_pos = i;
        i += 2;
        let mut depth = 1;
        while i + 1 < n && depth > 0 {
            if b[i] == b'{' && b[i + 1] == b'{' {
                depth += 1;
                i += 2;
                continue;
            }
            if b[i] == b'}' && b[i + 1] == b'}' {
                depth -= 1;
                if depth == 0 {
                    let close_pos = i + 2;
                    let content = &expr[open_pos + 2..i];
                    if !content.contains("{{") {
                        return Some((open_pos, close_pos, content.to_string()));
                    }
                    match find_innermost_pair(content) {
                        Some((s, e, c)) => return Some((open_pos + 2 + s, open_pos + 2 + e, c)),
                        None => {
                            i += 2;
                            continue;
                        }
                    }
                }
                i += 2;
                continue;
            }
            i += 1;
        }
        i = open_pos + 1;
    }
    None
}

/// JS `evaluateLiteralPropertyAccess` (`2.val` is `undefined`, `"abc".length` is 3). kirun-java
/// has no property access on literals: a path whose prefix names no extractor is the literal
/// text of the whole path (`2.val`, `"abc".length`).
fn evaluate_literal_property_access(str: &str) -> Value {
    Value::String(str.to_string())
}

/// JS `buildPathString`: a path expression without parentheses (`a.(b.c)` -> `a.b.c`).
fn build_path_string(expr: &Expression) -> String {
    let ops = expr.get_operations();
    let tokens = expr.get_tokens();

    if ops.is_empty() {
        if tokens.len() == 1 {
            return match &tokens[0] {
                ExpressionToken::Expression(e) => build_path_string(e),
                t => t.get_expression().to_string(),
            };
        }
        return expr.get_expression().to_string();
    }

    if tokens.len() >= 2 {
        let part = |t: &ExpressionToken| match t {
            ExpressionToken::Expression(e) => build_path_string(e),
            t => t.get_expression().to_string(),
        };
        let right_str = part(&tokens[0]);
        let left_str = part(&tokens[1]);
        let op = ops[0];
        if op == Operation::OBJECT_OPERATOR {
            return format!("{left_str}.{right_str}");
        } else if op == Operation::ARRAY_OPERATOR {
            return format!("{left_str}[{right_str}]");
        } else if op == Operation::ARRAY_RANGE_INDEX_OPERATOR {
            return format!("{left_str}..{right_str}");
        }
    }

    strip_outer_parens(&expr.to_string())
}

fn strip_outer_parens(s: &str) -> String {
    if s.len() >= 2 && s.starts_with('(') && s.ends_with(')') {
        let mut depth = 0i32;
        let n = s.len();
        for (i, c) in s.bytes().enumerate() {
            if c == b'(' {
                depth += 1;
            } else if c == b')' {
                depth -= 1;
            }
            if depth == 0 && i < n - 1 {
                return s.to_string();
            }
        }
        return s[1..n - 1].to_string();
    }
    s.to_string()
}

fn is_path_operator(op: Operation) -> bool {
    op == Operation::OBJECT_OPERATOR || op == Operation::ARRAY_OPERATOR || op == Operation::ARRAY_RANGE_INDEX_OPERATOR
}

/// JS `isPathExpression`: identifiers and object/array access with static indices.
fn is_path_expression(expr: &Expression) -> bool {
    let ops = expr.get_operations();
    let tokens = expr.get_tokens();

    if ops.is_empty() {
        return true;
    }

    for op in ops {
        if !is_path_operator(*op) {
            return false;
        }
        if *op == Operation::ARRAY_OPERATOR && !tokens.is_empty() && !is_static_array_index(&tokens[0]) {
            return false;
        }
    }

    tokens.iter().all(|t| match t {
        ExpressionToken::Expression(e) => is_path_expression(e),
        _ => true,
    })
}

fn is_static_array_index(token: &ExpressionToken) -> bool {
    match token {
        ExpressionToken::Expression(e) => is_static_array_index_expression(e),
        t => is_static_literal(t.get_expression()),
    }
}

fn is_static_array_index_expression(expr: &Expression) -> bool {
    let ops = expr.get_operations();
    let tokens = expr.get_tokens();
    if ops.is_empty() && tokens.len() == 1 {
        return is_static_literal(tokens[0].get_expression());
    }
    if ops.len() == 1 && ops[0] == Operation::ARRAY_RANGE_INDEX_OPERATOR {
        return tokens.iter().all(is_static_array_index);
    }
    false
}

fn is_static_literal(s: &str) -> bool {
    if number_regex().is_match(s) {
        return true;
    }
    (s.starts_with('"') && s.ends_with('"')) || (s.starts_with('\'') && s.ends_with('\''))
}
