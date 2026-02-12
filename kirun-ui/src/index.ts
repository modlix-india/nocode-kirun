// Main editor component
export { default as KIRunEditor } from './editor/KIRunEditor';

// Types
export type { KIRunEditorProps, PersonalizationData, SchemaFormProps } from './types';

// Text editor types
export type { EditorTheme } from './texteditor/KIRunTextEditor';

// SchemaForm (standalone form generator)
export { default as SchemaForm } from './components/SchemaForm/SchemaForm';

// Utilities that consumers might need
export { autoLayoutFunctionDefinition } from './util/autoLayout';
export { correctStatementNames, stringValue, makeObjectPaths } from './util/stringValue';

// CSS imports — consumers should import these
import './css/KIRunEditor.css';
import './css/KIRunEditorThemes.css';
