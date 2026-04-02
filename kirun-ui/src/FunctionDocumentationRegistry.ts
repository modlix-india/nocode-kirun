import type { FunctionSignature } from '@fincity/kirun-js';

/**
 * Interface representing function documentation metadata
 */
export interface FunctionDocumentation {
	fullName: string;
	description: string;
	documentation: string; // Markdown formatted
	metadata?: {
		examples?: Array<{
			title: string;
			description: string;
			code: string;
		}>;
		[key: string]: any;
	};
	availableIn: Array<'JS' | 'Java'>;
}

/**
 * Lazy-loaded documentation registry
 * The JSON file is only loaded when first accessed, not on module import
 */
let DOCUMENTATION_REGISTRY: Map<string, FunctionDocumentation> | null = null;
let loadingPromise: Promise<void> | null = null;

/**
 * Initialize and load the documentation registry
 * Call this before using any documentation functions to ensure data is loaded
 * @returns Promise that resolves when the registry is loaded
 */
export async function initializeDocumentation(): Promise<void> {
	if (DOCUMENTATION_REGISTRY) return;

	if (loadingPromise) return loadingPromise;

	loadingPromise = (async () => {
		try {
			// Dynamic import - only loads when this function is called
			const module = await import('./FunctionDocumentation.json');
			const data = module.default || module;
			DOCUMENTATION_REGISTRY = new Map(
				Object.entries(data as Record<string, FunctionDocumentation>),
			);
		} catch (error) {
			console.error('Failed to load function documentation:', error);
			DOCUMENTATION_REGISTRY = new Map();
		} finally {
			loadingPromise = null;
		}
	})();

	return loadingPromise;
}

/**
 * Check if documentation is loaded
 * @returns true if documentation has been loaded
 */
export function isDocumentationLoaded(): boolean {
	return DOCUMENTATION_REGISTRY !== null;
}

/**
 * Get the loaded registry (synchronous, returns empty map if not loaded)
 * @returns The documentation registry
 */
function getRegistry(): Map<string, FunctionDocumentation> {
	return DOCUMENTATION_REGISTRY || new Map();
}

/**
 * Get documentation for a function by its full name (namespace.functionName)
 * Note: Ensure initializeDocumentation() is called first
 * @param fullName - The full qualified name of the function (e.g., "System.Array.Sort")
 * @returns FunctionDocumentation object or undefined if not found
 */
export function getFunctionDocumentationByName(fullName: string): FunctionDocumentation | undefined {
	return getRegistry().get(fullName);
}

/**
 * Get documentation for a function from its signature, with fallback to registry
 * Note: Ensure initializeDocumentation() is called first
 * @param signature - The FunctionSignature object
 * @returns FunctionDocumentation object with data from signature or registry
 */
export function getFunctionDocumentation(signature: FunctionSignature): FunctionDocumentation {
	const fullName = signature.getFullName();
	const registryDoc = getRegistry().get(fullName);

	// Get description from signature first, fallback to registry
	const description =
		(signature as any).getDescription?.() ||
		registryDoc?.description ||
		`${fullName} - No description available`;

	// Get documentation from signature first, fallback to registry
	const documentation =
		(signature as any).getDocumentation?.() ||
		registryDoc?.documentation ||
		`# ${fullName}\n\nNo documentation available.`;

	// Get metadata from signature first, fallback to registry
	const metadata = (signature as any).getMetadata?.() || registryDoc?.metadata || {};

	// Get availability from registry
	const availableIn = registryDoc?.availableIn || ['JS'];

	return {
		fullName,
		description,
		documentation,
		metadata,
		availableIn,
	};
}

/**
 * Get all function documentation entries
 * Note: Ensure initializeDocumentation() is called first
 * @returns Array of all FunctionDocumentation objects
 */
export function getAllFunctionDocumentation(): FunctionDocumentation[] {
	return Array.from(getRegistry().values());
}

/**
 * Get documentation for functions in a specific namespace
 * Note: Ensure initializeDocumentation() is called first
 * @param namespace - The namespace to filter by (e.g., "System.Array")
 * @returns Array of FunctionDocumentation objects in that namespace
 */
export function getFunctionDocumentationByNamespace(namespace: string): FunctionDocumentation[] {
	return Array.from(getRegistry().values()).filter(doc =>
		doc.fullName.startsWith(namespace + '.'),
	);
}

/**
 * Get all unique namespaces
 * Note: Ensure initializeDocumentation() is called first
 * @returns Array of namespace strings
 */
export function getAllNamespaces(): string[] {
	const namespaces = new Set<string>();
	getRegistry().forEach(doc => {
		const parts = doc.fullName.split('.');
		if (parts.length > 1) {
			// Add full namespace path (e.g., "System.Array")
			for (let i = 1; i < parts.length; i++) {
				namespaces.add(parts.slice(0, i).join('.'));
			}
		}
	});
	return Array.from(namespaces).sort();
}

/**
 * Search function documentation by keyword
 * Note: Ensure initializeDocumentation() is called first
 * @param keyword - Search term to look for in name, description, or documentation
 * @returns Array of matching FunctionDocumentation objects
 */
export function searchFunctionDocumentation(keyword: string): FunctionDocumentation[] {
	const lowerKeyword = keyword.toLowerCase();
	return Array.from(getRegistry().values()).filter(
		doc =>
			doc.fullName.toLowerCase().includes(lowerKeyword) ||
			doc.description.toLowerCase().includes(lowerKeyword) ||
			doc.documentation.toLowerCase().includes(lowerKeyword),
	);
}

/**
 * Get function documentation grouped by top-level namespace
 * Note: Ensure initializeDocumentation() is called first
 * @returns Object with namespace as key and array of functions as value
 */
export function getFunctionsByTopLevelNamespace(): Record<string, FunctionDocumentation[]> {
	const grouped: Record<string, FunctionDocumentation[]> = {};

	getRegistry().forEach(doc => {
		const topLevelNamespace = doc.fullName.split('.')[0];
		if (!grouped[topLevelNamespace]) {
			grouped[topLevelNamespace] = [];
		}
		grouped[topLevelNamespace].push(doc);
	});

	return grouped;
}

/**
 * Check if a function is available in a specific platform
 * Note: Ensure initializeDocumentation() is called first
 * @param fullName - The full qualified name of the function
 * @param platform - The platform to check ('JS' or 'Java')
 * @returns true if the function is available on that platform
 */
export function isFunctionAvailableOn(fullName: string, platform: 'JS' | 'Java'): boolean {
	const doc = getRegistry().get(fullName);
	return doc?.availableIn.includes(platform) ?? false;
}

export default {
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
};
