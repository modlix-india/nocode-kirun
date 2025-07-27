package tokenextractor

import (
	"errors"
	"fmt"
	"strconv"
	"strings"
)

// TokenValueExtractor interface defines methods for extracting values from tokens
type TokenValueExtractor interface {
	GetValue(token string) (any, error)
	// GetPrefix Prefix should be suffixed with a dot. For example, "Store."
	GetPrefix() string
	GetStore() any
}

type BaseTokenValueExtractor struct {
	prefix string
	store  any
}

func (b *BaseTokenValueExtractor) GetPrefix() string {
	return b.prefix
}

func (b *BaseTokenValueExtractor) GetStore() any {
	return b.store
}

func (b *BaseTokenValueExtractor) GetValue(token string) (any, error) {
	return GetValueInternal(b, token)
}

func MakeTokenValueExtractor(prefix string, store any) TokenValueExtractor {
	return &BaseTokenValueExtractor{
		prefix: prefix,
		store:  store,
	}
}

func GetValueInternal(b TokenValueExtractor, token string) (any, error) {

	prefixLength := len(b.GetPrefix())

	if !strings.HasPrefix(token, b.GetPrefix()[:prefixLength-1]) {
		return nil, fmt.Errorf("token %s doesn't start with %s", token, b.GetPrefix())
	}

	if strings.HasSuffix(token, ".__index") {
		parentPart := token[:len(token)-len(".__index")]
		parentValue, err := GetValueFrom(b.GetStore(), parentPart)
		if err != nil {
			return nil, err
		}

		// Check if parentValue has __index property
		if m, ok := parentValue.(map[string]any); ok {
			if idx, exists := m["__index"]; exists {
				return idx, nil
			}
		}

		if strings.HasSuffix(parentPart, "]") {
			indexString := parentPart[strings.LastIndex(parentPart, "[")+1 : len(parentPart)-1]
			indexInt, err := strconv.Atoi(indexString)
			if err != nil {
				return indexString, nil
			}
			return indexInt, nil
		} else {
			return parentPart[strings.LastIndex(parentPart, ".")+1:], nil
		}
	}

	if token[prefixLength-1] == '[' {
		return GetValueFrom(b.GetStore(), token[prefixLength-1:])
	}

	return GetValueFrom(b.GetStore(), token[prefixLength:])
}

// parse path splits a path like "user.address.street[0]" or "user['address']" into parts, without a regex.
func parsePathNoRegex(path string) ([]string, error) {
	var parts []string
	var buf strings.Builder
	inBracket := false
	inQuote := false
	quoteChar := byte(0)

	for i := 0; i < len(path); i++ {
		c := path[i]
		switch {
		case inBracket:
			if inQuote {
				if c == quoteChar {
					inQuote = false
				} else {
					buf.WriteByte(c)
				}
			} else {
				switch c {
				case '\'', '"':
					inQuote = true
					quoteChar = c
				case ']':
					parts = append(parts, buf.String())
					buf.Reset()
					inBracket = false
				default:
					buf.WriteByte(c)
				}
			}
		case c == '.':
			if buf.Len() > 0 {
				parts = append(parts, buf.String())
				buf.Reset()
			}
		case c == '[':
			if buf.Len() > 0 {
				parts = append(parts, buf.String())
				buf.Reset()
			}
			inBracket = true
		default:
			buf.WriteByte(c)
		}
	}
	if buf.Len() > 0 {
		parts = append(parts, buf.String())
	}
	if inBracket || inQuote {
		return nil, errors.New("invalid path: unclosed bracket or quote")
	}
	return parts, nil
}

func GetValueFrom(store any, path string) (any, error) {
	parts, err := parsePathNoRegex(path)
	if err != nil {
		return nil, err
	}

	var current = store
	for _, part := range parts {
		switch curr := current.(type) {
		case map[string]any:
			current = curr[part]
		case []any:
			idx, err := strconv.Atoi(part)
			if err != nil {
				return nil, errors.New("invalid array index: " + part)
			}
			if idx < 0 || idx >= len(curr) {
				return nil, errors.New("array index out of range")
			}
			current = curr[idx]
		default:
			return nil, nil
		}
	}
	return current, nil
}
