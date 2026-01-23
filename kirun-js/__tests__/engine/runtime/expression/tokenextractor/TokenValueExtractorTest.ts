import { TokenValueExtractor } from '../../../../../src/engine/runtime/expression/tokenextractor/TokenValueExtractor';

class TestExtractor extends TokenValueExtractor {
    protected getValueInternal(token: string): any {
        return undefined;
    }

    public getPrefix(): string {
        return 'Testing';
    }

    public retrieveElementFrom(
        token: string,
        parts: string[],
        partNumber: number,
        jsonElement: any,
    ): any {
        return super.retrieveElementFrom(token, parts, partNumber, jsonElement);
    }

    public getStore(): any {
        return undefined;
    }
}

let extractor: TestExtractor;

beforeEach(() => {
    extractor = new TestExtractor();
});

test('TokenValueExtractor Test', () => {
    let darr0: number[] = [2, 4, 6];
    let darr1: number[] = [3, 6, 9];
    let darr2: number[] = [4, 8, 12, 16];

    let darr: number[][] = [darr0, darr1, darr2];

    let arr: number[] = [0, 2, 4, 6];
    let b: any = {
        c: 'K',
        arr: arr,
        darr: darr,
    };

    let a: any = { b: b };

    let obj: any = { a: a, array: arr };

    let token: string = '[2]';
    expect(extractor.retrieveElementFrom(token, token.split(new RegExp('\\.')), 0, arr)).toBe(4);

    token = '[1][1]';
    expect(extractor.retrieveElementFrom(token, token.split(new RegExp('\\.')), 0, darr)).toBe(6);

    token = '[2].length';
    expect(extractor.retrieveElementFrom(token, token.split(new RegExp('\\.')), 0, darr)).toBe(4);

    token = 'a.b.c';
    expect(extractor.retrieveElementFrom(token, token.split(new RegExp('\\.')), 0, obj)).toBe('K');

    token = 'a.b';
    expect(extractor.retrieveElementFrom(token, token.split(new RegExp('\\.')), 0, obj)).toBe(b);

    token = 'a.b.c';
    expect(extractor.retrieveElementFrom(token, token.split(new RegExp('\\.')), 1, a)).toBe('K');

    token = 'a.b.arr[2]';
    expect(extractor.retrieveElementFrom(token, token.split(new RegExp('\\.')), 1, a)).toBe(4);

    token = 'a.b.darr[2][3]';
    expect(extractor.retrieveElementFrom(token, token.split(new RegExp('\\.')), 1, a)).toBe(16);

    token = 'a.b.darr[2].length';
    expect(extractor.retrieveElementFrom(token, token.split(new RegExp('\\.')), 1, a)).toBe(4);
});

test('Bracket notation with dotted keys', () => {
    // Test bracket notation with keys containing dots
    const config: any = {
        'mail.props.port': 587,
        'mail.props.host': 'smtp.example.com',
        'api.key.secret': 'secret123',
        simple: 'value',
    };

    const obj: any = { config };

    // Access splitPath via reflection since it's protected
    const splitPath = (TokenValueExtractor as any).splitPath;

    // Test with double quotes
    let token = 'config["mail.props.port"]';
    expect(extractor.retrieveElementFrom(token, splitPath(token), 0, obj)).toBe(587);

    // Test with single quotes
    token = "config['mail.props.host']";
    expect(extractor.retrieveElementFrom(token, splitPath(token), 0, obj)).toBe('smtp.example.com');

    // Test nested bracket notation with dots
    token = "config['api.key.secret']";
    expect(extractor.retrieveElementFrom(token, splitPath(token), 0, obj)).toBe('secret123');

    // Test mix of dot and bracket notation
    const nested = { 'field.with.dots': 'nestedValue' };
    config.nested = nested;

    token = "config.nested['field.with.dots']";
    expect(extractor.retrieveElementFrom(token, splitPath(token), 0, obj)).toBe('nestedValue');

    // Test that regular dot notation still works
    token = 'config.simple';
    expect(extractor.retrieveElementFrom(token, splitPath(token), 0, obj)).toBe('value');
});

test('splitPath correctly handles bracket notation', () => {
    // Access splitPath via reflection since it's protected
    const splitPath = (TokenValueExtractor as any).splitPath;

    let parts: string[];

    parts = splitPath("Context.obj['mail.props.port']");
    expect(parts.length).toBe(2);
    expect(parts[0]).toBe('Context');
    expect(parts[1]).toBe("obj['mail.props.port']");

    parts = splitPath("Context.obj['mail.props.port'].value");
    expect(parts.length).toBe(3);
    expect(parts[0]).toBe('Context');
    expect(parts[1]).toBe("obj['mail.props.port']");
    expect(parts[2]).toBe('value');

    parts = splitPath("Steps.source.output['field.name']");
    expect(parts.length).toBe(3);
    expect(parts[0]).toBe('Steps');
    expect(parts[1]).toBe('source');
    expect(parts[2]).toBe("output['field.name']");

    // Test that range operator (..) is preserved
    parts = splitPath('array[0..5]');
    expect(parts.length).toBe(1);
    expect(parts[0]).toBe('array[0..5]');

    // Test multiple bracket notations
    parts = splitPath("obj['key.one']['key.two']");
    expect(parts.length).toBe(1);
    expect(parts[0]).toBe("obj['key.one']['key.two']");
});
