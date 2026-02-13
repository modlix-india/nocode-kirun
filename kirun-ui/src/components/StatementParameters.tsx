import { Function, Repository, Schema } from '@fincity/kirun-js';
import React from 'react';

interface StatementParametersProps {
    position: { left: number; top: number };
    statement: any;
    storePaths?: Set<string>;
    functionRepository?: Repository<Function>;
    schemaRepository?: Repository<Schema>;
    children?: React.ReactNode;
    onEditParametersClose?: () => void;
}

export default function StatementParameters({
    children,
    onEditParametersClose,
}: StatementParametersProps) {
    return (
        <div
            className="_paramEditorBack"
            onClick={(ev) => {
                ev.stopPropagation();
                ev.preventDefault();
                onEditParametersClose?.();
            }}
        >
            <div className="_statementBack">{children}</div>
        </div>
    );
}
