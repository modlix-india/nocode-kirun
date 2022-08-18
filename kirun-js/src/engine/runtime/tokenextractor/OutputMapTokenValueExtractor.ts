import { TokenValueExtractor } from '../expression/tokenextractor/TokenValueExtractor';

export class OutputMapTokenValueExtractor extends TokenValueExtractor {
    public static readonly PREFIX: string = 'Steps.';

    private output: Map<string, Map<string, Map<string, any>>>;

    public constructor(output: Map<string, Map<string, Map<string, any>>>) {
        super();
        this.output = output;
    }

    protected getValueInternal(token: string): any {
        let parts: string[] = token.split(TokenValueExtractor.REGEX_DOT);

        let ind: number = 1;

        let events: Map<string, Map<string, any>> | undefined = this.output.get(parts[ind++]);
        if (!events || ind >= parts.length) return undefined;

        let eachEvent: Map<string, any> | undefined = events.get(parts[ind++]);
        if (!eachEvent || ind >= parts.length) return undefined;

        let element: any = eachEvent.get(parts[ind++]);

        return this.retrieveElementFrom(token, parts, ind, element);
    }

    public getPrefix(): string {
        return OutputMapTokenValueExtractor.PREFIX;
    }
}
