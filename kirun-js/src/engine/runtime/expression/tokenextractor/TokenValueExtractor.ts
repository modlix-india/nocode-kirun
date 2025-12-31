import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { isNullValue } from '../../../util/NullCheck';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { StringUtil } from '../../../util/string/StringUtil';
import { ExpressionEvaluationException } from '../exception/ExpressionEvaluationException';

// Simple performance timer for token value extraction
class TVEPerfTimer {
    private static timings: Map<string, { count: number; total: number }> = new Map();
    private static enabled = false;
    
    static enable() { this.enabled = true; }
    static disable() { this.enabled = false; }
    static isEnabled() { return this.enabled; }
    
    static start(label: string): number {
        return this.enabled ? performance.now() : 0;
    }
    
    static end(label: string, startTime: number) {
        if (!this.enabled) return;
        const elapsed = performance.now() - startTime;
        const existing = this.timings.get(label) || { count: 0, total: 0 };
        existing.count++;
        existing.total += elapsed;
        this.timings.set(label, existing);
    }
    
    static report() {
        if (!this.enabled) return;
        console.log('\n=== TokenValueExtractor Performance ===');
        const sorted = Array.from(this.timings.entries())
            .sort((a, b) => b[1].total - a[1].total);
        for (const [label, { count, total }] of sorted) {
            console.log(`${label}: ${total.toFixed(2)}ms (${count} calls, avg ${(total/count).toFixed(3)}ms)`);
        }
        console.log('========================================\n');
    }
    
    static reset() { this.timings.clear(); }
}

export { TVEPerfTimer };

export abstract class TokenValueExtractor {
    public static readonly REGEX_SQUARE_BRACKETS: RegExp = /[\[\]]/;
    public static readonly REGEX_DOT: RegExp = /(?<!\.)\.(?!\.)/;
    
    // Cache for parsed paths to avoid repeated regex splits
    private static pathCache: Map<string, string[]> = new Map();
    
    // Cache for parsed bracket segments to avoid repeated regex splits
    private static bracketCache: Map<string, string[]> = new Map();
    
    protected static splitPath(token: string): string[] {
        let parts = TokenValueExtractor.pathCache.get(token);
        if (!parts) {
            parts = token.split(TokenValueExtractor.REGEX_DOT);
            TokenValueExtractor.pathCache.set(token, parts);
        }
        return parts;
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
        const t0 = TVEPerfTimer.start('getValue');
        let prefix: string = this.getPrefix();

        if (!token.startsWith(prefix))
            throw new KIRuntimeException(
                StringFormatter.format("Token $ doesn't start with $", token, prefix),
            );

        if (token.endsWith('.__index')) {
            const parentPart = token.substring(0, token.length - '.__index'.length);
            const parentValue = this.getValueInternal(parentPart);

            if (!isNullValue(parentValue?.['__index'])) {
                TVEPerfTimer.end('getValue', t0);
                return parentValue['__index'];
            }
            if (parentPart.endsWith(']')) {
                const indexString = parentPart.substring(
                    parentPart.lastIndexOf('[') + 1,
                    parentPart.length - 1,
                );
                const indexInt = parseInt(indexString);
                TVEPerfTimer.end('getValue', t0);
                if (isNaN(indexInt)) return indexString;
                return indexInt;
            } else {
                TVEPerfTimer.end('getValue', t0);
                return parentPart.substring(parentPart.lastIndexOf('.') + 1);
            }
        }

        const result = this.getValueInternal(token);
        TVEPerfTimer.end('getValue', t0);
        return result;
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
        
        // Fast path: simple property access on object (most common case)
        if (typeof element === 'object' && !Array.isArray(element)) {
            if (segment in element) {
                return element[segment];
            }
            // Check for 'length' on object
            if (segment === 'length') {
                return Object.keys(element).length;
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

        if (cPart === 'length') return this.getLength(token, cElement);

        if (typeof cElement == 'string' || Array.isArray(cElement))
            return this.handleArrayAccess(token, cPart, cElement);

        return this.handleObjectAccess(token, parts, partNumber, cPart, cElement);
    }

    private getLength(token: string, cElement: any): any {
        const type = typeof cElement;

        if (type === 'string' || Array.isArray(cElement)) return cElement.length;
        if (type === 'object') {
            if ('length' in cElement) return cElement['length'];
            else return Object.keys(cElement).length;
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
        if (cPart.startsWith('"')) {
            if (!cPart.endsWith('"') || cPart.length == 1 || cPart.length == 2) {
                throw new ExpressionEvaluationException(
                    token,
                    StringFormatter.format('$ is missing a double quote or empty key found', token),
                );
            }

            cPart = cPart.substring(1, parts.length - 2);
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
