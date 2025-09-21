package repository

import (
	"strings"

	"github.com/modlix-india/nocode-kirun/engine"
	"github.com/modlix-india/nocode-kirun/engine/json/schema"
	"github.com/modlix-india/nocode-kirun/engine/model"
	"github.com/modlix-india/nocode-kirun/engine/namespaces"
)

// KIRunSchemaRepository implements Repository[Schema] for built-in schemas
type KIRunSchemaRepository struct {
	schemaMap       map[string]*schema.Schema
	filterableNames []string
}

// NewKIRunSchemaRepository creates a new KIRunSchemaRepository
func NewKIRunSchemaRepository() *KIRunSchemaRepository {
	repo := &KIRunSchemaRepository{
		schemaMap: make(map[string]*schema.Schema),
	}

	// Initialize built-in schemas
	repo.initializeSchemas()

	// Build filterable names
	repo.buildFilterableNames()

	return repo
}

// Find implements Repository[Schema].Find
func (r *KIRunSchemaRepository) Find(namespace, name string) (*schema.Schema, error) {
	if namespace != namespaces.SYSTEM && namespace != namespaces.SYSTEM_DATE {
		return nil, nil
	}

	schema, exists := r.schemaMap[name]
	if !exists {
		return nil, nil
	}

	return schema, nil
}

// Filter implements Repository[Schema].Filter
func (r *KIRunSchemaRepository) Filter(name string) ([]string, error) {
	var result []string
	lowerName := strings.ToLower(name)

	for _, fullName := range r.filterableNames {
		if strings.Contains(strings.ToLower(fullName), lowerName) {
			result = append(result, fullName)
		}
	}

	return result, nil
}

// initializeSchemas initializes all built-in schemas
func (r *KIRunSchemaRepository) initializeSchemas() {
	// Basic types
	r.schemaMap["any"] = schema.OfAny("any").SetNamespace(namespaces.SYSTEM)
	r.schemaMap["boolean"] = schema.OfBoolean("boolean").SetNamespace(namespaces.SYSTEM)
	r.schemaMap["double"] = schema.OfDouble("double").SetNamespace(namespaces.SYSTEM)
	r.schemaMap["float"] = schema.OfFloat("float").SetNamespace(namespaces.SYSTEM)
	r.schemaMap["integer"] = schema.OfInteger("integer").SetNamespace(namespaces.SYSTEM)
	r.schemaMap["long"] = schema.OfLong("long").SetNamespace(namespaces.SYSTEM)
	r.schemaMap["number"] = schema.OfNumber("number").SetNamespace(namespaces.SYSTEM)
	r.schemaMap["string"] = schema.OfString("string").SetNamespace(namespaces.SYSTEM)

	// Date types
	r.schemaMap["Timestamp"] = schema.OfString("Timestamp").SetNamespace(namespaces.SYSTEM_DATE)

	// Timeunit enum
	r.schemaMap["Timeunit"] = schema.OfString("Timeunit").
		SetNamespace(namespaces.SYSTEM_DATE).
		SetEnums([]string{
			"YEARS", "QUARTERS", "MONTHS", "WEEKS", "DAYS",
			"HOURS", "MINUTES", "SECONDS", "MILLISECONDS",
		})

	// Duration object
	r.schemaMap["Duration"] = schema.OfObject("Duration").
		SetNamespace(namespaces.SYSTEM_DATE).
		SetProperties(map[string]*schema.Schema{
			"years":        schema.OfNumber("years"),
			"quarters":     schema.OfNumber("quarters"),
			"months":       schema.OfNumber("months"),
			"weeks":        schema.OfNumber("weeks"),
			"days":         schema.OfNumber("days"),
			"hours":        schema.OfNumber("hours"),
			"minutes":      schema.OfNumber("minutes"),
			"seconds":      schema.OfNumber("seconds"),
			"milliseconds": schema.OfNumber("milliseconds"),
		}).
		SetAdditionalItems(schema.NewAdditionalTypeFromBool(false).AdditionalType)

	// TimeObject
	r.schemaMap["TimeObject"] = schema.OfObject("TimeObject").
		SetNamespace(namespaces.SYSTEM_DATE).
		SetProperties(map[string]*schema.Schema{
			"year":        schema.OfNumber("year"),
			"month":       schema.OfNumber("month"),
			"day":         schema.OfNumber("day"),
			"hour":        schema.OfNumber("hour"),
			"minute":      schema.OfNumber("minute"),
			"second":      schema.OfNumber("second"),
			"millisecond": schema.OfNumber("millisecond"),
		}).
		SetAdditionalItems(schema.NewAdditionalTypeFromBool(false).AdditionalType)

	// Parameter expression
	expressionSchema := model.GetExpressionSchema()
	r.schemaMap[expressionSchema.GetName()] = expressionSchema

	// NULL and SCHEMA constants
	r.schemaMap[schema.NULL_SCHEMA.GetName()] = schema.NULL_SCHEMA
	r.schemaMap[schema.SCHEMA_SCHEMA.GetName()] = schema.SCHEMA_SCHEMA
}

// buildFilterableNames builds the list of filterable names
func (r *KIRunSchemaRepository) buildFilterableNames() {
	for _, schema := range r.schemaMap {
		r.filterableNames = append(r.filterableNames, schema.GetFullName())
	}
}

// Ensure KIRunSchemaRepository implements Repository[Schema]
var _ engine.Repository[*schema.Schema] = (*KIRunSchemaRepository)(nil)
