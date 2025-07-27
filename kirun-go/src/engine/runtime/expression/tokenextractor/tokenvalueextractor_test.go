package tokenextractor

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

var extractor = MakeTokenValueExtractor("Arguments.", map[string]any{
	"user": map[string]any{
		"name": "John Doe",
	},
	"a": 10,
	"b": []any{
		"item1",
		"item2",
		"item3",
	},
	"c": map[string]any{
		"d": "item4",
	},
})

func TestArgumentsExtractor(t *testing.T) {
	value, err := extractor.GetValue("Arguments.user.name")
	assert.NoError(t, err)
	assert.Equal(t, "John Doe", value)
}

func TestDeepExtractor(t *testing.T) {
	value, err := extractor.GetValue("Arguments.c.d")
	assert.NoError(t, err)
	assert.Equal(t, "item4", value)
}

func TestDeepExtractorWithString(t *testing.T) {
	value, err := extractor.GetValue("Arguments.c['d']")
	assert.NoError(t, err)
	assert.Equal(t, "item4", value)
}

func TestDeepWithStrings(t *testing.T) {
	value, err := extractor.GetValue("Arguments[\"user\"]['name']")
	assert.NoError(t, err)
	assert.Equal(t, "John Doe", value)
}

func TestArrayExtractor(t *testing.T) {
	value, err := extractor.GetValue("Arguments.b[1]")
	assert.NoError(t, err)
	assert.Equal(t, "item2", value)
}

func TestArrayExtractorWithDot(t *testing.T) {
	value, err := extractor.GetValue("Arguments.b.1")
	assert.NoError(t, err)
	assert.Equal(t, "item2", value)
}

func TestErrorWrongPrefix(t *testing.T) {
	_, err := extractor.GetValue("Argument.b[10]")
	assert.Error(t, err)
}

func TestArrayExtractorNotEndingBracket(t *testing.T) {
	_, err := extractor.GetValue("Arguments.b[1")
	assert.Error(t, err)
}
