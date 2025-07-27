package expression

import (
	"testing"

	"github.com/modlix-india/nocode-kirun/engine/runtime/expression/tokenextractor"
	"github.com/stretchr/testify/assert"
)

func TestEvaluation_SimpleAddition(t *testing.T) {
	expr, err := NewEvaluator("2+3")
	assert.NoError(t, err)
	result, err := expr.Evaluate(map[string]tokenextractor.TokenValueExtractor{})
	assert.NoError(t, err)
	assert.Equal(t, 5, result)
}

var store = tokenextractor.MakeTokenValueExtractor("Store.", map[string]any{
	"user": map[string]any{
		"name": "John Doe",
	},
})

func TestEvaluation_SimpleAdditionWithObjects(t *testing.T) {
	expr, err := NewEvaluator("Store.user.name * 2")
	assert.NoError(t, err)
	result, err := expr.Evaluate(map[string]tokenextractor.TokenValueExtractor{
		"Store.": store,
	})
	assert.NoError(t, err)
	assert.Equal(t, "John DoeJohn Doe", result)
}
