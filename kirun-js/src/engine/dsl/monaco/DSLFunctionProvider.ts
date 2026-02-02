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
    private static cachedFunctions: FunctionInfo[] | null = null;

    /**
     * Get all available functions from repository
     */
    public static async getAllFunctions(
        repo?: Repository<Function>,
    ): Promise<FunctionInfo[]> {
        if (this.cachedFunctions) {
            return this.cachedFunctions;
        }

        const functionRepo = repo || new KIRunFunctionRepository();
        const functions: FunctionInfo[] = [];

        // Get all namespaces and functions
        // Note: KIRunFunctionRepository has getAllFunctions() method
        const allFunctions = await this.getAllFromRepository(functionRepo);

        for (const func of allFunctions) {
            const signature = func.getSignature();
            const namespace = signature.getNamespace();
            const name = signature.getName();

            functions.push({
                namespace,
                name,
                fullName: `${namespace}.${name}`,
                parameters: this.extractParameters(func),
                events: this.extractEvents(func),
                description: '', // TODO: Add description if available
            });
        }

        // Cache the results
        this.cachedFunctions = functions;

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
    public static clearCache(): void {
        this.cachedFunctions = null;
    }

    /**
     * Get all functions from repository
     */
    private static async getAllFromRepository(repo: Repository<Function>): Promise<Function[]> {
        // KIRunFunctionRepository provides all built-in functions
        // This is a simplified implementation - in reality, we'd query the repository
        const functions: Function[] = [];

        // Get all namespaces
        const namespaces = [
            'System',
            'System.Math',
            'System.Array',
            'System.String',
            'System.Object',
            'System.Date',
            'System.Context',
            'System.Loop',
            'System.JSON',
        ];

        // For each namespace, try to find common functions
        // This is a workaround since Repository doesn't have a getAllFunctions method
        // In practice, this would be implemented properly in the repository
        for (const namespace of namespaces) {
            try {
                // Try to find common function names
                const commonNames = this.getCommonFunctionNames(namespace);
                for (const name of commonNames) {
                    try {
                        const func = await repo.find(namespace, name);
                        if (func) {
                            functions.push(func);
                        }
                    } catch {
                        // Function not found, skip
                    }
                }
            } catch {
                // Namespace not found, skip
            }
        }

        return functions;
    }

    /**
     * Get common function names for a namespace
     */
    private static getCommonFunctionNames(namespace: string): string[] {
        const commonFunctions: { [key: string]: string[] } = {
            System: ['If', 'GenerateEvent', 'Print', 'ValidateSchema', 'Wait'],
            'System.Math': [
                'Add',
                'Subtract',
                'Multiply',
                'Divide',
                'Modulo',
                'Power',
                'Absolute',
                'Ceiling',
                'Floor',
                'Round',
                'Maximum',
                'Minimum',
                'Random',
            ],
            'System.Array': [
                'AddFirst',
                'InsertLast',
                'Insert',
                'Delete',
                'DeleteFirst',
                'DeleteLast',
                'Sort',
                'Reverse',
                'IndexOf',
                'LastIndexOf',
                'Join',
                'Concatenate',
                'SubArray',
            ],
            'System.String': [
                'Concatenate',
                'Split',
                'Replace',
                'Substring',
                'ToLowerCase',
                'ToUpperCase',
                'Trim',
                'Length',
                'IndexOf',
                'LastIndexOf',
            ],
            'System.Object': ['Keys', 'Values', 'Entries', 'PutValue', 'DeleteKey', 'Convert'],
            'System.Date': [
                'GetCurrent',
                'FromDateString',
                'ToDateString',
                'AddSubtractTime',
                'SetTimeZone',
            ],
            'System.Context': ['Create', 'Get', 'Set'],
            'System.Loop': ['RangeLoop', 'CountLoop', 'ForEachLoop', 'Break'],
            'System.JSON': ['Parse', 'Stringify'],
        };

        return commonFunctions[namespace] || [];
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
                paramInfos.push({
                    name,
                    type: this.getSchemaType(param.getSchema()),
                    required: true, // TODO: Determine if parameter is required
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
        if (!schema) return 'ANY';
        const type = schema.getType?.();
        if (!type) return 'ANY';
        return String(type);
    }
}
