import React, { CSSProperties, useEffect, useMemo, useState } from 'react';

interface SearchProps {
    value?: string;
    options: { label?: string; key?: string; value: string }[];
    placeholder?: string;
    onChange: (value: string) => void;
    onClose?: () => void;
    style?: CSSProperties;
}

export default function Search({ value, options, style, onClose, onChange }: SearchProps) {
    const [filter, setFilter] = useState('');

    const filtered = useMemo(() => {
        if (!filter.trim()) return undefined;
        return options.filter((option) => {
            return (
                option.label?.toLowerCase().includes(filter.toLowerCase()) ??
                option.value.toLowerCase().includes(filter.toLowerCase())
            );
        });
    }, [filter]);

    return (
        <div className="_search" style={style} onMouseLeave={onClose}>
            <input
                className="_value"
                value={filter}
                placeholder={value ?? 'Search...'}
                onChange={(e) => setFilter(e.target.value)}
                onKeyUp={(e) => {
                    if (e.key === 'Delete' || e.key === 'Backspace') {
                        e.stopPropagation();
                        e.preventDefault();
                    }
                }}
                autoFocus
            />
            <div className="_options">
                {(filtered ?? options)
                    .sort((a, b) =>
                        (a.label ?? a.value)
                            .toLowerCase()
                            .localeCompare((b.label ?? b.value).toLowerCase()),
                    )
                    .map((option) => {
                        return (
                            <div
                                className="_option"
                                key={option.value}
                                onMouseDown={(e) => {
                                    e.stopPropagation();
                                    e.preventDefault();
                                    onChange(option.value);
                                }}
                            >
                                {option.label ?? option.value}
                            </div>
                        );
                    })}
            </div>
        </div>
    );
}
