import { Repository, Schema, SchemaUtil } from '@fincity/kirun-js';
import React, { useEffect, useState } from 'react';
import { ArrowUp, ArrowDown, Trash2, Plus } from 'lucide-react';
import SchemaForm from './SchemaForm';
import AnyEditor from './AnyEditor';

interface ArrayEditorProps {
    schema: Schema;
    schemaRepository: Repository<Schema>;
    value: any[] | undefined;
    onChange: (value: any[] | undefined) => void;
    readOnly?: boolean;
    label?: string;
}

export default function ArrayEditor({
    schema,
    schemaRepository,
    value,
    onChange,
    readOnly,
    label,
}: ArrayEditorProps) {
    const items = value ?? [];
    const [itemSchema, setItemSchema] = useState<Schema | undefined>(undefined);

    useEffect(() => {
        (async () => {
            const schemaItems = schema.getItems();
            if (!schemaItems) {
                setItemSchema(undefined);
                return;
            }

            let s: Schema | undefined;
            if (schemaItems instanceof Schema) {
                s = schemaItems;
            } else if (Array.isArray(schemaItems)) {
                // Tuple schemas — for now use the first as the generic item schema
                s = schemaItems.length > 0 ? schemaItems[0] : undefined;
            }

            if (s?.getRef()) {
                s = await SchemaUtil.getSchemaFromRef(s, schemaRepository, s.getRef());
            }
            setItemSchema(s);
        })();
    }, [schema, schemaRepository]);

    const addItem = () => {
        onChange([...items, undefined]);
    };

    const removeItem = (index: number) => {
        const newItems = [...items];
        newItems.splice(index, 1);
        onChange(newItems.length > 0 ? newItems : undefined);
    };

    const moveItem = (index: number, direction: number) => {
        const newIndex = index + direction;
        if (newIndex < 0 || newIndex >= items.length) return;
        const newItems = [...items];
        [newItems[index], newItems[newIndex]] = [newItems[newIndex], newItems[index]];
        onChange(newItems);
    };

    const updateItem = (index: number, newValue: any) => {
        const newItems = [...items];
        newItems[index] = newValue;
        onChange(newItems);
    };

    const maxItems = schema.getMaxItems();
    const canAdd = !readOnly && (!maxItems || items.length < maxItems);

    return (
        <div className="_schemaFormField _schemaFormArray">
            {label && <label className="_schemaFormLabel">{label}</label>}
            <div className="_schemaFormArrayItems">
                {items.map((item, index) => (
                    <div className="_schemaFormArrayItem" key={index}>
                        <div className="_schemaFormArrayItemControls">
                            <span className="_schemaFormArrayIndex">{index}</span>
                            {!readOnly && (
                                <>
                                    {index > 0 && (
                                        <ArrowUp
                                            size={14}
                                            onClick={() => moveItem(index, -1)}
                                        >
                                            <title>Move up</title>
                                        </ArrowUp>
                                    )}
                                    {index < items.length - 1 && (
                                        <ArrowDown
                                            size={14}
                                            onClick={() => moveItem(index, 1)}
                                        >
                                            <title>Move down</title>
                                        </ArrowDown>
                                    )}
                                    <Trash2
                                        className="_trashIcon"
                                        size={14}
                                        onClick={() => removeItem(index)}
                                    >
                                        <title>Remove</title>
                                    </Trash2>
                                </>
                            )}
                        </div>
                        <div className="_schemaFormArrayItemValue">
                            {itemSchema ? (
                                <SchemaForm
                                    schema={itemSchema}
                                    schemaRepository={schemaRepository}
                                    value={item}
                                    onChange={(v) => updateItem(index, v)}
                                    readOnly={readOnly}
                                />
                            ) : (
                                <AnyEditor
                                    value={item}
                                    onChange={(v) => updateItem(index, v)}
                                    readOnly={readOnly}
                                />
                            )}
                        </div>
                    </div>
                ))}
            </div>
            {canAdd && (
                <button
                    className="_schemaFormArrayAdd"
                    onClick={addItem}
                    type="button"
                >
                    <Plus size={14} /> Add Item
                </button>
            )}
        </div>
    );
}
