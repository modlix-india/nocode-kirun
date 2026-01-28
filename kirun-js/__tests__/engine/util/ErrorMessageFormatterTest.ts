import { ErrorMessageFormatter } from '../../../src/engine/util/ErrorMessageFormatter';
import { Schema, ArraySchemaType } from '../../../src';

describe('ErrorMessageFormatter', () => {
    describe('formatValue', () => {
        test('should format null', () => {
            expect(ErrorMessageFormatter.formatValue(null)).toBe('null');
        });

        test('should format undefined', () => {
            expect(ErrorMessageFormatter.formatValue(undefined)).toBe('undefined');
        });

        test('should format strings with quotes', () => {
            expect(ErrorMessageFormatter.formatValue('hello')).toBe('"hello"');
        });

        test('should format numbers', () => {
            expect(ErrorMessageFormatter.formatValue(42)).toBe('42');
            expect(ErrorMessageFormatter.formatValue(3.14)).toBe('3.14');
        });

        test('should format booleans', () => {
            expect(ErrorMessageFormatter.formatValue(true)).toBe('true');
            expect(ErrorMessageFormatter.formatValue(false)).toBe('false');
        });

        test('should format objects as JSON', () => {
            const obj = { name: 'John', age: 30 };
            const result = ErrorMessageFormatter.formatValue(obj);
            expect(result).toContain('"name"');
            expect(result).toContain('"John"');
            expect(result).toContain('"age"');
            expect(result).toContain('30');
        });

        test('should format arrays as JSON', () => {
            const arr = [1, 2, 3];
            const result = ErrorMessageFormatter.formatValue(arr);
            expect(result).toContain('[');
            expect(result).toContain('1');
            expect(result).toContain('2');
            expect(result).toContain('3');
            expect(result).toContain(']');
        });

        test('should handle circular references', () => {
            const obj: any = { name: 'test' };
            obj.self = obj;
            const result = ErrorMessageFormatter.formatValue(obj);
            expect(result).toContain('[Circular]');
        });

        test('should truncate long values', () => {
            const longObj = { data: 'x'.repeat(300) };
            const result = ErrorMessageFormatter.formatValue(longObj, 50);
            expect(result.length).toBeLessThanOrEqual(53); // 50 + "..."
            expect(result).toContain('...');
        });
    });

    describe('formatFunctionName', () => {
        test('should format namespace.name', () => {
            expect(ErrorMessageFormatter.formatFunctionName('UIEngine', 'SetStore')).toBe(
                'UIEngine.SetStore',
            );
        });

        test('should handle undefined namespace', () => {
            expect(ErrorMessageFormatter.formatFunctionName(undefined, 'loadEverything')).toBe(
                'loadEverything',
            );
        });

        test('should handle null namespace', () => {
            expect(ErrorMessageFormatter.formatFunctionName(null, 'loadApp')).toBe('loadApp');
        });

        test('should handle "undefined" string', () => {
            expect(ErrorMessageFormatter.formatFunctionName('undefined', 'loadEverything')).toBe(
                'loadEverything',
            );
        });
    });

    describe('formatStatementName', () => {
        test('should format statement name with quotes', () => {
            expect(ErrorMessageFormatter.formatStatementName('storeString')).toBe("'storeString'");
        });

        test('should return null for undefined statement name', () => {
            expect(ErrorMessageFormatter.formatStatementName(undefined)).toBeNull();
        });

        test('should return null for null statement name', () => {
            expect(ErrorMessageFormatter.formatStatementName(null)).toBeNull();
        });

        test('should return null for "undefined" string', () => {
            expect(ErrorMessageFormatter.formatStatementName('undefined')).toBeNull();
        });
    });

    describe('buildFunctionExecutionError', () => {
        test('should build error message with statement name', () => {
            const result = ErrorMessageFormatter.buildFunctionExecutionError(
                'UIEngine.SetStore',
                "'storeString'",
                'Expected an array but found {"key": "value"}',
            );
            expect(result).toBe(
                "Error while executing the function UIEngine.SetStore in statement 'storeString': Expected an array but found {\"key\": \"value\"}",
            );
        });

        test('should build error message without statement name', () => {
            const result = ErrorMessageFormatter.buildFunctionExecutionError(
                'loadEverything',
                null,
                'Some error occurred',
            );
            expect(result).toBe(
                'Error while executing the function loadEverything: Some error occurred',
            );
        });

        test('should build error message with parameter name', () => {
            const result = ErrorMessageFormatter.buildFunctionExecutionError(
                'UIEngine.SetStore',
                "'storeString'",
                'Expected an array but found {}',
                'value',
            );
            expect(result).toBe(
                "Error while executing the function UIEngine.SetStore's parameter value in statement 'storeString': Expected an array but found {}",
            );
        });

        test('should build error message with parameter but no statement name', () => {
            const result = ErrorMessageFormatter.buildFunctionExecutionError(
                'loadApp',
                null,
                'Invalid parameter',
                'config',
            );
            expect(result).toBe(
                "Error while executing the function loadApp's parameter config: Invalid parameter",
            );
        });

        test('should build error message with parameter schema definition', () => {
            const ast = new ArraySchemaType();
            ast.setSingleSchema(Schema.ofInteger('item'));
            const schema = Schema.ofArray('value').setItems(ast);
            const result = ErrorMessageFormatter.buildFunctionExecutionError(
                'System.Array.Concatenate',
                null,
                'Expected an array but found null',
                'secondSource',
                schema,
            );
            expect(result).toBe(
                "Error while executing the function System.Array.Concatenate's parameter secondSource [Expected: Array<Integer>]: Expected an array but found null",
            );
        });

        test('should wrap nested error messages to show execution path', () => {
            // Simulate nested function calls where inner error is already formatted
            const innerError = "Error while executing the function UIEngine.SetStore in statement 'storeString' [Expected: Array]: Expected an array but found {}";

            // Wrap it from an outer function to show call chain
            const result = ErrorMessageFormatter.buildFunctionExecutionError(
                'loadApp',
                null,
                innerError,
            );

            // Should wrap the error with a newline to show the full execution path
            expect(result).toBe("Error while executing the function loadApp: \n" + innerError);
        });
    });

    describe('formatErrorMessage', () => {
        test('should extract message from Error object', () => {
            const error = new Error('Something went wrong');
            expect(ErrorMessageFormatter.formatErrorMessage(error)).toBe('Something went wrong');
        });

        test('should handle string errors', () => {
            expect(ErrorMessageFormatter.formatErrorMessage('Error message')).toBe('Error message');
        });

        test('should handle undefined error', () => {
            expect(ErrorMessageFormatter.formatErrorMessage(undefined)).toBe('Unknown error');
        });

        test('should handle object errors', () => {
            const error = { message: 'Expected an array but found something' };
            const result = ErrorMessageFormatter.formatErrorMessage(error);
            expect(result).toBe('Expected an array but found something');
        });
    });
});
