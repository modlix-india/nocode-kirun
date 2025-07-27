// src/internal/engine/runtime/expression/doc.go

// Package expression handles parsing and evaluation of expressions used in KIRun.
//
// Features:
//   - Infix notation with full operator precedence (arithmetic, logical, bitwise, comparison, etc.)
//   - Support for variables, identifiers, and context references (e.g., Context.a, Steps.loop.iteration.index)
//   - String literals (single and double quotes), number literals, boolean and null
//   - Array and object access (e.g., a[0], obj.prop)
//   - Sub-expressions using {{ ... }} syntax, which are parsed as TokenSubExpression and can be nested
//   - Logical operators: and (&&), or (||), not, etc.
//   - Parentheses for grouping
//   - Robust tokenization and conversion to postfix (RPN) for evaluation
//
// Token Types:
//   - TokenNumber: Numeric literals
//   - TokenString: String literals (single or double quoted)
//   - TokenIdentifier: Variable or property names
//   - TokenOperator: All supported operators (+, -, *, /, <<, >>, and, or, etc.)
//   - TokenLeftParen, TokenRightParen: Parentheses
//   - TokenLeftBracket, TokenRightBracket: Array access
//   - TokenDot: Object property access
//   - TokenBoolean, TokenNull: true/false/null
//   - TokenSubExpression: Represents a sub-expression wrapped in {{ ... }}; its value is parsed as a new expression
//   - TokenEOF: End of input
//
// Sub-Expressions:
//   - Any expression wrapped in {{ ... }} is tokenized as TokenSubExpression.
//   - When evaluating or converting to string, the sub-expression is parsed and processed recursively.
//   - Example: "{{ 1 + 2 }} * 3" is parsed as a sub-expression (1+2) multiplied by 3.
//
// Example Usage:
//
//	expr, err := expression.NewExpression("Context.a[Steps.loop.iteration.index - 1] + 2")
//	if err != nil {
//	    // handle error
//	}
//	fmt.Println(expr.ToString()) // prints: Context.a[Steps.loop.iteration.index-1]+2
//
//	expr2, err := expression.NewExpression("{{ 1 + 2 }} * 3")
//	fmt.Println(expr2.ToString()) // prints: ((1+2)*3)
//
// Notes:
//   - The package currently focuses on parsing and stringification; evaluation logic may be extended as needed.
//   - Sub-expressions can be nested.
//   - Logical operators 'and' and 'or' are converted to '&&' and '||' in string output.
//   - String literals are preserved with their original quotes in output.
//
// For more details, see the tests in expression_test.go.
package expression
