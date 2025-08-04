package expression

import (
	"fmt"
	"strings"
	"unicode"

	"github.com/modlix-india/nocode-kirun/engine/runtime/expression/tokenextractor"
)

type Evaluator struct {
	evaluationStack *EvaluationStack
}

// NewEvaluator creates a new expression from a string
func NewEvaluator(expression string) (*Evaluator, error) {
	evaluationStack, err := ParseExpression(expression)
	if err != nil {
		return nil, err
	}
	return &Evaluator{evaluationStack: evaluationStack}, nil
}

// preprocessTokens combines identifier and dot tokens into single identifier tokens
func (e *Evaluator) preprocessTokens() []Token {
	tokens := e.evaluationStack.tokens
	var processedTokens []Token

	i := 0
	for i < len(tokens) {
		// Only start combining if we see an identifier
		if tokens[i].Type == TokenIdentifier {
			combinedValue := tokens[i].Value
			combinedPosition := tokens[i].Position
			i++
			// Combine .identifier pairs
			for i+1 < len(tokens) && tokens[i].Type == TokenDot && tokens[i+1].Type == TokenIdentifier {
				combinedValue += "." + tokens[i+1].Value
				i += 2
			}
			processedTokens = append(processedTokens, Token{
				Type:     TokenIdentifier,
				Value:    combinedValue,
				Position: combinedPosition,
			})
		} else if tokens[i].Type == TokenDot {
			// Skip dot tokens as they should be combined with identifiers
			i++
		} else {
			// For all other tokens (operators, parens, etc), just add as-is
			processedTokens = append(processedTokens, tokens[i])
			i++
		}
	}
	return processedTokens
}

// objects is a map of objects, where the key is the object name and the value is a map of properties
// the properties are, a map of property names and values,
// For example, if you use Store.user.name then objects would be:
//
//	objects = {
//		"Store": {
//			"user": {
//				"name": "John Doe"
//			}
//		}

func (e *Evaluator) Evaluate(extractors map[string]tokenextractor.TokenValueExtractor) (interface{}, error) {
	// Preprocess tokens to combine identifier and dot tokens into single identifiers
	processedTokens := e.preprocessTokens()

	stack := make([]interface{}, 0)

	for _, token := range processedTokens {
		switch token.Type {
		case TokenNumber:
			val, err := parseNumber(token.Value)
			if err != nil {
				return nil, err
			}
			stack = append(stack, val)

		case TokenString:
			stack = append(stack, token.Value)

		case TokenBoolean:
			val := token.Value == "true"
			stack = append(stack, val)

		case TokenNull:
			stack = append(stack, nil)

		case TokenIdentifier:
			val, err := resolveIdentifier(token.Value, extractors)
			if err != nil {
				return nil, fmt.Errorf("error resolving identifier: %w", err)
			}
			stack = append(stack, val)

		case TokenOperator:
			op := operatorMap[token.Value]
			if op.Unary {
				if len(stack) < 1 {
					return nil, fmt.Errorf("invalid expression: not enough operands for unary operator %s", op.Symbol)
				}
				operand := stack[len(stack)-1]
				stack = stack[:len(stack)-1]

				result, err := evaluateUnaryOperator(op.Symbol, operand)
				if err != nil {
					return nil, err
				}
				stack = append(stack, result)
			} else {
				if len(stack) < 2 {
					return nil, fmt.Errorf("invalid expression: not enough operands for operator %s", op.Symbol)
				}
				right := stack[len(stack)-1]
				left := stack[len(stack)-2]
				stack = stack[:len(stack)-2]

				result, err := evaluateBinaryOperator(op.Symbol, left, right)
				if err != nil {
					return nil, err
				}
				stack = append(stack, result)
			}
		default:
			return nil, fmt.Errorf("invalid token type: %v", token.Type)
		}
	}

	if len(stack) != 1 {
		return nil, fmt.Errorf("invalid expression: expected 1 result but got %d", len(stack))
	}
	return stack[0], nil
}

func evaluateUnaryOperator(op string, operand interface{}) (interface{}, error) {
	// For now, supporting unary minus
	switch op {
	case "-":
		if v, ok := operand.(int); ok {
			return -v, nil
		}
	case "+":
		if v, ok := operand.(int); ok {
			return v, nil
		}
	}
	return nil, fmt.Errorf("unsupported unary operator: %s", op)
}

func evaluateBinaryOperator(s string, left, right interface{}) (interface{}, error) {
	switch s {
	case "+":
		// Handle numeric addition
		if l, ok := left.(int); ok {
			if r, ok := right.(int); ok {
				return l + r, nil
			}
		}
		// Handle string concatenation
		if l, ok := left.(string); ok {
			if r, ok := right.(string); ok {
				return l + r, nil
			}
		}
		return nil, fmt.Errorf("invalid operands for +: %T and %T", left, right)
	case "-":
		if l, ok := left.(int); ok {
			if r, ok := right.(int); ok {
				return l - r, nil
			}
		}
		return nil, fmt.Errorf("invalid operands for -: %T and %T", left, right)
	case "*":
		// Handle numeric multiplication
		if l, ok := left.(int); ok {
			if r, ok := right.(int); ok {
				return l * r, nil
			}
		}
		// Handle string multiplication (string * number)
		if l, ok := left.(string); ok {
			if r, ok := right.(int); ok {
				if r < 0 {
					return nil, fmt.Errorf("cannot multiply string by negative number")
				}
				return strings.Repeat(l, r), nil
			}
		}
		// Handle string multiplication (number * string)
		if l, ok := left.(int); ok {
			if r, ok := right.(string); ok {
				if l < 0 {
					return nil, fmt.Errorf("cannot multiply string by negative number")
				}
				return strings.Repeat(r, l), nil
			}
		}
		return nil, fmt.Errorf("invalid operands for *: %T and %T", left, right)
	case "/":
		if l, ok := left.(int); ok {
			if r, ok := right.(int); ok {
				if r == 0 {
					return nil, fmt.Errorf("division by zero")
				}
				return l / r, nil
			}
		}
		return nil, fmt.Errorf("invalid operands for /: %T and %T", left, right)
		// ... (other operators as before)
	}
	return nil, fmt.Errorf("unsupported operator: %s", s)
}

func resolveIdentifier(s string, extractors map[string]tokenextractor.TokenValueExtractor) (interface{}, error) {
	// Find the extractor that has a prefix matching the identifier
	var matchingExtractor tokenextractor.TokenValueExtractor
	var matchingKey string
	for key, extractor := range extractors {
		// The key should end with a dot, so we need to match the prefix without the dot
		prefixWithoutDot := key[:len(key)-1]
		if strings.HasPrefix(s, prefixWithoutDot) {
			matchingExtractor = extractor
			matchingKey = key
			break
		}
	}

	if matchingExtractor == nil {
		return nil, fmt.Errorf("undefined object: %s", strings.Split(s, ".")[0])
	}

	// The GetValueInternal function expects the token to start with the prefix
	// So we need to prepend the prefix to the identifier
	tokenWithPrefix := matchingKey + s[len(matchingKey)-1:] // matchingKey ends with dot, so we skip the first character of s

	value, err := matchingExtractor.GetValue(tokenWithPrefix)
	if err != nil {
		return nil, fmt.Errorf("error resolving identifier: %w", err)
	}
	return value, nil
}

func parseNumber(indexStr string) (interface{}, error) {
	val := 0
	for _, c := range indexStr {
		if !unicode.IsDigit(c) {
			return nil, fmt.Errorf("invalid number: %s", indexStr)
		}
		val = val*10 + int(c-'0')
	}
	return val, nil
}
