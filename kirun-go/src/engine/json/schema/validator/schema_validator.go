package validator

import (
	"encoding/json"
	"fmt"
	"sort"
	"strings"

	"github.com/modlix-india/nocode-kirun/engine"
	"github.com/modlix-india/nocode-kirun/engine/json/schema/convertor/enums"
	"github.com/modlix-india/nocode-kirun/engine/json/schema/validator/exception"
	"github.com/modlix-india/nocode-kirun/engine/util"
	schemapb "github.com/modlix-india/nocode-kirun/proto/generated"
)

// SchemaValidator provides validation functionality for schemas
type SchemaValidator struct{}

// ORDER defines the validation order for different schema types
var ORDER = map[schemapb.SchemaType]int{
	schemapb.SchemaType_OBJECT:  0,
	schemapb.SchemaType_ARRAY:   1,
	schemapb.SchemaType_DOUBLE:  2,
	schemapb.SchemaType_FLOAT:   3,
	schemapb.SchemaType_LONG:    4,
	schemapb.SchemaType_INTEGER: 5,
	schemapb.SchemaType_STRING:  6,
	schemapb.SchemaType_BOOLEAN: 7,
	schemapb.SchemaType_NULL:    8,
}

// Path generates a path string from parent schemas
func (sv *SchemaValidator) Path(parents []*Schema) string {
	if parents == nil {
		return ""
	}

	var pathParts []string
	for _, s := range parents {
		if title := s.GetTitle(); title != "" {
			pathParts = append(pathParts, title)
		}
	}

	return strings.Join(pathParts, ".")
}

// Validate validates an element against a schema
func (sv *SchemaValidator) Validate(
	parents []*Schema,
	schema *Schema,
	repository engine.Repository[*Schema],
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	if schema == nil {
		return nil, exception.NewSchemaValidationException(
			sv.Path(parents),
			"No schema found to validate",
		)
	}

	if parents == nil {
		parents = []*Schema{}
	}
	parents = append(parents, schema)

	// Handle default value
	if util.IsNullValue(element) && !util.IsNullValue(schema.GetDefaultValue()) {
		// Return a deep copy of the default value
		if defaultBytes, err := json.Marshal(schema.GetDefaultValue()); err == nil {
			var result interface{}
			if err := json.Unmarshal(defaultBytes, &result); err == nil {
				return result, nil
			}
		}
	}

	// Handle constant validation
	if !util.IsNullValue(schema.GetConstant()) {
		return sv.constantValidation(parents, schema, element)
	}

	// Handle enum validation
	if enums := schema.GetEnums(); len(enums) > 0 {
		return sv.enumCheck(parents, schema, element)
	}

	// Check format without type
	if schema.GetFormat() != schemapb.StringFormat_STRING_FORMAT_UNSPECIFIED && schema.GetType() == nil {
		return nil, exception.NewSchemaValidationException(
			sv.Path(parents),
			fmt.Sprintf("Type is missing in schema for declared %s format.", schema.GetFormat()),
		)
	}

	// Check convert mode without type
	if convert != nil && *convert && schema.GetType() == nil {
		modeStr := "unknown"
		if mode != nil {
			modeStr = string(*mode)
		}
		return nil, exception.NewSchemaValidationException(
			sv.Path(parents),
			fmt.Sprintf("Type is missing in schema for declared %s", modeStr),
		)
	}

	// Type validation
	if schema.GetType() != nil {
		var err error
		element, err = sv.typeValidation(parents, schema, repository, element, convert, mode)
		if err != nil {
			return nil, err
		}
	}

	// Handle reference
	if ref := schema.GetRef(); ref != "" {
		refSchema, err := schema.GetSchemaFromRef(parents[0], repository, ref)
		if err != nil {
			return nil, err
		}
		element, err = sv.Validate(parents, refSchema, repository, element, convert, mode)
		if err != nil {
			return nil, err
		}
	}

	// Handle anyOf, allOf, oneOf
	if schema.GetAnyOf() != nil || schema.GetAllOf() != nil || schema.GetOneOf() != nil {
		anyOfAllOfOneOfValidator := NewAnyOfAllOfOneOfValidator()
		var err error
		element, err = anyOfAllOfOneOfValidator.Validate(parents, schema, repository, element, convert, mode)
		if err != nil {
			return nil, err
		}
	}

	// Handle not condition
	if notSchema := schema.GetNot(); notSchema != nil {
		// Convert protobuf schema to wrapper schema
		wrapperSchema := &Schema{Schema: notSchema}
		_, err := sv.Validate(parents, wrapperSchema, repository, element, convert, mode)
		if err == nil {
			return nil, exception.NewSchemaValidationException(
				sv.Path(parents),
				"Schema validated value in not condition.",
			)
		}
	}

	return element, nil
}

// constantValidation validates against a constant value
func (sv *SchemaValidator) constantValidation(parents []*Schema, schema *Schema, element interface{}) (interface{}, error) {
	if !util.DeepEqual(schema.GetConstant(), element) {
		return nil, exception.NewSchemaValidationException(
			sv.Path(parents),
			fmt.Sprintf("Expecting a constant value: %v", element),
		)
	}
	return element, nil
}

// enumCheck validates against enum values
func (sv *SchemaValidator) enumCheck(parents []*Schema, schema *Schema, element interface{}) (interface{}, error) {
	enums := schema.GetEnums()

	// Convert enums from anypb.Any to strings for comparison
	var enumStrings []string
	for _, enum := range enums {
		if enum != nil {
			// Extract string value from anypb.Any
			// This is a simplified implementation
			enumStrings = append(enumStrings, string(enum.Value))
		}
	}

	for _, enum := range enumStrings {
		if enum == fmt.Sprintf("%v", element) {
			return element, nil
		}
	}

	return nil, exception.NewSchemaValidationException(
		sv.Path(parents),
		fmt.Sprintf("Value is not one of %v", enumStrings),
	)
}

// typeValidation validates against schema types
func (sv *SchemaValidator) typeValidation(
	parents []*Schema,
	schema *Schema,
	repository engine.Repository[*Schema],
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	allowedTypes := sv.getAllowedSchemaTypes(schema)

	// Sort types by order
	sort.Slice(allowedTypes, func(i, j int) bool {
		orderI, existsI := ORDER[allowedTypes[i]]
		orderJ, existsJ := ORDER[allowedTypes[j]]

		if !existsI {
			orderI = 999999 // Infinity equivalent
		}
		if !existsJ {
			orderJ = 999999 // Infinity equivalent
		}

		return orderI < orderJ
	})

	var errors []*exception.SchemaValidationException

	for _, schemaType := range allowedTypes {
		typeValidator := NewTypeValidator()
		result, err := typeValidator.Validate(parents, schemaType, schema, repository, element, convert, mode)
		if err == nil {
			return result, nil
		}

		if schemaErr, ok := err.(*exception.SchemaValidationException); ok {
			errors = append(errors, schemaErr)
		} else {
			errors = append(errors, exception.NewSchemaValidationException(
				sv.Path(parents),
				err.Error(),
			))
		}
	}

	if len(errors) == 1 {
		return nil, exception.NewSchemaValidationException(
			sv.Path(parents),
			errors[0].Message,
		)
	}

	elementJSON, _ := json.Marshal(element)
	return nil, exception.NewSchemaValidationException(
		sv.Path(parents),
		fmt.Sprintf("Value %s is not of valid type(s)", string(elementJSON)),
		errors,
	)
}

// getAllowedSchemaTypes extracts allowed types from schema
func (sv *SchemaValidator) getAllowedSchemaTypes(schema *Schema) []schemapb.SchemaType {
	if schema.GetType() == nil {
		return []schemapb.SchemaType{}
	}

	// Get types from the schema's type union
	return schema.GetSchemaTypes()
}

// Static methods for backward compatibility
func Path(parents []*Schema) string {
	validator := &SchemaValidator{}
	return validator.Path(parents)
}

func Validate(
	parents []*Schema,
	schema *Schema,
	repository engine.Repository[*Schema],
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	validator := &SchemaValidator{}
	return validator.Validate(parents, schema, repository, element, convert, mode)
}

func ConstantValidation(parents []*Schema, schema *Schema, element interface{}) (interface{}, error) {
	validator := &SchemaValidator{}
	return validator.constantValidation(parents, schema, element)
}

func EnumCheck(parents []*Schema, schema *Schema, element interface{}) (interface{}, error) {
	validator := &SchemaValidator{}
	return validator.enumCheck(parents, schema, element)
}

func TypeValidation(
	parents []*Schema,
	schema *Schema,
	repository engine.Repository[*Schema],
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	validator := &SchemaValidator{}
	return validator.typeValidation(parents, schema, repository, element, convert, mode)
}
