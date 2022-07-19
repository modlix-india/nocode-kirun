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
