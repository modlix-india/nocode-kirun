import { StringFormatter } from '../../../util/string/StringFormatter';
import { StringUtil } from '../../../util/string/StringUtil';
import { ExpressionEvaluationException } from '../exception/ExpressionEvaluationException';
import { TokenValueExtractor } from './TokenValueExtractor';

const KEYWORDS: Map<string, any> = new Map([
    ['true', true],
    ['false', false],
    ['null', undefined],
    ['undefined', undefined],
]);

export class LiteralTokenValueExtractor extends TokenValueExtractor {
    public static readonly INSTANCE: LiteralTokenValueExtractor = new LiteralTokenValueExtractor();

    public static readonly FAILED: unique symbol = Symbol('LITERAL_PARSE_FAILED');

    // Cache for number parsing: stores the parsed number, or FAILED symbol for non-numeric tokens
    private static readonly numberCache: Map<string, number | symbol> = new Map();

    protected getValueInternal(token: string): any {
        if (StringUtil.isNullOrBlank(token)) return undefined;

        token = token.trim();

        if (KEYWORDS.has(token)) return KEYWORDS.get(token);

        if (token.startsWith('"')) {
            return this.processString(token);
        }

        return this.processNumbers(token);
    }

    /**
     * Non-throwing version of getValueInternal. Returns FAILED symbol instead of throwing.
     * This is the hot-path method — avoids ExpressionEvaluationException creation entirely.
     */
    private tryGetValueInternal(token: string): any {
        if (StringUtil.isNullOrBlank(token)) return undefined;

        token = token.trim();

        if (KEYWORDS.has(token)) return KEYWORDS.get(token);

        if (token.startsWith('"')) {
            if (!token.endsWith('"')) return LiteralTokenValueExtractor.FAILED;
            return token.substring(1, token.length - 1);
        }

        return this.tryProcessNumbers(token);
    }

    private processNumbers(token: string): any {
        const result = this.tryProcessNumbers(token);
        if (result === LiteralTokenValueExtractor.FAILED) {
            throw new ExpressionEvaluationException(
                token,
                'Unable to parse the literal or expression ' + token,
            );
        }
        return result;
    }

    /**
     * Non-throwing number parser. Returns FAILED symbol for non-numeric tokens.
     * Results are cached so each unique token is parsed at most once.
     */
    private tryProcessNumbers(token: string): number | symbol {
        const cached = LiteralTokenValueExtractor.numberCache.get(token);
        if (cached !== undefined) return cached;

        const v = Number(token);
        if (Number.isNaN(v) || token.trim() === '') {
            LiteralTokenValueExtractor.numberCache.set(token, LiteralTokenValueExtractor.FAILED);
            return LiteralTokenValueExtractor.FAILED;
        }

        LiteralTokenValueExtractor.numberCache.set(token, v);
        return v;
    }

    private processString(token: string): any {
        if (!token.endsWith('"'))
            throw new ExpressionEvaluationException(
                token,
                StringFormatter.format('String literal $ is not closed properly', token),
            );

        return token.substring(1, token.length - 1);
    }

    public getPrefix(): string {
        return '';
    }

    public getStore(): any {
        return undefined;
    }

    /**
     * Try to get a literal value without throwing or creating any exception objects.
     * Returns FAILED symbol if not a valid literal.
     */
    public tryGetValue(token: string): any {
        const prefix = this.getPrefix();
        if (prefix && !token.startsWith(prefix)) return LiteralTokenValueExtractor.FAILED;
        return this.tryGetValueInternal(token);
    }

    /**
     * Check if a result from tryGetValue indicates failure.
     */
    public static isFailed(value: any): boolean {
        return value === LiteralTokenValueExtractor.FAILED;
    }

    public getValueFromExtractors(token: string, maps: Map<string, TokenValueExtractor>): any {
        if (maps.has(token + '.')) return maps.get(token + '.')?.getStore();
        return this.getValue(token);
    }
}
