import React, { useEffect, useState } from 'react';

interface AnyEditorProps {
    value: any;
    onChange: (value: any) => void;
    readOnly?: boolean;
    label?: string;
}

export default function AnyEditor({ value, onChange, readOnly, label }: AnyEditorProps) {
    const [text, setText] = useState(() => toJsonString(value));
    const [error, setError] = useState<string | undefined>();

    useEffect(() => {
        setText(toJsonString(value));
        setError(undefined);
    }, [value]);

    const handleBlur = () => {
        if (text.trim() === '') {
            onChange(undefined);
            setError(undefined);
            return;
        }
        try {
            const parsed = JSON.parse(text);
            onChange(parsed);
            setError(undefined);
        } catch (e: any) {
            setError('Invalid JSON: ' + e.message);
        }
    };

    return (
        <div className="_schemaFormField">
            {label && <label className="_schemaFormLabel">{label}</label>}
            <textarea
                className={`_schemaFormTextarea _schemaFormJsonEditor ${error ? '_schemaFormError' : ''}`}
                value={text}
                onChange={(e) => setText(e.target.value)}
                onBlur={handleBlur}
                readOnly={readOnly}
                rows={5}
                spellCheck={false}
            />
            {error && <div className="_schemaFormErrorMessage">{error}</div>}
        </div>
    );
}

function toJsonString(value: any): string {
    if (value === undefined) return '';
    if (value === null) return 'null';
    return JSON.stringify(value, null, 2);
}
