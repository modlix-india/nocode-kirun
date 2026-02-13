import React from 'react';
import { List, MessageCircle, Trash2, Clipboard, Unlink } from 'lucide-react';

interface StatementButtonsProps {
    selected: boolean;
    onEditParameters?: (name: string) => void;
    onEditComment: () => void;
    statementName: string;
    onDelete: (statementName: string) => void;
    statement: any;
    showEditParameters: boolean;
    editParameters?: boolean;
    onRemoveAllDependencies: () => void;
    onCopy: (statementName: string) => void;
}

export default function StatementButtons({
    selected,
    onEditComment,
    onEditParameters,
    statementName,
    onDelete,
    statement,
    showEditParameters,
    editParameters,
    onRemoveAllDependencies,
    onCopy,
}: StatementButtonsProps) {
    if (!selected) return <></>;

    const editParamsButton =
        showEditParameters && !editParameters ? (
            <>
                <List
                    size={14}
                    onMouseDown={(e) => {
                        e.stopPropagation();
                        e.preventDefault();
                        onEditParameters?.(statementName);
                    }}
                >
                    <title>Edit Parameters</title>
                </List>
                <div className="_buttonsGap"></div>
            </>
        ) : (
            <></>
        );

    return (
        <div className="_buttons">
            {editParamsButton}
            <MessageCircle
                size={14}
                onMouseDown={(e) => {
                    e.stopPropagation();
                    e.preventDefault();
                    onEditComment();
                }}
            >
                <title>Comment</title>
            </MessageCircle>
            <div className="_buttonsGap"></div>
            <Trash2
                className="_hideInEdit"
                size={14}
                onMouseDown={(e) => {
                    e.stopPropagation();
                    e.preventDefault();
                    onDelete(statement.statementName);
                }}
            >
                <title>{`Delete ${statementName} Step`}</title>
            </Trash2>
            <Clipboard
                size={14}
                onMouseDown={(e) => {
                    e.stopPropagation();
                    e.preventDefault();
                    onCopy(statementName);
                }}
            >
                <title>{`Copy ${statementName} Step`}</title>
            </Clipboard>
            <div className="_buttonsGap"></div>
            <Unlink
                size={14}
                onMouseDown={(e) => {
                    e.stopPropagation();
                    e.preventDefault();

                    onRemoveAllDependencies?.();
                }}
            >
                <title>Remove all dependencies</title>
            </Unlink>
        </div>
    );
}
