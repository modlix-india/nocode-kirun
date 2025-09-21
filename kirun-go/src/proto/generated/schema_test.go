package schema

import (
	"testing"

	"google.golang.org/protobuf/proto"
)

func TestSchemaCreation(t *testing.T) {
	// Test creating a simple string schema
	stringSchema := &Schema{
		Namespace: "test",
		Name:      "TestSchema",
		Version:   1,
		Type: &Type{
			TypeUnion: &Type_SingleType{
				SingleType: &SingleType{
					Type: SchemaType_STRING,
				},
			},
		},
		Description: "A test string schema",
		MinLength:   1,
		MaxLength:   100,
	}

	if stringSchema.Name != "TestSchema" {
		t.Errorf("Expected name 'TestSchema', got '%s'", stringSchema.Name)
	}

	if stringSchema.Type.GetSingleType().Type != SchemaType_STRING {
		t.Errorf("Expected type STRING, got %v", stringSchema.Type.GetSingleType().Type)
	}

	// Test serialization
	data, err := proto.Marshal(stringSchema)
	if err != nil {
		t.Fatalf("Failed to marshal schema: %v", err)
	}

	if len(data) == 0 {
		t.Error("Serialized data should not be empty")
	}

	// Test deserialization
	var deserialized Schema
	err = proto.Unmarshal(data, &deserialized)
	if err != nil {
		t.Fatalf("Failed to unmarshal schema: %v", err)
	}

	if deserialized.Name != "TestSchema" {
		t.Errorf("Expected deserialized name 'TestSchema', got '%s'", deserialized.Name)
	}
}

func TestObjectSchema(t *testing.T) {
	// Test creating an object schema with properties
	objectSchema := &Schema{
		Namespace: "test",
		Name:      "ObjectSchema",
		Version:   1,
		Type: &Type{
			TypeUnion: &Type_SingleType{
				SingleType: &SingleType{
					Type: SchemaType_OBJECT,
				},
			},
		},
		Description: "A test object schema",
		Properties: map[string]*Schema{
			"id": {
				Type: &Type{
					TypeUnion: &Type_SingleType{
						SingleType: &SingleType{
							Type: SchemaType_INTEGER,
						},
					},
				},
				Description: "ID field",
			},
			"name": {
				Type: &Type{
					TypeUnion: &Type_SingleType{
						SingleType: &SingleType{
							Type: SchemaType_STRING,
						},
					},
				},
				Description: "Name field",
				MinLength:   1,
				MaxLength:   50,
			},
		},
		Required: []string{"id", "name"},
	}

	if len(objectSchema.Properties) != 2 {
		t.Errorf("Expected 2 properties, got %d", len(objectSchema.Properties))
	}

	if len(objectSchema.Required) != 2 {
		t.Errorf("Expected 2 required fields, got %d", len(objectSchema.Required))
	}

	// Test serialization and deserialization
	data, err := proto.Marshal(objectSchema)
	if err != nil {
		t.Fatalf("Failed to marshal object schema: %v", err)
	}

	var deserialized Schema
	err = proto.Unmarshal(data, &deserialized)
	if err != nil {
		t.Fatalf("Failed to unmarshal object schema: %v", err)
	}

	if len(deserialized.Properties) != 2 {
		t.Errorf("Expected 2 deserialized properties, got %d", len(deserialized.Properties))
	}
}
