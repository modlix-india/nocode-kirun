package schema

import (
	schemapb "github.com/modlix-india/nocode-kirun/proto/generated"
	"google.golang.org/protobuf/types/known/anypb"
)

// Schema wraps the protobuf Schema with additional methods
type Schema struct {
	*schemapb.Schema
}

// NewSchema creates a new Schema instance
func NewSchema() *Schema {
	return &Schema{
		Schema: &schemapb.Schema{
			Properties:        make(map[string]*schemapb.Schema),
			PatternProperties: make(map[string]*schemapb.Schema),
			Defs:              make(map[string]*schemapb.Schema),
			Version:           1,
		},
	}
}

// SetNamespace sets the namespace
func (s *Schema) SetNamespace(namespace string) *Schema {
	s.Namespace = namespace
	return s
}

// SetName sets the name
func (s *Schema) SetName(name string) *Schema {
	s.Name = name
	return s
}

// SetType sets the type using a single SchemaType
func (s *Schema) SetType(schemaType schemapb.SchemaType) *Schema {
	s.Type = &schemapb.Type{
		TypeUnion: &schemapb.Type_SingleType{
			SingleType: &schemapb.SingleType{
				Type: schemaType,
			},
		},
	}
	return s
}

// SetMultipleTypes sets multiple possible types
func (s *Schema) SetMultipleTypes(types ...schemapb.SchemaType) *Schema {
	s.Type = &schemapb.Type{
		TypeUnion: &schemapb.Type_MultipleType{
			MultipleType: &schemapb.MultipleType{
				Types: types,
			},
		},
	}
	return s
}

// SetEnums sets the enum values
func (s *Schema) SetEnums(enums []string) *Schema {
	s.Enums = make([]*anypb.Any, len(enums))
	for i, enum := range enums {
		if anyVal, err := anypb.New(&anypb.Any{Value: []byte(enum)}); err == nil {
			s.Enums[i] = anyVal
		}
	}
	return s
}

// SetProperties sets the properties
func (s *Schema) SetProperties(properties map[string]*Schema) *Schema {
	if s.Properties == nil {
		s.Properties = make(map[string]*schemapb.Schema)
	}
	for key, value := range properties {
		s.Properties[key] = value.Schema
	}
	return s
}

// SetAdditionalItems sets additional items
func (s *Schema) SetAdditionalItems(additionalItems *schemapb.AdditionalType) *Schema {
	s.AdditionalItems = additionalItems
	return s
}

// SetDefaultValue sets the default value
func (s *Schema) SetDefaultValue(defaultValue interface{}) *Schema {
	if anyVal, err := anypb.New(&anypb.Any{Value: []byte("default")}); err == nil {
		s.DefaultValue = anyVal
	}
	return s
}

// SetAdditionalProperties sets additional properties
func (s *Schema) SetAdditionalProperties(additionalProperties *schemapb.AdditionalType) *Schema {
	s.AdditionalProperties = additionalProperties
	return s
}

// SetRequired sets the required fields
func (s *Schema) SetRequired(required []string) *Schema {
	s.Required = required
	return s
}

// SetAnyOf sets anyOf schemas
func (s *Schema) SetAnyOf(schemas []*Schema) *Schema {
	s.AnyOf = make([]*schemapb.Schema, len(schemas))
	for i, schema := range schemas {
		s.AnyOf[i] = schema.Schema
	}
	return s
}

// SetAllOf sets allOf schemas
func (s *Schema) SetAllOf(schemas []*Schema) *Schema {
	s.AllOf = make([]*schemapb.Schema, len(schemas))
	for i, schema := range schemas {
		s.AllOf[i] = schema.Schema
	}
	return s
}

// SetOneOf sets oneOf schemas
func (s *Schema) SetOneOf(schemas []*Schema) *Schema {
	s.OneOf = make([]*schemapb.Schema, len(schemas))
	for i, schema := range schemas {
		s.OneOf[i] = schema.Schema
	}
	return s
}

// SetNot sets not schema
func (s *Schema) SetNot(not *Schema) *Schema {
	s.Not = not.Schema
	return s
}

// SetRef sets the reference
func (s *Schema) SetRef(ref string) *Schema {
	s.Ref = ref
	return s
}

// SetDescription sets the description
func (s *Schema) SetDescription(description string) *Schema {
	s.Description = description
	return s
}

// SetComment sets the comment
func (s *Schema) SetComment(comment string) *Schema {
	s.Comment = comment
	return s
}

// SetPattern sets the pattern
func (s *Schema) SetPattern(pattern string) *Schema {
	s.Pattern = pattern
	return s
}

// SetFormat sets the format
func (s *Schema) SetFormat(format schemapb.StringFormat) *Schema {
	s.Format = format
	return s
}

// SetMinLength sets the minimum length
func (s *Schema) SetMinLength(minLength int32) *Schema {
	s.MinLength = minLength
	return s
}

// SetMaxLength sets the maximum length
func (s *Schema) SetMaxLength(maxLength int32) *Schema {
	s.MaxLength = maxLength
	return s
}

// SetMultipleOf sets the multiple of
func (s *Schema) SetMultipleOf(multipleOf float64) *Schema {
	s.MultipleOf = multipleOf
	return s
}

// SetMinimum sets the minimum value
func (s *Schema) SetMinimum(minimum float64) *Schema {
	s.Minimum = minimum
	return s
}

// SetMaximum sets the maximum value
func (s *Schema) SetMaximum(maximum float64) *Schema {
	s.Maximum = maximum
	return s
}

// SetExclusiveMinimum sets the exclusive minimum
func (s *Schema) SetExclusiveMinimum(exclusiveMinimum float64) *Schema {
	s.ExclusiveMinimum = exclusiveMinimum
	return s
}

// SetExclusiveMaximum sets the exclusive maximum
func (s *Schema) SetExclusiveMaximum(exclusiveMaximum float64) *Schema {
	s.ExclusiveMaximum = exclusiveMaximum
	return s
}

// SetPropertyNames sets property names schema
func (s *Schema) SetPropertyNames(propertyNames *Schema) *Schema {
	s.PropertyNames = propertyNames.Schema
	return s
}

// SetMinProperties sets minimum properties
func (s *Schema) SetMinProperties(minProperties int32) *Schema {
	s.MinProperties = minProperties
	return s
}

// SetMaxProperties sets maximum properties
func (s *Schema) SetMaxProperties(maxProperties int32) *Schema {
	s.MaxProperties = maxProperties
	return s
}

// SetPatternProperties sets pattern properties
func (s *Schema) SetPatternProperties(patternProperties map[string]*Schema) *Schema {
	if s.PatternProperties == nil {
		s.PatternProperties = make(map[string]*schemapb.Schema)
	}
	for key, value := range patternProperties {
		s.PatternProperties[key] = value.Schema
	}
	return s
}

// SetItems sets items schema
func (s *Schema) SetItems(items *ArraySchemaType) *Schema {
	s.Items = items.ArraySchemaType
	return s
}

// SetContains sets contains schema
func (s *Schema) SetContains(contains *Schema) *Schema {
	s.Contains = contains.Schema
	return s
}

// SetMinContains sets minimum contains
func (s *Schema) SetMinContains(minContains int32) *Schema {
	s.MinContains = minContains
	return s
}

// SetMaxContains sets maximum contains
func (s *Schema) SetMaxContains(maxContains int32) *Schema {
	s.MaxContains = maxContains
	return s
}

// SetMinItems sets minimum items
func (s *Schema) SetMinItems(minItems int32) *Schema {
	s.MinItems = minItems
	return s
}

// SetMaxItems sets maximum items
func (s *Schema) SetMaxItems(maxItems int32) *Schema {
	s.MaxItems = maxItems
	return s
}

// SetUniqueItems sets unique items
func (s *Schema) SetUniqueItems(uniqueItems bool) *Schema {
	s.UniqueItems = uniqueItems
	return s
}

// SetPermission sets permission
func (s *Schema) SetPermission(permission string) *Schema {
	s.Permission = permission
	return s
}

// SetDetails sets details
func (s *Schema) SetDetails(details *schemapb.SchemaDetails) *Schema {
	s.Details = details
	return s
}

// SetViewDetails sets view details
func (s *Schema) SetViewDetails(viewDetails *schemapb.SchemaDetails) *Schema {
	s.ViewDetails = viewDetails
	return s
}

// GetFullName returns the full name (namespace.name)
func (s *Schema) GetFullName() string {
	if s.Namespace == "" {
		return s.Name
	}
	return s.Namespace + "." + s.Name
}

// GetName returns the name
func (s *Schema) GetName() string {
	return s.Name
}

// GetTitle returns the title (namespace.name or just name if namespace is empty)
func (s *Schema) GetTitle() string {
	if s.Namespace == "" || s.Namespace == "_" {
		return s.Name
	}
	return s.Namespace + "." + s.Name
}

// Static factory methods
func OfString(name string) *Schema {
	return NewSchema().SetName(name).SetType(schemapb.SchemaType_STRING)
}

func OfBoolean(name string) *Schema {
	return NewSchema().SetName(name).SetType(schemapb.SchemaType_BOOLEAN)
}

func OfInteger(name string) *Schema {
	return NewSchema().SetName(name).SetType(schemapb.SchemaType_INTEGER)
}

func OfLong(name string) *Schema {
	return NewSchema().SetName(name).SetType(schemapb.SchemaType_LONG)
}

func OfFloat(name string) *Schema {
	return NewSchema().SetName(name).SetType(schemapb.SchemaType_FLOAT)
}

func OfDouble(name string) *Schema {
	return NewSchema().SetName(name).SetType(schemapb.SchemaType_DOUBLE)
}

func OfNumber(name string) *Schema {
	// Create a multiple type for number (INTEGER, LONG, FLOAT, DOUBLE)
	return NewSchema().SetName(name).SetMultipleTypes(
		schemapb.SchemaType_INTEGER,
		schemapb.SchemaType_LONG,
		schemapb.SchemaType_FLOAT,
		schemapb.SchemaType_DOUBLE,
	)
}

func OfAny(name string) *Schema {
	return NewSchema().SetName(name).SetMultipleTypes(
		schemapb.SchemaType_INTEGER,
		schemapb.SchemaType_LONG,
		schemapb.SchemaType_FLOAT,
		schemapb.SchemaType_DOUBLE,
		schemapb.SchemaType_STRING,
		schemapb.SchemaType_BOOLEAN,
		schemapb.SchemaType_ARRAY,
		schemapb.SchemaType_NULL,
		schemapb.SchemaType_OBJECT,
	)
}

func OfAnyNotNull(name string) *Schema {
	return NewSchema().SetName(name).SetMultipleTypes(
		schemapb.SchemaType_INTEGER,
		schemapb.SchemaType_LONG,
		schemapb.SchemaType_FLOAT,
		schemapb.SchemaType_DOUBLE,
		schemapb.SchemaType_STRING,
		schemapb.SchemaType_BOOLEAN,
		schemapb.SchemaType_ARRAY,
		schemapb.SchemaType_OBJECT,
	)
}

func Of(name string, types ...schemapb.SchemaType) *Schema {
	if len(types) == 1 {
		return NewSchema().SetName(name).SetType(types[0])
	}
	return NewSchema().SetName(name).SetMultipleTypes(types...)
}

func OfObject(name string) *Schema {
	return NewSchema().SetName(name).SetType(schemapb.SchemaType_OBJECT)
}

func OfRef(ref string) *Schema {
	return NewSchema().SetRef(ref)
}

func OfArray(name string, itemSchemas ...*Schema) *Schema {
	schema := NewSchema().SetName(name).SetType(schemapb.SchemaType_ARRAY)
	if len(itemSchemas) == 1 {
		schema.Items = &schemapb.ArraySchemaType{
			SchemaUnion: &schemapb.ArraySchemaType_SingleSchema{
				SingleSchema: itemSchemas[0].Schema,
			},
		}
	} else if len(itemSchemas) > 1 {
		tupleSchemas := make([]*schemapb.Schema, len(itemSchemas))
		for i, item := range itemSchemas {
			tupleSchemas[i] = item.Schema
		}
		schema.Items = &schemapb.ArraySchemaType{
			SchemaUnion: &schemapb.ArraySchemaType_TupleSchema{
				TupleSchema: &schemapb.TupleSchema{
					Schemas: tupleSchemas,
				},
			},
		}
	}
	return schema
}

// FromListOfSchemas converts a list of schemas
func FromListOfSchemas(list []*Schema) []*Schema {
	if list == nil {
		return nil
	}
	return list
}

// FromMapOfSchemas converts a map of schemas
func FromMapOfSchemas(mapSchemas map[string]*Schema) map[string]*Schema {
	if mapSchemas == nil {
		return nil
	}
	return mapSchemas
}

// From creates a Schema from a generic object (simplified version)
func From(obj interface{}) *Schema {
	// This is a simplified implementation
	// In a real implementation, you would parse the object and create the appropriate schema
	return NewSchema()
}

// AdditionalType wrapper
type AdditionalType struct {
	*schemapb.AdditionalType
}

// NewAdditionalTypeFromBool creates an AdditionalType from a boolean
func NewAdditionalTypeFromBool(value bool) *AdditionalType {
	return &AdditionalType{
		AdditionalType: &schemapb.AdditionalType{
			Value: &schemapb.AdditionalType_BooleanValue{
				BooleanValue: value,
			},
		},
	}
}

// NewAdditionalTypeFromSchema creates an AdditionalType from a schema
func NewAdditionalTypeFromSchema(schema *Schema) *AdditionalType {
	return &AdditionalType{
		AdditionalType: &schemapb.AdditionalType{
			Value: &schemapb.AdditionalType_SchemaValue{
				SchemaValue: schema.Schema,
			},
		},
	}
}

// ArraySchemaType wrapper
type ArraySchemaType struct {
	*schemapb.ArraySchemaType
}

// NewArraySchemaType creates a new ArraySchemaType
func NewArraySchemaType() *ArraySchemaType {
	return &ArraySchemaType{
		ArraySchemaType: &schemapb.ArraySchemaType{},
	}
}

// SetSingleSchema sets a single schema
func (ast *ArraySchemaType) SetSingleSchema(schema *Schema) *ArraySchemaType {
	ast.ArraySchemaType = &schemapb.ArraySchemaType{
		SchemaUnion: &schemapb.ArraySchemaType_SingleSchema{
			SingleSchema: schema.Schema,
		},
	}
	return ast
}

// SetTupleSchema sets tuple schemas
func (ast *ArraySchemaType) SetTupleSchema(schemas []*Schema) *ArraySchemaType {
	tupleSchemas := make([]*schemapb.Schema, len(schemas))
	for i, schema := range schemas {
		tupleSchemas[i] = schema.Schema
	}
	ast.ArraySchemaType = &schemapb.ArraySchemaType{
		SchemaUnion: &schemapb.ArraySchemaType_TupleSchema{
			TupleSchema: &schemapb.TupleSchema{
				Schemas: tupleSchemas,
			},
		},
	}
	return ast
}

// IsSingleType checks if this is a single type
func (ast *ArraySchemaType) IsSingleType() bool {
	return ast.GetSingleSchema() != nil
}

// GetSingleSchema gets the single schema
func (ast *ArraySchemaType) GetSingleSchema() *Schema {
	if single := ast.ArraySchemaType.GetSingleSchema(); single != nil {
		return &Schema{Schema: single}
	}
	return nil
}

// GetTupleSchema gets the tuple schemas
func (ast *ArraySchemaType) GetTupleSchema() []*Schema {
	if tuple := ast.ArraySchemaType.GetTupleSchema(); tuple != nil {
		schemas := make([]*Schema, len(tuple.Schemas))
		for i, s := range tuple.Schemas {
			schemas[i] = &Schema{Schema: s}
		}
		return schemas
	}
	return nil
}

// Of creates an ArraySchemaType from schemas
func (ast *ArraySchemaType) Of(schemas ...*Schema) *ArraySchemaType {
	if len(schemas) == 1 {
		return NewArraySchemaType().SetSingleSchema(schemas[0])
	}
	return NewArraySchemaType().SetTupleSchema(schemas)
}

// From creates an ArraySchemaType from an object
func (ast *ArraySchemaType) From(obj interface{}) *ArraySchemaType {
	// Simplified implementation
	return NewArraySchemaType()
}

// Static schema instances
var (
	NULL_SCHEMA = NewSchema().
			SetNamespace("System").
			SetName("Null").
			SetType(schemapb.SchemaType_NULL)

	SCHEMA_SCHEMA = NewSchema().
			SetNamespace("System").
			SetName("Schema").
			SetType(schemapb.SchemaType_OBJECT)
)

// Helper functions to work with protobuf types
func (s *Schema) GetSchemaType() schemapb.SchemaType {
	if s.Type == nil {
		return schemapb.SchemaType_SCHEMA_TYPE_UNSPECIFIED
	}

	if singleType := s.Type.GetSingleType(); singleType != nil {
		return singleType.GetType()
	}

	// For multiple types, return the first one as a default
	if multipleType := s.Type.GetMultipleType(); multipleType != nil && len(multipleType.GetTypes()) > 0 {
		return multipleType.GetTypes()[0]
	}

	return schemapb.SchemaType_SCHEMA_TYPE_UNSPECIFIED
}

func (s *Schema) GetSchemaTypes() []schemapb.SchemaType {
	if s.Type == nil {
		return []schemapb.SchemaType{}
	}

	if singleType := s.Type.GetSingleType(); singleType != nil {
		return []schemapb.SchemaType{singleType.GetType()}
	}

	if multipleType := s.Type.GetMultipleType(); multipleType != nil {
		return multipleType.GetTypes()
	}

	return []schemapb.SchemaType{}
}

func (s *Schema) IsMultipleType() bool {
	if s.Type == nil {
		return false
	}
	return s.Type.GetMultipleType() != nil
}

func (s *Schema) IsSingleType() bool {
	if s.Type == nil {
		return false
	}
	return s.Type.GetSingleType() != nil
}
