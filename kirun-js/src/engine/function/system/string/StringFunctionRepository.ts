import { Namespaces } from '../../../namespaces/Namespaces';
import { Repository } from '../../../Repository';
import { MapUtil } from '../../../util/MapUtil';
import { Function } from '../../Function';
import { AbstractStringFunction } from './AbstractStringFunction';

export class StringFunctionRepository implements Repository<Function> {
    private static readonly repoMap: Map<string, Function> = MapUtil.ofArrayEntries(
        AbstractStringFunction.ofEntryString('Trim', (e) => e.trim()),
        AbstractStringFunction.ofEntryString('LowerCase', (e) => e.toLocaleLowerCase()),
        AbstractStringFunction.ofEntryString('UpperCase', (e) => e.toUpperCase()),
        AbstractStringFunction.ofEntryStringBooleanOutput('Blank', (e) => e.trim() == ''),
        AbstractStringFunction.ofEntryStringBooleanOutput('Empty', (e) => e == ''),

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
    );

    public find(namespace: string, name: string): Function {
        if (namespace != Namespaces.STRING) {
            return null;
        }
        return StringFunctionRepository.repoMap.get(name);
    }
}
