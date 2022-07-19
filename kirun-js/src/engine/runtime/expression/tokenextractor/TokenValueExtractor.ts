import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { StringUtil } from '../../../util/string/StringUtil';
import { ExpressionEvaluationException } from '../exception/ExpressionEvaluationException';

export abstract class TokenValueExtractor {
    private static readonly REGEX_SQUARE_BRACKETS: RegExp = /[\[\]]/;
    public static readonly REGEX_DOT: RegExp = /\./;

    public getValue(token: string): any {
        let prefix: string = this.getPrefix();

        if (!token.startsWith(prefix))
            throw new KIRuntimeException(
                StringFormatter.format("Token $ doesn't start with $", token, prefix),
            );

        return this.getValueInternal(token);
    }

    protected retrieveElementFrom(
        token: string,
        parts: string[],
        partNumber: number,
        jsonElement: any,
    ): any {
        if (!jsonElement) return undefined;

        if (parts.length == partNumber) return jsonElement;

        let bElement: any = parts[partNumber]
            .split(TokenValueExtractor.REGEX_SQUARE_BRACKETS)
            .map((e) => e.trim())
            .filter((e) => !StringUtil.isNullOrBlank(e))
            .reduce(
                (a, c, i) =>
                    this.resolveForEachPartOfTokenWithBrackets(token, parts, partNumber, c, a, i),
                jsonElement,
            );

        return this.retrieveElementFrom(token, parts, partNumber + 1, bElement);
    }

    protected resolveForEachPartOfTokenWithBrackets(
        token: string,
        parts: string[],
        partNumber: number,
        c: string,
        a: any,
        i: any,
    ): any {
        if (!a) return undefined;

        if (i == 0) {
            if (Array.isArray(a)) {
                if (c == 'length') return a.length;
                try {
                    let index: number = parseInt(c);
                    if (index >= a.length) return undefined;

                    return a[index];
                } catch (err) {
                    throw new ExpressionEvaluationException(
                        token,
                        StringFormatter.format("$ couldn't be parsed into integer in $", c, token),
                        err,
                    );
                }
            }

            this.checkIfObject(token, parts, partNumber, a);
            return a[c];
        } else if (c?.startsWith('"')) {
            if (!c.endsWith('"') || c.length == 1 || c.length == 2)
                throw new ExpressionEvaluationException(
                    token,
                    StringFormatter.format('$ is missing a double quote or empty key found', token),
                );

            this.checkIfObject(token, parts, partNumber, a);
            return a[c.substring(1, c.length - 1)];
        }

        try {
            let index: number = parseInt(c);

            if (!Array.isArray(a))
                throw new ExpressionEvaluationException(
                    token,
                    StringFormatter.format(
                        'Expecting an array with index $ while processing the expression',
                        index,
                        token,
                    ),
                );

            if (index >= a.length) return undefined;

            return a[index];
        } catch (err) {
            throw new ExpressionEvaluationException(
                token,
                StringFormatter.format("$ couldn't be parsed into integer in $", c, token),
                err,
            );
        }
    }

    private checkIfObject(
        token: string,
        parts: string[],
        partNumber: number,
        jsonElement: any,
    ): void {
        if (typeof jsonElement != 'object' || Array.isArray(jsonElement))
            throw new ExpressionEvaluationException(
                token,
                StringFormatter.format(
                    'Unable to retrive $ from $ in the path $',
                    parts[partNumber],
                    jsonElement.toString(),
                    token,
                ),
            );
    }

    protected abstract getValueInternal(token: string): any;

    public abstract getPrefix(): string;
}
