import React, { useEffect, useRef, useState } from 'react';
import { SquareArrowOutUpRight } from 'lucide-react';
import * as monaco from 'monaco-editor';

interface JsonEditorPopupProps {
    value: any;
    onChange: (value: any) => void;
    readOnly?: boolean;
}

function toJsonString(value: any): string {
    if (value === undefined) return '';
    if (value === null) return 'null';
    return JSON.stringify(value, null, 2);
}

// Ensure Monaco has a fallback worker so JSON language service doesn't crash.
// Only set if no MonacoEnvironment is already configured by the consuming app.
if (typeof window !== 'undefined' && !(window as any).MonacoEnvironment) {
    (window as any).MonacoEnvironment = {
        getWorker: () =>
            new Worker(
                URL.createObjectURL(
                    new Blob(['self.onmessage = function() {}'], { type: 'text/javascript' }),
                ),
            ),
    };
}

export default function JsonEditorPopup({ value, onChange, readOnly }: JsonEditorPopupProps) {
    const [showEditor, setShowEditor] = useState(false);
    const [editorValue, setEditorValue] = useState('');
    const [enableOk, setEnableOk] = useState(true);
    const editorContainerRef = useRef<HTMLDivElement>(null);
    const editorRef = useRef<monaco.editor.IStandaloneCodeEditor | null>(null);

    useEffect(() => {
        if (!showEditor || !editorContainerRef.current) return;

        const textValue = toJsonString(value);
        setEditorValue(textValue);
        setEnableOk(true);

        const editor = monaco.editor.create(editorContainerRef.current, {
            value: textValue,
            language: 'json',
            theme: 'vs',
            minimap: { enabled: false },
            lineNumbers: 'on',
            scrollBeyondLastLine: false,
            automaticLayout: true,
            fontSize: 13,
            fontFamily: "'SF Mono', Monaco, 'Cascadia Code', monospace",
            tabSize: 2,
            readOnly,
        });

        editor.onDidChangeModelContent(() => {
            const val = editor.getValue();
            setEditorValue(val);
            try {
                const trimmed = val.trim();
                if (trimmed !== '' && trimmed !== 'undefined' && trimmed !== 'null') {
                    JSON.parse(trimmed);
                }
                setEnableOk(true);
            } catch {
                setEnableOk(false);
            }
        });

        editorRef.current = editor;

        return () => {
            editor.dispose();
            editorRef.current = null;
        };
    }, [showEditor]);

    const handleOk = () => {
        const ev = (editorValue ?? '').trim();
        let v: any = undefined;
        if (ev === 'undefined' || ev === '') v = undefined;
        else if (ev === 'null') v = null;
        else if (ev) v = JSON.parse(ev);
        onChange(v);
        setShowEditor(false);
    };

    const handleCancel = () => {
        setShowEditor(false);
    };

    let popup = null;
    if (showEditor) {
        popup = (
            <div className="_jsonPopupBackground" onClick={handleCancel}>
                <div className="_jsonPopupContainer" onClick={(e) => e.stopPropagation()}>
                    <div className="_jsonPopupEditorContainer" ref={editorContainerRef} />
                    {!enableOk && (
                        <div className="_jsonPopupError">Invalid JSON</div>
                    )}
                    <div className="_jsonPopupButtons">
                        <button
                            className="_okButton"
                            disabled={!enableOk}
                            onClick={handleOk}
                        >
                            OK
                        </button>
                        <button onClick={handleCancel}>CANCEL</button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <>
            <SquareArrowOutUpRight
                className="_jsonPopoutIcon"
                size={14}
                onClick={() => setShowEditor(true)}
            >
                <title>Edit JSON</title>
            </SquareArrowOutUpRight>
            {popup}
        </>
    );
}
