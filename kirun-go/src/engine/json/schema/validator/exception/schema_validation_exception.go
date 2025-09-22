package exception

import "fmt"

// SchemaValidationException represents a schema validation error
type SchemaValidationException struct {
	Path    string
	Message string
	Errors  []*SchemaValidationException
}

// NewSchemaValidationException creates a new SchemaValidationException
func NewSchemaValidationException(path, message string, errors ...[]*SchemaValidationException) *SchemaValidationException {
	var errs []*SchemaValidationException
	if len(errors) > 0 {
		errs = errors[0]
	}

	return &SchemaValidationException{
		Path:    path,
		Message: message,
		Errors:  errs,
	}
}

// Error implements the error interface
func (e *SchemaValidationException) Error() string {
	if e.Path == "" {
		return e.Message
	}
	return fmt.Sprintf("%s: %s", e.Path, e.Message)
}

// GetPath returns the path where the error occurred
func (e *SchemaValidationException) GetPath() string {
	return e.Path
}

// GetMessage returns the error message
func (e *SchemaValidationException) GetMessage() string {
	return e.Message
}

// GetErrors returns the nested errors
func (e *SchemaValidationException) GetErrors() []*SchemaValidationException {
	return e.Errors
}
