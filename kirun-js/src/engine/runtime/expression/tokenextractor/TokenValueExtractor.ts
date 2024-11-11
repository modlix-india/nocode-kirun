import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { isNullValue } from '../../../util/NullCheck';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { StringUtil } from '../../../util/string/StringUtil';
import { ExpressionEvaluationException } from '../exception/ExpressionEvaluationException';

export abstract class TokenValueExtractor {
    public static readonly REGEX_SQUARE_BRACKETS: RegExp = /[\[\]]/;
    public static readonly REGEX_DOT: RegExp = /\./;

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
                const indexInt = parseInt(indexString);
                if (isNaN(indexInt)) return indexString;
                return indexInt;
            } else return parentPart.substring(parentPart.lastIndexOf('.') + 1);
        }

        return this.getValueInternal(token);
    }

    protected retrieveElementFrom(
        token: string,
        parts: string[],
        partNumber: number,
        jsonElement: any,
    ): any {
        if (isNullValue(jsonElement)) return undefined;

        if (parts.length == partNumber) return jsonElement;

        let bElement: any = parts[partNumber]
            .split(TokenValueExtractor.REGEX_SQUARE_BRACKETS)
            .map((e) => e.trim())
            .filter((e) => !StringUtil.isNullOrBlank(e))
            .reduce(
                (a, c) =>
                    this.resolveForEachPartOfTokenWithBrackets(token, parts, partNumber, c, a),
                jsonElement,
            );

        return this.retrieveElementFrom(token, parts, partNumber + 1, bElement);
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

        if (Array.isArray(cElement)) return this.handleArrayAccess(token, cPart, cElement);

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

    private handleArrayAccess(token: string, cPart: string, cArray: any[]): any {
        const index: number = parseInt(cPart);

        if (isNaN(index)) {
            throw new ExpressionEvaluationException(
                token,
                StringFormatter.format('$ is not a number', cPart),
            );
        }

        if (index < 0 || index >= cArray.length) {
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
        if (typeof jsonElement != 'object' || Array.isArray(jsonElement))
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
