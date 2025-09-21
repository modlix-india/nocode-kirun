# Makefile for protobuf code generation

# Variables
PROTO_DIR := src/proto
GENERATED_DIR := src/proto/generated
PROTO_FILE := $(PROTO_DIR)/schema.proto

# Default target
.PHONY: all
all: proto

# Install protoc and protoc-gen-go if not already installed
.PHONY: install-tools
install-tools:
	@echo "Installing protoc and protoc-gen-go..."
	@if ! command -v protoc >/dev/null 2>&1; then \
		echo "Installing protoc..."; \
		if [[ "$$OSTYPE" == "darwin"* ]]; then \
			brew install protobuf; \
		elif [[ "$$OSTYPE" == "linux-gnu"* ]]; then \
			sudo apt-get update && sudo apt-get install -y protobuf-compiler; \
		else \
			echo "Please install protoc manually for your OS"; \
			exit 1; \
		fi \
	fi
	@if ! command -v protoc-gen-go >/dev/null 2>&1; then \
		echo "Installing protoc-gen-go..."; \
		go install google.golang.org/protobuf/cmd/protoc-gen-go@latest; \
	fi

# Create generated directory
$(GENERATED_DIR):
	mkdir -p $(GENERATED_DIR)

# Generate Go code from protobuf
.PHONY: proto
proto: install-tools $(GENERATED_DIR)
	@echo "Generating Go code from $(PROTO_FILE)..."
	export PATH=$$PATH:$$(go env GOPATH)/bin && \
	protoc --go_out=$(GENERATED_DIR) --go_opt=paths=source_relative \
		--go-grpc_out=$(GENERATED_DIR) --go-grpc_opt=paths=source_relative \
		--proto_path=$(PROTO_DIR) $(PROTO_FILE)
	@echo "Generated files in $(GENERATED_DIR)/"

# Clean generated files
.PHONY: clean
clean:
	@echo "Cleaning generated files..."
	rm -rf $(GENERATED_DIR)
	@echo "Cleaned $(GENERATED_DIR)/"

# Run tests
.PHONY: test
test:
	@echo "Running tests..."
	cd src && go test ./...

# Build the project
.PHONY: build
build: proto
	@echo "Building project..."
	cd src && go build ./...

# Install dependencies
.PHONY: deps
deps:
	@echo "Installing dependencies..."
	cd src && go mod tidy

# Format code
.PHONY: fmt
fmt:
	@echo "Formatting code..."
	cd src && go fmt ./...

# Lint code
.PHONY: lint
lint:
	@echo "Linting code..."
	cd src && go vet ./...

# Show help
.PHONY: help
help:
	@echo "Available targets:"
	@echo "  all         - Generate protobuf code (default)"
	@echo "  proto       - Generate Go code from protobuf files"
	@echo "  install-tools - Install protoc and protoc-gen-go"
	@echo "  clean       - Remove generated files"
	@echo "  test        - Run tests"
	@echo "  build       - Build the project"
	@echo "  deps        - Install dependencies"
	@echo "  fmt         - Format code"
	@echo "  lint        - Lint code"
	@echo "  help        - Show this help"
