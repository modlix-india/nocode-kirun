import { Repository, Schema, SchemaUtil } from '@fincity/kirun-js';
import React, { useEffect, useState } from 'react';
import { Trash2, Plus } from 'lucide-react';
import SchemaForm from './SchemaForm';

interface ObjectEditorProps {
    schema: Schema;
    schemaRepository: Repository<Schema>;
    value: Record<string, any> | undefined;
    onChange: (value: Record<string, any> | undefined) => void;
    readOnly?: boolean;
    label?: string;
}

export default function ObjectEditor({
    schema,
    schemaRepository,
    value,
    onChange,
    readOnly,
    label,
}: ObjectEditorProps) {
    const obj = value ?? {};
    const [resolvedProperties, setResolvedProperties] = useState<Map<string, Schema>>(new Map());
    const [additionalSchema, setAdditionalSchema] = useState<Schema | undefined>(undefined);

    useEffect(() => {
        (async () => {
            const props = schema.getProperties();
            if (!props) {
                setResolvedProperties(new Map());
            } else {
                const resolved = new Map<string, Schema>();
                for (const [key, propSchema] of props.entries()) {
                    if (propSchema.getRef()) {
                        const s = await SchemaUtil.getSchemaFromRef(
                            propSchema,
                            schemaRepository,
                            propSchema.getRef(),
                        );
                        if (s) resolved.set(key, s);
                        else resolved.set(key, propSchema);
                    } else {
                        resolved.set(key, propSchema);
                    }
                }
                setResolvedProperties(resolved);
            }

            const addl = schema.getAdditionalProperties();
            if (!addl) {
                // undefined means additional properties are allowed (default)
                setAdditionalSchema(Schema.ofAny('_additionalProp'));
            } else {
                const addlSchema = addl.getSchemaValue();
                if (addlSchema) {
                    if (addlSchema.getRef()) {
                        const s = await SchemaUtil.getSchemaFromRef(
                            addlSchema,
                            schemaRepository,
                            addlSchema.getRef(),
                        );
                        setAdditionalSchema(s ?? addlSchema);
                    } else {
                        setAdditionalSchema(addlSchema);
                    }
                } else if (addl.getBooleanValue() === false) {
                    setAdditionalSchema(undefined);
                } else {
                    // booleanValue is true or undefined — allow any additional properties
                    setAdditionalSchema(Schema.ofAny('_additionalProp'));
                }
            }
        })();
    }, [schema, schemaRepository]);

    const requiredSet = new Set(schema.getRequired() ?? []);
    const definedKeys = new Set(resolvedProperties.keys());
    const additionalKeys = Object.keys(obj).filter((k) => !definedKeys.has(k));

    const [newKey, setNewKey] = useState('');

    const updateProperty = (key: string, newValue: any) => {
        const newObj = { ...obj };
        if (newValue === undefined) {
            delete newObj[key];
        } else {
            newObj[key] = newValue;
        }
        onChange(Object.keys(newObj).length > 0 ? newObj : undefined);
    };

    const addProperty = () => {
        if (!newKey.trim() || obj[newKey] !== undefined) return;
        onChange({ ...obj, [newKey.trim()]: undefined });
        setNewKey('');
    };

    const removeProperty = (key: string) => {
        const newObj = { ...obj };
        delete newObj[key];
        onChange(Object.keys(newObj).length > 0 ? newObj : undefined);
    };

    return (
        <div className="_schemaFormField _schemaFormObject">
            {label && <label className="_schemaFormLabel">{label}</label>}
            <div className="_schemaFormObjectProperties">
                {Array.from(resolvedProperties.entries()).map(([key, propSchema]) => (
                    <div className="_schemaFormObjectProperty" key={key}>
                        <SchemaForm
                            schema={propSchema}
                            schemaRepository={schemaRepository}
                            value={obj[key]}
                            onChange={(v) => updateProperty(key, v)}
                            readOnly={readOnly}
                            label={`${key}${requiredSet.has(key) ? ' *' : ''}`}
                        />
                    </div>
                ))}

                {additionalKeys.map((key) => (
                    <div className="_schemaFormObjectProperty _schemaFormAdditional" key={key}>
                        <div className="_schemaFormAdditionalHeader">
                            <span className="_schemaFormAdditionalKey">{key}</span>
                            {!readOnly && (
                                <Trash2
                                    size={14}
                                    onClick={() => removeProperty(key)}
                                >
                                    <title>Remove property</title>
                                </Trash2>
                            )}
                        </div>
                        {additionalSchema ? (
                            <SchemaForm
                                schema={additionalSchema}
                                schemaRepository={schemaRepository}
                                value={obj[key]}
                                onChange={(v) => updateProperty(key, v)}
                                readOnly={readOnly}
                            />
                        ) : (
                            <input
                                className="_schemaFormInput"
                                type="text"
                                value={typeof obj[key] === 'string' ? obj[key] : JSON.stringify(obj[key] ?? '')}
                                onChange={(e) => updateProperty(key, e.target.value || undefined)}
                                readOnly={readOnly}
                            />
                        )}
                    </div>
                ))}
            </div>

            {!readOnly && additionalSchema && (
                <div className="_schemaFormObjectAddProperty">
                    <input
                        className="_schemaFormInput"
                        type="text"
                        value={newKey}
                        onChange={(e) => setNewKey(e.target.value)}
                        placeholder="New property name"
                        onKeyDown={(e) => {
                            if (e.key === 'Enter') {
                                e.preventDefault();
                                addProperty();
                            }
                        }}
                    />
                    <button
                        className="_schemaFormArrayAdd"
                        onClick={addProperty}
                        type="button"
                        disabled={!newKey.trim()}
                    >
                        <Plus size={14} /> Add
                    </button>
                </div>
            )}
        </div>
    );
}
