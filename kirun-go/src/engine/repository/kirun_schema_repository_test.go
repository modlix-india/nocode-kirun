package repository

import (
	"testing"

	"github.com/modlix-india/nocode-kirun/engine/namespaces"
)

func TestKIRunSchemaRepository_Find(t *testing.T) {
	repo := NewKIRunSchemaRepository()

	// Test finding existing schema
	schema, err := repo.Find(namespaces.SYSTEM, "string")
	if err != nil {
		t.Errorf("Expected no error, got %v", err)
	}
	if schema == nil {
		t.Error("Expected schema to be found")
	}
	if schema.GetName() != "string" {
		t.Errorf("Expected name 'string', got '%s'", schema.GetName())
	}

	// Test finding non-existing schema
	schema, err = repo.Find(namespaces.SYSTEM, "nonexistent")
	if err != nil {
		t.Errorf("Expected no error, got %v", err)
	}
	if schema != nil {
		t.Error("Expected schema to be nil for non-existing item")
	}

	// Test wrong namespace
	schema, err = repo.Find("WrongNamespace", "string")
	if err != nil {
		t.Errorf("Expected no error, got %v", err)
	}
	if schema != nil {
		t.Error("Expected schema to be nil for wrong namespace")
	}
}

func TestKIRunSchemaRepository_Filter(t *testing.T) {
	repo := NewKIRunSchemaRepository()

	// Test filtering by partial name
	names, err := repo.Filter("string")
	if err != nil {
		t.Errorf("Expected no error, got %v", err)
	}
	if len(names) == 0 {
		t.Error("Expected to find at least one schema with 'string' in name")
	}

	// Test case insensitive filtering
	names, err = repo.Filter("STRING")
	if err != nil {
		t.Errorf("Expected no error, got %v", err)
	}
	if len(names) == 0 {
		t.Error("Expected to find schemas with case insensitive search")
	}

	// Test filtering with no matches
	names, err = repo.Filter("nonexistent")
	if err != nil {
		t.Errorf("Expected no error, got %v", err)
	}
	if len(names) != 0 {
		t.Errorf("Expected no results, got %v", names)
	}
}

func TestKIRunSchemaRepository_BuiltInSchemas(t *testing.T) {
	repo := NewKIRunSchemaRepository()

	// Test that all expected built-in schemas exist
	expectedSchemas := []string{
		"any", "boolean", "double", "float", "integer", "long", "number", "string",
		"Timestamp", "Timeunit", "Duration", "TimeObject",
	}

	for _, schemaName := range expectedSchemas {
		schema, err := repo.Find(namespaces.SYSTEM, schemaName)
		if schemaName == "Timestamp" || schemaName == "Timeunit" ||
			schemaName == "Duration" || schemaName == "TimeObject" {
			// These are in SYSTEM_DATE namespace
			schema, err = repo.Find(namespaces.SYSTEM_DATE, schemaName)
		}

		if err != nil {
			t.Errorf("Expected no error for schema '%s', got %v", schemaName, err)
		}
		if schema == nil {
			t.Errorf("Expected schema '%s' to be found", schemaName)
		}
	}
}
