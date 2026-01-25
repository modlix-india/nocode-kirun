// Debug helper to trace parser behavior
import { ExpressionParser } from './ExpressionParser';
import { Expression } from './Expression';

export function debugParse(expr: string): void {
    console.log(`\n=== Parsing: ${expr} ===`);
    try {
        const parser = new ExpressionParser(expr);
        const result = parser.parse();
        console.log('Tokens:', result.getTokens().toArray().map(t => t.toString()));
        console.log('Ops:', result.getOperations().toArray().map(o => o.getOperator()));
        console.log('ToString:', result.toString());
    } catch (error: any) {
        console.error('Error:', error.message);
    }
}

// Test cases
debugParse('Context.a[1]');
debugParse('Context.a');
debugParse('a[1]');
