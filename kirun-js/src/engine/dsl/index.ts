/**
 * DSL Module Exports
 */

// Lexer
export { DSLToken, DSLTokenType, SourceLocation } from './lexer/DSLToken';
export { DSLLexer } from './lexer/DSLLexer';
export { LexerError } from './lexer/LexerError';
export { isKeyword, isBlockName, isPrimitiveType } from './lexer/Keywords';

// Parser
export { DSLParser } from './parser/DSLParser';
export { DSLParserError } from './parser/DSLParserError';

// AST Nodes
export * from './parser/ast';

// Transformers
export { ASTToJSONTransformer } from './transformer/ASTToJSON';
export { JSONToTextTransformer } from './transformer/JSONToText';
export { SchemaTransformer } from './transformer/SchemaTransformer';
export { ExpressionHandler } from './transformer/ExpressionHandler';

// High-level API
export { DSLCompiler } from './DSLCompiler';
export type { ValidationResult, FormattedError } from './DSLCompiler';

// Monaco integration helpers
export { DSLFunctionProvider } from './monaco/DSLFunctionProvider';
export type { FunctionInfo, ParameterInfo, EventInfo } from './monaco/DSLFunctionProvider';
