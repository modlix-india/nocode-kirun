package util

import (
	"reflect"
)

// DeepEqual performs a deep equality check between two values
func DeepEqual(a, b interface{}) bool {
	return reflect.DeepEqual(a, b)
}
