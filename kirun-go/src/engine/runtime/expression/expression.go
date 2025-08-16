package expression

import (
	"fmt"
	"strings"
	"unicode"
)

// TokenType represents the type of token
type TokenType int

const (
	TokenNumber = iota
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
	TokenSubExpression
	TokenArrayLiteral
	TokenObjectLiteral
)

var TokenName = map[TokenType]string{
	TokenNumber:        "Number",
	TokenString:        "String",
	TokenIdentifier:    "Identifier",
	TokenOperator:      "Operator",
	TokenLeftParen:     "Left Parenthesis",
	TokenRightParen:    "Right Parenthesis",
	TokenLeftBracket:   "Left Bracket",
	TokenRightBracket:  "Right Bracket",
	TokenComma:         "Comma",
	TokenDot:           "Dot",
	TokenQuestion:      "Question",
	TokenColon:         "Colon",
	TokenBoolean:       "Boolean",
	TokenNull:          "Null",
	TokenEOF:           "EOF",
	TokenSubExpression: "Sub Expression",
	TokenArrayLiteral:  "Array Literal",
	TokenObjectLiteral: "Object Literal",
}

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

// GetOperatorMap Export a copy of the operatorMap when needed
func GetOperatorMap() map[string]Operator {
	c := make(map[string]Operator)
	for k, v := range operatorMap {
		c[k] = v
	}
	return c
}

// Parser represents the expression parser
type Parser struct {
	input    string
	tokens   []Token
	position int
}

// NewParser creates a new expression parser
func NewParser(input string) *Parser {
	return &Parser{
		input:    input,
		tokens:   []Token{},
		position: 0,
	}
}

// Tokenize converts the input string into tokens
func (p *Parser) Tokenize() error {
	i := 0
	input := []rune(p.input)

	for i < len(input) {
		char := input[i]

		// Handle sub-expressions: {{ ... }}
		if char == '{' && i+1 < len(input) && input[i+1] == '{' {
			start := i
			i += 2 // Skip '{{'
			braceCount := 1
			innerStart := i
			for i < len(input) {
				if input[i] == '{' && i+1 < len(input) && input[i+1] == '{' {
					braceCount++
					i += 2
					continue
				}
				if input[i] == '}' && i+1 < len(input) && input[i+1] == '}' {
					braceCount--
					if braceCount == 0 {
						break
					}
					i += 2
					continue
				}
				i++
			}
			if braceCount != 0 {
				return fmt.Errorf("unterminated sub-expression at position %d", start)
			}
			inner := string(input[innerStart:i])
			p.tokens = append(p.tokens, Token{
				Type:     TokenSubExpression,
				Value:    inner,
				Position: start,
			})
			i += 2 // Skip '}}'
			continue
		}

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
			// Check if this is an array literal or array access
			// Look ahead to see if the next non-whitespace character suggests a literal
			nextPos := i + 1
			for nextPos < len(input) && unicode.IsSpace(input[nextPos]) {
				nextPos++
			}

			// Check if this bracket is preceded by an identifier (indicating array access)
			isArrayAccess := false
			if i > 0 {
				prevPos := i - 1
				// Skip backwards over whitespace
				for prevPos >= 0 && unicode.IsSpace(input[prevPos]) {
					prevPos--
				}
				// If the previous character is alphanumeric, underscore, or dot, it's likely array access
				if prevPos >= 0 && (unicode.IsLetter(input[prevPos]) || unicode.IsDigit(input[prevPos]) || input[prevPos] == '_' || input[prevPos] == '$' || input[prevPos] == '.') {
					isArrayAccess = true
				}
			}

			if !isArrayAccess && nextPos < len(input) {
				nextChar := input[nextPos]
				// If the next char is a quote, number, or another bracket, it's likely a literal
				if nextChar == '"' || nextChar == '\'' || unicode.IsDigit(nextChar) || nextChar == '[' || nextChar == '{' {
					// This is an array literal - parse the entire literal
					start := i
					i++ // Skip opening bracket
					braceCount := 1

					for i < len(input) && braceCount > 0 {
						switch input[i] {
						case '[':
							braceCount++
						case ']':
							braceCount--
						}
						// Advance the pointer - we'll extract the raw content
						i++
					}

					if braceCount != 0 {
						return fmt.Errorf("unterminated array literal at position %d", start)
					}

					// Extract the array content (without the brackets)
					arrayContent := string(input[start+1 : i-1])
					p.tokens = append(p.tokens, Token{
						Type:     TokenArrayLiteral,
						Value:    arrayContent,
						Position: start,
					})
					continue
				}
			}

			// For array access, we need to validate bracket matching
			if isArrayAccess {
				start := i
				i++ // Skip opening bracket
				braceCount := 1

				// Track brackets to ensure proper matching
				for i < len(input) && braceCount > 0 {
					switch input[i] {
					case '[':
						braceCount++
					case ']':
						braceCount--
					}
					i++
				}

				if braceCount != 0 {
					return fmt.Errorf("unterminated array access at position %d", start)
				}

				// Go back to the opening bracket to tokenize it properly
				i = start
			}

			// Regular array access
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
		case '{':
			// Check if this is an object literal
			// Look ahead to see if the next non-whitespace character suggests a literal
			nextPos := i + 1
			for nextPos < len(input) && unicode.IsSpace(input[nextPos]) {
				nextPos++
			}

			if nextPos < len(input) {
				nextChar := input[nextPos]
				// If the next char is a quote, it's likely an object literal
				if nextChar == '"' || nextChar == '\'' {
					// This is an object literal - parse the entire literal
					start := i
					i++ // Skip opening brace
					braceCount := 1

					for i < len(input) && braceCount > 0 {
						if input[i] == '{' {
							braceCount++
						} else if input[i] == '}' {
							braceCount--
						}
						// Advance the pointer - we'll extract the raw content
						i++
					}

					if braceCount != 0 {
						return fmt.Errorf("unterminated object literal at position %d", start)
					}

					// Extract the object content (without the braces) - preserve all whitespace and newlines
					objectContent := string(input[start+1 : i-1])
					p.tokens = append(p.tokens, Token{
						Type:     TokenObjectLiteral,
						Value:    objectContent,
						Position: start,
					})
					continue
				}
			}

			// If not an object literal, treat as an error for now
			// (since we don't have other uses for '{' in the current grammar)
			return fmt.Errorf("unexpected character '{' at position %d - object literals must start with quoted keys", i)
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
				for i < len(input) && (unicode.IsLetter(input[i]) || unicode.IsDigit(input[i]) ||
					input[i] == '_' || input[i] == '$' || input[i] == '.') {
					i++
				}
				value := string(input[start:i])
				// Check for boolean/null literals
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
				continue // Ensure we don't fall through and parse the next char as a new identifier
			}
			return fmt.Errorf("unexpected character '%c' at position %d", char, i)
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
func (p *Parser) GetTokens() []Token {
	return p.tokens
}

// ToPostfix converts the tokenized expression to postfix notation using Shunting Yard algorithm
func (p *Parser) ToPostfix() ([]Token, error) {
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
		case TokenNumber, TokenString, TokenIdentifier, TokenBoolean, TokenNull, TokenSubExpression, TokenArrayLiteral, TokenObjectLiteral:
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
			// Remove the left bracket and add the array access operator
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
		parts = append(parts, fmt.Sprintf("Token[%s]: %s", TokenName[token.Type], token.Value))
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
			// Preserve the original string format with double quotes and escape sequences
			// We need to re-escape special characters and quotes to maintain the original format
			escapedValue := strings.ReplaceAll(token.Value, "\\", "\\\\")
			escapedValue = strings.ReplaceAll(escapedValue, "\"", "\\\"")
			escapedValue = strings.ReplaceAll(escapedValue, "\n", "\\n")
			escapedValue = strings.ReplaceAll(escapedValue, "\t", "\\t")
			escapedValue = strings.ReplaceAll(escapedValue, "\r", "\\r")
			stack = append(stack, fmt.Sprintf("\"%s\"", escapedValue))
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
		case TokenSubExpression:
			// Instead of parsing and stringifying, just output as {{...}}
			stack = append(stack, fmt.Sprintf("{{%s}}", token.Value))
		case TokenArrayLiteral:
			// Array literal: [content]
			stack = append(stack, fmt.Sprintf("[%s]", token.Value))
		case TokenObjectLiteral:
			// Object literal: {content}
			stack = append(stack, fmt.Sprintf("{%s}", token.Value))
		}
	}

	if len(stack) == 1 {
		return stack[0]
	}

	// If we have multiple items on the stack, join them
	return strings.Join(stack, "")
}

// ParseExpression parses an expression and returns the evaluation stack
func ParseExpression(expression string) (*EvaluationStack, error) {

	parser := NewParser(expression)

	err := parser.Tokenize()
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
