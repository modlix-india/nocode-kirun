package validator

import (
	"fmt"
	"strconv"

	"github.com/modlix-india/nocode-kirun/engine"
	"github.com/modlix-india/nocode-kirun/engine/json/schema"
	"github.com/modlix-india/nocode-kirun/engine/json/schema/convertor/enums"
	"github.com/modlix-india/nocode-kirun/engine/json/schema/validator/exception"
	"github.com/modlix-india/nocode-kirun/engine/util"
	schemapb "github.com/modlix-india/nocode-kirun/proto/generated"
)

// TypeValidator handles validation for specific schema types
type TypeValidator struct{}

// NewTypeValidator creates a new TypeValidator
func NewTypeValidator() *TypeValidator {
	return &TypeValidator{}
}

// Validate validates an element against a specific schema type
func (tv *TypeValidator) Validate(
	parents []*schema.Schema,
	schemaType schemapb.SchemaType,
	schema *schema.Schema,
	repository engine.Repository[*schema.Schema],
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	switch schemaType {
	case schemapb.SchemaType_OBJECT:
		return tv.validateObject(parents, schema, repository, element, convert, mode)
	case schemapb.SchemaType_ARRAY:
		return tv.validateArray(parents, schema, repository, element, convert, mode)
	case schemapb.SchemaType_STRING:
		return tv.validateString(parents, schema, element, convert, mode)
	case schemapb.SchemaType_INTEGER:
		return tv.validateInteger(parents, schema, element, convert, mode)
	case schemapb.SchemaType_LONG:
		return tv.validateLong(parents, schema, element, convert, mode)
	case schemapb.SchemaType_FLOAT:
		return tv.validateFloat(parents, schema, element, convert, mode)
	case schemapb.SchemaType_DOUBLE:
		return tv.validateDouble(parents, schema, element, convert, mode)
	case schemapb.SchemaType_BOOLEAN:
		return tv.validateBoolean(parents, schema, element, convert, mode)
	case schemapb.SchemaType_NULL:
		return tv.validateNull(parents, schema, element, convert, mode)
	default:
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Unsupported schema type: %v", schemaType),
		)
	}
}

// validateObject validates an object
func (tv *TypeValidator) validateObject(
	parents []*schema.Schema,
	schema *schema.Schema,
	repository engine.Repository[*schema.Schema],
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	// Implementation for object validation
	// This is a simplified version
	if element == nil {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			"Expected object, got null",
		)
	}

	// Add more object validation logic here
	return element, nil
}

// validateArray validates an array
func (tv *TypeValidator) validateArray(
	parents []*schema.Schema,
	schema *schema.Schema,
	repository engine.Repository[*schema.Schema],
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	// Implementation for array validation
	// This is a simplified version
	if element == nil {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			"Expected array, got null",
		)
	}

	// Add more array validation logic here
	return element, nil
}

// validateString validates a string
func (tv *TypeValidator) validateString(
	parents []*schema.Schema,
	schema *schema.Schema,
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	if element == nil {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			"Expected string, got null",
		)
	}

	// Convert to string if needed
	var str string
	switch v := element.(type) {
	case string:
		str = v
	case int:
		str = strconv.Itoa(v)
	case int64:
		str = strconv.FormatInt(v, 10)
	case float64:
		str = strconv.FormatFloat(v, 'f', -1, 64)
	case bool:
		str = strconv.FormatBool(v)
	default:
		if convert != nil && *convert {
			str = fmt.Sprintf("%v", v)
		} else {
			return nil, exception.NewSchemaValidationException(
				Path(parents),
				fmt.Sprintf("Expected string, got %T", element),
			)
		}
	}

	// Validate string constraints
	if minLen := schema.GetMinLength(); minLen > 0 && len(str) < int(minLen) {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("String length %d is less than minimum %d", len(str), minLen),
		)
	}

	if maxLen := schema.GetMaxLength(); maxLen > 0 && len(str) > int(maxLen) {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("String length %d exceeds maximum %d", len(str), maxLen),
		)
	}

	// Add pattern validation here if needed

	return str, nil
}

// validateInteger validates an integer
func (tv *TypeValidator) validateInteger(
	parents []*schema.Schema,
	schema *schema.Schema,
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	if element == nil {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			"Expected integer, got null",
		)
	}

	var intVal int32
	switch v := element.(type) {
	case int32:
		intVal = v
	case int:
		intVal = int32(v)
	case int64:
		intVal = int32(v)
	case float64:
		intVal = int32(v)
	case string:
		if convert != nil && *convert {
			if parsed, err := strconv.ParseInt(v, 10, 32); err == nil {
				intVal = int32(parsed)
			} else {
				return nil, exception.NewSchemaValidationException(
					Path(parents),
					fmt.Sprintf("Cannot convert string '%s' to integer", v),
				)
			}
		} else {
			return nil, exception.NewSchemaValidationException(
				Path(parents),
				fmt.Sprintf("Expected integer, got string '%s'", v),
			)
		}
	default:
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Expected integer, got %T", element),
		)
	}

	// Validate numeric constraints
	if min := schema.GetMinimum(); min > 0 && float64(intVal) < min {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Integer %d is less than minimum %f", intVal, min),
		)
	}

	if max := schema.GetMaximum(); max > 0 && float64(intVal) > max {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Integer %d exceeds maximum %f", intVal, max),
		)
	}

	return intVal, nil
}

// validateLong validates a long integer
func (tv *TypeValidator) validateLong(
	parents []*schema.Schema,
	schema *schema.Schema,
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	// Similar to validateInteger but for int64
	if element == nil {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			"Expected long, got null",
		)
	}

	var longVal int64
	switch v := element.(type) {
	case int64:
		longVal = v
	case int:
		longVal = int64(v)
	case int32:
		longVal = int64(v)
	case float64:
		longVal = int64(v)
	case string:
		if convert != nil && *convert {
			if parsed, err := strconv.ParseInt(v, 10, 64); err == nil {
				longVal = parsed
			} else {
				return nil, exception.NewSchemaValidationException(
					Path(parents),
					fmt.Sprintf("Cannot convert string '%s' to long", v),
				)
			}
		} else {
			return nil, exception.NewSchemaValidationException(
				Path(parents),
				fmt.Sprintf("Expected long, got string '%s'", v),
			)
		}
	default:
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Expected long, got %T", element),
		)
	}

	// Validate numeric constraints
	if min := schema.GetMinimum(); min > 0 && float64(longVal) < min {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Long %d is less than minimum %f", longVal, min),
		)
	}

	if max := schema.GetMaximum(); max > 0 && float64(longVal) > max {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Long %d exceeds maximum %f", longVal, max),
		)
	}

	return longVal, nil
}

// validateFloat validates a float
func (tv *TypeValidator) validateFloat(
	parents []*schema.Schema,
	schema *schema.Schema,
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	if element == nil {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			"Expected float, got null",
		)
	}

	var floatVal float32
	switch v := element.(type) {
	case float32:
		floatVal = v
	case float64:
		floatVal = float32(v)
	case int:
		floatVal = float32(v)
	case int32:
		floatVal = float32(v)
	case int64:
		floatVal = float32(v)
	case string:
		if convert != nil && *convert {
			if parsed, err := strconv.ParseFloat(v, 32); err == nil {
				floatVal = float32(parsed)
			} else {
				return nil, exception.NewSchemaValidationException(
					Path(parents),
					fmt.Sprintf("Cannot convert string '%s' to float", v),
				)
			}
		} else {
			return nil, exception.NewSchemaValidationException(
				Path(parents),
				fmt.Sprintf("Expected float, got string '%s'", v),
			)
		}
	default:
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Expected float, got %T", element),
		)
	}

	// Validate numeric constraints
	if min := schema.GetMinimum(); min > 0 && float64(floatVal) < min {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Float %f is less than minimum %f", floatVal, min),
		)
	}

	if max := schema.GetMaximum(); max > 0 && float64(floatVal) > max {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Float %f exceeds maximum %f", floatVal, max),
		)
	}

	return floatVal, nil
}

// validateDouble validates a double
func (tv *TypeValidator) validateDouble(
	parents []*schema.Schema,
	schema *schema.Schema,
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	if element == nil {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			"Expected double, got null",
		)
	}

	var doubleVal float64
	switch v := element.(type) {
	case float64:
		doubleVal = v
	case float32:
		doubleVal = float64(v)
	case int:
		doubleVal = float64(v)
	case int32:
		doubleVal = float64(v)
	case int64:
		doubleVal = float64(v)
	case string:
		if convert != nil && *convert {
			if parsed, err := strconv.ParseFloat(v, 64); err == nil {
				doubleVal = parsed
			} else {
				return nil, exception.NewSchemaValidationException(
					Path(parents),
					fmt.Sprintf("Cannot convert string '%s' to double", v),
				)
			}
		} else {
			return nil, exception.NewSchemaValidationException(
				Path(parents),
				fmt.Sprintf("Expected double, got string '%s'", v),
			)
		}
	default:
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Expected double, got %T", element),
		)
	}

	// Validate numeric constraints
	if min := schema.GetMinimum(); min > 0 && doubleVal < min {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Double %f is less than minimum %f", doubleVal, min),
		)
	}

	if max := schema.GetMaximum(); max > 0 && doubleVal > max {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Double %f exceeds maximum %f", doubleVal, max),
		)
	}

	return doubleVal, nil
}

// validateBoolean validates a boolean
func (tv *TypeValidator) validateBoolean(
	parents []*schema.Schema,
	schema *schema.Schema,
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	if element == nil {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			"Expected boolean, got null",
		)
	}

	var boolVal bool
	switch v := element.(type) {
	case bool:
		boolVal = v
	case string:
		if convert != nil && *convert {
			if parsed, err := strconv.ParseBool(v); err == nil {
				boolVal = parsed
			} else {
				return nil, exception.NewSchemaValidationException(
					Path(parents),
					fmt.Sprintf("Cannot convert string '%s' to boolean", v),
				)
			}
		} else {
			return nil, exception.NewSchemaValidationException(
				Path(parents),
				fmt.Sprintf("Expected boolean, got string '%s'", v),
			)
		}
	default:
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Expected boolean, got %T", element),
		)
	}

	return boolVal, nil
}

// validateNull validates a null value
func (tv *TypeValidator) validateNull(
	parents []*schema.Schema,
	schema *schema.Schema,
	element interface{},
	convert *bool,
	mode *enums.ConversionMode,
) (interface{}, error) {
	if !util.IsNullValue(element) {
		return nil, exception.NewSchemaValidationException(
			Path(parents),
			fmt.Sprintf("Expected null, got %T", element),
		)
	}

	return nil, nil
}
