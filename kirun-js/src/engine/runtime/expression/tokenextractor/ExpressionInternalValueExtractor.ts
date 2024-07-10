import { TokenValueExtractor } from './TokenValueExtractor';

export class ExpressionInternalValueExtractor extends TokenValueExtractor {
    public static readonly PREFIX: string = '_internal.';

    private values: Map<string, any> = new Map();

    public addValue(key: string, value: any) {
        this.values.set(key, value);
    }

    public getValueInternal(token: string): any {
        let parts: string[] = token.split(TokenValueExtractor.REGEX_DOT);

        let key: string = parts[1];
        let bIndex: number = key.indexOf('[');
        let fromIndex = 2;
        if (bIndex != -1) {
            key = parts[1].substring(0, bIndex);
            parts[1] = parts[1].substring(bIndex);
            fromIndex = 1;
        }
        return this.retrieveElementFrom(token, parts, fromIndex, this.values.get(key));
    }

    public getPrefix(): string {
        return ExpressionInternalValueExtractor.PREFIX;
    }

    public getStore(): any {
        return undefined;
    }
}
