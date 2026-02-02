/**
 * Schema Transformer
 * Converts between simple schema syntax and JSON Schema
 */
export class SchemaTransformer {
    private static readonly PRIMITIVE_TYPES = new Set([
        'INTEGER',
        'LONG',
        'FLOAT',
        'DOUBLE',
        'STRING',
        'BOOLEAN',
        'NULL',
        'ANY',
        'OBJECT',
    ]);

    /**
     * Transform simple schema syntax to JSON Schema
     * Examples:
     *   "INTEGER" → { type: "INTEGER" }
     *   "ARRAY OF INTEGER" → { type: "ARRAY", items: { type: "INTEGER" } }
     *   { type: "STRING", minLength: 5 } → { type: "STRING", minLength: 5 } (pass through)
     */
    public static transform(schemaSpec: string | object): any {
        // If already an object, return as-is (complex JSON Schema)
        if (typeof schemaSpec === 'object') {
            return schemaSpec;
        }

        const spec = schemaSpec.trim();

        // Handle ARRAY OF X
        if (spec.startsWith('ARRAY OF ')) {
            const innerType = spec.substring('ARRAY OF '.length).trim();
            return {
                type: 'ARRAY',
                items: this.transform(innerType),
            };
        }

        // Handle primitive types
        if (this.PRIMITIVE_TYPES.has(spec)) {
            return {
                type: spec,
            };
        }

        // If we can't parse it, return as string type with pattern
        // Unknown schema specification, treating as STRING
        return {
            type: 'STRING',
        };
    }

    /**
     * Transform JSON Schema back to simple schema syntax (best effort)
     * Examples:
     *   { type: "INTEGER" } → "INTEGER"
     *   { type: "ARRAY", items: { type: "INTEGER" } } → "ARRAY OF INTEGER"
     *   Complex schema → JSON.stringify(schema)
     */
    public static toText(schema: any): string {
        if (!schema || typeof schema !== 'object') {
            return String(schema);
        }

        // Handle ARRAY type
        if (schema.type === 'ARRAY' && schema.items) {
            const itemsText = this.toText(schema.items);
            return `ARRAY OF ${itemsText}`;
        }

        // Handle primitive types
        if (schema.type && this.PRIMITIVE_TYPES.has(schema.type)) {
            // Check if it's a simple type (no additional properties)
            const keys = Object.keys(schema);
            if (keys.length === 1 && keys[0] === 'type') {
                return schema.type;
            }
        }

        // Complex schema - return JSON
        return JSON.stringify(schema);
    }

    /**
     * Check if a schema specification is simple (can be represented as text)
     */
    public static isSimpleSchema(schema: any): boolean {
        if (!schema || typeof schema !== 'object') {
            return false;
        }

        // Simple primitive type
        if (schema.type && this.PRIMITIVE_TYPES.has(schema.type)) {
            const keys = Object.keys(schema);
            if (keys.length === 1) {
                return true;
            }
        }

        // Simple array type
        if (schema.type === 'ARRAY' && schema.items) {
            return this.isSimpleSchema(schema.items);
        }

        return false;
    }
}
