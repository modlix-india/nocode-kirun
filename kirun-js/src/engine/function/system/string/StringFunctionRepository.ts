import { Namespaces } from '../../../namespaces/Namespaces';
import { Repository } from '../../../Repository';
import mapEntry from '../../../util/mapEntry';
import { MapUtil } from '../../../util/MapUtil';
import { Function } from '../../Function';
import { AbstractStringFunction } from './AbstractStringFunction';
import { Concatenate } from './Concatenate';
import { DeleteForGivenLength } from './DeleteForGivenLength';
import { InsertAtGivenPosition } from './InsertAtGivenPosition';
import { PostPad } from './PostPad';
import { PrePad } from './PrePad';
import { RegionMatches } from './RegionMatches';
import { ReplaceAtGivenPosition } from './ReplaceAtGivenPosition';
import { Reverse } from './Reverse';
import { Split } from './Split';
import { ToString } from './ToString';
import { TrimTo } from './TrimTo';

export class StringFunctionRepository implements Repository<Function> {
    private static readonly repoMap: Map<string, Function> = MapUtil.ofArrayEntries(
        AbstractStringFunction.ofEntryString('Trim', (e) => e.trim()),
        AbstractStringFunction.ofEntryString('LowerCase', (e) => e.toLocaleLowerCase()),
        AbstractStringFunction.ofEntryString('UpperCase', (e) => e.toUpperCase()),
        AbstractStringFunction.ofEntryStringBooleanOutput('IsBlank', (e) => e.trim() === ''),
        AbstractStringFunction.ofEntryStringBooleanOutput('IsEmpty', (e) => e === ''),

        AbstractStringFunction.ofEntryAsStringBooleanOutput(
            'Contains',
            (a, b) => a.indexOf(b) != -1,
        ),
        AbstractStringFunction.ofEntryAsStringBooleanOutput('EndsWith', (a, b) => a.endsWith(b)),
        AbstractStringFunction.ofEntryAsStringBooleanOutput(
            'EqualsIgnoreCase',
            (a, b) => a.toUpperCase() == b.toUpperCase(),
        ),
        AbstractStringFunction.ofEntryAsStringBooleanOutput('Matches', (a, b) =>
            new RegExp(b).test(a),
        ),
        AbstractStringFunction.ofEntryAsStringIntegerOutput('IndexOf', (a, b) => a.indexOf(b)),
        AbstractStringFunction.ofEntryAsStringIntegerOutput('LastIndexOf', (a, b) =>
            a.lastIndexOf(b),
        ),
        AbstractStringFunction.ofEntryAsStringAndIntegerStringOutput('Repeat', (a, b) =>
            a.repeat(b),
        ),

        AbstractStringFunction.ofEntryAsStringStringIntegerIntegerOutput(
            'IndexOfWithStartPoint',
            (a, b, c) => a.indexOf(b, c),
        ),
        AbstractStringFunction.ofEntryAsStringStringIntegerIntegerOutput(
            'LastIndexOfWithStartPoint',
            (a, b, c) => a.lastIndexOf(b, c),
        ),
        AbstractStringFunction.ofEntryAsStringStringStringStringOutput('Replace', (a, b, c) => {
            return a;
        }),
        AbstractStringFunction.ofEntryAsStringStringStringStringOutput('ReplaceFirst', (a, b, c) =>
            a.replace(b, c),
        ),
        AbstractStringFunction.ofEntryAsStringIntegerIntegerStringOutput('SubString', (a, b, c) =>
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
    );

    private static readonly filterableNames = Array.from(
        StringFunctionRepository.repoMap.values(),
    ).map((e) => e.getSignature().getFullName());

    public async find(namespace: string, name: string): Promise<Function | undefined> {
        if (namespace != Namespaces.STRING) {
            return Promise.resolve(undefined);
        }
        return Promise.resolve(StringFunctionRepository.repoMap.get(name));
    }

    public async filter(name: string): Promise<string[]> {
        return Promise.resolve(
            StringFunctionRepository.filterableNames.filter(
                (e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1,
            ),
        );
    }
}
