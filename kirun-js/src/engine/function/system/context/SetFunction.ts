import { ExecutionException } from '../../../exception/ExecutionException';
import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { SchemaType } from '../../../json/schema/type/SchemaType';
import { TypeUtil } from '../../../json/schema/type/TypeUtil';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { ContextElement } from '../../../runtime/ContextElement';
import { ExpressionEvaluator } from '../../../runtime/expression/ExpressionEvaluator';
import { TokenValueExtractor } from '../../../runtime/expression/tokenextractor/TokenValueExtractor';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { isNullValue } from '../../../util/NullCheck';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { StringUtil } from '../../../util/string/StringUtil';
import { AbstractFunction } from '../../AbstractFunction';

const NAME = 'name';
const VALUE = 'value';

export class SetFunction extends AbstractFunction {
    private readonly signature = new FunctionSignature('Set')
        .setNamespace(Namespaces.SYSTEM_CTX)
        .setParameters(
            new Map([
                Parameter.ofEntry(
                    NAME,
                    new Schema()
                        .setName(NAME)
                        .setType(TypeUtil.of(SchemaType.STRING))
                        .setMinLength(1),
                    false,
                ),
                Parameter.ofEntry(VALUE, Schema.ofAny(VALUE)),
            ]),
        )
        .setEvents(new Map([Event.outputEventMapEntry(new Map())]));
    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let key: string = context?.getArguments()?.get(NAME);

        if (StringUtil.isNullOrBlank(key)) {
            throw new KIRuntimeException(
                'Empty string is not a valid name for the context element',
            );
        }

        let value: any = context?.getArguments()?.get(VALUE);

        // Use TokenValueExtractor.splitPath for consistent path parsing
        const parts = TokenValueExtractor.splitPath(key);
        
        if (parts.length < 1 || parts[0] !== 'Context') {
            throw new ExecutionException(
                StringFormatter.format('The context path $ is not a valid path in context', key),
            );
        }

        // Evaluate any dynamic expressions in the path (e.g., Context.a[Steps.loop.index])
        const evaluatedParts = this.evaluateDynamicParts(parts, context);

        return this.modifyContextWithParts(context, key, value, evaluatedParts);
    }
    
    /**
     * Evaluate any dynamic expressions in path parts
     * E.g., "Context.a[Steps.loop.index]" where the index is dynamic
     */
    private evaluateDynamicParts(parts: string[], context: FunctionExecutionParameters): string[] {
        const result: string[] = [];
        
        for (const part of parts) {
            // Check if this part contains dynamic bracket expressions
            const evaluated = this.evaluateBracketExpressions(part, context);
            result.push(evaluated);
        }
        
        return result;
    }
    
    /**
     * Evaluate bracket expressions in a path part
     * E.g., "arr[Steps.loop.index]" -> "arr[0]" if Steps.loop.index evaluates to 0
     */
    private evaluateBracketExpressions(part: string, context: FunctionExecutionParameters): string {
        // Find bracket expressions that need evaluation
        let result = '';
        let i = 0;
        
        while (i < part.length) {
            if (part[i] === '[') {
                result += '[';
                i++;
                
                // Find the matching ]
                let bracketContent = '';
                let depth = 1;
                let inQuote = false;
                let quoteChar = '';
                
                while (i < part.length && depth > 0) {
                    const ch = part[i];
                    
                    if (inQuote) {
                        if (ch === quoteChar && part[i - 1] !== '\\') {
                            inQuote = false;
                        }
                        bracketContent += ch;
                    } else {
                        if (ch === '"' || ch === "'") {
                            inQuote = true;
                            quoteChar = ch;
                            bracketContent += ch;
                        } else if (ch === '[') {
                            depth++;
                            bracketContent += ch;
                        } else if (ch === ']') {
                            depth--;
                            if (depth > 0) bracketContent += ch;
                        } else {
                            bracketContent += ch;
                        }
                    }
                    i++;
                }
                
                // Check if bracket content is a static value (number or quoted string)
                if (/^-?\d+$/.test(bracketContent) || 
                    (bracketContent.startsWith('"') && bracketContent.endsWith('"')) ||
                    (bracketContent.startsWith("'") && bracketContent.endsWith("'"))) {
                    result += bracketContent + ']';
                } else {
                    // Dynamic expression - evaluate it
                    try {
                        const evaluator = new ExpressionEvaluator(bracketContent);
                        const evaluatedValue = evaluator.evaluate(context.getValuesMap());
                        result += String(evaluatedValue) + ']';
                    } catch (err) {
                        // If evaluation fails, keep original
                        result += bracketContent + ']';
                    }
                }
            } else {
                result += part[i];
                i++;
            }
        }
        
        return result;
    }

    private modifyContextWithParts(
        context: FunctionExecutionParameters,
        key: string,
        value: any,
        parts: string[],
    ): FunctionOutput {
        // parts[0] is "Context", parts[1] is the context element name
        if (parts.length < 2) {
            throw new KIRuntimeException(
                StringFormatter.format("Context path '$' is too short", key),
            );
        }
        
        // Get the first segment after "Context" - this should be a context element key
        // The segment may contain bracket notation like "a[0]" which we need to parse
        const firstSegment = parts[1];
        const firstSegmentParts = this.parseBracketSegments(firstSegment);
        const contextKey = firstSegmentParts[0];
        
        let ce: ContextElement | undefined = context.getContext()?.get(contextKey);

        if (isNullValue(ce)) {
            throw new KIRuntimeException(
                StringFormatter.format("Context doesn't have any element with name '$' ", contextKey),
            );
        }

        // If we just have "Context.a" with no further path
        if (parts.length === 2 && firstSegmentParts.length === 1) {
            ce!.setElement(value);
            return new FunctionOutput([EventResult.outputOf(new Map())]);
        }

        let el: any = ce!.getElement();
        
        // Initialize element if null
        if (isNullValue(el)) {
            // Determine if first access is array or object
            const nextIsArray = firstSegmentParts.length > 1 
                ? this.isArrayIndex(firstSegmentParts[1])
                : (parts.length > 2 ? this.isArrayAccess(parts[2]) : false);
            el = nextIsArray ? [] : {};
            ce!.setElement(el);
        }

        // Collect all path segments (including bracket notation within segments)
        const allSegments: { value: string; isArray: boolean }[] = [];
        
        // Process remaining parts of the first segment (after context key)
        for (let j = 1; j < firstSegmentParts.length; j++) {
            allSegments.push({
                value: this.stripQuotes(firstSegmentParts[j]),
                isArray: this.isArrayIndex(firstSegmentParts[j])
            });
        }
        
        // Process remaining parts (parts[2], parts[3], etc.)
        for (let i = 2; i < parts.length; i++) {
            const segmentParts = this.parseBracketSegments(parts[i]);
            for (const seg of segmentParts) {
                allSegments.push({
                    value: this.stripQuotes(seg),
                    isArray: this.isArrayIndex(seg)
                });
            }
        }
        
        // Navigate to the parent of the final element
        for (let i = 0; i < allSegments.length - 1; i++) {
            const segment = allSegments[i];
            const nextSegment = allSegments[i + 1];
            
            if (segment.isArray) {
                el = this.getDataFromArray(el, segment.value, nextSegment.isArray);
            } else {
                el = this.getDataFromObject(el, segment.value, nextSegment.isArray);
            }
        }
        
        // Set the final value
        const lastSegment = allSegments[allSegments.length - 1];
        if (lastSegment.isArray) {
            this.putDataInArray(el, lastSegment.value, value);
        } else {
            this.putDataInObject(el, lastSegment.value, value);
        }

        return new FunctionOutput([EventResult.outputOf(new Map())]);
    }
    
    /**
     * Parse bracket segments from a path part
     * E.g., "arr[0]" -> ["arr", "0"], "obj" -> ["obj"]
     */
    private parseBracketSegments(part: string): string[] {
        const segments: string[] = [];
        let start = 0;
        let i = 0;
        
        while (i < part.length) {
            if (part[i] === '[') {
                if (i > start) {
                    segments.push(part.substring(start, i));
                }
                // Find matching ]
                let end = i + 1;
                let inQuote = false;
                let quoteChar = '';
                while (end < part.length) {
                    if (inQuote) {
                        if (part[end] === quoteChar && part[end - 1] !== '\\') {
                            inQuote = false;
                        }
                    } else {
                        if (part[end] === '"' || part[end] === "'") {
                            inQuote = true;
                            quoteChar = part[end];
                        } else if (part[end] === ']') {
                            break;
                        }
                    }
                    end++;
                }
                segments.push(part.substring(i + 1, end));
                start = end + 1;
                i = start;
            } else {
                i++;
            }
        }
        
        if (start < part.length) {
            segments.push(part.substring(start));
        }
        
        return segments.length > 0 ? segments : [part];
    }
    
    private isArrayIndex(segment: string): boolean {
        return /^-?\d+$/.test(segment);
    }
    
    private isArrayAccess(part: string): boolean {
        // Check if the part starts with bracket notation or is a pure number
        return part.startsWith('[') || this.isArrayIndex(part);
    }
    
    private stripQuotes(segment: string): string {
        if ((segment.startsWith('"') && segment.endsWith('"')) ||
            (segment.startsWith("'") && segment.endsWith("'"))) {
            return segment.substring(1, segment.length - 1);
        }
        return segment;
    }

    private getDataFromArray(el: any, mem: string, nextIsArray: boolean): any {
        if (!Array.isArray(el))
            throw new KIRuntimeException(
                StringFormatter.format('Expected an array but found $', el),
            );

        const index = parseInt(mem);
        if (isNaN(index))
            throw new KIRuntimeException(
                StringFormatter.format('Expected an array index but found $', mem),
            );
        if (index < 0)
            throw new KIRuntimeException(
                StringFormatter.format('Array index is out of bound - $', mem),
            );

        let je = el[index];

        if (isNullValue(je)) {
            je = nextIsArray ? [] : {};
            el[index] = je;
        }
        return je;
    }

    private getDataFromObject(el: any, mem: string, nextIsArray: boolean): any {
        if (Array.isArray(el) || typeof el !== 'object')
            throw new KIRuntimeException(
                StringFormatter.format('Expected an object but found $', el),
            );

        let je = el[mem];

        if (isNullValue(je)) {
            je = nextIsArray ? [] : {};
            el[mem] = je;
        }
        return je;
    }

    private putDataInArray(el: any, mem: string, value: any): void {
        if (!Array.isArray(el))
            throw new KIRuntimeException(
                StringFormatter.format('Expected an array but found $', el),
            );

        const index = parseInt(mem);
        if (isNaN(index))
            throw new KIRuntimeException(
                StringFormatter.format('Expected an array index but found $', mem),
            );
        if (index < 0)
            throw new KIRuntimeException(
                StringFormatter.format('Array index is out of bound - $', mem),
            );

        el[index] = value;
    }

    private putDataInObject(el: any, mem: string, value: any): void {
        if (Array.isArray(el) || typeof el !== 'object')
            throw new KIRuntimeException(
                StringFormatter.format('Expected an object but found $', el),
            );

        el[mem] = value;
    }
}
