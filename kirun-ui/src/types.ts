import type { CSSProperties } from 'react';
import type {
    ExecutionLog,
    Function,
    FunctionDefinition,
    Repository,
    Schema,
    TokenValueExtractor,
} from '@fincity/kirun-js';

export type EditorTheme = 'light' | 'dark' | 'high-contrast' | 'easy-on-eyes' | 'flared-up';

export interface PersonalizationData {
    magnification?: number;
    showComments?: boolean;
    showStores?: boolean;
    showParamValues?: boolean;
    textEditorTheme?: EditorTheme;
    textEditorWordWrap?: 'on' | 'off';
    editorMode?: 'visual' | 'text';
}

export interface KIRunEditorProps {
    /** The raw function definition JSON (plain object, not FunctionDefinition instance) */
    functionDefinition?: any;
    /** Called when the function definition changes. Receives the new raw JSON. */
    onChange?: (def: any) => void;

    /** Function repository for validation and autocomplete */
    functionRepository: Repository<Function>;
    /** Schema repository for validation and autocomplete */
    schemaRepository: Repository<Schema>;
    /** Optional token value extractors */
    tokenValueExtractors?: Map<string, TokenValueExtractor>;

    /** Read-only mode */
    readOnly?: boolean;
    /** Optional key identifying this function */
    functionKey?: string;
    /** Store names to display as store nodes */
    stores?: string[];
    /** Set of known store paths for expression autocomplete */
    storePaths?: Set<string>;
    /** Whether to hide the arguments section */
    hideArguments?: boolean;

    /** Debug view mode */
    debugViewMode?: boolean;
    /** Execution log for debug mode */
    executionLog?: ExecutionLog;

    /** Editor personalization preferences */
    personalization?: PersonalizationData;
    /** Called when personalization preferences change */
    onPersonalizationChange?: (data: PersonalizationData) => void;

    /** Called when user copies a statement */
    onCopy?: (clipboardData: string) => void;
    /** Called when user pastes. Should return clipboard text or undefined. */
    onPaste?: () => Promise<string | undefined>;

    /** Additional CSS class name for the root element */
    className?: string;
    /** Additional inline styles for the root element */
    style?: CSSProperties;
}

export interface SchemaFormProps {
    /** The schema to render a form for */
    schema?: Schema;
    /** Schema repository for resolving $ref schemas */
    schemaRepository: Repository<Schema>;
    /** Current value */
    value: any;
    /** Called when value changes */
    onChange: (value: any) => void;
    /** Read-only mode */
    readOnly?: boolean;
}
