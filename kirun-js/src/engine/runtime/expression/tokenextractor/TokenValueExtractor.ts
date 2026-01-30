import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { isNullValue } from '../../../util/NullCheck';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { StringUtil } from '../../../util/string/StringUtil';
import { ExpressionEvaluationException } from '../exception/ExpressionEvaluationException';

export abstract class TokenValueExtractor {
    public static readonly REGEX_SQUARE_BRACKETS: RegExp = /[\[\]]/;
    public static readonly REGEX_DOT: RegExp = /(?<!\.)\.(?!\.)/;

    // Cache for parsed paths to avoid repeated regex splits
    private static pathCache: Map<string, string[]> = new Map();

    // Cache for parsed bracket segments to avoid repeated regex splits
    private static bracketCache: Map<string, string[]> = new Map();

    // Optional valuesMap for resolving dynamic bracket indices like Parent.__index
    protected valuesMap?: Map<string, TokenValueExtractor>;

    public static splitPath(token: string): string[] {
        let parts = TokenValueExtractor.pathCache.get(token);
        if (!parts) {
            parts = TokenValueExtractor.splitPathInternal(token);
            TokenValueExtractor.pathCache.set(token, parts);
        }
        return parts;
    }

    private static splitPathInternal(token: string): string[] {
        const parts: string[] = [];
        let start = 0;
        let inBracket = false;

        for (let i = 0; i < token.length; i++) {
            const c = token.charAt(i);

            if (c === '[') {
                inBracket = true;
            } else if (c === ']') {
                inBracket = false;
            } else if (c === '.' && !inBracket && !TokenValueExtractor.isDoubleDot(token, i)) {
                // Found a separator dot
                if (i > start) {
                    parts.push(token.substring(start, i));
                }
                start = i + 1;
            }
        }

        // Add the last part
        if (start < token.length) {
            parts.push(token.substring(start));
        }

        return parts;
    }

    private static isDoubleDot(str: string, pos: number): boolean {
        // Check if this dot is part of a ".." range operator
        return (pos > 0 && str.charAt(pos - 1) === '.') ||
               (pos < str.length - 1 && str.charAt(pos + 1) === '.');
    }
    
    // Parse bracket segments with caching
    private static parseBracketSegment(segment: string): string[] {
        let cached = TokenValueExtractor.bracketCache.get(segment);
        if (!cached) {
            cached = segment
                .split(TokenValueExtractor.REGEX_SQUARE_BRACKETS)
                .map((e) => e.trim())
                .filter((e) => !StringUtil.isNullOrBlank(e));
            TokenValueExtractor.bracketCache.set(segment, cached);
        }
        return cached;
    }

    public getValue(token: string): any {
        let prefix: string = this.getPrefix();

        if (!token.startsWith(prefix))
            throw new KIRuntimeException(
                StringFormatter.format("Token $ doesn't start with $", token, prefix),
            );

        if (token.endsWith('.__index')) {
            const parentPart = token.substring(0, token.length - '.__index'.length);
            const parentValue = this.getValueInternal(parentPart);

            if (!isNullValue(parentValue?.['__index'])) {
                return parentValue['__index'];
            }
            if (parentPart.endsWith(']')) {
                const indexString = parentPart.substring(
                    parentPart.lastIndexOf('[') + 1,
                    parentPart.length - 1,
                );
                const indexInt = Number.parseInt(indexString);
                if (isNaN(indexInt)) return indexString;
                return indexInt;
            } else {
                return parentPart.substring(parentPart.lastIndexOf('.') + 1);
            }
        }

        return this.getValueInternal(token);
    }

    public setValuesMap(valuesMap: Map<string, TokenValueExtractor>): void {
        this.valuesMap = valuesMap;
    }

    protected retrieveElementFrom(
        token: string,
        parts: string[],
        startPart: number,
        jsonElement: any,
    ): any {
        // Iterative version - avoids recursive call overhead
        let current = jsonElement;
        
        for (let partNumber = startPart; partNumber < parts.length; partNumber++) {
            if (isNullValue(current)) return undefined;
            
            // Use cached bracket segment parsing
            const segments = TokenValueExtractor.parseBracketSegment(parts[partNumber]);
            
            for (const segment of segments) {
                current = this.resolveSegmentFast(token, parts, partNumber, segment, current);
                if (current === undefined) return undefined;
            }
        }
        
        return current;
    }
    
    // Fast path for common cases - inline to avoid function call overhead
    private resolveSegmentFast(
        token: string,
        parts: string[],
        partNumber: number,
        segment: string,
        element: any,
    ): any {
        if (element === null || element === undefined) return undefined;

        // Skip fast path for quoted segments - they need quote stripping
        if (segment.startsWith('"') || segment.startsWith("'")) {
            return this.resolveForEachPartOfTokenWithBrackets(token, parts, partNumber, segment, element);
        }

        // Fast path: simple property access on object (most common case)
        if (typeof element === 'object' && !Array.isArray(element)) {
            // For 'length' on objects, check if there's a length property
            // If it's a primitive (number, string, boolean), use it
            // If it's an object/array, use Object.keys length to avoid bugs
            if (segment === 'length') {
                if ('length' in element) {
                    const lengthValue = element['length'];
                    // If length property is a primitive, use it; otherwise use Object.keys length
                    if (typeof lengthValue === 'object' && lengthValue !== null) {
                        return Object.keys(element).length;
                    }
                    return lengthValue;
                }
                return Object.keys(element).length;
            }
            if (segment in element) {
                return element[segment];
            }
            return element[segment];
        }
        
        // Fast path: array index access
        if (Array.isArray(element)) {
            // Check for 'length' first
            if (segment === 'length') return element.length;
            
            // Only use fast path for pure integer strings (no range operators like '..')
            // Note: parseInt('2..4', 10) incorrectly returns 2, so we need to validate first
            if (/^-?\d+$/.test(segment)) {
                const idx = Number.parseInt(segment, 10);
                const actualIdx = idx < 0 ? element.length + idx : idx;
                return actualIdx >= 0 && actualIdx < element.length ? element[actualIdx] : undefined;
            }
        }
        
        // Fast path: string access
        if (typeof element === 'string') {
            if (segment === 'length') return element.length;
            // Only use fast path for pure integer strings
            if (/^-?\d+$/.test(segment)) {
                const idx = Number.parseInt(segment, 10);
                const actualIdx = idx < 0 ? element.length + idx : idx;
                return actualIdx >= 0 && actualIdx < element.length ? element[actualIdx] : undefined;
            }
        }
        
        // Fall back to full handling for edge cases (range operator, etc.)
        return this.resolveForEachPartOfTokenWithBrackets(token, parts, partNumber, segment, element);
    }

    protected resolveForEachPartOfTokenWithBrackets(
        token: string,
        parts: string[],
        partNumber: number,
        cPart: string,
        cElement: any,
    ): any {
        if (isNullValue(cElement)) return undefined;

        // Check for 'length' keyword - both unquoted and quoted versions
        // e.g., .length and ["length"] should both return the length
        if (cPart === 'length' || cPart === '"length"' || cPart === "'length'") 
            return this.getLength(token, cElement);

        if (typeof cElement == 'string' || Array.isArray(cElement))
            return this.handleArrayAccess(token, cPart, cElement);

        return this.handleObjectAccess(token, parts, partNumber, cPart, cElement);
    }

    private getLength(token: string, cElement: any): any {
        const type = typeof cElement;

        if (type === 'string' || Array.isArray(cElement)) return cElement.length;
        if (type === 'object') {
            // For objects, check if there's a length property
            // If it's a primitive (number, string, boolean), use it
            // If it's an object/array, use Object.keys length to avoid bugs
            if ('length' in cElement) {
                const lengthValue = cElement['length'];
                // If length property is a primitive, use it; otherwise use Object.keys length
                if (typeof lengthValue === 'object' && lengthValue !== null) {
                    return Object.keys(cElement).length;
                }
                return lengthValue;
            }
            return Object.keys(cElement).length;
        }

        throw new ExpressionEvaluationException(
            token,
            StringFormatter.format("Length can't be found in token $", token),
        );
    }

    private handleArrayAccess(token: string, cPart: string, cArray: any[] | string): any {
        const dotDotIndex = cPart.indexOf('..');
        if (dotDotIndex >= 0) {
            const startIndex = cPart.substring(0, dotDotIndex);
            const endIndex = cPart.substring(dotDotIndex + 2);

            let intStart = startIndex.length == 0 ? 0 : parseInt(startIndex);
            let intEnd = endIndex.length == 0 ? cArray.length : parseInt(endIndex);

            if (isNaN(intStart) || isNaN(intEnd)) return undefined;

            while (intStart < 0) intStart += cArray.length;
            while (intEnd < 0) intEnd += cArray.length;

            const cArrayType = typeof cArray;
            if (intStart >= intEnd) return cArrayType == 'string' ? '' : [];

            return cArrayType == 'string'
                ? (cArray as string).substring(intStart, intEnd)
                : cArray.slice(intStart, intEnd);
        }

        let index: number = parseInt(cPart);

        // If parsing failed and we have a valuesMap, try to resolve cPart as a token
        // This allows Parent.__index or similar dynamic indices to work
        if (isNaN(index) && this.valuesMap) {
            const dotIndex = cPart.indexOf('.');
            if (dotIndex > 0) {
                const prefix = cPart.substring(0, dotIndex + 1);
                const extractor = this.valuesMap.get(prefix);
                if (extractor) {
                    try {
                        const resolvedValue = extractor.getValue(cPart);
                        if (typeof resolvedValue === 'number') {
                            index = resolvedValue;
                        } else if (typeof resolvedValue === 'string') {
                            index = parseInt(resolvedValue);
                        }
                    } catch (resolveErr) {
                        // Resolution failed, will use fallback below
                    }
                }
                // If extractor not found or resolution failed, use 0 as fallback
                // This allows path extraction to work even without a parent context
                if (isNaN(index)) {
                    index = 0;
                }
            }
        }

        if (isNaN(index)) {
            throw new ExpressionEvaluationException(
                token,
                StringFormatter.format('$ is not a number', cPart),
            );
        }

        while (index < 0) index = cArray.length + index;
        if (index >= cArray.length) {
            return undefined;
        }

        return cArray[index];
    }

    private handleObjectAccess(
        token: string,
        parts: string[],
        partNumber: number,
        cPart: string,
        cObject: any,
    ): any {
        // Handle both single and double quoted keys
        if (cPart.startsWith('"') || cPart.startsWith("'")) {
            const quoteChar = cPart[0];
            // Allow empty string key: "" or ''
            if (!cPart.endsWith(quoteChar) || cPart.length == 1) {
                throw new ExpressionEvaluationException(
                    token,
                    StringFormatter.format('$ is missing a closing quote or empty key found', token),
                );
            }

            cPart = cPart.substring(1, cPart.length - 1);
        }

        this.checkIfObject(token, parts, partNumber, cObject);

        return cObject[cPart];
    }

    protected checkIfObject(
        token: string,
        parts: string[],
        partNumber: number,
        jsonElement: any,
    ): void {
        const jsonElementType = typeof jsonElement;
        if (
            (jsonElementType != 'object' && jsonElementType != 'string') ||
            Array.isArray(jsonElement)
        )
            throw new ExpressionEvaluationException(
                token,
                StringFormatter.format(
                    'Unable to retrieve $ from $ in the path $',
                    parts[partNumber],
                    jsonElement.toString(),
                    token,
                ),
            );
    }

    protected abstract getValueInternal(token: string): any;

    public abstract getPrefix(): string;

    public abstract getStore(): any;
}
