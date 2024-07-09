import { isNullValue } from '../../util/NullCheck';
import { TokenValueExtractor } from '../expression/tokenextractor/TokenValueExtractor';

export class ArgumentsTokenValueExtractor extends TokenValueExtractor {
    public static readonly PREFIX: string = 'Arguments.';

    private args: Map<string, any>;

    public constructor(args: Map<string, any>) {
        super();
        this.args = args;
    }

    protected getValueInternal(token: string): any {
        let parts: string[] = token.split(TokenValueExtractor.REGEX_DOT);

        let key: string = parts[1];
        let bIndex: number = key.indexOf('[');
        let fromIndex = 2;
        if (bIndex != -1) {
            key = parts[1].substring(0, bIndex);
            parts[1] = parts[1].substring(bIndex);
            fromIndex = 1;
        }
        return this.retrieveElementFrom(token, parts, fromIndex, this.args.get(key));
    }

    public getPrefix(): string {
        return ArgumentsTokenValueExtractor.PREFIX;
    }

    public getStore(): any {
        if (isNullValue(this.args)) return this.args;
        return Array.from(this.args.entries()).reduce((acc, [key, value]) => {
            acc[key] = value;
            return acc;
        }, {} as { [key: string]: any });
    }
}
