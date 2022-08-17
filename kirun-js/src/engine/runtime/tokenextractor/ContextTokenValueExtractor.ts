import { ContextElement } from '../ContextElement';
import { TokenValueExtractor } from '../expression/tokenextractor/TokenValueExtractor';

export class ContextTokenValueExtractor extends TokenValueExtractor {
    public static readonly PREFIX: string = 'Context.';

    private context: Map<string, ContextElement>;

    public constructor(context: Map<string, ContextElement>) {
        super();
        this.context = context;
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

        return this.retrieveElementFrom(
            token,
            parts,
            fromIndex,
            this.context.get(key)?.getElement(),
        );
    }

    public getPrefix(): string {
        return ContextTokenValueExtractor.PREFIX;
    }
}
