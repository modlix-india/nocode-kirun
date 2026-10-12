import type * as MonacoEditor from 'monaco-editor';

export function registerKIRunLanguage(monaco: typeof MonacoEditor) {
    monaco.languages.register({ id: 'kirun-dsl' });

    monaco.languages.setLanguageConfiguration('kirun-dsl', {
        comments: {
            blockComment: ['/*', '*/'],
        },
        brackets: [
            ['{', '}'],
            ['[', ']'],
            ['(', ')'],
        ],
        autoClosingPairs: [
            { open: '{', close: '}' },
            { open: '[', close: ']' },
            { open: '(', close: ')' },
            { open: '"', close: '"' },
            { open: "'", close: "'" },
            { open: '`', close: '`' },
        ],
        surroundingPairs: [
            { open: '{', close: '}' },
            { open: '[', close: ']' },
            { open: '(', close: ')' },
            { open: '"', close: '"' },
            { open: "'", close: "'" },
            { open: '`', close: '`' },
        ],
    });

    monaco.languages.setMonarchTokensProvider('kirun-dsl', {
        defaultToken: '',
        tokenPostfix: '.kirun',

        keywords: [
            'FUNCTION',
            'NAMESPACE',
            'PARAMETERS',
            'EVENTS',
            'LOGIC',
            'VALUES',
            'AS',
            'OF',
            'AFTER',
            'IF',
            'WITH',
            'DEFAULT',
            'VALUE',
        ],

        typeKeywords: [
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
        ],

        blockNames: ['iteration', 'true', 'false', 'output', 'error'],

        expressionPrefixes: [
            'Steps',
            'Arguments',
            'Context',
            'Store',
            'Page',
            'Application',
            'LocalStore',
            'SessionStore',
            'Parent',
            'Cookies',
            'URL',
        ],

        operators: ['=', ':', ',', '.', '+', '-', '*', '/', '%', '<', '>', '!', '&', '|', '?'],

        symbols: /[=><!~?:&|+\-*\/\^%]+/,
        escapes: /\\(?:[abfnrtv\\"'`]|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,

        tokenizer: {
            root: [
                [/`/, 'string.expression', '@backtick_string'],
                // A named value from the VALUES section: @cfg.resultShape. `[@]` because Monarch
                // reads `@word` in a regex as one of its own attributes.
                [/[@][A-Za-z0-9_][\w.]*/, 'variable.value'],
                // A step name is followed by its call; any other `name:` is an object key
                [/([a-zA-Z_]\w*)(\s*)(:)(?=\s*[a-zA-Z_][\w.]*\s*\()/, ['entity.name.tag', '', 'delimiter']],
                [/([a-zA-Z_]\w*)(\s*)(:)/, ['attribute.name', '', 'delimiter']],
                [/([a-zA-Z_][\w.]*)(\s*)(\()/, ['entity.name.function', '', '@brackets']],
                [/([a-zA-Z_]\w*)(\s*)(=)/, ['variable.parameter', '', 'operator']],
                [
                    /\b(Steps|Arguments|Context|Store|Page|Application|LocalStore|SessionStore|Parent|Cookies|URL)\b/,
                    'variable.predefined',
                ],
                [
                    /[a-zA-Z_]\w*/,
                    {
                        cases: {
                            '@keywords': 'keyword',
                            '@typeKeywords': 'type.identifier',
                            '@blockNames': 'keyword.control',
                            '@default': 'identifier',
                        },
                    },
                ],
                { include: '@whitespace' },
                [/\{\{/, 'delimiter.bracket.embed', '@embedded_expression'],
                [/[{}()\[\]]/, '@brackets'],
                [/[<>](?!@symbols)/, '@brackets'],
                [/@symbols/, 'operator'],
                [/-?\d*\.\d+([eE][\-+]?\d+)?/, 'number.float'],
                [/-?\d+/, 'number'],
                [/"([^"\\]|\\.)*$/, 'string.invalid'],
                [/'([^'\\]|\\.)*$/, 'string.invalid'],
                [/"/, 'string.value', '@string_double'],
                [/'/, 'string.expression', '@string_single'],
            ],

            whitespace: [
                [/[ \t\r\n]+/, ''],
                [/\/\*/, 'comment', '@comment'],
            ],

            comment: [
                [/[^\/*]+/, 'comment'],
                [/\*\//, 'comment', '@pop'],
                [/[\/*]/, 'comment'],
            ],

            string_double: [
                [/[^\\"]+/, 'string.value'],
                [/@escapes/, 'string.escape'],
                [/\\./, 'string.escape.invalid'],
                [/"/, 'string.value', '@pop'],
            ],

            string_single: [
                [/[^\\']+/, 'string.expression'],
                [/@escapes/, 'string.escape'],
                [/\\./, 'string.escape.invalid'],
                [/'/, 'string.expression', '@pop'],
            ],

            backtick_string: [
                [/[^\\`]+/, 'string.expression'],
                [/@escapes/, 'string.escape'],
                [/\\./, 'string.escape.invalid'],
                [/`/, 'string.expression', '@pop'],
            ],

            embedded_expression: [
                [/\}\}/, 'delimiter.bracket.embed', '@pop'],
                [/[a-zA-Z_]\w*/, 'variable'],
                [/\./, 'delimiter'],
                [/\[/, '@brackets'],
                [/\]/, '@brackets'],
                [/[^}]+/, 'variable'],
            ],
        },
    });
}
