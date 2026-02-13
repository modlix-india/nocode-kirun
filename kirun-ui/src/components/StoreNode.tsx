import React from 'react';
import { Database } from 'lucide-react';
import { generateColor } from '../colors';

interface StoreNodeProps {
    name: string;
}

export function StoreNode({ name }: StoreNodeProps) {
    const color = `#${generateColor('stor', name)}`;
    return (
        <div className={`_storeNode`}>
            <Database size={14} style={{ backgroundColor: color }} />
            <div className="_storeNode_name">{name}</div>
            <div
                className="_storeNode_node"
                id={`_storeNode_${name}`}
                style={{ borderColor: color }}
            />
        </div>
    );
}
