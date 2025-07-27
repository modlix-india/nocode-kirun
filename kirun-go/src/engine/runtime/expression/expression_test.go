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
	assert.Equal(t, "'1 and 2'", expr.ToString())
}

func TestStringConcatenation(t *testing.T) {
	expr, err := NewExpression("'1 and 2' + \"3 or 4\"")
	assert.NoError(t, err)
	assert.Equal(t, "('1 and 2'+'3 or 4')", expr.ToString())
}

func TestSubExpression(t *testing.T) {
	expr, err := NewExpression("{{ 1 + 2}} * 3")
	assert.NoError(t, err)

	// Debug: Print tokens and postfix tokens
	parser := NewExpressionParser("{{ 1 + 2}} * 3")
	err = parser.Tokenize()
	assert.NoError(t, err)

	assert.Equal(t, "((1+2)*3)", expr.ToString())
}
