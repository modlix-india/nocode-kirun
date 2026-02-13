import React, { useEffect, useState } from 'react';

interface FunctionDetailsEditorProps {
    rawDef: any;
    onChange: (newDef: any) => void;
    onEditFunctionClose: () => void;
    functionKey?: string;
}

const NAME_REGEX = /^[A-Za-z_][_A-Za-z0-9]*$/;

export default function FunctionDetailsEditor({
    rawDef,
    functionKey = '',
    onChange,
    onEditFunctionClose,
}: FunctionDetailsEditorProps) {
    const [errors, setErrors] = useState<any>({});
    const [name, setName] = React.useState(rawDef?.name);
    const [namespace, setNamespace] = React.useState(rawDef?.namespace ?? '');

    useEffect(() => {
        setName(rawDef?.name ?? '');
        const errors: any = {};
        if (!NAME_REGEX.test(rawDef?.name)) {
            errors.name =
                'Name contains only letter, numbers and underscore and should not start with number';
        }
        setNamespace(rawDef?.namespace ?? '');
        if (rawDef?.namespace) {
            errors.namespace = rawDef.namespace
                .split('.')
                .reduce((a: string | undefined, c: string) => {
                    if (!NAME_REGEX.test(c))
                        return 'Namespace contains only letter, numbers and underscore and should not start with number and parts of it should be seperated by dot(.)';

                    return a;
                }, undefined);
        }

        setErrors(errors);
    }, [rawDef]);

    const functionKeyComp = functionKey ? (
        <div className="_field">
            <label>Key :</label>
            <input type="text" value={functionKey} readOnly />
        </div>
    ) : (
        <></>
    );

    return (
        <div
            className="_paramEditorBack"
            onClick={(ev) => {
                ev.stopPropagation();
                ev.preventDefault();
                onEditFunctionClose?.();
            }}
        >
            <div
                className="_statement _editForm"
                onClick={(ev) => {
                    ev.stopPropagation();
                    ev.preventDefault();
                }}
            >
                <div className="_header">Edit</div>
                <div className="_form">
                    <div className="_field">
                        <label>Name :</label>
                        <input
                            type="text"
                            value={name}
                            onChange={(ev) => setName(ev.target.value)}
                            onBlur={() => {
                                if (name === '' || !name) {
                                    setName(rawDef.name);
                                    return;
                                }
                                if (!NAME_REGEX.test(name)) {
                                    setErrors({
                                        ...errors,
                                        name: 'Name contains only letter, numbers and underscore and should not start with number',
                                    });
                                } else {
                                    setErrors({
                                        ...errors,
                                        name: undefined,
                                    });
                                }
                            }}
                        />
                        {errors.name && <span className="_errors">{errors.name}</span>}
                    </div>
                    <div className="_field">
                        <label>Namespace :</label>
                        <input
                            type="text"
                            value={namespace}
                            onChange={(ev) => setNamespace(ev.target.value)}
                            onBlur={() => {
                                if (namespace === '') {
                                    setNamespace('');
                                    setErrors({
                                        ...errors,
                                        namespace: undefined,
                                    });
                                    return;
                                }

                                const msg = namespace
                                    .split('.')
                                    .reduce((a: string | undefined, c: string) => {
                                        if (!NAME_REGEX.test(c))
                                            return 'Namespace contains only letter, numbers and underscore and should not start with number and parts of it should be seperated by dot(.)';

                                        return a;
                                    }, undefined);

                                setErrors({
                                    ...errors,
                                    namespace: msg,
                                });
                            }}
                        />
                        {errors.namespace && (
                            <span className="_errors">{errors.namespace}</span>
                        )}
                    </div>
                    {functionKeyComp}
                </div>
                <div className="_formButtons">
                    <button
                        className="_okButton"
                        onClick={() => {
                            if (errors.name || errors.namespace) {
                                return;
                            }
                            onChange({ ...rawDef, name, namespace });
                            onEditFunctionClose?.();
                        }}
                    >
                        OK
                    </button>
                    <button onClick={() => onEditFunctionClose?.()}>Cancel</button>
                </div>
            </div>
        </div>
    );
}
