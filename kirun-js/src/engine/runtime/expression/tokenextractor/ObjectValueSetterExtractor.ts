import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { isNullValue } from '../../../util/NullCheck';
import { duplicate } from '../../../util/duplicate';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { Operation } from '../Operation';
import { TokenValueExtractor } from './TokenValueExtractor';

export class ObjectValueSetterExtractor extends TokenValueExtractor {
    private store: any;
    private prefix: string;
    constructor(store: any, prefix: string) {
        super();
        this.store = store;
        this.prefix = prefix;
    }
    protected getValueInternal(token: string) {
        let parts: string[] = TokenValueExtractor.splitPath(token);
        return this.retrieveElementFrom(token, parts, 1, this.store);
    }

    public getStore(): any {
        return this.store;
    }
    public setStore(store: any): ObjectValueSetterExtractor {
        this.store = store;
        return this;
    }

    public setValue(
        token: string,
        value: any,
        overwrite: boolean = true,
        deleteOnNull: boolean = false,
    ) {
        this.store = duplicate(this.store);
        this.modifyStore(token, value, overwrite, deleteOnNull);
    }

    private modifyStore(
        stringToken: string,
        value: any,
        overwrite: boolean,
        deleteOnNull: boolean,
    ) {
        // Use TokenValueExtractor.splitPath to get path segments instead of Expression parsing
        // This is more reliable as it directly handles the path string
        const parts = TokenValueExtractor.splitPath(stringToken);
        
        if (parts.length < 2) {
            throw new KIRuntimeException(
                StringFormatter.format('Invalid path: $', stringToken),
            );
        }
        
        // Start from index 1 (skip the prefix like 'Store')
        let el = this.store;
        
        // Navigate to the parent of the final element
        for (let i = 1; i < parts.length - 1; i++) {
            const part = parts[i];
            const nextPart = parts[i + 1];
            
            // Parse bracket segments within this part
            const segments = this.parseBracketSegments(part);
            
            for (let j = 0; j < segments.length; j++) {
                const segment = segments[j];
                const isLastSegment = (i === parts.length - 2 && j === segments.length - 1);
                const nextOp = isLastSegment ? this.getOpForSegment(parts[parts.length - 1]) : this.getOpForSegment(nextPart);
                
                if (this.isArrayIndex(segment)) {
                    el = this.getDataFromArray(el, segment, nextOp);
                } else {
                    el = this.getDataFromObject(el, this.stripQuotes(segment), nextOp);
                }
            }
        }
        
        // Handle the final part (set the value)
        const finalPart = parts[parts.length - 1];
        const finalSegments = this.parseBracketSegments(finalPart);
        
        // Navigate through all but the last segment of the final part
        for (let j = 0; j < finalSegments.length - 1; j++) {
            const segment = finalSegments[j];
            const nextOp = this.isArrayIndex(finalSegments[j + 1]) ? Operation.ARRAY_OPERATOR : Operation.OBJECT_OPERATOR;
            
            if (this.isArrayIndex(segment)) {
                el = this.getDataFromArray(el, segment, nextOp);
            } else {
                el = this.getDataFromObject(el, this.stripQuotes(segment), nextOp);
            }
        }
        
        // Set the final value
        const lastSegment = finalSegments[finalSegments.length - 1];
        if (this.isArrayIndex(lastSegment)) {
            this.putDataInArray(el, lastSegment, value, overwrite, deleteOnNull);
        } else {
            this.putDataInObject(el, this.stripQuotes(lastSegment), value, overwrite, deleteOnNull);
        }
    }
    
    /**
     * Parse a path segment that may contain bracket notation.
     * E.g., "addresses[0]" -> ["addresses", "0"]
     * E.g., 'obj["key"]' -> ["obj", "key"]
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
                // Extract bracket content (without the brackets)
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
    
    /**
     * Check if a segment is an array index (numeric)
     */
    private isArrayIndex(segment: string): boolean {
        // Check if it's a pure number (possibly negative)
        return /^-?\d+$/.test(segment);
    }
    
    /**
     * Strip quotes from a segment if present
     */
    private stripQuotes(segment: string): string {
        if ((segment.startsWith('"') && segment.endsWith('"')) ||
            (segment.startsWith("'") && segment.endsWith("'"))) {
            return segment.substring(1, segment.length - 1);
        }
        return segment;
    }
    
    /**
     * Determine the operation type for the next segment
     */
    private getOpForSegment(segment: string): Operation {
        // Check if the segment starts with a bracket or is a pure number
        if (this.isArrayIndex(segment) || segment.startsWith('[')) {
            return Operation.ARRAY_OPERATOR;
        }
        return Operation.OBJECT_OPERATOR;
    }

    private getDataFromArray(el: any, mem: string, nextOp: Operation): any {
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
            je = nextOp == Operation.OBJECT_OPERATOR ? {} : [];
            el[index] = je;
        }
        return je;
    }

    private getDataFromObject(el: any, mem: string, nextOp: Operation): any {
        if (Array.isArray(el) || typeof el !== 'object')
            throw new KIRuntimeException(
                StringFormatter.format('Expected an object but found $', el),
            );

        let je = el[mem];

        if (isNullValue(je)) {
            je = nextOp == Operation.OBJECT_OPERATOR ? {} : [];
            el[mem] = je;
        }
        return je;
    }

    private putDataInArray(
        el: any,
        mem: string,
        value: any,
        overwrite: boolean,
        deleteOnNull: boolean,
    ): void {
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

        if (overwrite || isNullValue(el[index])) {
            if (deleteOnNull && isNullValue(value)) el.splice(index, 1);
            else el[index] = value;
        }
    }

    private putDataInObject(
        el: any,
        mem: string,
        value: any,
        overwrite: boolean,
        deleteOnNull: boolean,
    ): void {
        if (Array.isArray(el) || typeof el !== 'object')
            throw new KIRuntimeException(
                StringFormatter.format('Expected an object but found $', el),
            );

        if (overwrite || isNullValue(el[mem])) {
            if (deleteOnNull && isNullValue(value)) delete el[mem];
            else el[mem] = value;
        }
    }

    getPrefix(): string {
        return this.prefix;
    }
}
