package validator

import (
	"fmt"

	"github.com/modlix-india/nocode-kirun/engine"
	"github.com/modlix-india/nocode-kirun/engine/json/schema"
	"github.com/modlix-india/nocode-kirun/engine/json/schema/convertor/enums"
	"github.com/modlix-india/nocode-kirun/engine/json/schema/validator/exception"
)

// Type alias for convenience
type Schema = schema.Schema

// AnyOfAllOfOneOfValidator handles validation for anyOf, allOf, and oneOf schemas
type AnyOfAllOfOneOfValidator struct{}

// NewAnyOfAllOfOneOfValidator creates a new AnyOfAllOfOneOfValidator
func NewAnyOfAllOfOneOfValidator() *AnyOfAllOfOneOfValidator {
	return &AnyOfAllOfOneOfValidator{}
}

// Validate validates an element against anyOf, allOf, or oneOf schemas
func (a *AnyOfAllOfOneOfValidator) Validate(
	parents []*Schema,
	schema *Schema,
	repository engine.Repository[*Schema],
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	validator := &SchemaValidator{}

	// Handle anyOf
	if anyOf := schema.GetAnyOf(); anyOf != nil {
		// Convert protobuf schemas to wrapper schemas
		wrapperSchemas := make([]*Schema, len(anyOf))
		for i, s := range anyOf {
			wrapperSchemas[i] = &Schema{Schema: s}
		}
		return a.validateAnyOf(parents, wrapperSchemas, repository, element, convert, mode, validator)
	}

	// Handle allOf
	if allOf := schema.GetAllOf(); allOf != nil {
		// Convert protobuf schemas to wrapper schemas
		wrapperSchemas := make([]*Schema, len(allOf))
		for i, s := range allOf {
			wrapperSchemas[i] = &Schema{Schema: s}
		}
		return a.validateAllOf(parents, wrapperSchemas, repository, element, convert, mode, validator)
	}

	// Handle oneOf
	if oneOf := schema.GetOneOf(); oneOf != nil {
		// Convert protobuf schemas to wrapper schemas
		wrapperSchemas := make([]*Schema, len(oneOf))
		for i, s := range oneOf {
			wrapperSchemas[i] = &Schema{Schema: s}
		}
		return a.validateOneOf(parents, wrapperSchemas, repository, element, convert, mode, validator)
	}

	return element, nil
}

// validateAnyOf validates against anyOf schemas (at least one must pass)
func (a *AnyOfAllOfOneOfValidator) validateAnyOf(
	parents []*Schema,
	schemas []*Schema,
	repository engine.Repository[*Schema],
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
	validator *SchemaValidator,
) (interface{}, error) {
	var lastError error

	for _, subSchema := range schemas {
		result, err := validator.Validate(parents, subSchema, repository, element, convert, mode)
		if err == nil {
			return result, nil
		}
		lastError = err
	}

	// If we get here, all schemas failed
	if lastError != nil {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Element does not match any of the anyOf schemas. Last error: %v", lastError),
		)
	}

	return nil, exception.NewSchemaValidationException(
		Path(parents),
		"Element does not match any of the anyOf schemas",
	)
}

// validateAllOf validates against allOf schemas (all must pass)
func (a *AnyOfAllOfOneOfValidator) validateAllOf(
	parents []*Schema,
	schemas []*Schema,
	repository engine.Repository[*Schema],
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
	validator *SchemaValidator,
) (interface{}, error) {
	var errors []*exception.SchemaValidationException
	var result interface{} = element

	for i, subSchema := range schemas {
		subResult, err := validator.Validate(parents, subSchema, repository, result, convert, mode)
		if err != nil {
			if schemaErr, ok := err.(*exception.SchemaValidationException); ok {
				errors = append(errors, schemaErr)
			} else {
				errors = append(errors, exception.NewSchemaValidationException(
					Path(parents),
					fmt.Sprintf("Error in allOf schema %d: %v", i, err),
				))
			}
		} else {
			result = subResult
		}
	}

	if len(errors) > 0 {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Element does not match all of the allOf schemas. %d errors occurred", len(errors)),
			errors,
		)
	}

	return result, nil
}

// validateOneOf validates against oneOf schemas (exactly one must pass)
func (a *AnyOfAllOfOneOfValidator) validateOneOf(
	parents []*Schema,
	schemas []*Schema,
	repository engine.Repository[*Schema],
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
	validator *SchemaValidator,
) (interface{}, error) {
	var validResults []interface{}
	var errors []*exception.SchemaValidationException

	for i, subSchema := range schemas {
		result, err := validator.Validate(parents, subSchema, repository, element, convert, mode)
		if err == nil {
			validResults = append(validResults, result)
		} else {
			if schemaErr, ok := err.(*exception.SchemaValidationException); ok {
				errors = append(errors, schemaErr)
			} else {
				errors = append(errors, exception.NewSchemaValidationException(
					Path(parents),
					fmt.Sprintf("Error in oneOf schema %d: %v", i, err),
				))
			}
		}
	}

	if len(validResults) == 0 {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			"Element does not match any of the oneOf schemas",
			errors,
		)
	}

	if len(validResults) > 1 {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Element matches %d oneOf schemas, but exactly one is required", len(validResults)),
		)
	}

	return validResults[0], nil
}
