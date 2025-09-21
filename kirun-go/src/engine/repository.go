package engine

// Repository defines the interface for finding and filtering items by namespace and name
type Repository[T any] interface {
	// Find retrieves an item by namespace and name
	// Returns the item if found, a boolean indicating if found, and an error if something went wrong
	Find(namespace, name string) (T, error)

	// Filter returns a list of names that match the given filter
	// Returns a slice of matching names and an error if something went wrong
	Filter(name string) ([]string, error)
}
