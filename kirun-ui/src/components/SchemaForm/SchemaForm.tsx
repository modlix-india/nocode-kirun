import { Repository, Schema, SchemaType, SchemaUtil } from '@fincity/kirun-js';
import React, { useEffect, useState } from 'react';
import StringEditor from './StringEditor';
import NumberEditor from './NumberEditor';
import BooleanEditor from './BooleanEditor';
import ArrayEditor from './ArrayEditor';
import ObjectEditor from './ObjectEditor';
import AnyEditor from './AnyEditor';

const NUMBER_TYPES = new Set([
    SchemaType.INTEGER,
    SchemaType.LONG,
    SchemaType.FLOAT,
    SchemaType.DOUBLE,
]);

export interface SchemaFormProps {
    schema?: Schema;
    schemaRepository: Repository<Schema>;
    value: any;
    onChange: (value: any) => void;
    readOnly?: boolean;
    label?: string;
}

export default function SchemaForm({
    schema,
    schemaRepository,
    value,
    onChange,
    readOnly,
    label,
}: SchemaFormProps) {
    const [resolvedSchema, setResolvedSchema] = useState<Schema | undefined>(schema);

    useEffect(() => {
        if (!schema) {
            setResolvedSchema(undefined);
            return;
        }

        if (!schema.getRef()) {
            setResolvedSchema(schema);
            return;
        }

        let cancelled = false;
        (async () => {
            const s = await SchemaUtil.getSchemaFromRef(
                schema,
                schemaRepository,
                schema.getRef(),
            );
            if (!cancelled) setResolvedSchema(s ?? schema);
        })();
        return () => {
            cancelled = true;
        };
    }, [schema, schemaRepository]);

    if (!resolvedSchema) {
        return <AnyEditor value={value} onChange={onChange} readOnly={readOnly} label={label} />;
    }

    // Check for constant value
    const constant = resolvedSchema.getConstant();
    if (constant !== undefined) {
        return (
            <div className="_schemaFormField">
                {label && <label className="_schemaFormLabel">{label}</label>}
                <span className="_schemaFormConstant">{JSON.stringify(constant)}</span>
            </div>
        );
    }

    // Handle composition schemas (anyOf, oneOf, allOf)
    const anyOf = resolvedSchema.getAnyOf();
    const oneOf = resolvedSchema.getOneOf();
    if (anyOf || oneOf) {
        const options = anyOf ?? oneOf ?? [];
        return (
            <CompositeSchemaForm
                options={options}
                schemaRepository={schemaRepository}
                value={value}
                onChange={onChange}
                readOnly={readOnly}
                label={label}
            />
        );
    }

    const allowedTypes = resolvedSchema.getType()?.getAllowedSchemaTypes();

    if (!allowedTypes || allowedTypes.size === 0) {
        // No type specified — treat as ANY
        return <AnyEditor value={value} onChange={onChange} readOnly={readOnly} label={label} />;
    }

    // Single type
    if (allowedTypes.size === 1) {
        const type = allowedTypes.values().next().value;
        return renderForType(
            type!,
            resolvedSchema,
            schemaRepository,
            value,
            onChange,
            readOnly,
            label,
        );
    }

    // Multi type — detect actual type from value and render, or show type selector
    return (
        <MultiTypeForm
            schema={resolvedSchema}
            schemaRepository={schemaRepository}
            allowedTypes={allowedTypes}
            value={value}
            onChange={onChange}
            readOnly={readOnly}
            label={label}
        />
    );
}

function renderForType(
    type: SchemaType,
    schema: Schema,
    schemaRepository: Repository<Schema>,
    value: any,
    onChange: (value: any) => void,
    readOnly?: boolean,
    label?: string,
): React.ReactNode {
    switch (type) {
        case SchemaType.STRING:
            return (
                <StringEditor
                    schema={schema}
                    value={typeof value === 'string' ? value : value != null ? String(value) : undefined}
                    onChange={onChange}
                    readOnly={readOnly}
                    label={label}
                />
            );
        case SchemaType.INTEGER:
        case SchemaType.LONG:
        case SchemaType.FLOAT:
        case SchemaType.DOUBLE:
            return (
                <NumberEditor
                    schema={schema}
                    value={typeof value === 'number' ? value : undefined}
                    onChange={onChange}
                    readOnly={readOnly}
                    label={label}
                />
            );
        case SchemaType.BOOLEAN:
            return (
                <BooleanEditor
                    schema={schema}
                    value={typeof value === 'boolean' ? value : undefined}
                    onChange={onChange}
                    readOnly={readOnly}
                    label={label}
                />
            );
        case SchemaType.ARRAY:
            return (
                <ArrayEditor
                    schema={schema}
                    schemaRepository={schemaRepository}
                    value={Array.isArray(value) ? value : undefined}
                    onChange={onChange}
                    readOnly={readOnly}
                    label={label}
                />
            );
        case SchemaType.OBJECT:
            return (
                <ObjectEditor
                    schema={schema}
                    schemaRepository={schemaRepository}
                    value={typeof value === 'object' && value !== null && !Array.isArray(value) ? value : undefined}
                    onChange={onChange}
                    readOnly={readOnly}
                    label={label}
                />
            );
        case SchemaType.NULL:
            return (
                <div className="_schemaFormField">
                    {label && <label className="_schemaFormLabel">{label}</label>}
                    <span className="_schemaFormConstant">null</span>
                </div>
            );
        default:
            return (
                <AnyEditor value={value} onChange={onChange} readOnly={readOnly} label={label} />
            );
    }
}

function detectType(value: any): SchemaType | undefined {
    if (value === null || value === undefined) return SchemaType.NULL;
    if (typeof value === 'string') return SchemaType.STRING;
    if (typeof value === 'boolean') return SchemaType.BOOLEAN;
    if (typeof value === 'number') {
        return Number.isInteger(value) ? SchemaType.INTEGER : SchemaType.DOUBLE;
    }
    if (Array.isArray(value)) return SchemaType.ARRAY;
    if (typeof value === 'object') return SchemaType.OBJECT;
    return undefined;
}

function MultiTypeForm({
    schema,
    schemaRepository,
    allowedTypes,
    value,
    onChange,
    readOnly,
    label,
}: {
    schema: Schema;
    schemaRepository: Repository<Schema>;
    allowedTypes: Set<SchemaType>;
    value: any;
    onChange: (value: any) => void;
    readOnly?: boolean;
    label?: string;
}) {
    const typeArray = Array.from(allowedTypes).filter((t) => t !== SchemaType.NULL);
    const allowsNull = allowedTypes.has(SchemaType.NULL);
    const detectedType = detectType(value);

    // Collapse numeric types to a single "Number" option
    const hasNumeric = typeArray.some((t) => NUMBER_TYPES.has(t));
    const displayTypes = hasNumeric
        ? [...typeArray.filter((t) => !NUMBER_TYPES.has(t)), SchemaType.INTEGER]
        : typeArray;

    const [selectedType, setSelectedType] = useState<SchemaType>(
        detectedType && allowedTypes.has(detectedType)
            ? detectedType
            : displayTypes[0] ?? SchemaType.STRING,
    );

    useEffect(() => {
        if (detectedType && allowedTypes.has(detectedType)) {
            setSelectedType(detectedType);
        }
    }, [value, detectedType, allowedTypes]);

    return (
        <div className="_schemaFormField _schemaFormMultiType">
            {label && <label className="_schemaFormLabel">{label}</label>}
            <div className="_schemaFormMultiTypeSelector">
                <select
                    className="_schemaFormSelect _schemaFormTypeSelect"
                    value={selectedType}
                    onChange={(e) => {
                        const newType = e.target.value as SchemaType;
                        setSelectedType(newType);
                        onChange(undefined);
                    }}
                    disabled={readOnly}
                >
                    {displayTypes.map((t) => (
                        <option key={t} value={t}>
                            {hasNumeric && NUMBER_TYPES.has(t) ? 'Number' : t}
                        </option>
                    ))}
                    {allowsNull && <option value={SchemaType.NULL}>Null</option>}
                </select>
            </div>
            {renderForType(selectedType, schema, schemaRepository, value, onChange, readOnly)}
        </div>
    );
}

function CompositeSchemaForm({
    options,
    schemaRepository,
    value,
    onChange,
    readOnly,
    label,
}: {
    options: Schema[];
    schemaRepository: Repository<Schema>;
    value: any;
    onChange: (value: any) => void;
    readOnly?: boolean;
    label?: string;
}) {
    const [selectedIndex, setSelectedIndex] = useState(0);

    if (options.length === 0) {
        return <AnyEditor value={value} onChange={onChange} readOnly={readOnly} label={label} />;
    }

    if (options.length === 1) {
        return (
            <SchemaForm
                schema={options[0]}
                schemaRepository={schemaRepository}
                value={value}
                onChange={onChange}
                readOnly={readOnly}
                label={label}
            />
        );
    }

    return (
        <div className="_schemaFormField _schemaFormComposite">
            {label && <label className="_schemaFormLabel">{label}</label>}
            <select
                className="_schemaFormSelect"
                value={selectedIndex}
                onChange={(e) => {
                    setSelectedIndex(Number(e.target.value));
                    onChange(undefined);
                }}
                disabled={readOnly}
            >
                {options.map((opt, idx) => (
                    <option key={idx} value={idx}>
                        {opt.getName() ?? opt.getTitle() ?? `Option ${idx + 1}`}
                    </option>
                ))}
            </select>
            <SchemaForm
                schema={options[selectedIndex]}
                schemaRepository={schemaRepository}
                value={value}
                onChange={onChange}
                readOnly={readOnly}
            />
        </div>
    );
}
