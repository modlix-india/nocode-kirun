import { Schema } from '../json/schema/Schema';
import { SchemaType } from '../json/schema/type/SchemaType';

/**
 * Utility class for formatting error messages with proper value representation
 */
export class ErrorMessageFormatter {
    /**
     * Formats a value for display in error messages.
     * - JSON.stringify for objects and arrays
     * - toString() for primitives
     * - Handles circular references
     * @param value - The value to format
     * @param maxLength - Maximum length of the formatted string (default: 200)
     * @returns Formatted string representation
     */
    public static formatValue(value: any, maxLength: number = 200): string {
        if (value === null) return 'null';
        if (value === undefined) return 'undefined';

        const type = typeof value;

        // For primitives, use toString()
        if (type === 'string') return `"${value}"`;
        if (type === 'number' || type === 'boolean') return String(value);

        // For objects and arrays, use JSON.stringify with circular reference handling
        try {
            const seen = new WeakSet();
            const json = JSON.stringify(
                value,
                (key, val) => {
                    if (typeof val === 'object' && val !== null) {
                        if (seen.has(val)) {
                            return '[Circular]';
                        }
                        seen.add(val);
                    }
                    return val;
                },
            );

            // For better readability, add minimal spacing for objects/arrays
            let formatted = json;
            if (json.length > 2) {
                // Add spaces after colons and commas for readability
                formatted = json
                    .replaceAll(',', ', ')
                    .replaceAll(':', ': ')
                    .replaceAll(/\s+/g, ' '); // Normalize multiple spaces to single space
            }

            // Truncate if too long
            if (formatted.length > maxLength) {
                return formatted.substring(0, maxLength) + '...';
            }
            return formatted;
        } catch (err) {
            // Fallback if JSON.stringify fails
            return `[${Object.prototype.toString.call(value)}]`;
        }
    }

    /**
     * Formats a function identifier (namespace.name) with proper handling of undefined values
     * @param namespace - Function namespace (can be undefined/null)
     * @param name - Function name
     * @returns Formatted function identifier
     */
    public static formatFunctionName(namespace: string | undefined | null, name: string): string {
        if (!namespace || namespace === 'undefined' || namespace === 'null') {
            return name;
        }
        return `${namespace}.${name}`;
    }

    /**
     * Formats a statement name with fallback for unknown statements
     * @param statementName - The statement name (can be undefined/null)
     * @returns Formatted statement name or null if not available
     */
    public static formatStatementName(statementName: string | undefined | null): string | null {
        if (!statementName || statementName === 'undefined' || statementName === 'null') {
            return null;
        }
        return `'${statementName}'`;
    }

    /**
     * Formats a schema definition for display in error messages
     * @param schema - The schema to format
     * @returns Formatted schema description
     */
    public static formatSchemaDefinition(schema: Schema | undefined): string {
        if (!schema) return 'any';

        const type = schema.getType();
        if (!type) return 'any';

        // Get all allowed types
        const allowedTypes = type.getAllowedSchemaTypes();

        if (allowedTypes.size === 0) return 'any';
        if (allowedTypes.size === 1) {
            const singleType = Array.from(allowedTypes)[0];
            return this.formatSingleSchemaType(schema, singleType);
        }

        // Multiple types
        const typeNames = Array.from(allowedTypes).map(t => t).join(' | ');
        return typeNames;
    }

    /**
     * Formats a single schema type with additional constraints
     * @param schema - The schema
     * @param schemaType - The schema type
     * @returns Formatted type description
     */
    private static formatSingleSchemaType(schema: Schema, schemaType: SchemaType): string {
        const typeName = schemaType;

        // Add array item type if applicable
        if (schemaType === SchemaType.ARRAY) {
            const items = schema.getItems();
            if (items) {
                const singleSchema = items.getSingleSchema();
                if (singleSchema) {
                    const itemType = this.formatSchemaDefinition(singleSchema);
                    return `Array<${itemType}>`;
                }
                const tupleSchemas = items.getTupleSchema();
                if (tupleSchemas && tupleSchemas.length > 0) {
                    const tupleTypes = tupleSchemas.map(s => this.formatSchemaDefinition(s)).join(', ');
                    return `[${tupleTypes}]`;
                }
            }
            return 'Array';
        }

        // Add enum constraint if applicable
        const enums = schema.getEnums();
        if (enums && enums.length > 0) {
            const enumValues = enums.slice(0, 5).map(e => this.formatValue(e, 50)).join(' | ');
            const more = enums.length > 5 ? ' | ...' : '';
            return `${typeName}(${enumValues}${more})`;
        }

        return typeName;
    }

    /**
     * Builds an error message for function execution with optional statement name and parameter definition
     * @param functionName - The formatted function name
     * @param statementName - The formatted statement name (or null)
     * @param errorMessage - The error message
     * @param parameterName - Optional parameter name for parameter validation errors
     * @param parameterSchema - Optional parameter schema for showing definition
     * @returns Complete error message
     */
    public static buildFunctionExecutionError(
        functionName: string,
        statementName: string | null,
        errorMessage: string,
        parameterName?: string,
        parameterSchema?: Schema,
    ): string {
        const parameterPart = parameterName ? `'s parameter ${parameterName}` : '';
        const statementPart = statementName ? ` in statement ${statementName}` : '';
        const definitionPart = parameterSchema ? ` [Expected: ${this.formatSchemaDefinition(parameterSchema)}]` : '';

        // If the error message is already a nested error (starts with "Error while executing"),
        // add a newline before it for better readability
        const separator = errorMessage.startsWith('Error while executing the function ') ? '\n' : '';

        return `Error while executing the function ${functionName}${parameterPart}${statementPart}${definitionPart}: ${separator}${errorMessage}`;
    }

    /**
     * Extracts and formats error message from various error types
     * @param error - The error object
     * @returns Formatted error message
     */
    public static formatErrorMessage(error: any): string {
        if (!error) return 'Unknown error';

        // If error is a string, return it
        if (typeof error === 'string') return error;

        // If error has a message property, use it
        if (error.message) {
            const message = String(error.message);
            // Check if the message contains object representations that need formatting
            if (message.includes('[object Object]')) {
                // Try to replace [object Object] with actual object representation
                return message.replace(/\[object Object\]/g, () => {
                    return this.formatValue(error);
                });
            }
            return message;
        }

        // If error is an object, try to stringify it
        return this.formatValue(error);
    }

    private constructor() {}
}
