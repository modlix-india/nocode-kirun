// Main editor component
export { default as KIRunEditor } from './editor/KIRunEditor';

// Types
export type { KIRunEditorProps, PersonalizationData, SchemaFormProps } from './types';

// Text editor types
export type { EditorTheme } from './texteditor/KIRunTextEditor';

// SchemaForm (standalone form generator)
export { default as SchemaForm } from './components/SchemaForm/SchemaForm';

// Function Documentation Components
export { default as FunctionDocumentationViewer } from './components/FunctionDocumentationViewer';
export { default as FunctionDetailModal } from './components/FunctionDetailModal';
export { default as MarkdownRenderer } from './components/MarkdownRenderer';

// Function Documentation Registry
export {
	initializeDocumentation,
	isDocumentationLoaded,
	getFunctionDocumentationByName,
	getFunctionDocumentation,
	getAllFunctionDocumentation,
	getFunctionDocumentationByNamespace,
	getAllNamespaces,
	searchFunctionDocumentation,
	getFunctionsByTopLevelNamespace,
	isFunctionAvailableOn,
	registerFunctionDocumentation,
} from './FunctionDocumentationRegistry';

// Function Documentation Types
export type { FunctionDocumentation } from './FunctionDocumentationRegistry';

// Utilities that consumers might need
export { autoLayoutFunctionDefinition } from './util/autoLayout';
export { correctStatementNames, stringValue, makeObjectPaths } from './util/stringValue';

// CSS imports — consumers should import these
import './css/KIRunEditor.css';
import './css/KIRunEditorThemes.css';
import './css/FunctionDocumentationViewer.css';
import './css/FunctionDetailModal.css';
import './css/MarkdownRenderer.css';
