import React, { CSSProperties, useEffect, useRef, useMemo, useState } from 'react';
import { createPortal } from 'react-dom';
import { HelpCircle } from 'lucide-react';
import { getFunctionDocumentationByName, type FunctionDocumentation } from '../FunctionDocumentationRegistry';
import FunctionDetailModal from './FunctionDetailModal';

interface SearchProps {
    value?: string;
    options: { label?: string; key?: string; value: string; description?: string }[];
    placeholder?: string;
    onChange: (value: string) => void;
    onClose?: () => void;
    style?: CSSProperties;
    showDocumentation?: boolean; // Enable documentation help button
}

export default function Search({ value, options, style, onClose, onChange, showDocumentation = true }: SearchProps) {
    const [filter, setFilter] = useState('');
    const [selectedDoc, setSelectedDoc] = useState<FunctionDocumentation | null>(null);

    // Enhance options with descriptions from registry if not provided
    const enhancedOptions = useMemo(() => {
        return options.map(option => {
            if (option.description) {
                return option; // Already has description
            }
            // Try to get description from registry
            const doc = getFunctionDocumentationByName(option.value);
            return {
                ...option,
                description: doc?.description
            };
        });
    }, [options]);

    const filtered = useMemo(() => {
        if (!filter.trim()) return undefined;
        return enhancedOptions.filter((option) => {
            const label = option.label ?? option.value;
            const description = option.description ?? '';
            return (
                label.toLowerCase().includes(filter.toLowerCase()) ||
                description.toLowerCase().includes(filter.toLowerCase())
            );
        });
    }, [filter, enhancedOptions]);

    const handleHelpClick = (e: React.MouseEvent, optionValue: string) => {
        e.stopPropagation();
        e.preventDefault();

        const doc = getFunctionDocumentationByName(optionValue);
        if (doc) {
            setSelectedDoc(doc);
        }
    };

    const closeDocModal = () => {
        setSelectedDoc(null);
    };

    const searchRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (e: MouseEvent) => {
            if (selectedDoc) return;
            if (searchRef.current && !searchRef.current.contains(e.target as Node)) {
                onClose?.();
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [onClose, selectedDoc]);

    return (
        <>
            <div className="_search" style={style} ref={searchRef}>
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
                    {(filtered ?? enhancedOptions)
                        .sort((a, b) =>
                            (a.label ?? a.value ?? '')
                                .toLowerCase()
                                .localeCompare((b.label ?? b.value ?? '').toLowerCase()),
                        )
                        .map((option) => {
                            const hasDescription = option.description && option.description.trim().length > 0;
                            const hasDocumentation = showDocumentation && getFunctionDocumentationByName(option.value);

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
                                    <div className="_option-content">
                                        <div className="_option-label">
                                            {option.label ?? option.value}
                                        </div>
                                        {hasDescription && (
                                            <div className="_option-description">
                                                {option.description}
                                            </div>
                                        )}
                                    </div>
                                    {hasDocumentation && (
                                        <button
                                            className="_option-help"
                                            onMouseDown={(e) => handleHelpClick(e, option.value)}
                                            title="View documentation"
                                        >
                                            <HelpCircle size={16} />
                                        </button>
                                    )}
                                </div>
                            );
                        })}
                </div>
            </div>

            {selectedDoc && typeof document !== 'undefined' && document.body && createPortal(
                <FunctionDetailModal
                    functionDoc={selectedDoc}
                    onClose={closeDocModal}
                />,
                document.body
            )}
        </>
    );
}
