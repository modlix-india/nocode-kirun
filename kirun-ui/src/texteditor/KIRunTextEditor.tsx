import React, { useEffect, useRef } from 'react';
import * as monaco from 'monaco-editor';
import {
    DSLCompiler,
    Repository,
    Function,
    Schema,
    FunctionDefinition,
    KIRuntime,
    StatementMessageType,
} from '@fincity/kirun-js';
import { registerKIRunLanguage } from './syntax/kirunLanguage';
import { registerKIRunThemes } from './syntax/kirunTheme';
import {
    registerKIRunCompletionProvider,
    setCompletionFunctionRepository,
} from './syntax/kirunCompletion';
import { setMonacoForJsonEditor } from '../components/JsonEditorPopup';
import {
    APP_MONACO_THEME,
    buildAppMonacoTheme,
    domColorReader,
    isBuiltInEditorTheme,
    readCodeFont,
    watchHostStyles,
} from '../editor/editorTheme';
import type { EditorTheme } from '../editor/editorTheme';

// Set monaco instance immediately at module load for components that need it
// (e.g. JsonEditorPopup used by visual editor before text editor mounts)
setMonacoForJsonEditor(monaco);

export type { EditorTheme };

// Monaco's own platform default, restored when leaving an app theme that set a font.
const DEFAULT_CODE_FONT = monaco.editor.EditorOptions.fontFamily.defaultValue;

function getMonacoThemeName(theme: string): string {
    if (!isBuiltInEditorTheme(theme)) return APP_MONACO_THEME;
    const themeMap: Record<EditorTheme, string> = {
        light: 'kirun-light',
        dark: 'kirun-dark',
        'high-contrast': 'kirun-high-contrast',
        'easy-on-eyes': 'kirun-easy-on-eyes',
        'flared-up': 'kirun-flared-up',
    };
    return themeMap[theme] || 'kirun-light';
}

interface KIRunTextEditorProps {
    value: string;
    onChange: (value: string) => void;
    onValidate?: (errors: monaco.editor.IMarker[]) => void;
    /** A built-in theme name, or any other name to take the colours from the host's --kirun-* variables. */
    theme?: string;
    readOnly?: boolean;
    wordWrap?: 'on' | 'off';
    functionRepository?: Repository<Function>;
    schemaRepository?: Repository<Schema>;
    onEditorReady?: (editor: monaco.editor.IStandaloneCodeEditor) => void;
}

let languageRegistered = false;

export function KIRunTextEditor({
    value,
    onChange,
    onValidate,
    theme = 'dark',
    readOnly = false,
    wordWrap = 'on',
    functionRepository,
    schemaRepository,
    onEditorReady,
}: KIRunTextEditorProps) {
    const containerRef = useRef<HTMLDivElement>(null);
    const editorRef = useRef<monaco.editor.IStandaloneCodeEditor | null>(null);
    const monacoRef = useRef<typeof monaco | null>(null);
    const reposRef = useRef({ functionRepository, schemaRepository });

    useEffect(() => {
        reposRef.current = { functionRepository, schemaRepository };
    }, [functionRepository, schemaRepository]);

    useEffect(() => {
        setCompletionFunctionRepository(functionRepository);
    }, [functionRepository]);

    useEffect(() => {
        if (!containerRef.current) return;

        if (!languageRegistered) {
            registerKIRunLanguage(monaco);
            registerKIRunThemes(monaco);
            registerKIRunCompletionProvider(monaco);
            languageRegistered = true;
        }

        const editor = monaco.editor.create(containerRef.current, {
            value,
            language: 'kirun-dsl',
            theme: getMonacoThemeName(theme),
            readOnly,
            minimap: { enabled: true },
            lineNumbers: 'on',
            folding: true,
            automaticLayout: true,
            scrollBeyondLastLine: false,
            wordWrap,
            fontSize: 14,
            tabSize: 4,
            insertSpaces: true,
        });

        editorRef.current = editor;
        monacoRef.current = monaco;

        if (onEditorReady) {
            onEditorReady(editor);
        }

        const onChangeDisposable = editor.onDidChangeModelContent(() => {
            const newValue = editor.getValue();
            onChange(newValue);
        });

        const buildStatementLineMap = (text: string): Map<string, number> => {
            const lineMap = new Map<string, number>();
            const lines = text.split('\n');
            const stmtPattern = /^\s*([a-zA-Z_]\w*)\s*:/;
            for (let i = 0; i < lines.length; i++) {
                const match = lines[i].match(stmtPattern);
                if (match) {
                    lineMap.set(match[1], i + 1);
                }
            }
            return lineMap;
        };

        let validationTimeout: ReturnType<typeof setTimeout>;
        const validateContent = async () => {
            const model = editor.getModel();
            if (!model) return;

            const text = model.getValue();
            const markers: monaco.editor.IMarkerData[] = [];

            try {
                const syntaxResult = DSLCompiler.validate(text);
                if (!syntaxResult.valid) {
                    for (const err of syntaxResult.errors) {
                        markers.push({
                            severity: monaco.MarkerSeverity.Error,
                            startLineNumber: err.line || 1,
                            startColumn: err.column || 1,
                            endLineNumber: err.line || 1,
                            endColumn: (err.column || 1) + 10,
                            message: err.message,
                        });
                    }
                    monaco.editor.setModelMarkers(model, 'kirun-dsl', markers);
                    onValidate?.(markers as any);
                    return;
                }

                const { functionRepository: fRepo, schemaRepository: sRepo } = reposRef.current;
                if (!fRepo || !sRepo) {
                    monaco.editor.setModelMarkers(model, 'kirun-dsl', []);
                    onValidate?.([]);
                    return;
                }

                const json = DSLCompiler.compile(text);
                const funcDef = FunctionDefinition.from(json);
                const runtime = new KIRuntime(funcDef);

                const executionPlan = await runtime.getExecutionPlan(fRepo, sRepo);

                const statementLineMap = buildStatementLineMap(text);

                const nodeMap = executionPlan.getNodeMap();
                for (const [statementName, vertex] of nodeMap) {
                    const stmtExecution = vertex.getData();
                    const messages = stmtExecution.getMessages();

                    for (const msg of messages) {
                        const msgType = msg.getMessageType();
                        const msgText = msg.getMessage();

                        if (msgType === StatementMessageType.ERROR) {
                            const lineNumber = statementLineMap.get(statementName) || 1;
                            markers.push({
                                severity: monaco.MarkerSeverity.Error,
                                startLineNumber: lineNumber,
                                startColumn: 1,
                                endLineNumber: lineNumber,
                                endColumn: 1000,
                                message: `${statementName}: ${msgText}`,
                            });
                        } else if (msgType === StatementMessageType.WARNING) {
                            const lineNumber = statementLineMap.get(statementName) || 1;
                            markers.push({
                                severity: monaco.MarkerSeverity.Warning,
                                startLineNumber: lineNumber,
                                startColumn: 1,
                                endLineNumber: lineNumber,
                                endColumn: 1000,
                                message: `${statementName}: ${msgText}`,
                            });
                        }
                    }
                }

                monaco.editor.setModelMarkers(model, 'kirun-dsl', markers);
                onValidate?.(markers as any);
            } catch (error: any) {
                markers.push({
                    severity: monaco.MarkerSeverity.Error,
                    startLineNumber: 1,
                    startColumn: 1,
                    endLineNumber: 1,
                    endColumn: 10,
                    message: error.message || 'Unknown error',
                });
                monaco.editor.setModelMarkers(model, 'kirun-dsl', markers);
                onValidate?.(markers as any);
            }
        };

        const onContentChangeDisposable = editor.onDidChangeModelContent(() => {
            clearTimeout(validationTimeout);
            validationTimeout = setTimeout(validateContent, 500);
        });

        validateContent();

        return () => {
            onChangeDisposable.dispose();
            onContentChangeDisposable.dispose();
            clearTimeout(validationTimeout);
            editor.dispose();
        };
    }, []);

    useEffect(() => {
        if (editorRef.current && editorRef.current.getValue() !== value) {
            editorRef.current.setValue(value);
        }
    }, [value]);

    useEffect(() => {
        if (!monacoRef.current) return;
        if (isBuiltInEditorTheme(theme)) {
            monacoRef.current.editor.setTheme(getMonacoThemeName(theme));
            editorRef.current?.updateOptions({ fontFamily: DEFAULT_CODE_FONT });
            return;
        }
        // An app theme: Monaco cannot read CSS variables, so build its theme from them now and
        // again whenever the host's styles change (a light/dark switch, for one).
        const applyAppTheme = () => {
            const root = containerRef.current;
            if (!root || !monacoRef.current) return;
            const reader = domColorReader(root);
            try {
                monacoRef.current.editor.defineTheme(APP_MONACO_THEME, buildAppMonacoTheme(reader.read));
            } finally {
                reader.dispose();
            }
            monacoRef.current.editor.setTheme(APP_MONACO_THEME);
            editorRef.current?.updateOptions({ fontFamily: readCodeFont(root) ?? DEFAULT_CODE_FONT });
        };
        applyAppTheme();
        return watchHostStyles(applyAppTheme);
    }, [theme]);

    useEffect(() => {
        if (editorRef.current) {
            editorRef.current.updateOptions({ readOnly });
        }
    }, [readOnly]);

    useEffect(() => {
        if (editorRef.current) {
            editorRef.current.updateOptions({ wordWrap });
        }
    }, [wordWrap]);

    return (
        <div
            ref={containerRef}
            style={{
                width: '100%',
                height: '100%',
                minHeight: '400px',
            }}
        />
    );
}
