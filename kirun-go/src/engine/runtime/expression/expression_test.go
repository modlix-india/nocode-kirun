package expression

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestSimpleAddition(t *testing.T) {
	expr, err := NewExpression("2+3")
	assert.NoError(t, err)
	assert.Equal(t, "(2+3)", expr.ToString())
}

func TestComplexArithmeticWithDecimals(t *testing.T) {
	expr, err := NewExpression("2.234 +3* 1.22243")
	assert.NoError(t, err)
	assert.Equal(t, "(2.234+(3*1.22243))", expr.ToString())
}

func TestComplexArithmeticWithDivision(t *testing.T) {
	expr, err := NewExpression("10*11+12*13*14/7")
	assert.NoError(t, err)
	assert.Equal(t, "((10*11)+(((12*13)*14)/7))", expr.ToString())
}

func TestBitwiseShiftAndEquality(t *testing.T) {
	expr, err := NewExpression("34 << 2 = 8")
	assert.NoError(t, err)
	assert.Equal(t, "((34<<2)=8)", expr.ToString())
}

func TestComplexContextAndStepsExpression(t *testing.T) {
	expr, err := NewExpression("Context.a[Steps.loop.iteration.index - 1]+ Context.a[Steps.loop.iteration.index - 2]")
	assert.NoError(t, err)
	assert.Equal(t, "(Context.a[(Steps.loop.iteration.index-1)]+Context.a[(Steps.loop.iteration.index-2)])", expr.ToString())
}

func TestStepsWithArrayAccess(t *testing.T) {
	expr, err := NewExpression("Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+2")
	assert.NoError(t, err)
	assert.Equal(t, "(Steps.step1.output.obj.array[(Steps.step1.output.obj.num+1)]+2)", expr.ToString())
}

func TestNestedArrayAccess(t *testing.T) {
	expr, err := NewExpression("Context.a[Steps.loop.iteration.index][Steps.loop.iteration.index + 1]")
	assert.NoError(t, err)
	assert.Equal(t, "Context.a[Steps.loop.iteration.index][(Steps.loop.iteration.index+1)]", expr.ToString())
}

func TestDeepObjectAccess(t *testing.T) {
	expr, err := NewExpression("Context.a.b.c")
	assert.NoError(t, err)
	assert.Equal(t, "Context.a.b.c", expr.ToString())
}

func TestDeepObjectWithArray(t *testing.T) {
	expr, err := NewExpression("Context.a.b[2].c")
	assert.NoError(t, err)
	assert.Equal(t, "Context.a.b[2].c", expr.ToString())
}

func TestLogicalORWithSpaces(t *testing.T) {
	expr, err := NewExpression("Store.a.b.c or Store.c.d.x")
	assert.NoError(t, err)
	assert.Equal(t, "(Store.a.b.c||Store.c.d.x)", expr.ToString())
}

func TestIdentifierWithOperatorInName(t *testing.T) {
	expr, err := NewExpression("Store.a.b.corStore.c.d.x")
	assert.NoError(t, err)
	assert.Equal(t, "Store.a.b.corStore.c.d.x", expr.ToString())
}

func TestExpressionParser(t *testing.T) {
	parser := NewParser("1 + 2 * 3")
	err := parser.Tokenize()
	assert.NoError(t, err)

	expected := []Token{
		{Type: TokenNumber, Value: "1", Position: 0},
		{Type: TokenOperator, Value: "+", Position: 2},
		{Type: TokenNumber, Value: "2", Position: 4},
		{Type: TokenOperator, Value: "*", Position: 6},
		{Type: TokenNumber, Value: "3", Position: 8},
		{Type: TokenEOF, Value: "", Position: 9},
	}

	assert.Equal(t, expected, parser.GetTokens())
}

func TestExpressionParserWithAnd(t *testing.T) {
	expr, err := NewExpression("1 and 2")
	assert.NoError(t, err)
	assert.Equal(t, "(1&&2)", expr.ToString())
}

func TestStringExpression(t *testing.T) {
	expr, err := NewExpression("'1 and 2'")
	assert.NoError(t, err)
	assert.Equal(t, `"1 and 2"`, expr.ToString())
}

func TestArrayLiteralTokenization(t *testing.T) {
	// Test simple array literal
	expr, err := NewExpression("[1, 2, 3]")
	assert.NoError(t, err)
	assert.Equal(t, "[1, 2, 3]", expr.ToString())

	// Test array literal with strings
	expr, err = NewExpression(`["a", "b", "c"]`)
	assert.NoError(t, err)
	assert.Equal(t, `["a", "b", "c"]`, expr.ToString())

	// Test array literal with mixed types
	expr, err = NewExpression(`[1, "hello", true, null]`)
	assert.NoError(t, err)
	assert.Equal(t, `[1, "hello", true, null]`, expr.ToString())

	// Test array literal with newlines in string values
	expr, err = NewExpression(`["line1", "line2\nwith newline", "line3"]`)
	assert.NoError(t, err)
	assert.Equal(t, `["line1", "line2\nwith newline", "line3"]`, expr.ToString())

	// Test nested array literal
	expr, err = NewExpression(`[[1, 2], [3, 4]]`)
	assert.NoError(t, err)
	assert.Equal(t, `[[1, 2], [3, 4]]`, expr.ToString())

	// Test nested array literal with newlines
	expr, err = NewExpression(`[["item1\nwith newline", "item2"], ["item3", "item4\nanother newline"]]`)
	assert.NoError(t, err)
	assert.Equal(t, `[["item1\nwith newline", "item2"], ["item3", "item4\nanother newline"]]`, expr.ToString())
}

func TestObjectLiteralTokenization(t *testing.T) {
	// Test simple object literal
	expr, err := NewExpression(`{"key": "value"}`)
	assert.NoError(t, err)
	assert.Equal(t, `{"key": "value"}`, expr.ToString())

	// Test object literal with multiple properties
	expr, err = NewExpression(`{"name": "John", "age": 30, "active": true}`)
	assert.NoError(t, err)
	assert.Equal(t, `{"name": "John", "age": 30, "active": true}`, expr.ToString())

	// Test object literal with newlines in string values
	expr, err = NewExpression(`{"description": "This is a\nmulti-line\ndescription", "name": "John"}`)
	assert.NoError(t, err)
	assert.Equal(t, `{"description": "This is a\nmulti-line\ndescription", "name": "John"}`, expr.ToString())

	// Test object literal with newlines in array values
	expr, err = NewExpression(`{"tags": ["tag1", "tag2\nwith newline"], "count": 2}`)
	assert.NoError(t, err)
	assert.Equal(t, `{"tags": ["tag1", "tag2\nwith newline"], "count": 2}`, expr.ToString())

	// Test nested object literal
	expr, err = NewExpression(`{"user": {"name": "John", "settings": {"theme": "dark"}}}`)
	assert.NoError(t, err)
	assert.Equal(t, `{"user": {"name": "John", "settings": {"theme": "dark"}}}`, expr.ToString())

	// Test object with array property
	expr, err = NewExpression(`{"tags": ["tag1", "tag2"], "count": 2}`)
	assert.NoError(t, err)
	assert.Equal(t, `{"tags": ["tag1", "tag2"], "count": 2}`, expr.ToString())
}

func TestArrayAccessVsArrayLiteral(t *testing.T) {
	// Test array access (should not be treated as literal)
	expr, err := NewExpression("Context.array[0]")
	assert.NoError(t, err)
	assert.Equal(t, "Context.array[0]", expr.ToString())

	// Test array literal
	expr, err = NewExpression("[1, 2, 3]")
	assert.NoError(t, err)
	assert.Equal(t, "[1, 2, 3]", expr.ToString())

	// Test mixed: array literal in expression
	expr, err = NewExpression("Context.array + [1, 2, 3]")
	assert.NoError(t, err)
	assert.Equal(t, "(Context.array+[1, 2, 3])", expr.ToString())
}

func TestObjectAccessVsObjectLiteral(t *testing.T) {
	// Test object property access (should not be treated as literal)
	expr, err := NewExpression("Context.user.name")
	assert.NoError(t, err)
	assert.Equal(t, "Context.user.name", expr.ToString())

	// Test object literal
	expr, err = NewExpression(`{"name": "John"}`)
	assert.NoError(t, err)
	assert.Equal(t, `{"name": "John"}`, expr.ToString())

	// Test mixed: object literal in expression
	expr, err = NewExpression(`Context.user + {"name": "John"}`)
	assert.NoError(t, err)
	assert.Equal(t, `(Context.user+{"name": "John"})`, expr.ToString())
}

func TestStringConcatenation(t *testing.T) {
	expr, err := NewExpression("'1 and 2' + \"3 or 4\"")
	assert.NoError(t, err)
	assert.Equal(t, `("1 and 2"+"3 or 4")`, expr.ToString())
}

func TestStringLiteralsWithNewlines(t *testing.T) {
	// Test string literal with newline escape sequence
	expr, err := NewExpression(`"Hello\nWorld"`)
	assert.NoError(t, err)
	assert.Equal(t, `"Hello\nWorld"`, expr.ToString())

	// Test string literal with tab escape sequence
	expr, err = NewExpression(`"Hello\tWorld"`)
	assert.NoError(t, err)
	assert.Equal(t, `"Hello\tWorld"`, expr.ToString())

	// Test string literal with carriage return escape sequence
	expr, err = NewExpression(`"Hello\rWorld"`)
	assert.NoError(t, err)
	assert.Equal(t, `"Hello\rWorld"`, expr.ToString())

	// Test string literal with backslash escape sequence
	expr, err = NewExpression(`"Hello\\World"`)
	assert.NoError(t, err)
	assert.Equal(t, `"Hello\\World"`, expr.ToString())
}

func TestSubExpression(t *testing.T) {
	expr, err := NewExpression("{{ 1 + 2}} * 3")
	assert.NoError(t, err)

	// Debug: Print tokens and postfix tokens
	parser := NewParser("{{ 1 + 2}} * 3")
	err = parser.Tokenize()
	assert.NoError(t, err)

	assert.Equal(t, "({{ 1 + 2}}*3)", expr.ToString())
}

func TestExpressionWithIdentifiers(t *testing.T) {
	expr, err := NewExpression("Store.user.name* 2")
	assert.NoError(t, err)
	tokens := expr.GetTokens()
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "Store.user.name", Position: 0}, tokens[0])
	assert.Equal(t, Token{Type: TokenNumber, Value: "2", Position: 17}, tokens[1])
	assert.Equal(t, Token{Type: TokenOperator, Value: "*", Position: 15}, tokens[2])
}

func TestExpressionWithIdentifiersAndArrayOperator(t *testing.T) {
	expr, err := NewExpression("Store.user.nos[  0 ] - Store.user.nos[ 1 ]")
	assert.NoError(t, err)
	tokens := expr.GetTokens()
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "Store.user.nos", Position: 0}, tokens[0])
	assert.Equal(t, Token{Type: TokenNumber, Value: "0", Position: 17}, tokens[1])
	assert.Equal(t, Token{Type: TokenOperator, Value: "[]", Position: 19}, tokens[2])
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "Store.user.nos", Position: 23}, tokens[3])
	assert.Equal(t, Token{Type: TokenNumber, Value: "1", Position: 39}, tokens[4])
	assert.Equal(t, Token{Type: TokenOperator, Value: "[]", Position: 41}, tokens[5])
	assert.Equal(t, Token{Type: TokenOperator, Value: "-", Position: 21}, tokens[6])
}

func TestExpressionWithOnlyIdentifier(t *testing.T) {
	expr, err := NewExpression("Store.user.nos")
	assert.NoError(t, err)
	tokens := expr.GetTokens()
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "Store.user.nos", Position: 0}, tokens[0])
}

// Now testing error cases

func TestTokenizationErrors(t *testing.T) {
	// Test unterminated sub-expression
	_, err := NewExpression("{{ 1 + 2")
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated sub-expression")

	// Test unterminated string literal
	_, err = NewExpression(`"Hello world`)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated string literal")

	// Test unterminated array literal
	_, err = NewExpression("[1, 2, 3")
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated array literal")

	// Test unterminated object literal
	_, err = NewExpression(`{"name": "John"`)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated object literal")

	// Test invalid object literal (doesn't start with quoted key)
	_, err = NewExpression("{name: 'John'}")
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "object literals must start with quoted keys")

	// Test unexpected character
	_, err = NewExpression("1 + 2 @ 3")
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unexpected character '@'")
}

func TestPostfixConversionErrors(t *testing.T) {
	// Test mismatched parentheses
	parser := NewParser("(1 + 2")
	err := parser.Tokenize()
	assert.NoError(t, err)

	_, err = parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "mismatched parentheses")

	// Test mismatched parentheses (too many closing)
	parser = NewParser("1 + 2)")
	err = parser.Tokenize()
	assert.NoError(t, err)

	_, err = parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "mismatched parentheses")
}

func TestNestedExpressionErrors(t *testing.T) {
	// Test deeply nested sub-expressions
	_, err := NewExpression("{{ {{ {{ 1 + 2 }} }}")
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated sub-expression")

	// Test mixed nested expressions
	_, err = NewExpression("{{ 1 + {{ 2 + 3 }}")
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated sub-expression")
}

func TestArrayLiteralErrors(t *testing.T) {
	// Test nested array literal with mismatched brackets
	_, err := NewExpression("[[1, 2], [3, 4")
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated array literal")
}

func TestObjectLiteralErrors(t *testing.T) {
	// Test nested object literal with mismatched braces
	_, err := NewExpression(`{"user": {"name": "John"}`)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated object literal")
}

func TestStringLiteralErrors(t *testing.T) {
	// Test unterminated string with escape sequence
	_, err := NewExpression(`"Hello\nWorld`)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated string literal")
}

func TestComplexExpressionErrors(t *testing.T) {
	// Test complex expression with multiple errors
	_, err := NewExpression("(1 + 2 * [3, 4, 5")
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated array literal")
}

func TestWhitespaceAndFormattingErrors(t *testing.T) {
	// Test expression with invalid characters in identifiers
	_, err := NewExpression("Context.user@name")
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unexpected character '@'")
}

func TestMismatchBrackets(t *testing.T) {
	// Test mismatched brackets during postfix conversion
	parser := NewParser("Store.user.name[2 + 3] + 4]")
	err := parser.Tokenize()
	assert.NoError(t, err)

	_, err = parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "mismatched brackets")
}

func TestMissingEndingBracket(t *testing.T) {
	// Test missing ending bracket in array literal (not array access)
	_, err := NewExpression("Store.user.name[2")
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "tokenization error: unterminated array access at position 15")
}

func TestStringMethods(t *testing.T) {
	expr, _ := NewExpression(`2 or false`)
	assert.Equal(t, "Token[Number]: 2 Token[Boolean]: false Token[Operator]: or", expr.String())
}

func TestPositiveUnaryOperator(t *testing.T) {
	expr, err := NewExpression("+2")
	assert.NoError(t, err)
	assert.Equal(t, "(+2)", expr.ToString())
}

func TestAllBracketsToSubExpressions(t *testing.T) {

	parser := NewParser("Store.user.numbers[{{Store.user.number[(4 + Store.user.what) * 12]}}]")
	err := parser.Tokenize()
	assert.NoError(t, err)
	postfix, err := parser.ToPostfix()

	for _, token := range postfix {
		fmt.Printf("%15s: %s\n", TokenName[token.Type], token.Value)
	}
}
