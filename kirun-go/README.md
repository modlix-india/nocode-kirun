# Kirun Go - No-Code Engine

A Go-based no-code engine with protobuf support for JSON schema definitions.

## Features

- JSON Schema validation and processing
- Protobuf support for efficient serialization
- Expression evaluation engine
- Built-in functions support
- Comprehensive testing framework

## Prerequisites

- Go 1.24.4 or later
- Protocol Buffers compiler (protoc)
- Make (for build automation)

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd kirun-go
```

2. Install dependencies:
```bash
make deps
```

3. Install protobuf tools (if not already installed):
```bash
make install-tools
```

## Building

### Generate Protobuf Code

To generate Go code from the protobuf schema:

```bash
make proto
```

This will:
- Install protoc and protoc-gen-go if not present
- Generate Go code from `src/proto/schema.proto`
- Place generated files in `src/proto/generated/`

### Build the Project

```bash
make build
```

### Run Tests

```bash
make test
```

### Format Code

```bash
make fmt
```

### Lint Code

```bash
make lint
```

## Project Structure

```
kirun-go/
├── src/
│   ├── engine/
│   │   ├── runtime/
│   │   │   └── expression/     # Expression evaluation engine
│   │   └── util/               # Utility functions
│   ├── proto/
│   │   ├── schema.proto        # Protobuf schema definition
│   │   └── generated/          # Generated Go code from protobuf
│   ├── examples/
│   │   └── protobuf_example.go # Example usage of protobuf
│   ├── go.mod
│   └── go.sum
├── Concept/
│   ├── Documentation/          # Language documentation
│   └── Example Programs/       # Example programs
├── Makefile                    # Build automation
└── README.md
```

## Protobuf Usage

The project includes a comprehensive protobuf schema for JSON Schema definitions. Here's a quick example:

```go
package main

import (
    "github.com/modlix-india/nocode-kirun/schema"
)

func main() {
    // Create a string schema
    stringSchema := &schema.Schema{
        Namespace: "example",
        Name:      "UserSchema",
        Version:   1,
        Type: &schema.Type{
            TypeUnion: &schema.Type_SingleType{
                SingleType: &schema.SingleType{
                    Type: schema.SchemaType_STRING,
                },
            },
        },
        Description: "A simple string schema",
        MinLength:   1,
        MaxLength:   100,
    }
    
    // Serialize to protobuf binary
    data, err := stringSchema.Marshal()
    if err != nil {
        log.Fatal(err)
    }
    
    // Deserialize back
    var deserialized schema.Schema
    err = deserialized.Unmarshal(data)
    if err != nil {
        log.Fatal(err)
    }
}
```

For more examples, see `src/examples/protobuf_example.go`.

## Available Make Targets

- `make all` - Generate protobuf code (default)
- `make proto` - Generate Go code from protobuf files
- `make install-tools` - Install protoc and protoc-gen-go
- `make clean` - Remove generated files
- `make test` - Run tests
- `make build` - Build the project
- `make deps` - Install dependencies
- `make fmt` - Format code
- `make lint` - Lint code
- `make help` - Show help

## Development

1. Make changes to `src/proto/schema.proto` if you need to modify the protobuf schema
2. Run `make proto` to regenerate Go code
3. Update your Go code to use the new schema
4. Run `make test` to ensure everything works
5. Run `make build` to build the project

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests and ensure they pass
5. Submit a pull request

## License

[Add your license information here]
