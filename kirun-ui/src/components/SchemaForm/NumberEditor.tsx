import { Schema, SchemaType } from '@fincity/kirun-js';
import React from 'react';

interface NumberEditorProps {
    schema: Schema;
    value: number | undefined;
    onChange: (value: number | undefined) => void;
    readOnly?: boolean;
    label?: string;
}

export default function NumberEditor({
    schema,
    value,
    onChange,
    readOnly,
    label,
}: NumberEditorProps) {
    const enums = schema.getEnums();
    if (enums && enums.length > 0) {
        return (
            <div className="_schemaFormField">
                {label && <label className="_schemaFormLabel">{label}</label>}
                <select
                    className="_schemaFormSelect"
                    value={value ?? ''}
                    onChange={(e) => {
                        const v = e.target.value;
                        onChange(v === '' ? undefined : Number(v));
                    }}
                    disabled={readOnly}
                >
                    <option value="">-- Select --</option>
                    {enums.map((enumVal: any) => (
                        <option key={String(enumVal)} value={enumVal}>
                            {String(enumVal)}
                        </option>
                    ))}
                </select>
            </div>
        );
    }

    const allowedTypes = schema.getType()?.getAllowedSchemaTypes();
    const isInteger =
        allowedTypes &&
        !allowedTypes.has(SchemaType.FLOAT) &&
        !allowedTypes.has(SchemaType.DOUBLE);
    const step = isInteger ? 1 : 'any';

    return (
        <div className="_schemaFormField">
            {label && <label className="_schemaFormLabel">{label}</label>}
            <input
                className="_schemaFormInput"
                type="number"
                value={value ?? ''}
                step={step}
                min={schema.getMinimum() ?? schema.getExclusiveMinimum()}
                max={schema.getMaximum() ?? schema.getExclusiveMaximum()}
                onChange={(e) => {
                    const v = e.target.value;
                    if (v === '') {
                        onChange(undefined);
                        return;
                    }
                    const num = isInteger ? parseInt(v, 10) : parseFloat(v);
                    if (!isNaN(num)) onChange(num);
                }}
                readOnly={readOnly}
                placeholder={schema.getDescription() || undefined}
            />
        </div>
    );
}
