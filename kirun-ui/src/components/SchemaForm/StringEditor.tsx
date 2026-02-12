import { Schema } from '@fincity/kirun-js';
import React from 'react';

interface StringEditorProps {
    schema: Schema;
    value: string | undefined;
    onChange: (value: string | undefined) => void;
    readOnly?: boolean;
    label?: string;
}

export default function StringEditor({ schema, value, onChange, readOnly, label }: StringEditorProps) {
    const enums = schema.getEnums();

    if (enums && enums.length > 0) {
        return (
            <div className="_schemaFormField">
                {label && <label className="_schemaFormLabel">{label}</label>}
                <select
                    className="_schemaFormSelect"
                    value={value ?? ''}
                    onChange={(e) => onChange(e.target.value || undefined)}
                    disabled={readOnly}
                >
                    <option value="">-- Select --</option>
                    {enums.map((enumVal: any) => (
                        <option key={String(enumVal)} value={String(enumVal)}>
                            {String(enumVal)}
                        </option>
                    ))}
                </select>
            </div>
        );
    }

    const format = schema.getFormat();
    let inputType = 'text';
    if (format === 'DATE') inputType = 'date';
    else if (format === 'DATETIME') inputType = 'datetime-local';
    else if (format === 'TIME') inputType = 'time';
    else if (format === 'EMAIL') inputType = 'email';

    const maxLength = schema.getMaxLength();
    const useTextarea = !format && (!maxLength || maxLength > 200);

    return (
        <div className="_schemaFormField">
            {label && <label className="_schemaFormLabel">{label}</label>}
            {useTextarea ? (
                <textarea
                    className="_schemaFormTextarea"
                    value={value ?? ''}
                    onChange={(e) => onChange(e.target.value || undefined)}
                    readOnly={readOnly}
                    placeholder={schema.getDescription() || undefined}
                    maxLength={maxLength}
                    rows={1}
                />
            ) : (
                <input
                    className="_schemaFormInput"
                    type={inputType}
                    value={value ?? ''}
                    onChange={(e) => onChange(e.target.value || undefined)}
                    readOnly={readOnly}
                    placeholder={schema.getDescription() || undefined}
                    maxLength={maxLength}
                    minLength={schema.getMinLength()}
                />
            )}
        </div>
    );
}
