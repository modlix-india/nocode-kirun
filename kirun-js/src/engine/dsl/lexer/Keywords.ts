/**
 * DSL Keywords and helpers
 */

export const KEYWORDS = new Set([
    // Function structure keywords
    'FUNCTION',
    'NAMESPACE',
    'PARAMETERS',
    'EVENTS',
    'LOGIC',

    // Type and schema keywords
    'AS',
    'OF',
    'WITH',
    'DEFAULT',
    'VALUE',

    // Control flow modifiers
    'AFTER',
    'IF',

    // Primitive types
    'INTEGER',
    'LONG',
    'FLOAT',
    'DOUBLE',
    'STRING',
    'BOOLEAN',
    'NULL',
    'ANY',
    'ARRAY',
    'OBJECT',

    // Boolean literals (also keywords)
    'true',
    'false',
    'null',
]);

export const BLOCK_NAMES = new Set([
    'iteration',
    'true',
    'false',
    'output',
    'error',
]);

export const PRIMITIVE_TYPES = new Set([
    'INTEGER',
    'LONG',
    'FLOAT',
    'DOUBLE',
    'STRING',
    'BOOLEAN',
    'NULL',
    'ANY',
]);

/**
 * Check if a string is a keyword
 */
export function isKeyword(str: string): boolean {
    return KEYWORDS.has(str);
}

/**
 * Check if a string is a block name
 */
export function isBlockName(str: string): boolean {
    return BLOCK_NAMES.has(str);
}

/**
 * Check if a string is a primitive type
 */
export function isPrimitiveType(str: string): boolean {
    return PRIMITIVE_TYPES.has(str);
}
