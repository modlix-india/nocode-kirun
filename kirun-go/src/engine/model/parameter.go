package model

import (
	"github.com/modlix-india/nocode-kirun/engine/json/schema"
	"github.com/modlix-india/nocode-kirun/engine/namespaces"
	schemapb "github.com/modlix-india/nocode-kirun/proto/generated"
)

// ParameterType represents the type of a parameter
type ParameterType string

const (
	EXPRESSION ParameterType = "EXPRESSION"
	CONSTANT   ParameterType = "CONSTANT"
)

// Parameter represents a function parameter
type Parameter struct {
	Schema           *schema.Schema
	ParameterName    string
	VariableArgument bool
	Type             ParameterType
}

// NewParameter creates a new Parameter
func NewParameter(parameterName string, schema *schema.Schema) *Parameter {
	return &Parameter{
		Schema:           schema,
		ParameterName:    parameterName,
		VariableArgument: false,
		Type:             EXPRESSION,
	}
}

// SetType sets the parameter type
func (p *Parameter) SetType(paramType ParameterType) *Parameter {
	p.Type = paramType
	return p
}

// SetVariableArgument sets whether this is a variable argument
func (p *Parameter) SetVariableArgument(variableArgument bool) *Parameter {
	p.VariableArgument = variableArgument
	return p
}

// GetName returns the parameter name
func (p *Parameter) GetName() string {
	return p.ParameterName
}

// GetExpressionSchema returns the EXPRESSION parameter schema
func GetExpressionSchema() *schema.Schema {
	return schema.NewSchema().
		SetNamespace(namespaces.SYSTEM).
		SetName("ParameterExpression").
		SetType(schemapb.SchemaType_OBJECT).
		SetProperties(map[string]*schema.Schema{
			"isExpression": schema.OfBoolean("isExpression").SetDefaultValue(true),
			"value":        schema.OfAny("value"),
		})
}
