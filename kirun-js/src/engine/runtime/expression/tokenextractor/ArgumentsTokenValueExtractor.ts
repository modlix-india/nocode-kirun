import { TokenValueExtractor } from './TokenValueExtractor';

export class ArgumentsTokenValueExtractor extends TokenValueExtractor {
    public static readonly PREFIX: string = 'Arguments.';

    private args: Map<string, any>;

    public constructor(args: Map<string, any>) {
        super();
        this.args = args;
    }

    protected getValueInternal(token: string): any {
        let parts: string[] = token.split('\\.');

        return this.retrieveElementFrom(token, parts, 2, this.args.get(parts[1]));
    }

    public getPrefix(): string {
        return ArgumentsTokenValueExtractor.PREFIX;
    }
}
