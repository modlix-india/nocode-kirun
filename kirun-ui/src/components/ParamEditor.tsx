import {
    Parameter,
    ParameterType,
    Repository,
    Schema,
    SchemaUtil,
    isNullValue,
} from '@fincity/kirun-js';
import React, { useCallback, useEffect, useState } from 'react';
import { shortUUID } from '../util/shortUUID';
import { duplicate } from '@fincity/kirun-js';
import SchemaForm from './SchemaForm/SchemaForm';
import JsonEditorPopup from './JsonEditorPopup';
import ExpressionInput from './ExpressionInput';
import { ArrowUp, ArrowDown, Trash2 } from 'lucide-react';

interface ParamEditorProps {
    parameter: Parameter;
    schemaRepository: Repository<Schema>;
    value: any;
    onChange: (newValue: any) => void;
}

export default function ParamEditor({
    parameter,
    schemaRepository,
    value,
    onChange,
}: ParamEditorProps) {
    const onlyValue = parameter.getType() === ParameterType.CONSTANT;
    const isArray = parameter.isVariableArgument();
    const [values, setValues] = React.useState<any[]>([]);

    useEffect(() => {
        (async () => {
            let inValue = value;
            let arr = [];
            if (isNullValue(inValue)) {
                if (!isArray) {
                    const defaultValue = await SchemaUtil.getDefaultValue(
                        parameter.getSchema(),
                        schemaRepository,
                    );
                    if (!isNullValue(defaultValue)) {
                        arr.push({
                            key: shortUUID(),
                            type: 'VALUE',
                            isNew: true,
                            expression: '',
                            value: defaultValue,
                        });
                    }
                }
            } else {
                arr = duplicate(Array.from(Object.values(inValue ?? {})));
            }

            arr.sort((a: any, b: any) => {
                let v = (a.order ?? 0) - (b.order ?? 0);
                if (v === 0) v = a.key.localeCompare(b.key);
                return v;
            });

            if (arr.length === 0 || (isArray && !arr.some((e: any) => e.isNew))) {
                const order =
                    (arr
                        .map((e: any) => e.order)
                        .reduce((a: number | undefined, c: number | undefined) => {
                            if (isNullValue(a)) return c;
                            if (isNullValue(c)) return a;
                            return a! > c! ? a : c;
                        }, undefined) ?? 0) + 1;

                arr.push({
                    key: shortUUID(),
                    type: onlyValue ? 'VALUE' : 'EXPRESSION',
                    isNew: true,
                    expression: '',
                    order,
                });
            }

            setValues(arr);
        })();
    }, [value, isArray]);

    const updateValue = useCallback(
        (key: string, prop: any, value: any, removeProp: boolean = false) => {
            let obj = values.reduce((a: any, c: any) => {
                a[c.key] = c;
                return a;
            }, {});
            if (isNullValue(value) && removeProp && !obj[key].expression) {
                if (values.length === 1) obj = undefined;
                else delete obj[key];
            } else {
                obj[key] = {
                    ...obj[key],
                    [prop]: value,
                };
                if (obj[key].isNew) delete obj[key].isNew;
            }
            const delKey = Object.values(obj ?? {})
                .filter((e: any) => e.isNew && !e.expression && isNullValue(e.value))
                .map((e: any) => e.key as string);
            if (delKey.length) {
                delKey.forEach((e: string) => delete obj[e]);
            }

            onChange(obj);
        },
        [values, onChange],
    );

    let [schema, setSchema] = useState<Schema | undefined>(parameter.getSchema());

    useEffect(() => {
        if (isNullValue(schema?.getRef())) return;
        (async () => {
            const s = await SchemaUtil.getSchemaFromRef(
                schema!,
                schemaRepository,
                schema!.getRef(),
            );
            setSchema(s);
        })();
    }, [schema, schemaRepository]);

    // Check if schema is "Any" (no type info) — only show JSON popout, not SchemaForm
    const isAnySchema =
        !schema ||
        ((!schema.getType() ||
            (schema.getType()?.getAllowedSchemaTypes()?.size ?? 0) === 0) &&
            !schema.getAnyOf() &&
            !schema.getOneOf() &&
            schema.getConstant() === undefined);

    const moveUpDown = useCallback(
        (key: string, direction: number) => {
            if (values.length < 2) return;
            const ov = duplicate(values);
            ov.forEach((e: any, i: number) => (e.order = i));
            const index = ov.findIndex((e: any) => e.key === key);
            if (index === -1) return;
            if (index === 0 && direction === -1) {
                ov[0].order = ov[ov.length - 1].order + 1;
            } else if (index === ov.length - 2 && direction === 1) {
                ov[ov.length - 2].order = ov[0].order - 1;
            } else {
                const temp = ov[index].order;
                ov[index].order = ov[index + direction].order;
                ov[index + direction].order = temp;
            }
            ov.sort((a: any, b: any) => a.order - b.order).forEach(
                (e: any, i: number) => (e.order = i),
            );

            let obj = ov.reduce((a: any, c: any) => {
                a[c.key] = c;
                return a;
            }, {});
            const delKey = Object.values(obj)
                .filter((e: any) => e.isNew && !e.expression && isNullValue(e.value))
                .map((e: any) => e.key as string);
            if (delKey.length) {
                delKey.forEach((e: string) => delete obj[e]);
            }
            onChange(obj);
        },
        [values, onChange],
    );

    const removeValue = useCallback(
        (key: string) => {
            let obj = values.reduce((a: any, c: any) => {
                a[c.key] = c;
                return a;
            }, {});
            delete obj[key];
            if (Object.keys(obj).length === 0) {
                onChange(undefined);
            } else {
                onChange(obj);
            }
        },
        [values, onChange],
    );

    return (
        <div className="_paramEditor">
            {values.map((eachValue) => {
                const key = eachValue.key;
                let valueEditor;
                if (eachValue.type === 'EXPRESSION') {
                    valueEditor = (
                        <div className="_paramExpression">
                            <ExpressionInput
                                value={eachValue.expression}
                                onChange={(e) => updateValue(key, 'expression', e)}
                            />
                        </div>
                    );
                } else if (isAnySchema) {
                    // Schema is unknown (Any) — show compact value preview, use popout to edit
                    const preview = eachValue.value === undefined || eachValue.value === null
                        ? 'value (Empty)'
                        : typeof eachValue.value === 'string'
                            ? eachValue.value
                            : JSON.stringify(eachValue.value).substring(0, 60) + (JSON.stringify(eachValue.value).length > 60 ? '...' : '');
                    valueEditor = (
                        <div
                            className="_paramValuePreview"
                            title={typeof eachValue.value === 'string' ? eachValue.value : JSON.stringify(eachValue.value, null, 2)}
                        >
                            {preview}
                        </div>
                    );
                } else {
                    valueEditor = (
                        <SchemaForm
                            schemaRepository={schemaRepository}
                            schema={schema}
                            value={eachValue.value}
                            onChange={(v: any) => {
                                updateValue(key, 'value', v, true);
                            }}
                        />
                    );
                }
                const paramToggle = onlyValue ? (
                    <></>
                ) : (
                    <div className="_paramToggleContainer">
                        <div
                            className={`_paramToggleOption ${eachValue.type === 'EXPRESSION' ? '_active' : ''}`}
                            onClick={() => updateValue(key, 'type', 'EXPRESSION')}
                        >
                            Expr
                        </div>
                        <div
                            className={`_paramToggleOption ${eachValue.type === 'VALUE' ? '_active' : ''}`}
                            onClick={() => updateValue(key, 'type', 'VALUE')}
                        >
                            Value
                        </div>
                    </div>
                );
                const jsonPopout =
                    eachValue.type === 'VALUE' ? (
                        <JsonEditorPopup
                            value={eachValue.value === undefined ? null : eachValue.value}
                            onChange={(v) => {
                                updateValue(key, 'value', v);
                            }}
                        />
                    ) : (
                        <></>
                    );
                let upDown = <></>;
                if (isArray && !eachValue.isNew && values.length > 2) {
                    upDown = (
                        <>
                            <ArrowUp
                                size={14}
                                onClick={() => moveUpDown(eachValue.key, -1)}
                            >
                                <title>Move up</title>
                            </ArrowUp>
                            <ArrowDown
                                size={14}
                                onClick={() => moveUpDown(eachValue.key, 1)}
                            >
                                <title>Move down</title>
                            </ArrowDown>
                        </>
                    );
                }
                const deleteButton = !eachValue.isNew ? (
                    <Trash2
                        className="_deleteParam"
                        size={14}
                        onClick={() => removeValue(eachValue.key)}
                    >
                        <title>Remove</title>
                    </Trash2>
                ) : (
                    <></>
                );
                return (
                    <div className="_paramEditorRow" key={key}>
                        <div className="_paramToggleValueGrid">
                            {paramToggle}
                            {jsonPopout}
                            {upDown}
                            {deleteButton}
                        </div>
                        {valueEditor}
                    </div>
                );
            })}
        </div>
    );
}
