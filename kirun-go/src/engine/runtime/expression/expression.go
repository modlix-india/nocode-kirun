package expression

import (
	"fmt"
	"strings"
	"unicode"
)

// TokenType represents the type of a token
type TokenType int

const (
	TokenNumber TokenType = iota
	TokenString
	TokenIdentifier
	TokenOperator
	TokenLeftParen
	TokenRightParen
	TokenLeftBracket
	TokenRightBracket
	TokenComma
	TokenDot
	TokenQuestion
	TokenColon
	TokenBoolean
	TokenNull
	TokenEOF
)

// Token represents a parsed token
type Token struct {
	Type     TokenType
	Value    string
	Position int
}

// Operator represents an operator with precedence and associativity
type Operator struct {
	Symbol     string
	Precedence int
	RightAssoc bool
	Unary      bool
}

// operatorMap defines all operators with their precedence (lower number = higher precedence)
var operatorMap = map[string]Operator{
	// Highest precedence - Unary operators
	"UN+":   {Symbol: "+", Precedence: 1, RightAssoc: true, Unary: true},
	"UN-":   {Symbol: "-", Precedence: 1, RightAssoc: true, Unary: true},
	"UNnot": {Symbol: "not", Precedence: 1, RightAssoc: true, Unary: true},
	"UN~":   {Symbol: "~", Precedence: 1, RightAssoc: true, Unary: true},

	// Array/Object operators
	"[": {Symbol: "[", Precedence: 1, RightAssoc: false, Unary: false},
	".": {Symbol: ".", Precedence: 1, RightAssoc: false, Unary: false},

	// Array Range and Arithmetic multiplication/division
	"..": {Symbol: "..", Precedence: 2, RightAssoc: false, Unary: false},
	"*":  {Symbol: "*", Precedence: 2, RightAssoc: false, Unary: false},
	"/":  {Symbol: "/", Precedence: 2, RightAssoc: false, Unary: false},
	"//": {Symbol: "//", Precedence: 2, RightAssoc: false, Unary: false},
	"%":  {Symbol: "%", Precedence: 2, RightAssoc: false, Unary: false},

	// Arithmetic addition/subtraction
	"+": {Symbol: "+", Precedence: 3, RightAssoc: false, Unary: false},
	"-": {Symbol: "-", Precedence: 3, RightAssoc: false, Unary: false},

	// Bitwise Shift
	"<<":  {Symbol: "<<", Precedence: 4, RightAssoc: false, Unary: false},
	">>":  {Symbol: ">>", Precedence: 4, RightAssoc: false, Unary: false},
	">>>": {Symbol: ">>>", Precedence: 4, RightAssoc: false, Unary: false},

	// Comparison
	"<":  {Symbol: "<", Precedence: 5, RightAssoc: false, Unary: false},
	"<=": {Symbol: "<=", Precedence: 5, RightAssoc: false, Unary: false},
	">":  {Symbol: ">", Precedence: 5, RightAssoc: false, Unary: false},
	">=": {Symbol: ">=", Precedence: 5, RightAssoc: false, Unary: false},

	// Equality
	"=":  {Symbol: "=", Precedence: 6, RightAssoc: false, Unary: false},
	"!=": {Symbol: "!=", Precedence: 6, RightAssoc: false, Unary: false},

	// Bitwise AND
	"&": {Symbol: "&", Precedence: 7, RightAssoc: false, Unary: false},

	// Bitwise XOR
	"^": {Symbol: "^", Precedence: 8, RightAssoc: false, Unary: false},

	// Bitwise OR
	"|": {Symbol: "|", Precedence: 9, RightAssoc: false, Unary: false},

	// Logical AND
	"and": {Symbol: "and", Precedence: 10, RightAssoc: false, Unary: false},

	// Logical OR and Nullish Coalescing
	"or": {Symbol: "or", Precedence: 11, RightAssoc: false, Unary: false},
	"??": {Symbol: "??", Precedence: 11, RightAssoc: false, Unary: false},

	// Lowest precedence - Conditional Ternary
	"?": {Symbol: "?", Precedence: 12, RightAssoc: true, Unary: false},
}

// Export a copy of the operatorMap when needed
func GetOperatorMap() map[string]Operator {
	copy := make(map[string]Operator)
	for k, v := range operatorMap {
		copy[k] = v
	}
	return copy
}

// ExpressionParser represents the expression parser
type ExpressionParser struct {
	input    string
	tokens   []Token
	position int
}

// NewExpressionParser creates a new expression parser
func NewExpressionParser(input string) *ExpressionParser {
	return &ExpressionParser{
		input:    input,
		tokens:   []Token{},
		position: 0,
	}
}

// Tokenize converts the input string into tokens
func (p *ExpressionParser) Tokenize() error {
	i := 0
	input := []rune(p.input)

	for i < len(input) {
		char := input[i]

		// Skip whitespace
		if unicode.IsSpace(char) {
			i++
			continue
		}

		// Numbers (integer and float)
		if unicode.IsDigit(char) {
			start := i
			for i < len(input) && (unicode.IsDigit(input[i]) || input[i] == '.') {
				i++
			}
			p.tokens = append(p.tokens, Token{
				Type:     TokenNumber,
				Value:    string(input[start:i]),
				Position: start,
			})
			continue
		}

		// String literals
		if char == '"' || char == '\'' {
			quote := char
			start := i
			i++ // Skip opening quote
			value := ""

			for i < len(input) && input[i] != quote {
				if input[i] == '\\' && i+1 < len(input) {
					// Handle escape sequences
					i++
					switch input[i] {
					case 'n':
						value += "\n"
					case 't':
						value += "\t"
					case 'r':
						value += "\r"
					case '\\':
						value += "\\"
					case '"':
						value += "\""
					case '\'':
						value += "'"
					default:
						value += string(input[i])
					}
				} else {
					value += string(input[i])
				}
				i++
			}

			if i >= len(input) {
				return fmt.Errorf("unterminated string literal at position %d", start)
			}

			i++ // Skip closing quote
			p.tokens = append(p.tokens, Token{
				Type:     TokenString,
				Value:    value,
				Position: start,
			})
			continue
		}

		// Multi-character operators
		if i+2 < len(input) {
			op3 := string(input[i : i+3])
			if _, exists := operatorMap[op3]; exists {
				p.tokens = append(p.tokens, Token{
					Type:     TokenOperator,
					Value:    op3,
					Position: i,
				})
				i += 3
				continue
			}
		}

		if i+1 < len(input) {
			op2 := string(input[i : i+2])
			if _, exists := operatorMap[op2]; exists {
				p.tokens = append(p.tokens, Token{
					Type:     TokenOperator,
					Value:    op2,
					Position: i,
				})
				i += 2
				continue
			}
		}

		// Single character operators and punctuation
		switch char {
		case '+', '-', '*', '/', '%', '<', '>', '=', '!', '&', '|', '^', '~':
			p.tokens = append(p.tokens, Token{
				Type:     TokenOperator,
				Value:    string(char),
				Position: i,
			})
			i++
		case '(':
			p.tokens = append(p.tokens, Token{
				Type:     TokenLeftParen,
				Value:    "(",
				Position: i,
			})
			i++
		case ')':
			p.tokens = append(p.tokens, Token{
				Type:     TokenRightParen,
				Value:    ")",
				Position: i,
			})
			i++
		case '[':
			p.tokens = append(p.tokens, Token{
				Type:     TokenLeftBracket,
				Value:    "[",
				Position: i,
			})
			i++
		case ']':
			p.tokens = append(p.tokens, Token{
				Type:     TokenRightBracket,
				Value:    "]",
				Position: i,
			})
			i++
		case '.':
			p.tokens = append(p.tokens, Token{
				Type:     TokenDot,
				Value:    ".",
				Position: i,
			})
			i++
		case '?':
			p.tokens = append(p.tokens, Token{
				Type:     TokenQuestion,
				Value:    "?",
				Position: i,
			})
			i++
		case ':':
			p.tokens = append(p.tokens, Token{
				Type:     TokenColon,
				Value:    ":",
				Position: i,
			})
			i++
		case ',':
			p.tokens = append(p.tokens, Token{
				Type:     TokenComma,
				Value:    ",",
				Position: i,
			})
			i++
		default:
			// Identifiers and keywords
			if unicode.IsLetter(char) || char == '_' {
				start := i
				for i < len(input) && (unicode.IsLetter(input[i]) || unicode.IsDigit(input[i]) || input[i] == '_') {
					i++
				}
				value := string(input[start:i])

				// Check for boolean literals
				if value == "true" || value == "false" {
					p.tokens = append(p.tokens, Token{
						Type:     TokenBoolean,
						Value:    value,
						Position: start,
					})
				} else if value == "null" || value == "nil" || value == "undefined" {
					p.tokens = append(p.tokens, Token{
						Type:     TokenNull,
						Value:    value,
						Position: start,
					})
				} else {
					p.tokens = append(p.tokens, Token{
						Type:     TokenIdentifier,
						Value:    value,
						Position: start,
					})
				}
			} else {
				return fmt.Errorf("unexpected character '%c' at position %d", char, i)
			}
		}
	}

	// Add EOF token
	p.tokens = append(p.tokens, Token{
		Type:     TokenEOF,
		Value:    "",
		Position: len(p.input),
	})

	return nil
}

// GetTokens returns the parsed tokens
func (p *ExpressionParser) GetTokens() []Token {
	return p.tokens
}

// ToPostfix converts the tokenized expression to postfix notation using Shunting Yard algorithm
func (p *ExpressionParser) ToPostfix() ([]Token, error) {
	if len(p.tokens) == 0 {
		return nil, fmt.Errorf("no tokens to process")
	}

	var output []Token
	var operatorStack []Token

	for i, token := range p.tokens {
		if token.Type == TokenEOF {
			break
		}

		switch token.Type {
		case TokenNumber, TokenString, TokenIdentifier, TokenBoolean, TokenNull:
			output = append(output, token)

		case TokenLeftParen:
			operatorStack = append(operatorStack, token)

		case TokenRightParen:
			// Pop operators until we find the left parenthesis
			for len(operatorStack) > 0 && operatorStack[len(operatorStack)-1].Type != TokenLeftParen {
				output = append(output, operatorStack[len(operatorStack)-1])
				operatorStack = operatorStack[:len(operatorStack)-1]
			}
			if len(operatorStack) == 0 {
				return nil, fmt.Errorf("mismatched parentheses")
			}
			// Remove the left parenthesis
			operatorStack = operatorStack[:len(operatorStack)-1]

		case TokenOperator, TokenQuestion, TokenDot:
			// Handle unary operators
			if i == 0 || p.tokens[i-1].Type == TokenLeftParen || p.tokens[i-1].Type == TokenOperator {
				if token.Value == "+" || token.Value == "-" || token.Value == "not" || token.Value == "~" {
					token.Value = "UN" + token.Value // Mark as unary
				}
			}

			currentOp, exists := operatorMap[token.Value]
			if !exists {
				return nil, fmt.Errorf("unknown operator: %s", token.Value)
			}

			// Pop operators with higher or equal precedence
			for len(operatorStack) > 0 {
				top := operatorStack[len(operatorStack)-1]
				if top.Type != TokenOperator && top.Type != TokenQuestion && top.Type != TokenDot {
					break
				}

				topOp, exists := operatorMap[top.Value]
				if !exists {
					break
				}

				if (!currentOp.RightAssoc && topOp.Precedence <= currentOp.Precedence) ||
					(currentOp.RightAssoc && topOp.Precedence < currentOp.Precedence) {
					output = append(output, top)
					operatorStack = operatorStack[:len(operatorStack)-1]
				} else {
					break
				}
			}

			operatorStack = append(operatorStack, token)

		case TokenLeftBracket:
			// Array access - treat as operator
			operatorStack = append(operatorStack, token)

		case TokenRightBracket:
			// Pop until we find the left bracket
			for len(operatorStack) > 0 && operatorStack[len(operatorStack)-1].Type != TokenLeftBracket {
				output = append(output, operatorStack[len(operatorStack)-1])
				operatorStack = operatorStack[:len(operatorStack)-1]
			}
			if len(operatorStack) == 0 {
				return nil, fmt.Errorf("mismatched brackets")
			}
			// Remove the left bracket and add array access operator
			operatorStack = operatorStack[:len(operatorStack)-1]
			output = append(output, Token{Type: TokenOperator, Value: "[]", Position: token.Position})
		}
	}

	// Pop remaining operators
	for len(operatorStack) > 0 {
		top := operatorStack[len(operatorStack)-1]
		if top.Type == TokenLeftParen || top.Type == TokenRightParen {
			return nil, fmt.Errorf("mismatched parentheses")
		}
		output = append(output, top)
		operatorStack = operatorStack[:len(operatorStack)-1]
	}

	return output, nil
}

// EvaluationStack represents a stack for expression evaluation
type EvaluationStack struct {
	tokens []Token
}

// NewEvaluationStack creates a new evaluation stack
func NewEvaluationStack(postfixTokens []Token) *EvaluationStack {
	return &EvaluationStack{
		tokens: postfixTokens,
	}
}

// GetTokens returns the tokens in the evaluation stack
func (es *EvaluationStack) GetTokens() []Token {
	return es.tokens
}

// String returns a string representation of the evaluation stack
func (es *EvaluationStack) String() string {
	var parts []string
	for _, token := range es.tokens {
		parts = append(parts, fmt.Sprintf("%s:%s", tokenTypeString(token.Type), token.Value))
	}
	return strings.Join(parts, " ")
}

// ToString converts the postfix expression back to a parenthesized infix representation
func (es *EvaluationStack) ToString() string {
	if len(es.tokens) == 0 {
		return ""
	}

	var stack []string
	for _, token := range es.tokens {
		switch token.Type {
		case TokenNumber, TokenIdentifier, TokenBoolean, TokenNull:
			stack = append(stack, token.Value)
		case TokenString:
			// Wrap string values in quotes
			stack = append(stack, fmt.Sprintf("'%s'", token.Value))
		case TokenOperator, TokenDot:
			if len(stack) < 2 {
				// Handle unary operators
				if token.Value == "UN+" || token.Value == "UN-" || token.Value == "UNnot" || token.Value == "UN~" {
					if len(stack) >= 1 {
						operand := stack[len(stack)-1]
						stack = stack[:len(stack)-1]
						op := strings.TrimPrefix(token.Value, "UN")
						stack = append(stack, fmt.Sprintf("(%s%s)", op, operand))
					}
				} else {
					stack = append(stack, token.Value)
				}
			} else {
				// Binary operator
				right := stack[len(stack)-1]
				left := stack[len(stack)-2]
				stack = stack[:len(stack)-2]

				// Handle special operators
				switch token.Value {
				case "[]":
					// Array access: left[right]
					stack = append(stack, fmt.Sprintf("%s[%s]", left, right))
				case ".":
					// Object property access: left.right
					stack = append(stack, fmt.Sprintf("%s.%s", left, right))
				default:
					// Regular binary operator
					switch token.Value {
					case "and":
						stack = append(stack, fmt.Sprintf("(%s&&%s)", left, right))
					case "or":
						stack = append(stack, fmt.Sprintf("(%s||%s)", left, right))
					default:
						stack = append(stack, fmt.Sprintf("(%s%s%s)", left, token.Value, right))
					}
				}
			}
		}
	}

	if len(stack) == 1 {
		return stack[0]
	}

	// If we have multiple items on stack, join them
	return strings.Join(stack, "")
}

// ParseExpression parses an expression and returns the evaluation stack
func ParseExpression(expression string) (*EvaluationStack, error) {
	// Phase 1: Process {{ }} sub-expressions first
	processedExpression, err := ProcessNestedExpressions(expression)
	if err != nil {
		return nil, fmt.Errorf("nested expression processing error: %w", err)
	}

	// Phase 2: Parse the processed expression normally
	parser := NewExpressionParser(processedExpression)

	err = parser.Tokenize()
	if err != nil {
		return nil, fmt.Errorf("tokenization error: %w", err)
	}

	postfixTokens, err := parser.ToPostfix()
	if err != nil {
		return nil, fmt.Errorf("postfix conversion error: %w", err)
	}

	return NewEvaluationStack(postfixTokens), nil
}

// NewExpression creates a new expression from a string
func NewExpression(expression string) (*EvaluationStack, error) {
	return ParseExpression(expression)
}

// ProcessNestedExpressions finds and evaluates {{ }} sub-expressions
func ProcessNestedExpressions(expression string) (string, error) {
	result := expression

	for {
		// Find the first {{ }} pair
		start := strings.Index(result, "{{")
		if start == -1 {
			break // No more nested expressions
		}

		end := strings.Index(result[start+2:], "}}")
		if end == -1 {
			return "", fmt.Errorf("unclosed nested expression starting at position %d", start)
		}
		end += start + 2 // Adjust for the offset

		// Extract the nested expression (without {{ }})
		nestedExpr := strings.TrimSpace(result[start+2 : end])
		if nestedExpr == "" {
			return "", fmt.Errorf("empty nested expression at position %d", start)
		}

		// Recursively evaluate the nested expression
		nestedResult, err := EvaluateNestedExpression(nestedExpr)
		if err != nil {
			return "", fmt.Errorf("error evaluating nested expression '{{ %s }}': %w", nestedExpr, err)
		}

		// Replace {{ expression }} with the evaluated result
		result = result[:start] + nestedResult + result[end+2:]
	}

	return result, nil
}

// EvaluateNestedExpression evaluates a nested expression and returns the string result
func EvaluateNestedExpression(expression string) (string, error) {
	// First recursively process any nested {{ }} within this expression
	processedExpr, err := ProcessNestedExpressions(expression)
	if err != nil {
		return "", err
	}

	// Create a parser for the nested expression
	parser := NewExpressionParser(processedExpr)

	err = parser.Tokenize()
	if err != nil {
		return "", fmt.Errorf("tokenization error in nested expression: %w", err)
	}

	postfixTokens, err := parser.ToPostfix()
	if err != nil {
		return "", fmt.Errorf("postfix conversion error in nested expression: %w", err)
	}

	// For now, return a placeholder evaluation
	// In a full implementation, you would evaluate the postfix tokens here
	// and return the actual computed value as a string
	evaluatedValue := EvaluatePostfixTokens(postfixTokens)

	return evaluatedValue, nil
}

// EvaluatePostfixTokens evaluates postfix tokens and returns the result as a string
// This is a simplified implementation - in practice, you'd implement full evaluation logic
func EvaluatePostfixTokens(tokens []Token) string {
	// Simplified evaluation for demonstration
	// In a real implementation, this would use a stack to evaluate the postfix expression

	if len(tokens) == 0 {
		return ""
	}

	// For simple cases, just return the first token if it's a literal
	if len(tokens) == 1 {
		token := tokens[0]
		switch token.Type {
		case TokenNumber, TokenString, TokenBoolean:
			return token.Value
		case TokenNull:
			return "null"
		default:
			return token.Value
		}
	}

	// For complex expressions, return a placeholder
	// TODO: Implement full postfix evaluation with operator stack
	return "[EVALUATED_EXPRESSION]"
}

// Helper function to convert token type to string for debugging
func tokenTypeString(t TokenType) string {
	switch t {
	case TokenNumber:
		return "NUM"
	case TokenString:
		return "STR"
	case TokenIdentifier:
		return "ID"
	case TokenOperator:
		return "OP"
	case TokenLeftParen:
		return "("
	case TokenRightParen:
		return ")"
	case TokenLeftBracket:
		return "["
	case TokenRightBracket:
		return "]"
	case TokenComma:
		return ","
	case TokenDot:
		return "."
	case TokenQuestion:
		return "?"
	case TokenColon:
		return ":"
	case TokenBoolean:
		return "BOOL"
	case TokenNull:
		return "NULL"
	case TokenEOF:
		return "EOF"
	default:
		return "UNKNOWN"
	}
}
