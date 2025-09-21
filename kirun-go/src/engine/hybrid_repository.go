package engine

import "errors"

// HybridRepository implements the Repository interface by combining multiple repositories
// It searches through repositories in order and returns the first match found
type HybridRepository[T any] struct {
	repos []Repository[T]
}

// NewHybridRepository creates a new HybridRepository with the given repositories
func NewHybridRepository[T any](repos ...Repository[T]) *HybridRepository[T] {
	return &HybridRepository[T]{
		repos: repos,
	}
}

// AddRepository adds a new repository to the hybrid repository
func (hr *HybridRepository[T]) AddRepository(repo Repository[T]) {
	hr.repos = append(hr.repos, repo)
}

// Find searches through all repositories in order and returns the first match
// Returns the item if found, a boolean indicating if found, and an error if something went wrong
func (hr *HybridRepository[T]) Find(namespace, name string) (T, error) {

	var zero T

	for _, repo := range hr.repos {
		item, err := repo.Find(namespace, name)
		if err != nil {
			continue
		}
		return item, nil
	}

	return zero, errors.New("unable to find item")
}

// Filter searches through all repositories and combines their filter results
// Returns a deduplicated slice of matching names and an error if something went wrong
func (hr *HybridRepository[T]) Filter(name string) ([]string, error) {
	nameSet := make(map[string]bool)

	for _, repo := range hr.repos {
		names, err := repo.Filter(name)
		if err != nil {
			return nil, err
		}

		for _, n := range names {
			nameSet[n] = true
		}
	}

	// Convert map keys to slice
	result := make([]string, 0, len(nameSet))
	for name := range nameSet {
		result = append(result, name)
	}

	return result, nil
}
