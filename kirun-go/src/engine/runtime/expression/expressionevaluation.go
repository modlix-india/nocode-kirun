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
	stack := make([]interface{}, 0)

	for _, token := range e.evaluationStack.tokens {
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
		if l, ok := left.(int); ok {
			if r, ok := right.(int); ok {
				return l * r, nil
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
	parts := strings.Split(s, ".")
	root, exists := extractors[parts[0]]
	if !exists {
		return nil, fmt.Errorf("undefined object: %s", parts[0])
	}

	value, err := root.GetValue(s)
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
