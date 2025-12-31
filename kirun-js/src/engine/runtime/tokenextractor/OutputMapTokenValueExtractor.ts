import { TokenValueExtractor } from '../expression/tokenextractor/TokenValueExtractor';

export class OutputMapTokenValueExtractor extends TokenValueExtractor {
    public static readonly PREFIX: string = 'Steps.';

    private output: Map<string, Map<string, Map<string, any>>>;

    public constructor(output: Map<string, Map<string, Map<string, any>>>) {
        super();
        this.output = output;
    }

    protected getValueInternal(token: string): any {
        let parts: string[] = TokenValueExtractor.splitPath(token);

        let ind: number = 1;

        let events: Map<string, Map<string, any>> | undefined = this.output.get(parts[ind++]);
        if (!events || ind >= parts.length) return undefined;

        let eachEvent: Map<string, any> | undefined = events.get(parts[ind++]);
        if (!eachEvent || ind > parts.length) return undefined;
        if (ind === parts.length) return eachEvent;

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

    public getStore(): any {
        return this.convertMapToObj(this.output);
    }

    private convertMapToObj(map: Map<string, Map<string, Map<string, any>>>) {
        if (map.size === 0) return {};
        return Array.from(map.entries()).reduce((acc, [key, value]) => {
            acc[key] = value instanceof Map ? this.convertMapToObj(value) : value;
            return acc;
        }, {} as { [key: string]: any });
    }
}
