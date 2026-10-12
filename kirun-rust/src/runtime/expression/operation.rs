//! Port of kirun-js `runtime/expression/Operation.ts`.

use std::fmt;

/// JS `Operation`. Every JS static instance is an associated `const` here.
///
/// An operation is identified by its operator string. `key` is a hash of it computed at
/// compile time, so the many `op == Operation::X` checks while evaluating compare one integer
/// instead of string contents (the string is compared only when the hashes match).
#[derive(Debug, Clone, Copy)]
pub struct Operation {
    operator: &'static str,
    operator_name: &'static str,
    should_be_wrapped_in_space: bool,
    key: u64,
}

impl PartialEq for Operation {
    #[inline]
    fn eq(&self, other: &Self) -> bool {
        self.key == other.key && self.operator == other.operator
    }
}

impl Eq for Operation {}

impl std::hash::Hash for Operation {
    fn hash<H: std::hash::Hasher>(&self, state: &mut H) {
        state.write_u64(self.key);
    }
}

/// FNV-1a of the operator string.
const fn operator_key(s: &str) -> u64 {
    let b = s.as_bytes();
    let mut h: u64 = 0xcbf2_9ce4_8422_2325;
    let mut i = 0;
    while i < b.len() {
        h ^= b[i] as u64;
        h = h.wrapping_mul(0x0100_0000_01b3);
        i += 1;
    }
    h
}

const fn op(operator: &'static str) -> Operation {
    Operation { operator, operator_name: operator, should_be_wrapped_in_space: false, key: operator_key(operator) }
}

const fn op_named(operator: &'static str, operator_name: &'static str) -> Operation {
    Operation { operator, operator_name, should_be_wrapped_in_space: false, key: operator_key(operator) }
}

const fn op_spaced(operator: &'static str) -> Operation {
    Operation { operator, operator_name: operator, should_be_wrapped_in_space: true, key: operator_key(operator) }
}

impl Operation {
    pub const MULTIPLICATION: Operation = op("*");
    pub const DIVISION: Operation = op("/");
    pub const INTEGER_DIVISION: Operation = op("//");
    pub const MOD: Operation = op("%");
    pub const ADDITION: Operation = op("+");
    pub const SUBTRACTION: Operation = op("-");

    pub const NOT: Operation = op_spaced("not");
    pub const AND: Operation = op_spaced("and");
    pub const OR: Operation = op_spaced("or");
    pub const LESS_THAN: Operation = op("<");
    pub const LESS_THAN_EQUAL: Operation = op("<=");
    pub const GREATER_THAN: Operation = op(">");
    pub const GREATER_THAN_EQUAL: Operation = op(">=");
    pub const EQUAL: Operation = op("=");
    pub const NOT_EQUAL: Operation = op("!=");

    pub const BITWISE_AND: Operation = op("&");
    pub const BITWISE_OR: Operation = op("|");
    pub const BITWISE_XOR: Operation = op("^");
    pub const BITWISE_COMPLEMENT: Operation = op("~");
    pub const BITWISE_LEFT_SHIFT: Operation = op("<<");
    pub const BITWISE_RIGHT_SHIFT: Operation = op(">>");
    pub const BITWISE_UNSIGNED_RIGHT_SHIFT: Operation = op(">>>");

    pub const UNARY_PLUS: Operation = op_named("UN: +", "+");
    pub const UNARY_MINUS: Operation = op_named("UN: -", "-");
    pub const UNARY_LOGICAL_NOT: Operation = op_named("UN: not", "not");
    pub const UNARY_BITWISE_COMPLEMENT: Operation = op_named("UN: ~", "~");

    pub const ARRAY_OPERATOR: Operation = op("[");
    pub const ARRAY_RANGE_INDEX_OPERATOR: Operation = op("..");
    pub const OBJECT_OPERATOR: Operation = op(".");

    pub const NULLISH_COALESCING_OPERATOR: Operation = op("??");

    pub const CONDITIONAL_TERNARY_OPERATOR: Operation = op("?");

    pub fn new(operator: &'static str, operator_name: Option<&'static str>, should_be_wrapped_in_space: bool) -> Self {
        Operation {
            operator,
            operator_name: operator_name.unwrap_or(operator),
            should_be_wrapped_in_space,
            key: operator_key(operator),
        }
    }

    pub fn get_operator(&self) -> &'static str {
        self.operator
    }

    pub fn get_operator_name(&self) -> &'static str {
        self.operator_name
    }

    pub fn should_be_wrapped_in_space(&self) -> bool {
        self.should_be_wrapped_in_space
    }

    /// Every named JS static instance, in JS `VALUE_OF` order.
    pub const ALL: [(&'static str, Operation); 31] = [
        ("MULTIPLICATION", Operation::MULTIPLICATION),
        ("DIVISION", Operation::DIVISION),
        ("INTEGER_DIVISION", Operation::INTEGER_DIVISION),
        ("MOD", Operation::MOD),
        ("ADDITION", Operation::ADDITION),
        ("SUBTRACTION", Operation::SUBTRACTION),
        ("NOT", Operation::NOT),
        ("AND", Operation::AND),
        ("OR", Operation::OR),
        ("LESS_THAN", Operation::LESS_THAN),
        ("LESS_THAN_EQUAL", Operation::LESS_THAN_EQUAL),
        ("GREATER_THAN", Operation::GREATER_THAN),
        ("GREATER_THAN_EQUAL", Operation::GREATER_THAN_EQUAL),
        ("EQUAL", Operation::EQUAL),
        ("NOT_EQUAL", Operation::NOT_EQUAL),
        ("BITWISE_AND", Operation::BITWISE_AND),
        ("BITWISE_OR", Operation::BITWISE_OR),
        ("BITWISE_XOR", Operation::BITWISE_XOR),
        ("BITWISE_COMPLEMENT", Operation::BITWISE_COMPLEMENT),
        ("BITWISE_LEFT_SHIFT", Operation::BITWISE_LEFT_SHIFT),
        ("BITWISE_RIGHT_SHIFT", Operation::BITWISE_RIGHT_SHIFT),
        ("BITWISE_UNSIGNED_RIGHT_SHIFT", Operation::BITWISE_UNSIGNED_RIGHT_SHIFT),
        ("UNARY_PLUS", Operation::UNARY_PLUS),
        ("UNARY_MINUS", Operation::UNARY_MINUS),
        ("UNARY_LOGICAL_NOT", Operation::UNARY_LOGICAL_NOT),
        ("UNARY_BITWISE_COMPLEMENT", Operation::UNARY_BITWISE_COMPLEMENT),
        ("ARRAY_OPERATOR", Operation::ARRAY_OPERATOR),
        ("ARRAY_RANGE_INDEX_OPERATOR", Operation::ARRAY_RANGE_INDEX_OPERATOR),
        ("OBJECT_OPERATOR", Operation::OBJECT_OPERATOR),
        ("NULLISH_COALESCING_OPERATOR", Operation::NULLISH_COALESCING_OPERATOR),
        ("CONDITIONAL_TERNARY_OPERATOR", Operation::CONDITIONAL_TERNARY_OPERATOR),
    ];

    /// JS `Operation.VALUE_OF.get(str)` / `OPERATION_VALUE_OF.get(str)`: looks the string up as an
    /// operator (`"+"`) first, then as a constant name (`"ADDITION"`).
    pub fn value_of(str: &str) -> Option<Operation> {
        Self::from_operator(str).or_else(|| Self::ALL.iter().find(|(n, _)| *n == str).map(|(_, o)| *o))
    }

    /// JS `Operation.OPERATION_VALUE_OF.get(str)`.
    pub fn from_operator(str: &str) -> Option<Operation> {
        Self::ALL.iter().find(|(_, o)| o.operator == str).map(|(_, o)| *o)
    }

    /// JS `Operation.OPERATORS.has(str)`: binary/logical/bitwise/access/conditional operator strings.
    pub fn is_operator_str(str: &str) -> bool {
        Self::OPERATORS.contains(&str)
    }

    /// JS `Operation.OPERATORS_WITHOUT_SPACE_WRAP.has(str)`.
    pub fn is_operator_without_space_wrap(str: &str) -> bool {
        Self::OPERATORS.contains(&str) && !matches!(str, "not" | "and" | "or")
    }

    const OPERATORS: [&'static str; 27] = [
        "*", "/", "//", "%", "+", "-", "not", "and", "or", "<", "<=", ">", ">=", "=", "!=", "??", "&", "~", "<<",
        "|", ">>", ">>>", "^", "[", "..", ".", "?",
    ];

    /// JS `Operation.OPERATOR_PRIORITY.get(op)`.
    pub fn get_priority(&self) -> Option<u32> {
        let o = *self;
        Some(
            if o == Self::UNARY_PLUS
                || o == Self::UNARY_MINUS
                || o == Self::UNARY_LOGICAL_NOT
                || o == Self::UNARY_BITWISE_COMPLEMENT
                || o == Self::ARRAY_OPERATOR
                || o == Self::OBJECT_OPERATOR
            {
                1
            } else if o == Self::ARRAY_RANGE_INDEX_OPERATOR
                || o == Self::MULTIPLICATION
                || o == Self::DIVISION
                || o == Self::INTEGER_DIVISION
                || o == Self::MOD
            {
                2
            } else if o == Self::ADDITION || o == Self::SUBTRACTION {
                3
            } else if o == Self::BITWISE_LEFT_SHIFT
                || o == Self::BITWISE_RIGHT_SHIFT
                || o == Self::BITWISE_UNSIGNED_RIGHT_SHIFT
            {
                4
            } else if o == Self::LESS_THAN
                || o == Self::LESS_THAN_EQUAL
                || o == Self::GREATER_THAN
                || o == Self::GREATER_THAN_EQUAL
            {
                5
            } else if o == Self::EQUAL || o == Self::NOT_EQUAL {
                6
            } else if o == Self::BITWISE_AND {
                7
            } else if o == Self::BITWISE_XOR {
                8
            } else if o == Self::BITWISE_OR {
                9
            } else if o == Self::NOT || o == Self::AND {
                10
            } else if o == Self::OR || o == Self::NULLISH_COALESCING_OPERATOR {
                11
            } else if o == Self::CONDITIONAL_TERNARY_OPERATOR {
                12
            } else {
                return None;
            },
        )
    }

    /// JS `Operation.UNARY_MAP.get(op)`.
    pub fn to_unary(&self) -> Option<Operation> {
        let o = *self;
        if o == Self::ADDITION || o == Self::UNARY_PLUS {
            Some(Self::UNARY_PLUS)
        } else if o == Self::SUBTRACTION || o == Self::UNARY_MINUS {
            Some(Self::UNARY_MINUS)
        } else if o == Self::NOT || o == Self::UNARY_LOGICAL_NOT {
            Some(Self::UNARY_LOGICAL_NOT)
        } else if o == Self::BITWISE_COMPLEMENT || o == Self::UNARY_BITWISE_COMPLEMENT {
            Some(Self::UNARY_BITWISE_COMPLEMENT)
        } else {
            None
        }
    }

    /// JS `Operation.UNARY_OPERATORS.has(op)`.
    pub fn is_unary(&self) -> bool {
        self.to_unary().is_some()
    }

    /// JS `Operation.BIGGEST_OPERATOR_SIZE`.
    pub fn biggest_operator_size() -> usize {
        Self::ALL
            .iter()
            .map(|(_, o)| o.operator)
            .filter(|o| !o.starts_with("UN: "))
            .map(|o| o.chars().count())
            .max()
            .unwrap_or(0)
    }
}

impl fmt::Display for Operation {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.write_str(self.operator)
    }
}
