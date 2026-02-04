import { Function } from '../../function/Function';
import { Repository } from '../../Repository';
import { KIRunFunctionRepository } from '../../repository/KIRunFunctionRepository';

export interface FunctionInfo {
    namespace: string;
    name: string;
    fullName: string;
    parameters: ParameterInfo[];
    events: EventInfo[];
    description?: string;
}

export interface ParameterInfo {
    name: string;
    type: string;
    required: boolean;
}

export interface EventInfo {
    name: string;
    parameters: { [key: string]: string };
}

/**
 * DSL Function Provider
 * Provides list of available functions for Monaco autocomplete
 */
export class DSLFunctionProvider {
    private static readonly cachedFunctions: Map<Repository<Function>, FunctionInfo[]> = new Map();

    /**
     * Get all available functions from repository
     * Uses repository.filter("") to dynamically discover all available functions
     */
    public static async getAllFunctions(
        repo?: Repository<Function>,
    ): Promise<FunctionInfo[]> {
        const functionRepo = repo || new KIRunFunctionRepository();

        // Check cache for this specific repository
        if (this.cachedFunctions.has(functionRepo)) {
            return this.cachedFunctions.get(functionRepo)!;
        }

        // Use filter("") to get all function names from the repository
        const allFunctionNames = await functionRepo.filter('');

        // Parse all function names and fetch in parallel for better performance
        const fetchPromises = allFunctionNames.map(async (fullName): Promise<FunctionInfo | null> => {
            const lastDotIndex = fullName.lastIndexOf('.');
            if (lastDotIndex === -1) return null;

            const namespace = fullName.substring(0, lastDotIndex);
            const name = fullName.substring(lastDotIndex + 1);

            try {
                const func = await functionRepo.find(namespace, name);
                if (func) {
                    return {
                        namespace,
                        name,
                        fullName,
                        parameters: this.extractParameters(func),
                        events: this.extractEvents(func),
                        description: '',
                    };
                }
            } catch {
                // Function not found or error, skip
            }
            return null;
        });

        const results = await Promise.all(fetchPromises);
        const functions = results.filter((f): f is FunctionInfo => f !== null);

        // Cache the results for this repository
        this.cachedFunctions.set(functionRepo, functions);

        return functions;
    }

    /**
     * Get functions by namespace
     */
    public static async getFunctionsByNamespace(
        namespace: string,
        repo?: Repository<Function>,
    ): Promise<FunctionInfo[]> {
        const allFunctions = await this.getAllFunctions(repo);
        return allFunctions.filter((f) => f.namespace === namespace);
    }

    /**
     * Get all unique namespaces
     */
    public static async getAllNamespaces(repo?: Repository<Function>): Promise<string[]> {
        const allFunctions = await this.getAllFunctions(repo);
        const namespaces = new Set(allFunctions.map((f) => f.namespace));
        return Array.from(namespaces).sort();
    }

    /**
     * Clear cached functions
     */
    public static clearCache(repo?: Repository<Function>): void {
        if (repo) {
            this.cachedFunctions.delete(repo);
        } else {
            this.cachedFunctions.clear();
        }
    }

    /**
     * Extract parameter info from function
     */
    private static extractParameters(func: Function): ParameterInfo[] {
        const signature = func.getSignature();
        const parameters = signature.getParameters();
        const paramInfos: ParameterInfo[] = [];

        if (parameters) {
            for (const [name, param] of parameters) {
                const schema = param.getSchema();
                paramInfos.push({
                    name,
                    type: this.getSchemaType(schema),
                    // Parameter is required if it has no default value
                    required: schema?.getDefaultValue() === undefined,
                });
            }
        }

        return paramInfos;
    }

    /**
     * Extract event info from function
     */
    private static extractEvents(func: Function): EventInfo[] {
        const signature = func.getSignature();
        const events = signature.getEvents();
        const eventInfos: EventInfo[] = [];

        if (events) {
            for (const [name, event] of events) {
                const params: { [key: string]: string } = {};
                const eventParams = event.getParameters();
                if (eventParams) {
                    for (const [paramName, schema] of eventParams) {
                        params[paramName] = this.getSchemaType(schema);
                    }
                }
                eventInfos.push({
                    name,
                    parameters: params,
                });
            }
        }

        return eventInfos;
    }

    /**
     * Get schema type as string
     */
    private static getSchemaType(schema: any): string {
        if (!schema) return 'Any';

        const type = schema.getType?.();
        if (!type) return 'Any';

        // Type is either SingleType or MultipleType
        // Get the allowed schema types and convert to readable string
        const allowedTypes = type.getAllowedSchemaTypes?.();
        if (allowedTypes && allowedTypes.size > 0) {
            const typeNames = Array.from(allowedTypes);
            if (typeNames.length === 1) {
                return String(typeNames[0]); // SchemaType enum values are strings like 'String', 'Integer'
            }
            return typeNames.join(' | ');
        }

        return 'Any';
    }
}
