package expression

import (
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
	parser := NewExpressionParser("1 + 2 * 3")
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
	parser := NewExpressionParser("{{ 1 + 2}} * 3")
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
