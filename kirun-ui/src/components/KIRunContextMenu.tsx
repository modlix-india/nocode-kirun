import React from 'react';
import { Trash2, SquarePlus, ClipboardPaste } from 'lucide-react';
import { duplicate } from '@fincity/kirun-js';
import { COPY_STMT_KEY } from '../constants';

interface KIRunContextMenuProps {
    menu: any;
    showMenu: React.Dispatch<any>;
    isReadonly: boolean;
    rawDef: any;
    onChange: (def: any) => void;
    setShowAddSearch: (position: { left: number; top: number }) => void;
    onPaste?: () => Promise<string | undefined>;
}

export default function KIRunContextMenu({
    menu,
    showMenu,
    isReadonly,
    rawDef,
    onChange,
    setShowAddSearch,
    onPaste,
}: KIRunContextMenuProps) {
    if (!menu) return <></>;
    return (
        <div
            className="_menu"
            style={{
                left: `${menu.position.left - 5}px`,
                top: `${menu.position.top - 5}px`,
            }}
            onMouseLeave={() => showMenu(undefined)}
        >
            {menu.type === 'dependent' && (
                <>
                    <div
                        className="_menuItem"
                        onMouseDown={(e) => {
                            e.stopPropagation();
                            e.preventDefault();
                            if (isReadonly) return;

                            const newDef = duplicate(rawDef);
                            const statement = newDef.steps[menu.value.statementName];
                            if (!statement) return;
                            const dependentStatements = statement.dependentStatements;
                            if (!dependentStatements) return;
                            dependentStatements[menu.value.dependency] = false;
                            showMenu(undefined);
                            onChange(newDef);
                        }}
                    >
                        <Trash2 size={14} /> Remove
                    </div>
                </>
            )}
            {menu.type === 'designer' && (
                <>
                    <div
                        className="_menuItem"
                        onMouseDown={(e) => {
                            e.stopPropagation();
                            e.preventDefault();
                            if (isReadonly) return;
                            setShowAddSearch({
                                left: menu.position.left - 5,
                                top: menu.position.top - 5,
                            });
                        }}
                    >
                        <SquarePlus size={14} /> Add a Step
                    </div>
                    <div
                        className="_menuItem"
                        onMouseDown={(e) => {
                            e.stopPropagation();
                            e.preventDefault();
                            if (isReadonly) return;
                            const readClipboard = onPaste
                                ? onPaste()
                                : navigator.clipboard.readText();
                            readClipboard.then((data) => {
                                if (!data || !data.startsWith(COPY_STMT_KEY)) return;

                                const steps = data
                                    .split(COPY_STMT_KEY)
                                    .filter((e) => e)
                                    .map((e) => JSON.parse(e));
                                if (!steps.length) return;
                                let newFun = duplicate(rawDef);
                                if (!newFun.steps) newFun.steps = {};

                                const minLeft =
                                    menu.position.left -
                                    Math.min(
                                        ...steps.map(
                                            (e: any) => (e.position?.left ?? 0) as number,
                                        ),
                                    );
                                const minTop =
                                    menu.position.top -
                                    Math.min(
                                        ...steps.map(
                                            (e: any) => (e.position?.top ?? 0) as number,
                                        ),
                                    );

                                const changes: Array<[string, string] | undefined> = steps.map(
                                    (step: any) => {
                                        let name: string = step.statementName;
                                        let i = 0;
                                        const oldStatementName: string = step.statementName;
                                        while (newFun.steps[name]) {
                                            i++;
                                            name = step.statementName + '_Copy_' + i;
                                        }
                                        step.position = {
                                            left: (step.position?.left ?? 0) + minLeft,
                                            top: (step.position?.top ?? 0) + minTop,
                                        };
                                        step.statementName = name;
                                        newFun.steps[name] = step;
                                        if (oldStatementName == name) return undefined;
                                        return [oldStatementName, name];
                                    },
                                );

                                changes.forEach((params) => {
                                    if (!params) return;
                                    const [oldName, newName] = params;

                                    for (let step of steps) {
                                        const str = JSON.stringify(step);
                                        const newStr = str.replace(
                                            `Steps.${oldName}.`,
                                            `Steps.${newName}.`,
                                        );
                                        newFun.steps[step.statementName] = JSON.parse(newStr);
                                    }
                                });

                                showMenu(undefined);
                                onChange(newFun);
                            });
                        }}
                    >
                        <ClipboardPaste size={14} /> Paste
                    </div>
                </>
            )}
        </div>
    );
}
