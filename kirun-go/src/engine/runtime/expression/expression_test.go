package expression

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestSimpleAddition(t *testing.T) {
	parser := NewParser("2+3")

	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)

	assert.Equal(t, "(2+3)", TokensToString(tokens))
}

func TestComplexArithmeticWithDecimals(t *testing.T) {
	parser := NewParser("2.234 +3* 1.22243")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "(2.234+(3*1.22243))", TokensToString(tokens))
}

func TestComplexArithmeticWithDivision(t *testing.T) {
	parser := NewParser("10*11+12*13*14/7")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "((10*11)+(((12*13)*14)/7))", TokensToString(tokens))
}

func TestBitwiseShiftAndEquality(t *testing.T) {
	parser := NewParser("34 << 2 = 8")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "((34<<2)=8)", TokensToString(tokens))
}

func TestComplexContextAndStepsExpression(t *testing.T) {
	parser := NewParser("Context.a[Steps.loop.iteration.index - 1]+ Context.a[Steps.loop.iteration.index - 2]")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "(Context.a[(Steps.loop.iteration.index-1)]+Context.a[(Steps.loop.iteration.index-2)])", TokensToString(tokens))
}

func TestStepsWithArrayAccess(t *testing.T) {
	parser := NewParser("Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+2")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "(Steps.step1.output.obj.array[(Steps.step1.output.obj.num+1)]+2)", TokensToString(tokens))
}

func TestNestedArrayAccess(t *testing.T) {
	parser := NewParser("Context.a[Steps.loop.iteration.index][Steps.loop.iteration.index + 1]")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "Context.a[Steps.loop.iteration.index][(Steps.loop.iteration.index+1)]", TokensToString(tokens))
}

func TestDeepObjectAccess(t *testing.T) {
	parser := NewParser("Context.a.b.c")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	fmt.Println(tokens)
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "Context", Position: 0}, tokens[0])
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "a", Position: 8}, tokens[1])
	assert.Equal(t, Token{Type: TokenDot, Value: ".", Position: 7}, tokens[2])
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "b", Position: 10}, tokens[3])
	assert.Equal(t, Token{Type: TokenDot, Value: ".", Position: 9}, tokens[4])
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "c", Position: 12}, tokens[5])
	assert.Equal(t, Token{Type: TokenDot, Value: ".", Position: 11}, tokens[6])

	assert.Equal(t, "Context.a.b.c", TokensToString(tokens))
}

func TestDeepObjectWithArray(t *testing.T) {
	parser := NewParser("Context.a.b[2].c")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "Context.a.b[2].c", TokensToString(tokens))
}

func TestLogicalORWithSpaces(t *testing.T) {
	parser := NewParser("Store.a.b.c or Store.c.d.x")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "(Store.a.b.c||Store.c.d.x)", TokensToString(tokens))
}

func TestIdentifierWithOperatorInName(t *testing.T) {
	parser := NewParser("Store.a.b.corStore.c.d.x")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "Store.a.b.corStore.c.d.x", TokensToString(tokens))
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
	parser := NewParser("1 and 2")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "(1&&2)", TokensToString(tokens))
}

func TestStringExpression(t *testing.T) {
	parser := NewParser("'1 and 2'")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `"1 and 2"`, TokensToString(tokens))
}

func TestArrayLiteralTokenization(t *testing.T) {
	// Test simple array literal
	parser := NewParser("[1, 2, 3]")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "[1, 2, 3]", TokensToString(tokens))

	// Test array literal with strings
	parser = NewParser(`["a", "b", "c"]`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `["a", "b", "c"]`, TokensToString(tokens))

	// Test array literal with mixed types
	parser = NewParser(`[1, "hello", true, null]`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `[1, "hello", true, null]`, TokensToString(tokens))

	// Test array literal with newlines in string values
	parser = NewParser(`["line1", "line2\nwith newline", "line3"]`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `["line1", "line2\nwith newline", "line3"]`, TokensToString(tokens))

	// Test nested array literal
	parser = NewParser(`[[1, 2], [3, 4]]`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `[[1, 2], [3, 4]]`, TokensToString(tokens))

	// Test nested array literal with newlines
	parser = NewParser(`[["item1\nwith newline", "item2"], ["item3", "item4\nanother newline"]]`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `[["item1\nwith newline", "item2"], ["item3", "item4\nanother newline"]]`, TokensToString(tokens))
}

func TestObjectLiteralTokenization(t *testing.T) {
	// Test simple object literal
	parser := NewParser(`{"key": "value"}`)
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `{"key": "value"}`, TokensToString(tokens))

	// Test object literal with multiple properties
	parser = NewParser(`{"name": "John", "age": 30, "active": true}`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `{"name": "John", "age": 30, "active": true}`, TokensToString(tokens))

	// Test object literal with newlines in string values
	parser = NewParser(`{"description": "This is a\nmulti-line\ndescription", "name": "John"}`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `{"description": "This is a\nmulti-line\ndescription", "name": "John"}`, TokensToString(tokens))

	// Test object literal with newlines in array values
	parser = NewParser(`{"tags": ["tag1", "tag2\nwith newline"], "count": 2}`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `{"tags": ["tag1", "tag2\nwith newline"], "count": 2}`, TokensToString(tokens))

	// Test nested object literal
	parser = NewParser(`{"user": {"name": "John", "settings": {"theme": "dark"}}}`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `{"user": {"name": "John", "settings": {"theme": "dark"}}}`, TokensToString(tokens))

	// Test object with array property
	parser = NewParser(`{"tags": ["tag1", "tag2"], "count": 2}`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `{"tags": ["tag1", "tag2"], "count": 2}`, TokensToString(tokens))
}

func TestArrayAccessVsArrayLiteral(t *testing.T) {
	// Test array access (should not be treated as literal)
	parser := NewParser("Context.array[0]")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "Context.array[0]", TokensToString(tokens))

	// Test array literal
	parser = NewParser("[1, 2, 3]")
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "[1, 2, 3]", TokensToString(tokens))

	// Test mixed: array literal in expression
	parser = NewParser("Context.array + [1, 2, 3]")
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "(Context.array+[1, 2, 3])", TokensToString(tokens))
}

func TestObjectAccessVsObjectLiteral(t *testing.T) {
	// Test object property access (should not be treated as literal)
	parser := NewParser("Context.user.name")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "Context.user.name", TokensToString(tokens))

	// Test object literal
	parser = NewParser(`{"name": "John"}`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `{"name": "John"}`, TokensToString(tokens))

	// Test mixed: object literal in expression
	parser = NewParser(`Context.user + {"name": "John"}`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `(Context.user+{"name": "John"})`, TokensToString(tokens))
}

func TestStringConcatenation(t *testing.T) {
	parser := NewParser("'1 and 2' + \"3 or 4\"")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `("1 and 2"+"3 or 4")`, TokensToString(tokens))
}

func TestStringLiteralsWithNewlines(t *testing.T) {
	// Test string literal with newline escape sequence
	parser := NewParser(`"Hello\nWorld"`)
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `"Hello\nWorld"`, TokensToString(tokens))

	// Test string literal with tab escape sequence
	parser = NewParser(`"Hello\tWorld"`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `"Hello\tWorld"`, TokensToString(tokens))

	// Test string literal with carriage return escape sequence
	parser = NewParser(`"Hello\rWorld"`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `"Hello\rWorld"`, TokensToString(tokens))

	// Test string literal with backslash escape sequence
	parser = NewParser(`"Hello\\World"`)
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, `"Hello\\World"`, TokensToString(tokens))
}

func TestSubExpression(t *testing.T) {
	parser := NewParser("{{ 1 + 2}} * 3")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)

	assert.Equal(t, "({{ 1 + 2}}*3)", TokensToString(tokens))
}

func TestExpressionWithIdentifiers(t *testing.T) {
	parser := NewParser("Store.user.name* 2")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "Store", Position: 0}, tokens[0])
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "user", Position: 6}, tokens[1])
	assert.Equal(t, Token{Type: TokenDot, Value: ".", Position: 5}, tokens[2])
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "name", Position: 11}, tokens[3])
	assert.Equal(t, Token{Type: TokenDot, Value: ".", Position: 10}, tokens[4])
	assert.Equal(t, Token{Type: TokenNumber, Value: "2", Position: 17}, tokens[5])
	assert.Equal(t, Token{Type: TokenOperator, Value: "*", Position: 15}, tokens[6])
}

func TestExpressionWithIdentifiersAndArrayOperator(t *testing.T) {
	parser := NewParser("Store.user.nos[  0 ] - Store.user.nos[ 1 ]")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)

	assert.Equal(t, Token{Type: TokenIdentifier, Value: "Store", Position: 0}, tokens[0])
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "user", Position: 6}, tokens[1])
	assert.Equal(t, Token{Type: TokenDot, Value: ".", Position: 5}, tokens[2])
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "nos", Position: 11}, tokens[3])
	assert.Equal(t, Token{Type: TokenDot, Value: ".", Position: 10}, tokens[4])
	assert.Equal(t, Token{Type: TokenNumber, Value: "0", Position: 17}, tokens[5])
	assert.Equal(t, Token{Type: TokenOperator, Value: "[]", Position: 19}, tokens[6])
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "Store", Position: 23}, tokens[7])
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "user", Position: 29}, tokens[8])
	assert.Equal(t, Token{Type: TokenDot, Value: ".", Position: 28}, tokens[9])
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "nos", Position: 34}, tokens[10])
	assert.Equal(t, Token{Type: TokenDot, Value: ".", Position: 33}, tokens[11])
	assert.Equal(t, Token{Type: TokenNumber, Value: "1", Position: 39}, tokens[12])
	assert.Equal(t, Token{Type: TokenOperator, Value: "[]", Position: 41}, tokens[13])
	assert.Equal(t, Token{Type: TokenOperator, Value: "-", Position: 21}, tokens[14])
}

func TestExpressionWithOnlyIdentifier(t *testing.T) {
	parser := NewParser("Store.user.nos")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "Store", Position: 0}, tokens[0])
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "user", Position: 6}, tokens[1])
	assert.Equal(t, Token{Type: TokenDot, Value: ".", Position: 5}, tokens[2])
	assert.Equal(t, Token{Type: TokenIdentifier, Value: "nos", Position: 11}, tokens[3])
	assert.Equal(t, Token{Type: TokenDot, Value: ".", Position: 10}, tokens[4])
}

// Now testing error cases

func TestTokenizationErrors(t *testing.T) {
	// Test unterminated sub-expression
	parser := NewParser("{{ 1 + 2")
	_, err := parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated sub-expression")

	// Test unterminated string literal
	parser = NewParser(`"Hello world`)
	_, err = parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated string literal")

	// Test unterminated array literal
	parser = NewParser("[1, 2, 3")
	_, err = parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated '[' at position 0")

	// Test unterminated object literal
	parser = NewParser(`{"name": "John"`)
	_, err = parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated object literal")

	// Test invalid object literal (doesn't start with quoted key)
	parser = NewParser("{name: 'John'}")
	_, err = parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "object literals must start with quoted keys")

	// Test unexpected character
	parser = NewParser("1 + 2 @ 3")
	_, err = parser.ToPostfix()
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
	parser := NewParser("{{ {{ {{ 1 + 2 }} }}")
	_, err := parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated sub-expression")

	// Test mixed nested expressions
	parser = NewParser("{{ 1 + {{ 2 + 3 }}")
	_, err = parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated sub-expression")
}

func TestArrayLiteralErrors(t *testing.T) {
	// Test nested array literal with mismatched brackets
	parser := NewParser("[[1, 2], [3, 4")
	_, err := parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated '[' at position 0")
}

func TestObjectLiteralErrors(t *testing.T) {
	// Test nested object literal with mismatched braces
	parser := NewParser(`{"user": {"name": "John"}`)
	_, err := parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated object literal")
}

func TestStringLiteralErrors(t *testing.T) {
	// Test unterminated string with escape sequence
	parser := NewParser(`"Hello\nWorld`)
	_, err := parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated string literal")
}

func TestComplexExpressionErrors(t *testing.T) {
	// Test complex expression with multiple errors
	parser := NewParser("(1 + 2 * [3, 4, 5")
	_, err := parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated '[' at position 9")
}

func TestWhitespaceAndFormattingErrors(t *testing.T) {
	// Test expression with invalid characters in identifiers
	parser := NewParser("Context.user@name")
	_, err := parser.ToPostfix()
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
	parser := NewParser("Store.user.name[2")
	_, err := parser.ToPostfix()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "unterminated '[' at position 15")
}

func TestStringMethods(t *testing.T) {
	parser := NewParser(`2 or false`)
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "(2||false)", TokensToString(tokens))
}

func TestPositiveUnaryOperator(t *testing.T) {
	parser := NewParser("+2")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "+(2)", TokensToString(tokens))
}

func TestTernaryOperator(t *testing.T) {
	parser := NewParser("a > 10 ? a > 15 ? a + 2 : a - 2 : a + 3")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "((a>10)?((a>15)?(a+2):(a-2)):(a+3))", TokensToString(tokens))

	parser = NewParser("a > 10 ? a - 2 : a + 3")
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "((a>10)?(a-2):(a+3))", TokensToString(tokens))

	parser = NewParser("a > 10 ? a - 2 : a + 3")
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "((a>10)?(a-2):(a+3))", TokensToString(tokens))
}

func TestArrayLiteralTokenization2(t *testing.T) {
	// Test simple array literal
	parser := NewParser("[1, 2, 3]")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "Array Literal", TokenName[tokens[0].Type])
}

func TestArrayLiteralObjectLiteralComplexLiterals(t *testing.T) {
	parser := NewParser("[1, 2, 3]")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "Array Literal", TokenName[tokens[0].Type])

	parser = NewParser("[1, 2, { 'x' :\"Value\", \"array\": [{ 'a' : null, 'b': true'}, 23] }]")
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "Array Literal", TokenName[tokens[0].Type])

	parser = NewParser("[1, 2, { 'x' :\"Value\", \"array\": [{ 'a' : null, 'b': true'}, 23] }][a+2]")
	tokens, err = parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "[1, 2, { 'x' :\"Value\", \"array\": [{ 'a' : null, 'b': true'}, 23] }][(a+2)]", TokensToString(tokens))
}

func TestArrayLiteralObjectLiteralComplexLiterals2(t *testing.T) {
	parser := NewParser("[true, 2, true][0]")
	tokens, err := parser.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "Array Literal", TokenName[tokens[0].Type])
	assert.Equal(t, "Number", TokenName[tokens[1].Type])
	assert.Equal(t, "Operator", TokenName[tokens[2].Type])
	assert.Equal(t, "[true, 2, true][0]", TokensToString(tokens))
}

func TestObjectLiteralTokenization2(t *testing.T) {
	expr1 := NewParser("{'name': 'John', 'age': 30}['name']")
	tokens, err := expr1.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "{'name': 'John', 'age': 30}[\"name\"]", TokensToString(tokens))
}

func TestDoubleUnaryOperator(t *testing.T) {

	expr1 := NewParser("--1")
	tokens, err := expr1.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "-(-(1))", TokensToString(tokens))

	expr1 = NewParser("not not 1")
	tokens, err = expr1.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "not(not(1))", TokensToString(tokens))
}

func TestUnaryOperators(t *testing.T) {
	expr1 := NewParser("not not Arguments.b")
	tokens, err := expr1.ToPostfix()
	assert.NoError(t, err)
	assert.Equal(t, "not(not(Arguments.b))", TokensToString(tokens))
}
