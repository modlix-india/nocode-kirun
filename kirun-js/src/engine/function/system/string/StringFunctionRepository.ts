import { Namespaces } from '../../../namespaces/Namespaces';
import { Repository } from '../../../Repository';
import mapEntry from '../../../util/mapEntry';
import { MapUtil } from '../../../util/MapUtil';
import { Function } from '../../Function';
import { AbstractStringFunction } from './AbstractStringFunction';
import { Concatenate } from './Concatenate';
import { DeleteForGivenLength } from './DeleteForGivenLength';
import { InsertAtGivenPosition } from './InsertAtGivenPosition';
import { Matches } from './Matches';
import { PostPad } from './PostPad';
import { PrePad } from './PrePad';
import { RegionMatches } from './RegionMatches';
import { ReplaceAtGivenPosition } from './ReplaceAtGivenPosition';
import { Reverse } from './Reverse';
import { Split } from './Split';
import { ToString } from './ToString';
import { TrimTo } from './TrimTo';

export class StringFunctionRepository implements Repository<Function> {
    private readonly repoMap: Map<string, Function> = MapUtil.ofArrayEntries(
        AbstractStringFunction.ofEntryStringAndStringOutput('Trim', (e) => e.trim()),
        AbstractStringFunction.ofEntryStringAndStringOutput('TrimStart', (e) => e.trimStart()),
        AbstractStringFunction.ofEntryStringAndStringOutput('TrimEnd', (e) => e.trimEnd()),

        AbstractStringFunction.ofEntryStringAndIntegerOutput('Length', (e) => e.length),

        AbstractStringFunction.ofEntryStringStringAndIntegerOutput('Frequency', (a, b) => {
            let count = 0;
            let index = a.indexOf(b);
            while (index != -1) {
                count++;
                index = a.indexOf(b, index + 1);
            }
            return count;
        }),

        AbstractStringFunction.ofEntryStringAndStringOutput('LowerCase', (e) =>
            e.toLocaleLowerCase(),
        ),
        AbstractStringFunction.ofEntryStringAndStringOutput('UpperCase', (e) => e.toUpperCase()),
        AbstractStringFunction.ofEntryStringAndBooleanOutput('IsBlank', (e) => e.trim() === ''),
        AbstractStringFunction.ofEntryStringAndBooleanOutput('IsEmpty', (e) => e === ''),

        AbstractStringFunction.ofEntryStringStringAndBooleanOutput(
            'Contains',
            (a, b) => a.indexOf(b) != -1,
        ),
        AbstractStringFunction.ofEntryStringStringAndBooleanOutput('EndsWith', (a, b) =>
            a.endsWith(b),
        ),
        AbstractStringFunction.ofEntryStringStringAndBooleanOutput('StartsWith', (a, b) =>
            a.startsWith(b),
        ),
        AbstractStringFunction.ofEntryStringStringAndBooleanOutput(
            'EqualsIgnoreCase',
            (a, b) => a.toUpperCase() == b.toUpperCase(),
        ),
        AbstractStringFunction.ofEntryStringStringAndBooleanOutput('Matches', (a, b) =>
            new RegExp(b).test(a),
        ),
        AbstractStringFunction.ofEntryStringStringAndIntegerOutput('IndexOf', (a, b) =>
            a.indexOf(b),
        ),
        AbstractStringFunction.ofEntryStringStringAndIntegerOutput('LastIndexOf', (a, b) =>
            a.lastIndexOf(b),
        ),
        AbstractStringFunction.ofEntryStringIntegerAndStringOutput('Repeat', (a, b) => a.repeat(b)),

        AbstractStringFunction.ofEntryStringStringIntegerAndIntegerOutput(
            'IndexOfWithStartPoint',
            (a, b, c) => a.indexOf(b, c),
        ),
        AbstractStringFunction.ofEntryStringStringIntegerAndIntegerOutput(
            'LastIndexOfWithStartPoint',
            (a, b, c) => a.lastIndexOf(b, c),
        ),
        AbstractStringFunction.ofEntryStringStringStringAndStringOutput('Replace', (a, b, c) => {
            return a.replaceAll(b, c);
        }),
        AbstractStringFunction.ofEntryStringStringStringAndStringOutput('ReplaceFirst', (a, b, c) =>
            a.replace(b, c),
        ),
        AbstractStringFunction.ofEntryStringIntegerIntegerAndStringOutput('SubString', (a, b, c) =>
            a.substring(b, c),
        ),
        mapEntry(new Concatenate()),
        mapEntry(new DeleteForGivenLength()),
        mapEntry(new InsertAtGivenPosition()),
        mapEntry(new PostPad()),
        mapEntry(new PrePad()),
        mapEntry(new RegionMatches()),
        mapEntry(new ReplaceAtGivenPosition()),
        mapEntry(new Reverse()),
        mapEntry(new Split()),
        mapEntry(new ToString()),
        mapEntry(new TrimTo()),
        mapEntry(new Matches()),
    );

    private readonly filterableNames = Array.from(this.repoMap.values()).map((e) =>
        e.getSignature().getFullName(),
    );

    public async find(namespace: string, name: string): Promise<Function | undefined> {
        if (namespace != Namespaces.STRING) {
            return Promise.resolve(undefined);
        }
        return Promise.resolve(this.repoMap.get(name));
    }

    public async filter(name: string): Promise<string[]> {
        return Promise.resolve(
            this.filterableNames.filter((e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1),
        );
    }
}
