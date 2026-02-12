import { Schema } from '@fincity/kirun-js';
import React from 'react';

interface BooleanEditorProps {
    schema: Schema;
    value: boolean | undefined | null;
    onChange: (value: boolean | undefined | null) => void;
    readOnly?: boolean;
    label?: string;
    allowNull?: boolean;
}

export default function BooleanEditor({
    schema,
    value,
    onChange,
    readOnly,
    label,
    allowNull,
}: BooleanEditorProps) {
    return (
        <div className="_schemaFormField">
            {label && <label className="_schemaFormLabel">{label}</label>}
            <select
                className="_schemaFormSelect"
                value={value === null || value === undefined ? '' : String(value)}
                onChange={(e) => {
                    const v = e.target.value;
                    if (v === '') onChange(allowNull ? null : undefined);
                    else onChange(v === 'true');
                }}
                disabled={readOnly}
            >
                {(allowNull || value === null || value === undefined) && (
                    <option value="">{allowNull ? 'null' : '-- Select --'}</option>
                )}
                <option value="true">true</option>
                <option value="false">false</option>
            </select>
        </div>
    );
}
