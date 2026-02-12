import React, { useEffect, useState } from 'react';

interface ExpressionInputProps {
    value: string | undefined;
    onChange: (value: string | undefined) => void;
    readOnly?: boolean;
    placeholder?: string;
}

export default function ExpressionInput({
    value,
    onChange,
    readOnly,
    placeholder,
}: ExpressionInputProps) {
    const [text, setText] = useState(value ?? '');

    useEffect(() => {
        setText(value ?? '');
    }, [value]);

    return (
        <input
            className="_expressionInput"
            type="text"
            value={text}
            onChange={(e) => setText(e.target.value)}
            onBlur={() => onChange(text || undefined)}
            onKeyDown={(e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    onChange(text || undefined);
                }
            }}
            readOnly={readOnly}
            placeholder={placeholder ?? 'Expression...'}
            spellCheck={false}
        />
    );
}
