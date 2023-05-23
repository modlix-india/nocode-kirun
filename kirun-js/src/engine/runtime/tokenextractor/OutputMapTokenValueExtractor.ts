import { StringUtil } from '../../util/string/StringUtil';
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

        const bracket = parts[ind].indexOf('[');

        if (bracket === -1) {
            let element: any = eachEvent.get(parts[ind++]);
            return this.retrieveElementFrom(token, parts, ind, element);
        }

        const evParamName = parts[ind].substring(0, bracket);
        let element: any = eachEvent.get(evParamName);
        return this.retrieveElementFrom(token, parts, ind, { [evParamName]: element });
    }

    public getPrefix(): string {
        return OutputMapTokenValueExtractor.PREFIX;
    }
}
