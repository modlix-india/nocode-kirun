import type * as MonacoEditor from 'monaco-editor';
import { DSLFunctionProvider, FunctionInfo, Repository, Function } from '@fincity/kirun-js';

let monaco!: typeof MonacoEditor;
let currentFunctionRepository: Repository<Function> | undefined = undefined;

export function setCompletionFunctionRepository(repo: Repository<Function> | undefined) {
    if (currentFunctionRepository !== repo) {
        currentFunctionRepository = repo;
    }
}

function parseFunctionNameFromContext(textBeforeCursor: string): string | null {
    let parenDepth = 0;
    let lastOpenParenIndex = -1;

    for (let i = textBeforeCursor.length - 1; i >= 0; i--) {
        const char = textBeforeCursor[i];
        if (char === ')') {
            parenDepth++;
        } else if (char === '(') {
            if (parenDepth === 0) {
                lastOpenParenIndex = i;
                break;
            }
            parenDepth--;
        }
    }

    if (lastOpenParenIndex === -1) {
        return null;
    }

    const textBeforeParen = textBeforeCursor.substring(0, lastOpenParenIndex);

    const funcNameMatch = textBeforeParen.match(
        /([A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)+)\s*$/,
    );
    if (funcNameMatch) {
        return funcNameMatch[1];
    }

    return null;
}

function getUsedParameters(textBeforeCursor: string): Set<string> {
    const usedParams = new Set<string>();

    let parenDepth = 0;
    let funcStartIndex = -1;

    for (let i = textBeforeCursor.length - 1; i >= 0; i--) {
        const char = textBeforeCursor[i];
        if (char === ')') {
            parenDepth++;
        } else if (char === '(') {
            if (parenDepth === 0) {
                funcStartIndex = i;
                break;
            }
            parenDepth--;
        }
    }

    if (funcStartIndex === -1) return usedParams;

    const argsContent = textBeforeCursor.substring(funcStartIndex + 1);

    const paramMatches = argsContent.matchAll(/([a-zA-Z_][a-zA-Z0-9_]*)\s*=/g);
    for (const match of paramMatches) {
        usedParams.add(match[1]);
    }

    return usedParams;
}

export function registerKIRunCompletionProvider(monacoInstance: typeof MonacoEditor) {
    monaco = monacoInstance;
    monaco.languages.registerCompletionItemProvider('kirun-dsl', {
        triggerCharacters: ['.', ' ', ':', '(', ','],

        provideCompletionItems: async (model, position) => {
            const word = model.getWordUntilPosition(position);
            const range = {
                startLineNumber: position.lineNumber,
                endLineNumber: position.lineNumber,
                startColumn: word.startColumn,
                endColumn: word.endColumn,
            };

            const lineContent = model.getLineContent(position.lineNumber);
            const textBeforeCursor = lineContent.substring(0, position.column - 1);

            let functions: FunctionInfo[] = [];
            try {
                functions = await DSLFunctionProvider.getAllFunctions(currentFunctionRepository);
            } catch (error) {
                console.warn('Failed to load functions for autocomplete:', error);
            }

            const suggestions: MonacoEditor.languages.CompletionItem[] = [];

            const functionName = parseFunctionNameFromContext(textBeforeCursor);
            if (functionName) {
                const func = functions.find((f) => f.fullName === functionName);
                if (func && func.parameters.length > 0) {
                    const usedParams = getUsedParameters(textBeforeCursor);
                    suggestions.push(...getParameterSuggestions(func, range, usedParams));
                    return { suggestions };
                }
            }

            if (textBeforeCursor.trim().endsWith('AS')) {
                suggestions.push(...getSchemaTypeSuggestions(range));
            } else if (
                textBeforeCursor.includes(':') &&
                !textBeforeCursor.trim().startsWith('//')
            ) {
                suggestions.push(...getFunctionSuggestions(functions, range));
            } else if (
                textBeforeCursor.trim() === '' ||
                textBeforeCursor.trim().endsWith('\n')
            ) {
                suggestions.push(...getKeywordSuggestions(range));
                suggestions.push(...getBlockNameSuggestions(range));
            } else {
                suggestions.push(...getKeywordSuggestions(range));
                suggestions.push(...getFunctionSuggestions(functions, range));
            }

            return { suggestions };
        },
    });
}

function getKeywordSuggestions(range: MonacoEditor.IRange): MonacoEditor.languages.CompletionItem[] {
    const keywords = [
        {
            label: 'FUNCTION',
            insertText: 'FUNCTION ${1:name}\n\tNAMESPACE ${2:namespace}\n\tLOGIC\n\t\t$0',
            detail: 'Function definition',
        },
        { label: 'NAMESPACE', insertText: 'NAMESPACE ${1:namespace}', detail: 'Namespace' },
        { label: 'PARAMETERS', insertText: 'PARAMETERS\n\t$0', detail: 'Parameters section' },
        { label: 'EVENTS', insertText: 'EVENTS\n\t$0', detail: 'Events section' },
        { label: 'LOGIC', insertText: 'LOGIC\n\t$0', detail: 'Logic section' },
        { label: 'AFTER', insertText: 'AFTER Steps.$0', detail: 'After clause' },
        { label: 'IF', insertText: 'IF Steps.$0', detail: 'If clause' },
    ];

    return keywords.map((kw) => ({
        label: kw.label,
        kind: monaco.languages.CompletionItemKind.Keyword,
        insertText: kw.insertText,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        detail: kw.detail,
        range,
    }));
}

function getSchemaTypeSuggestions(range: MonacoEditor.IRange): MonacoEditor.languages.CompletionItem[] {
    const types = [
        { label: 'INTEGER', detail: 'Integer type' },
        { label: 'LONG', detail: 'Long integer type' },
        { label: 'FLOAT', detail: 'Float type' },
        { label: 'DOUBLE', detail: 'Double precision float' },
        { label: 'STRING', detail: 'String type' },
        { label: 'BOOLEAN', detail: 'Boolean type' },
        { label: 'NULL', detail: 'Null type' },
        { label: 'ANY', detail: 'Any type' },
        { label: 'OBJECT', detail: 'Object type' },
        { label: 'ARRAY OF', insertText: 'ARRAY OF $0', detail: 'Array type' },
    ];

    return types.map((type) => ({
        label: type.label,
        kind: monaco.languages.CompletionItemKind.TypeParameter,
        insertText: type.insertText || type.label,
        insertTextRules: type.insertText
            ? monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            : undefined,
        detail: type.detail,
        range,
    }));
}

function getFunctionSuggestions(
    functions: FunctionInfo[],
    range: MonacoEditor.IRange,
): MonacoEditor.languages.CompletionItem[] {
    return functions.map((func) => {
        const params = func.parameters.map((p) => `${p.name} = \${${p.name}}`).join(', ');
        const insertText = `${func.fullName}(${params})$0`;

        return {
            label: func.fullName,
            kind: monaco.languages.CompletionItemKind.Function,
            insertText,
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            detail: `${func.namespace}.${func.name}`,
            documentation: func.description || `Function: ${func.fullName}`,
            range,
        };
    });
}

function getParameterSuggestions(
    func: FunctionInfo,
    range: MonacoEditor.IRange,
    usedParams: Set<string>,
): MonacoEditor.languages.CompletionItem[] {
    return func.parameters
        .filter((param) => !usedParams.has(param.name))
        .map((param, index) => ({
            label: param.name,
            kind: monaco.languages.CompletionItemKind.Field,
            insertText: `${param.name} = `,
            detail: `${param.type}${param.required ? ' (required)' : ''}`,
            documentation: `Parameter: ${param.name}\nType: ${param.type}${param.required ? '\nRequired' : ''}`,
            range,
            sortText: String(index).padStart(3, '0'),
        }));
}

function getBlockNameSuggestions(range: MonacoEditor.IRange): MonacoEditor.languages.CompletionItem[] {
    const blocks = [
        { label: 'iteration', detail: 'Loop iteration block' },
        { label: 'true', detail: 'If true block' },
        { label: 'false', detail: 'If false block' },
        { label: 'output', detail: 'Output block' },
        { label: 'error', detail: 'Error block' },
    ];

    return blocks.map((block) => ({
        label: block.label,
        kind: monaco.languages.CompletionItemKind.Keyword,
        insertText: block.label,
        detail: block.detail,
        range,
    }));
}
