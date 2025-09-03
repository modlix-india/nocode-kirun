package expression

import (
	"encoding/json"
	"fmt"
	"strings"
	"unicode"

	"github.com/modlix-india/nocode-kirun/engine/runtime/expression/tokenextractor"
)

type Evaluator struct {
	evaluationStack []Token
}

// NewEvaluator creates a new expression from a string
func NewEvaluator(expression string) (*Evaluator, error) {
	parser := NewParser(expression)

	tokens, err := parser.ToPostfix()
	if err != nil {
		return nil, err
	}
	return &Evaluator{evaluationStack: tokens}, nil
}

func NewEvaluatorString(expression string) *Evaluator {
	ev, _ := NewEvaluator(expression)
	return ev
}

// isTruthy checks if a value is considered "truthy" in a boolean context
func isTruthy(value interface{}) bool {
	if value == nil {
		return false
	}
	switch v := value.(type) {
	case bool:
		return v
	case string:
		return v != ""
	case int:
		return v != 0
	case float64:
		return v != 0
	case []interface{}:
		return len(v) > 0
	case map[string]interface{}:
		return len(v) > 0
	}
	return true // By default, non-nil values are truthy
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
	processedTokens := e.evaluationStack

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

		case TokenArrayLiteral:
			// Parse array literal
			array, err := parseArrayLiteral(token.Value)
			if err != nil {
				return nil, err
			}
			stack = append(stack, array)

		case TokenObjectLiteral:
			// Parse object literal
			obj, err := parseObjectLiteral(token.Value)
			if err != nil {
				return nil, err
			}
			stack = append(stack, obj)

		case TokenIdentifier:
			stack = append(stack, token)
		case TokenOperator, TokenDot:
			if token.Value == "" {
				return nil, fmt.Errorf("empty operator token found at evaluation")
			}

			// Handle special operators that aren't in operatorMap
			if token.Value == "[]" || token.Value == "." {
				if len(stack) < 2 {
					return nil, fmt.Errorf("invalid expression: not enough operands for array access operator []")
				}
				right := stack[len(stack)-1]
				left := stack[len(stack)-2]
				stack = stack[:len(stack)-2]

				result, err := evaluateArrayObjectAccess(left, right, extractors)
				if err != nil {
					return nil, err
				}
				stack = append(stack, result)
				continue
			}

			op, exists := operatorMap[token.Value]
			if !exists {
				return nil, fmt.Errorf("operator '%s' not found in operatorMap", token.Value)
			}
			if op.Unary {
				if len(stack) < 1 {
					return nil, fmt.Errorf("invalid expression: not enough operands for unary operator %s", op.Symbol)
				}
				operand := stack[len(stack)-1]
				stack = stack[:len(stack)-1]

				result, err := evaluateUnaryOperator(op.Symbol, operand, extractors)
				if err != nil {
					return nil, err
				}
				stack = append(stack, result)
			} else {
				if op.Symbol == "?" {
					if len(stack) < 3 {
						return nil, fmt.Errorf("invalid expression: not enough operands for ternary operator ?")
					}
					falseVal := stack[len(stack)-1]
					trueVal := stack[len(stack)-2]
					condition := stack[len(stack)-3]
					stack = stack[:len(stack)-3]

					if isTruthy(condition) {
						stack = append(stack, trueVal)
					} else {
						stack = append(stack, falseVal)
					}
				} else {
					if len(stack) < 2 {
						return nil, fmt.Errorf("invalid expression: not enough operands for operator %s", op.Symbol)
					}
					right := stack[len(stack)-1]
					left := stack[len(stack)-2]
					stack = stack[:len(stack)-2]

					result, err := evaluateBinaryOperator(op.Symbol, left, right, extractors)
					if err != nil {
						return nil, err
					}
					stack = append(stack, result)
				}
			}
		case TokenQuestion:
			// Handle ternary operator: expects 3 operands (condition, true_expr, false_expr)
			if len(stack) < 3 {
				return nil, fmt.Errorf("invalid expression: not enough operands for ternary operator ?")
			}
			falseVal := stack[len(stack)-1]
			trueVal := stack[len(stack)-2]
			condition := stack[len(stack)-3]
			stack = stack[:len(stack)-3]

			if isTruthy(condition) {
				stack = append(stack, trueVal)
			} else {
				stack = append(stack, falseVal)
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

func identiferValue(identifier string, extractors map[string]tokenextractor.TokenValueExtractor) interface{} {
	val, ok := extractors[identifier+"."]
	if !ok {
		return nil
	}
	return val.GetStore()
}

func evaluateUnaryOperator(op string, operand interface{}, extractors map[string]tokenextractor.TokenValueExtractor) (interface{}, error) {
	switch op {
	case "-":
		if v, ok := operand.(int); ok {
			return -v, nil
		}
		if v, ok := operand.(float64); ok {
			return -v, nil
		}
	case "+":
		if v, ok := operand.(int); ok {
			return v, nil
		}
		if v, ok := operand.(float64); ok {
			return v, nil
		}
	case "not":
		return !isTruthy(operand), nil
	case "~":
		if v, ok := operand.(int); ok {
			return ^v, nil
		}
	}
	return nil, fmt.Errorf("unsupported unary operator: %s", op)
}

func evaluateBinaryOperator(s string, left, right interface{}, extractors map[string]tokenextractor.TokenValueExtractor) (interface{}, error) {
	switch s {
	case "+":
		// Handle numeric addition
		if l, ok := left.(int); ok {
			if r, ok := right.(int); ok {
				return l + r, nil
			}
			if r, ok := right.(float64); ok {
				return float64(l) + r, nil
			}
		}
		if l, ok := left.(float64); ok {
			if r, ok := right.(float64); ok {
				return l + r, nil
			}
			if r, ok := right.(int); ok {
				return l + float64(r), nil
			}
		}

		// Handle string concatenation
		if l, ok := left.(string); ok {
			if r, ok := right.(string); ok {
				return l + r, nil
			}
			// String + number concatenation
			if r, ok := right.(int); ok {
				return l + fmt.Sprintf("%d", r), nil
			}
			if r, ok := right.(float64); ok {
				return l + fmt.Sprintf("%g", r), nil
			}
		}
		// Number + string concatenation
		if r, ok := right.(string); ok {
			if l, ok := left.(int); ok {
				return fmt.Sprintf("%d", l) + r, nil
			}
			if l, ok := left.(float64); ok {
				return fmt.Sprintf("%g", l) + r, nil
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
			if r, ok := right.(float64); ok {
				return float64(l) * r, nil
			}
		}
		if l, ok := left.(float64); ok {
			if r, ok := right.(float64); ok {
				return l * r, nil
			}
			if r, ok := right.(int); ok {
				return l * float64(r), nil
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

	// Comparison operators
	case "=":
		return compareEquals(left, right), nil
	case "!=":
		return !compareEquals(left, right), nil
	case "<":
		return compareNumbers(left, right, func(l, r int) bool { return l < r })
	case "<=":
		return compareNumbers(left, right, func(l, r int) bool { return l <= r })
	case ">":
		return compareNumbers(left, right, func(l, r int) bool { return l > r })
	case ">=":
		return compareNumbers(left, right, func(l, r int) bool { return l >= r })

	// Bitwise operators
	case ">>":
		if l, ok := left.(int); ok {
			if r, ok := right.(int); ok {
				return l >> r, nil
			}
		}
		return nil, fmt.Errorf("invalid operands for >>: %T and %T", left, right)
	case "<<":
		if l, ok := left.(int); ok {
			if r, ok := right.(int); ok {
				return l << r, nil
			}
		}
		return nil, fmt.Errorf("invalid operands for <<: %T and %T", left, right)

	// Logical operators
	case "and", "&&":
		return isTruthy(left) && isTruthy(right), nil
	case "or", "||":
		return isTruthy(left) || isTruthy(right), nil

	// Array/Object access
	case "[]":
		return evaluateArrayObjectAccess(left, right, extractors)

	case "??":
		if left == nil {
			return right, nil
		}
		return left, nil
	}
	return nil, fmt.Errorf("unsupported operator: '%s' (length: %d)", s, len(s))
}

func parseNumber(indexStr string) (interface{}, error) {
	// Check if it's a float
	if strings.Contains(indexStr, ".") {
		val := 0.0
		decimalPart := 0.0
		decimalDivisor := 1.0
		foundDecimal := false

		for _, c := range indexStr {
			if c == '.' {
				if foundDecimal {
					return nil, fmt.Errorf("invalid number: %s", indexStr)
				}
				foundDecimal = true
				continue
			}
			if !unicode.IsDigit(c) {
				return nil, fmt.Errorf("invalid number: %s", indexStr)
			}

			if foundDecimal {
				decimalDivisor *= 10
				decimalPart = decimalPart*10 + float64(c-'0')
			} else {
				val = val*10 + float64(c-'0')
			}
		}
		return val + decimalPart/decimalDivisor, nil
	}

	// Integer parsing
	val := 0
	for _, c := range indexStr {
		if !unicode.IsDigit(c) {
			return nil, fmt.Errorf("invalid number: %s", indexStr)
		}
		val = val*10 + int(c-'0')
	}
	return val, nil
}

// compareEquals compares two values for equality
func compareEquals(left, right interface{}) bool {
	if left == nil && right == nil {
		return true
	}
	if left == nil || right == nil {
		return false
	}

	// Handle same types
	switch l := left.(type) {
	case int:
		if r, ok := right.(int); ok {
			return l == r
		}
		if r, ok := right.(float64); ok {
			return float64(l) == r
		}
	case float64:
		if r, ok := right.(float64); ok {
			return l == r
		}
		if r, ok := right.(int); ok {
			return l == float64(r)
		}
	case string:
		if r, ok := right.(string); ok {
			return l == r
		}
	case bool:
		if r, ok := right.(bool); ok {
			return l == r
		}
	case []interface{}:
		if r, ok := right.([]interface{}); ok {
			if len(l) != len(r) {
				return false
			}
			for i := range l {
				if !compareEquals(l[i], r[i]) {
					return false
				}
			}
			return true
		}
	case map[string]interface{}:
		if r, ok := right.(map[string]interface{}); ok {
			if len(l) != len(r) {
				return false
			}
			for k, v := range l {
				if rv, exists := r[k]; !exists || !compareEquals(v, rv) {
					return false
				}
			}
			return true
		}
	}

	return false
}

// compareNumbers compares two numeric values using the provided comparison function
func compareNumbers(left, right interface{}, compare func(int, int) bool) (interface{}, error) {
	// Convert both to int for comparison
	var l, r int

	switch lv := left.(type) {
	case int:
		l = lv
	case float64:
		l = int(lv)
	default:
		return nil, fmt.Errorf("invalid left operand for comparison: %T", left)
	}

	switch rv := right.(type) {
	case int:
		r = rv
	case float64:
		r = int(rv)
	default:
		return nil, fmt.Errorf("invalid right operand for comparison: %T", right)
	}

	return compare(l, r), nil
}

// evaluateArrayObjectAccess handles array/object indexing
func evaluateArrayObjectAccess(left, rightIdentifier interface{}, extractors map[string]tokenextractor.TokenValueExtractor) (interface{}, error) {

	if left == nil {
		return nil, nil
	}

	var leftVal interface{} = left

	if leftT, ok := left.(Token); ok && leftT.Type == TokenIdentifier {
		leftVal = identiferValue(leftT.Value, extractors)
		if leftVal == nil {
			return nil, nil
		}
	}

	var right interface{}
	if rightT, ok := rightIdentifier.(Token); ok && rightT.Type == TokenIdentifier {
		right = rightT.Value
	} else {
		right = rightIdentifier
	}

	switch arr := leftVal.(type) {
	case []interface{}:
		// Array indexing
		index, ok := right.(int)
		if !ok {
			return nil, fmt.Errorf("cannot index into type []interface{} with %T key, expected int", right)
		}
		if index < 0 || index >= len(arr) {
			return nil, nil // Return nil for out of bounds access
		}
		return arr[index], nil

	case []string:
		index, ok := right.(int)
		if !ok {
			return nil, fmt.Errorf("cannot index into type []string with %T key, expected int", right)
		}
		if index < 0 || index >= len(arr) {
			return nil, nil // Return nil for out of bounds access
		}
		return arr[index], nil

	case []int:
		index, ok := right.(int)
		if !ok {
			return nil, fmt.Errorf("cannot index into type []int with %T key, expected int", right)
		}
		if index < 0 || index >= len(arr) {
			return nil, nil // Return nil for out of bounds access
		}
		return arr[index], nil

	case []float64:

		index, ok := right.(int)
		if !ok {
			return nil, fmt.Errorf("cannot index into type []float64 with %T key, expected int", right)
		}
		if index < 0 || index >= len(arr) {
			return nil, nil // Return nil for out of bounds access
		}
		return arr[index], nil

	case []bool:

		index, ok := right.(int)
		if !ok {
			return nil, fmt.Errorf("cannot index into type []bool with %T key, expected int", right)
		}
		if index < 0 || index >= len(arr) {
			return nil, nil // Return nil for out of bounds access
		}
		return arr[index], nil

	case map[string]interface{}:
		// Object indexing
		key, ok := right.(string)
		if !ok {
			return nil, fmt.Errorf("cannot index into type map[string]interface{} with %T key, expected string", right)
		}
		value, exists := arr[key]
		if !exists {
			return nil, nil // Return nil for non-existent keys
		}
		return value, nil

	default:
		return nil, fmt.Errorf("cannot index into type %T", left)
	}
}

// parseArrayLiteral parses an array literal string into a slice using JSON parsing
func parseArrayLiteral(value string) ([]interface{}, error) {
	if value == "" {
		return []interface{}{}, nil
	}

	// Normalize quotes: convert single quotes to double quotes for JSON compatibility
	normalizedValue := normalizeQuotes(value)

	// Add brackets if not present
	normalizedValue = "[" + normalizedValue + "]"

	var result []interface{}
	err := json.Unmarshal([]byte(normalizedValue), &result)
	if err != nil {
		return nil, fmt.Errorf("invalid array literal: %s (error: %v)", value, err)
	}

	// Convert float64 values that are actually integers back to int
	for i, v := range result {
		result[i] = normalizeJSONValue(v)
	}

	return result, nil
}

// parseObjectLiteral parses an object literal string into a map using JSON parsing
func parseObjectLiteral(value string) (map[string]interface{}, error) {
	if value == "" {
		return map[string]interface{}{}, nil
	}

	// Normalize quotes: convert single quotes to double quotes for JSON compatibility
	normalizedValue := normalizeQuotes(value)

	// Add braces if not present
	if !strings.HasPrefix(normalizedValue, "{") {
		normalizedValue = "{" + normalizedValue + "}"
	}

	var result map[string]interface{}
	err := json.Unmarshal([]byte(normalizedValue), &result)
	if err != nil {
		return nil, fmt.Errorf("invalid object literal: %s (error: %v)", value, err)
	}

	// Convert float64 values that are actually integers back to int
	for k, v := range result {
		result[k] = normalizeJSONValue(v)
	}

	return result, nil
}

// normalizeJSONValue converts JSON parsed values to more appropriate Go types
// Specifically, converts float64 values that are actually integers back to int
func normalizeJSONValue(value interface{}) interface{} {
	switch v := value.(type) {
	case float64:
		// Check if this float64 is actually an integer
		if v == float64(int(v)) {
			return int(v)
		}
		return v
	case []interface{}:
		// Recursively normalize arrays
		for i, item := range v {
			v[i] = normalizeJSONValue(item)
		}
		return v
	case map[string]interface{}:
		// Recursively normalize objects
		for k, item := range v {
			v[k] = normalizeJSONValue(item)
		}
		return v
	default:
		return v
	}
}

// normalizeQuotes converts single quotes to double quotes for JSON compatibility
// This handles both keys and values, being careful about escaped quotes
func normalizeQuotes(input string) string {
	var result strings.Builder
	inSingleQuote := false
	inDoubleQuote := false

	for i, char := range input {
		switch char {
		case '\'':
			if !inDoubleQuote {
				if inSingleQuote {
					// Check if this is an escaped single quote
					if i > 0 && input[i-1] == '\\' {
						result.WriteRune(char)
					} else {
						// End of single-quoted string
						result.WriteRune('"')
						inSingleQuote = false
					}
				} else {
					// Start of single-quoted string
					result.WriteRune('"')
					inSingleQuote = true
				}
			} else {
				// Inside double quotes, treat as literal
				result.WriteRune(char)
			}
		case '"':
			if !inSingleQuote {
				if inDoubleQuote {
					// Check if this is an escaped double quote
					if i > 0 && input[i-1] == '\\' {
						result.WriteRune(char)
					} else {
						// End of double-quoted string
						result.WriteRune(char)
						inDoubleQuote = false
					}
				} else {
					// Start of double-quoted string
					result.WriteRune(char)
					inDoubleQuote = true
				}
			} else {
				// Inside single quotes, escape the double quote
				result.WriteString("\\\"")
			}
		default:
			result.WriteRune(char)
		}
	}

	return result.String()
}
