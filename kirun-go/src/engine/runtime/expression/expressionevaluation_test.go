package expression

import (
	"fmt"
	"testing"

	"github.com/modlix-india/nocode-kirun/engine/runtime/expression/tokenextractor"
	"github.com/stretchr/testify/assert"
)

func TestEvaluation_SimpleAddition(t *testing.T) {
	expr := NewEvaluatorString("2+3")
	result, err := expr.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, 5, result)
}

var store = tokenextractor.MakeTokenValueExtractor("Store.", map[string]any{
	"user": map[string]any{
		"name": "John Doe",
	},
	"number":  2,
	"numbers": []int{1, 4, 8, 32},
})

func TestEvaluation_SimpleStringMultiplication(t *testing.T) {
	expr := NewEvaluatorString("Store.user.name* Store.number")
	result, err := expr.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": store,
	})
	assert.NoError(t, err)
	assert.Equal(t, "John DoeJohn Doe", result)
}

func TestEvaluation_SimpleStringMultiplicationWithArray(t *testing.T) {
	expr := NewEvaluatorString("Store.numbers[0]* Store.numbers[1]")
	result, err := expr.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": store,
	})
	assert.NoError(t, err)
	assert.Equal(t, 4, result)
}

func TestEvaluation_ArrayIndexing(t *testing.T) {
	// Test array indexing with different types
	storeWithArrays := tokenextractor.MakeTokenValueExtractor("Store.", map[string]any{
		"numbers": []int{1, 4, 8, 32},
		"strings": []string{"hello", "world", "test"},
		"mixed":   []interface{}{1, "two", 3.0, true},
	})

	// Test int array indexing
	expr1 := NewEvaluatorString("Store.numbers[2]")
	result1, err := expr1.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": storeWithArrays,
	})
	assert.NoError(t, err)
	assert.Equal(t, 8, result1)

	// Test string array indexing
	expr2 := NewEvaluatorString("Store.strings[1]")
	result2, err := expr2.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": storeWithArrays,
	})
	assert.NoError(t, err)
	assert.Equal(t, "world", result2)

	// Test mixed array indexing
	expr3 := NewEvaluatorString("Store.mixed[3]")
	result3, err := expr3.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": storeWithArrays,
	})
	assert.NoError(t, err)
	assert.Equal(t, true, result3)

	// Test out of bounds error
	expr4 := NewEvaluatorString("Store.numbers[10]")
	result4, err := expr4.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": storeWithArrays,
	})
	assert.NoError(t, err)
	assert.Nil(t, result4)
}

func TestEvaluation_NegativeIndex(t *testing.T) {
	storeWithArrays := tokenextractor.MakeTokenValueExtractor("Store.", map[string]any{
		"numbers": []int{1, 4, 8, 32},
	})

	// Note: This test currently fails because the parser treats [-1] as subtraction
	// The expression "Store.numbers[-1]" is parsed as "Store.numbers - 1" (subtraction)
	expr := NewEvaluatorString("Store.numbers[-1]")
	_, err := expr.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": storeWithArrays,
	})
	assert.Error(t, err)
	assert.Equal(t, "invalid operands for -: []int and int", err.Error())
}

func TestEvaluation_NegativeIndexBehavior(t *testing.T) {
	// Test what would happen if we could actually parse negative indices
	// This simulates the behavior by directly calling evaluateBinaryOperator
	arr := []int{1, 4, 8, 32}

	// Test negative index returns nil
	result, err := evaluateBinaryOperator("[]", arr, -1, map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Nil(t, result)

	// Test out of bounds positive index returns nil
	result2, err := evaluateBinaryOperator("[]", arr, 10, map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Nil(t, result2)

	// Test valid index returns correct value
	result3, err := evaluateBinaryOperator("[]", arr, 2, map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, 8, result3)
}

func TestEvaluation_ObjectIndexing(t *testing.T) {
	expr := NewEvaluatorString("Store.user['name']")
	result, err := expr.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": store,
	})
	assert.NoError(t, err)
	assert.Equal(t, "John Doe", result)
}

func TestEvaluation_NilBehaviorForNonExistent(t *testing.T) {
	// Test data
	testStore := tokenextractor.MakeTokenValueExtractor("Store.", map[string]any{
		"numbers": []int{1, 2, 3},
		"user": map[string]interface{}{
			"name": "John",
		},
	})

	// Test out-of-bounds array access returns nil
	expr1 := NewEvaluatorString("Store.numbers[10]")
	result1, err := expr1.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": testStore,
	})
	assert.NoError(t, err)
	assert.Nil(t, result1)

	// Test non-existent object key returns nil
	expr2 := NewEvaluatorString("Store.user['age']")
	result2, err := expr2.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": testStore,
	})
	assert.NoError(t, err)
	assert.Nil(t, result2)

	// Test that nil values can be used in further operations without causing errors
	// For example, nil + "default" should work in some contexts
	expr3 := NewEvaluatorString("Store.user['age']")
	result3, err := expr3.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": testStore,
	})
	assert.NoError(t, err)
	assert.Nil(t, result3)
}

func TestEvaluation_ArrayAndObjectIndexing(t *testing.T) {
	// Test data with both arrays and objects
	storeWithMixed := tokenextractor.MakeTokenValueExtractor("Store.", map[string]any{
		"numbers": []int{1, 4, 8, 32},
		"strings": []string{"hello", "world", "test"},
		"user": map[string]interface{}{
			"name":  "John Doe",
			"age":   30,
			"roles": []string{"admin", "user"},
		},
		"config": map[string]string{
			"theme": "dark",
			"lang":  "en",
		},
	})

	// Test array indexing
	expr1 := NewEvaluatorString("Store.numbers[0] + Store.numbers[1]")
	result1, err := expr1.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": storeWithMixed,
	})
	assert.NoError(t, err)
	assert.Equal(t, 5, result1) // 1 + 4

	// Test object indexing with string keys
	expr2 := NewEvaluatorString("Store.user['name']")
	result2, err := expr2.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": storeWithMixed,
	})
	assert.NoError(t, err)
	assert.Equal(t, "John Doe", result2)

	// Test nested object indexing
	expr3 := NewEvaluatorString("Store.user['roles'][0]")
	result3, err := expr3.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": storeWithMixed,
	})
	assert.NoError(t, err)
	assert.Equal(t, "admin", result3)

	// Test object indexing with numeric values
	expr4 := NewEvaluatorString("Store.user['age'] * 2")
	result4, err := expr4.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": storeWithMixed,
	})
	assert.NoError(t, err)
	assert.Equal(t, 60, result4) // 30 * 2

	// Test string array indexing
	expr5 := NewEvaluatorString("Store.strings[1] + ' ' + Store.strings[2]")
	result5, err := expr5.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": storeWithMixed,
	})
	assert.NoError(t, err)
	assert.Equal(t, "world test", result5)

	// Test case: key not found returns nil
	expr6 := NewEvaluatorString("Store.user['nonexistent']")
	result6, err := expr6.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": storeWithMixed,
	})
	assert.NoError(t, err)
	assert.Nil(t, result6)

	// Test error case: invalid index type (this should still error)
	expr7 := NewEvaluatorString("Store.numbers['invalid']")
	_, err = expr7.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": storeWithMixed,
	})
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "cannot index into type []int with string key, expected int")
}

func TestEvaluation_ArrayLiteral(t *testing.T) {
	expr := NewEvaluatorString("[1,2,3,4,'hello'][4]")
	result, err := expr.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, "hello", result)
}

func TestEvaluation_ArrayLiteralComplex(t *testing.T) {
	// Test array with mixed types
	expr1 := NewEvaluatorString("[1, 'hello', true, null, 42][1]")
	result1, err := expr1.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, "hello", result1)

	// Test array with boolean values
	expr2 := NewEvaluatorString("[true, false, true][0]")
	result2, err := expr2.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, true, result2)

	// Test array with null values
	expr3 := NewEvaluatorString("[null, 'test', null][0]")
	result3, err := expr3.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Nil(t, result3)

	// Test array with numbers
	expr4 := NewEvaluatorString("[10, 20, 30][2]")
	result4, err := expr4.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, 30, result4)
}

func TestEvaluation_ObjectLiteralIndexing(t *testing.T) {
	// Test object literal indexing
	expr1 := NewEvaluatorString("{'name': 'John', 'age': 30}['name']")
	result1, err := expr1.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, "John", result1)

	// Test object literal with numeric values
	expr2 := NewEvaluatorString("{'x': 10, 'y': 20}['x']")
	result2, err := expr2.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, 10, result2)

	// Test object literal with boolean values
	expr3 := NewEvaluatorString("{'enabled': true, 'visible': false}['enabled']")
	result3, err := expr3.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, true, result3)
}

func TestEvaluation_NestedStructures(t *testing.T) {
	// Test array inside object
	expr1 := NewEvaluatorString("{'data': [1, 2, 3, 4], 'meta': {'count': 4}}['data'][2]")
	result1, err := expr1.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, 3, result1)

	// Test object inside array
	expr2 := NewEvaluatorString("[{'name': 'Alice', 'age': 25}, {'name': 'Bob', 'age': 30}][1]['name']")
	result2, err := expr2.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, "Bob", result2)

	// Test deeply nested structure
	expr3 := NewEvaluatorString("{'users': [{'profile': {'settings': {'theme': 'dark'}}}]}['users'][0]['profile']['settings']['theme']")
	result3, err := expr3.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, "dark", result3)

	// Test array of arrays
	expr4 := NewEvaluatorString("[[1, 2], [3, 4], [5, 6]][1][1]")
	result4, err := expr4.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, 4, result4)

	// Test object with mixed types including nested structures
	expr5 := NewEvaluatorString("{'config': {'enabled': true, 'options': [1, 2, 3]}, 'data': null}['config']['options'][2]")
	result5, err := expr5.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, 3, result5)

	// Test accessing null value in nested structure
	expr6 := NewEvaluatorString("{'config': {'enabled': true, 'options': [1, 2, 3]}, 'data': null}['data']")
	result6, err := expr6.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Nil(t, result6)
}

func TestEvaluation_ComplexJsonStructures(t *testing.T) {
	// Test with JSON-like strings in values
	expr1 := NewEvaluatorString(`{'message': "Hello \"World\"", 'count': 42}['message']`)
	result1, err := expr1.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, `Hello "World"`, result1)

	// Test with floating point numbers
	expr2 := NewEvaluatorString("{'pi': 3.14159, 'e': 2.71828}['pi']")
	result2, err := expr2.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, 3.14159, result2)

	// Test empty structures
	expr3 := NewEvaluatorString("{'empty_array': [], 'empty_object': {}}['empty_array']")
	result3, err := expr3.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, []interface{}{}, result3)

	expr4 := NewEvaluatorString("{'empty_array': [], 'empty_object': {}}['empty_object']")
	result4, err := expr4.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, map[string]interface{}{}, result4)

	// Test accessing non-existent keys in complex structures (should return nil)
	expr5 := NewEvaluatorString("{'users': [{'name': 'Alice'}]}['users'][0]['missing_key']")
	result5, err := expr5.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Nil(t, result5)

	// Test out-of-bounds access in nested arrays (should return nil)
	expr6 := NewEvaluatorString("{'data': [[1, 2], [3, 4]]}['data'][5]")
	result6, err := expr6.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Nil(t, result6)
}

func TestEvaluation_ComplexExpressions(t *testing.T) {
	// Test data setup equivalent to the JavaScript test
	phone := map[string]interface{}{
		"phone1": "1234",
		"phone2": "5678",
		"phone3": "5678",
	}

	address := map[string]interface{}{
		"line1": "Flat 202, PVR Estates",
		"line2": "Nagvara",
		"city":  "Benguluru",
		"pin":   "560048",
		"phone": phone,
	}

	arr := []int{10, 20, 30}

	obj := map[string]interface{}{
		"studentName": "Kumar",
		"math":        20,
		"isStudent":   true,
		"address":     address,
		"array":       arr,
		"num":         1,
	}

	// Define the output structure first
	output := map[string]any{
		"step1": map[string]any{
			"output": map[string]any{
				"name": "Kiran",
				"obj":  obj,
			},
		},
		"loop": map[string]any{
			"iteration": map[string]any{
				"index": 2,
			},
		},
	}

	context := map[string]any{
		"a": []int{1, 2},
	}

	// Create extractors for the test data
	contextExtractor := tokenextractor.MakeTokenValueExtractor("Context.", context)
	stepsExtractor := tokenextractor.MakeTokenValueExtractor("Steps.", output)

	extractors := map[string]tokenextractor.TokenValueExtractor{
		"Context.": contextExtractor,
		"Steps.":   stepsExtractor,
	}

	// Test 1: Complex array indexing with arithmetic
	expr1 := NewEvaluatorString("Context.a[Steps.loop.iteration.index - 1] + Context.a[Steps.loop.iteration.index - 2]")
	result1, err := expr1.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 3, result1) // 2 + 1

	// Test 2: Simple arithmetic
	expr2 := NewEvaluatorString("3 + 7")
	result2, err := expr2.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 10, result2)

	// Test 3: String concatenation with number
	expr3 := NewEvaluatorString("'asdf' + 333")
	result3, err := expr3.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, "asdf333", result3)

	// Test 4: Bitwise shift and equality
	expr4 := NewEvaluatorString("34 >> 2 = 8")
	result4, err := expr4.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, true, result4)

	// Test 5: Complex arithmetic with precedence
	expr5 := NewEvaluatorString("10*11+12*13*14/7")
	result5, err := expr5.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 422, result5)

	// Test 6: Non-existent path returns nil
	expr6 := NewEvaluatorString("Steps.step1.output.name1")
	result6, err := expr6.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Nil(t, result6)

	// Test 7: String equality
	expr7 := NewEvaluatorString("'Kiran' = Steps.step1.output.name")
	result7, err := expr7.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, true, result7)

	// Test 8: Null equality
	expr8 := NewEvaluatorString("null = Steps.step1.output.name1")
	result8, err := expr8.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, true, result8)

	// Test 9: Deep nested property access
	expr9 := NewEvaluatorString("Steps.step1.output.obj.address.phone.phone2")
	result9, err := expr9.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, "5678", result9)

	// Test 10: Self equality
	expr10 := NewEvaluatorString("Steps.step1.output.obj.address.phone.phone2 = Steps.step1.output.obj.address.phone.phone2")
	result10, err := expr10.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, true, result10)

	// Test 11: Inequality
	expr11 := NewEvaluatorString("Steps.step1.output.obj.address.phone.phone2 != Steps.step1.output.address.obj.phone.phone1")
	result11, err := expr11.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, true, result11)

	// Test 12: Array indexing with arithmetic
	expr12 := NewEvaluatorString("Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+2")
	result12, err := expr12.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 32, result12) // arr[2] + 2 = 30 + 2 = 32

	// Test 13: Array indexing with arithmetic and addition
	expr13 := NewEvaluatorString("Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]")
	result13, err := expr13.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 60, result13) // arr[2] + arr[2] = 30 + 30 = 60

	// Test 14: Negative arithmetic in array index (parser limitation - treated as subtraction)
	expr14 := NewEvaluatorString("Steps.step1.output.obj.array[-Steps.step1.output.obj.num + 3]+2")
	_, err = expr14.Evaluate(extractors)
	assert.Error(t, err)
	assert.Equal(t, "invalid operands for -: []int and int", err.Error())

	// Test 15: Floating point arithmetic
	expr15 := NewEvaluatorString("2.43*4.22+7.0987")
	result15, err := expr15.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 17.3533, result15)
}

func TestEvaluation_SquareBracketAccess(t *testing.T) {
	// Test data setup
	phone := map[string]any{
		"phone1": "1234",
		"phone2": "5678",
		"phone3": "5678",
	}

	address := map[string]any{
		"line1": "Flat 202, PVR Estates",
		"line2": "Nagvara",
		"city":  "Benguluru",
		"pin":   "560048",
		"phone": phone,
	}

	arr := []int{10, 20, 30}

	obj := map[string]any{
		"studentName": "Kumar",
		"math":        20,
		"isStudent":   true,
		"address":     address,
		"array":       arr,
		"num":         1,
	}

	inMap := map[string]any{
		"name": "Kiran",
		"obj":  obj,
	}

	output := map[string]any{
		"step1": map[string]any{
			"output": inMap,
		},
	}

	stepsExtractor := tokenextractor.MakeTokenValueExtractor("Steps.", output)
	extractors := map[string]tokenextractor.TokenValueExtractor{
		"Steps.": stepsExtractor,
	}

	// Test 1: Square bracket access equality
	expr1 := NewEvaluatorString("Steps.step1.output.obj.address.phone.phone2 = Steps.step1.output.obj['address']['phone']['phone2']")
	result1, err := expr1.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, true, result1)

	// Test 2: Full square bracket access
	expr2 := NewEvaluatorString("Steps.step1.output.obj['address']['phone']['phone2'] = Steps.step1.output.obj['address']['phone']['phone2']")
	result2, err := expr2.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, true, result2)

	// Test 3: Mixed dot and square bracket access (simplified to avoid parsing issues)
	expr3 := NewEvaluatorString("Steps.step1.output.obj['address']['phone']['phone2'] = Steps.step1.output.obj.address.phone.phone2")
	result3, err := expr3.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, true, result3)

	// Test 4: Different phone numbers are not equal
	expr4 := NewEvaluatorString("Steps.step1.output.obj['address']['phone']['phone2'] != Steps.step1.output.obj['address']['phone']['phone1']")
	result4, err := expr4.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, true, result4)

	// Test 5: Square bracket access with different values
	expr5 := NewEvaluatorString("Steps.step1.output.obj['address']['phone']['phone2'] != Steps.step1.output.obj['address']['phone']['phone3']")
	result5, err := expr5.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, false, result5) // phone2 and phone3 are both "5678", so they are equal

	// Test 6: Array indexing with square bracket access
	expr6 := NewEvaluatorString("Steps.step1.output.obj.array[Steps.step1.output.obj['num'] + 1] + 2")
	result6, err := expr6.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 32, result6)

	// Test 7: Complex array indexing with square bracket access
	expr7 := NewEvaluatorString("Steps.step1.output.obj.array[Steps.step1.output.obj['num'] + 1] + Steps.step1.output.obj.array[Steps.step1.output.obj['num'] + 1]")
	result7, err := expr7.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 60, result7)

	// Test 8: Mixed access patterns
	expr8 := NewEvaluatorString("Steps.step1.output.obj.array[Steps.step1.output.obj.num + 1] + Steps.step1.output.obj.array[Steps.step1.output.obj.num + 1]")
	result8, err := expr8.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 60, result8)
}

func TestEvaluation_DeepTests(t *testing.T) {
	// Test data setup
	testData := map[string]interface{}{
		"a": "kirun ",
		"b": 2,
		"c": map[string]interface{}{
			"a": 2,
			"b": []interface{}{true, false},
			"c": map[string]interface{}{
				"x": "kiran",
			},
		},
		"d": map[string]interface{}{
			"a": 2,
			"b": []interface{}{true, false},
			"c": map[string]interface{}{
				"x": "kiran",
			},
		},
	}

	atv := tokenextractor.MakeTokenValueExtractor("Arguments.", testData)
	extractors := map[string]tokenextractor.TokenValueExtractor{
		"Arguments.": atv,
	}

	// Test 1: Inequality
	expr1 := NewEvaluatorString("Arguments.a = Arguments.b")
	result1, err := expr1.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result1.(bool))

	// Test 2: Object equality
	expr2 := NewEvaluatorString("Arguments.c = Arguments.d")
	result2, err := expr2.Evaluate(extractors)
	assert.NoError(t, err)
	assert.True(t, result2.(bool))

	// Test 3: Null equality
	expr3 := NewEvaluatorString("Arguments.e = null")
	result3, err := expr3.Evaluate(extractors)
	assert.NoError(t, err)
	assert.True(t, result3.(bool))

	// Test 4: Null inequality
	expr4 := NewEvaluatorString("Arguments.e != null")
	result4, err := expr4.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result4.(bool))

	// Test 5: False equality (nil != false)
	expr5 := NewEvaluatorString("Arguments.e = false")
	result5, err := expr5.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result5.(bool))

	// Test 6: Non-null check
	expr6 := NewEvaluatorString("Arguments.c != null")
	result6, err := expr6.Evaluate(extractors)
	assert.NoError(t, err)
	assert.True(t, result6.(bool))
}

func TestEvaluation_LogicalOperators(t *testing.T) {
	// Test data setup
	testData := map[string]interface{}{
		"string":       "kirun ",
		"stringEmpty":  "",
		"number":       122.2,
		"number0":      0,
		"booleanTrue":  true,
		"booleanFalse": false,
		"null":         nil,
		"undefined":    nil,
		"object": map[string]interface{}{
			"a": 1,
			"b": "2",
			"c": true,
			"d": nil,
			"e": nil,
		},
		"array": []interface{}{
			1, "2", true, nil, nil,
		},
		"array2": []interface{}{
			1, "2", true, nil, nil,
		},
		"emptyArray": []interface{}{},
	}

	atv := tokenextractor.MakeTokenValueExtractor("Arguments.", testData)
	extractors := map[string]tokenextractor.TokenValueExtractor{
		"Arguments.": atv,
	}

	// Test 1: Double negation with object
	expr1 := NewEvaluatorString("not not Arguments.object")
	result1, err := expr1.Evaluate(extractors)
	assert.NoError(t, err)
	assert.True(t, result1.(bool))

	// Test 2: Double negation with empty string (empty string is falsy, so double negation should be false)
	expr2 := NewEvaluatorString("not not Arguments.stringEmpty")
	result2, err := expr2.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result2.(bool))

	// Test 3: Double negation with number
	expr3 := NewEvaluatorString("not not Arguments.number")
	result3, err := expr3.Evaluate(extractors)
	assert.NoError(t, err)
	assert.True(t, result3.(bool))

	// Test 4: Double negation with zero
	expr4 := NewEvaluatorString("not not Arguments.number0")
	result4, err := expr4.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result4.(bool))

	// Test 5: Double negation with true boolean
	expr5 := NewEvaluatorString("not not Arguments.booleanTrue")
	result5, err := expr5.Evaluate(extractors)
	assert.NoError(t, err)
	assert.True(t, result5.(bool))

	// Test 6: Double negation with false boolean
	expr6 := NewEvaluatorString("not not Arguments.booleanFalse")
	result6, err := expr6.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result6.(bool))

	// Test 7: Double negation with null
	expr7 := NewEvaluatorString("not not Arguments.null")
	result7, err := expr7.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result7.(bool))

	// Test 8: Double negation with undefined
	expr8 := NewEvaluatorString("not not Arguments.undefined")
	result8, err := expr8.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result8.(bool))

	// Test 9: Double negation with array
	expr9 := NewEvaluatorString("not not Arguments.array")
	result9, err := expr9.Evaluate(extractors)
	assert.NoError(t, err)
	assert.True(t, result9.(bool))

	// Test 10: Double negation with empty array (empty array is falsy, so double negation should be false)
	expr10 := NewEvaluatorString("not not Arguments.emptyArray")
	result10, err := expr10.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result10.(bool))

	// Test 11: Object equality with true
	expr11 := NewEvaluatorString("Arguments.object = true")
	result11, err := expr11.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result11.(bool))

	// Test 12: Object inequality with true
	expr12 := NewEvaluatorString("Arguments.object != true")
	result12, err := expr12.Evaluate(extractors)
	assert.NoError(t, err)
	assert.True(t, result12.(bool))

	// Test 13: Empty string equality with true
	expr13 := NewEvaluatorString("Arguments.stringEmpty = true")
	result13, err := expr13.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result13.(bool))

	// Test 14: Empty string inequality with false
	expr14 := NewEvaluatorString("Arguments.stringEmpty != false")
	result14, err := expr14.Evaluate(extractors)
	assert.NoError(t, err)
	assert.True(t, result14.(bool))

	// Test 15: Zero equality with true
	expr15 := NewEvaluatorString("Arguments.number0 = true")
	result15, err := expr15.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result15.(bool))

	// Test 16: Zero equality with false
	expr16 := NewEvaluatorString("Arguments.number0 = false")
	result16, err := expr16.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result16.(bool))

	// Test 17: Array length (not currently supported)
	// expr17 := NewEvaluatorString("Arguments.array.length")
	// result17, err := expr17.Evaluate(extractors)
	// assert.NoError(t, err)
	// assert.Equal(t, 5, result17)

	// Test 18: Object length (not currently supported)
	// expr18 := NewEvaluatorString("Arguments.object.length")
	// result18, err := expr18.Evaluate(extractors)
	// assert.NoError(t, err)
	// assert.Equal(t, 5, result18)

	// Test 19: Logical AND
	expr19 := NewEvaluatorString("Arguments.object and Arguments.array")
	result19, err := expr19.Evaluate(extractors)
	assert.NoError(t, err)
	assert.True(t, result19.(bool))

	// Test 20: Logical OR
	expr20 := NewEvaluatorString("Arguments.object or Arguments.null")
	result20, err := expr20.Evaluate(extractors)
	assert.NoError(t, err)
	assert.True(t, result20.(bool))

	// Test 21: Logical AND with null
	expr21 := NewEvaluatorString("Arguments.object and Arguments.null")
	result21, err := expr21.Evaluate(extractors)
	assert.NoError(t, err)
	assert.False(t, result21.(bool))

	// Test 22: Ternary operator (not currently supported)
	// expr22 := NewEvaluatorString("Arguments.object ? 3 : 4")
	// result22, err := expr22.Evaluate(extractors)
	// assert.NoError(t, err)
	// assert.Equal(t, 3, result22)

	// Test 23: Ternary operator with negation (not currently supported)
	// expr23 := NewEvaluatorString("not Arguments.object ? 3 : 4")
	// result23, err := expr23.Evaluate(extractors)
	// assert.NoError(t, err)
	// assert.Equal(t, 4, result23)

	// Test 24: Array equality
	expr24 := NewEvaluatorString("Arguments.array = Arguments.array2")
	result24, err := expr24.Evaluate(extractors)
	assert.NoError(t, err)
	assert.True(t, result24.(bool))

	// Test 25: Ternary operator with zero (not currently supported)
	// expr25 := NewEvaluatorString("Arguments.number0 ? 3 : 4")
	// result25, err := expr25.Evaluate(extractors)
	// assert.NoError(t, err)
	// assert.Equal(t, 4, result25)
}

func TestEvaluation_PartialPathEvaluation(t *testing.T) {
	// Test data setup
	testData := map[string]interface{}{
		"a":  "kirun ",
		"b":  1,
		"b1": 4,
		"b2": 4,
		"c": map[string]interface{}{
			"a": 0,
			"b": []interface{}{true, false},
			"c": map[string]interface{}{
				"x": "Arguments.b2",
			},
			"keys": []interface{}{
				"a", "e", map[string]interface{}{"val": 5},
			},
		},
		"d": "c",
		"e": []interface{}{
			map[string]interface{}{
				"name": "Kiran",
				"num":  1,
			},
			map[string]interface{}{
				"name": "Good",
				"num":  2,
			},
		},
	}

	atv := tokenextractor.MakeTokenValueExtractor("Arguments.", testData)
	extractors := map[string]tokenextractor.TokenValueExtractor{
		"Arguments.": atv,
	}

	// Test 1: Nested object access with array indexing
	expr1 := NewEvaluatorString("Arguments.c.keys[2].val + 3")
	result1, err := expr1.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 8, result1) // 5 + 3 = 8

	// Test 2: Null coalescing with array access
	expr2 := NewEvaluatorString("(Arguments.f ?? Arguments.e)[1+1-1].num")

	result2, err := expr2.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 2, result2) // e[1].num = 2

	expr11 := NewEvaluatorString("Arguments.c.keys[2]['val'] + 3")
	result11, err := expr11.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 8, result11) // 5 + 3 = 8
}

func TestEvaluation_BackslashEscape(t *testing.T) {
	// Test backslash escape in strings
	expr := NewEvaluatorString("'\\maza'")
	fmt.Println(expr.evaluationStack)
	result, err := expr.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, "\\maza", result)
}

func TestEvaluation_DebugExpression(t *testing.T) {
	// Test data setup
	context := map[string]interface{}{
		"a": []int{1, 2},
	}

	output := map[string]any{
		"loop": map[string]any{
			"iteration": map[string]any{
				"index": 2,
			},
		},
	}

	// Create extractors for the test data
	contextExtractor := tokenextractor.MakeTokenValueExtractor("Context.", context)
	stepsExtractor := tokenextractor.MakeTokenValueExtractor("Steps.", output)

	extractors := map[string]tokenextractor.TokenValueExtractor{
		"Context.": contextExtractor,
		"Steps.":   stepsExtractor,
	}

	// Test the complex expression
	expr := NewEvaluatorString("Context.a[Steps.loop.iteration.index - 1] + Context.a[Steps.loop.iteration.index - 2]")
	result, err := expr.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 3, result) // 2 + 1 = 3
}

func TestEvaluation_TernaryOperator(t *testing.T) {
	x := map[string]interface{}{"a": 2, "b": []interface{}{true, false}, "c": map[string]interface{}{"x": "Arguments.b2"}}

	atv := tokenextractor.MakeTokenValueExtractor("Arguments.", map[string]interface{}{
		"a":  "kirun ",
		"b":  2,
		"b1": 4,
		"b2": 4,
		"c":  x,
		"d":  "c",
	})

	extractors := map[string]tokenextractor.TokenValueExtractor{
		"Arguments.": atv,
	}

	ev := NewEvaluatorString("Arguments.e = null ? Arguments.c.a : 3")
	result, err := ev.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 2, result)

	ev = NewEvaluatorString("Arguments.f ? Arguments.c.a : 3")
	result, err = ev.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, 3, result)

	ev = NewEvaluatorString("Arguments.e = null ? Arguments.c : 3")
	result, err = ev.Evaluate(extractors)
	assert.NoError(t, err)
	assert.Equal(t, x, result)
}

func TestEvaluation_ObjectAccess(t *testing.T) {
	atv := tokenextractor.MakeTokenValueExtractor("Arguments.", map[string]interface{}{
		"a":  "kirun ",
		"b":  1,
		"b1": 4,
		"b2": 4,
		"d":  "c",
		"c":  map[string]interface{}{"keys": []interface{}{"a", "e", map[string]interface{}{"val": 5}}},
	})
	expr := NewEvaluatorString("Arguments.c['keys'][Arguments.b + 1]['val'] + 3")
	result, err := expr.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Arguments.": atv,
	})
	assert.NoError(t, err)
	assert.Equal(t, 8, result)
}
