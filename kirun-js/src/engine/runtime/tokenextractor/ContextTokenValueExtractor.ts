import { isNullValue } from '../../util/NullCheck';
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
        let parts: string[] = TokenValueExtractor.splitPath(token);

        let key: string = parts[1];
        let bIndex: number = key.indexOf('[');
        let fromIndex = 2;
        if (bIndex != -1) {
            key = parts[1].substring(0, bIndex);
            parts = [...parts]; // Copy since we're modifying
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

    public getStore(): any {
        if (isNullValue(this.context)) return this.context;
        return Array.from(this.context.entries()).reduce((acc, [key, value]) => {
            if (isNullValue(value)) return acc;
            acc[key] = value.getElement();
            return acc;
        }, {} as { [key: string]: any });
    }
}
