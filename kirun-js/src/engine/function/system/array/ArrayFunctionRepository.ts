import { Namespaces } from '../../../namespaces/Namespaces';
import { Repository } from '../../../Repository';
import mapEntry from '../../../util/mapEntry';
import { MapUtil } from '../../../util/MapUtil';
import { Function } from '../../Function';
import { Concatenate } from './Concatenate';
import { AddFirst } from './AddFirst';
import { ArrayToArrayOfObjects } from './ArrayToArrayOfObjects';
import { BinarySearch } from './BinarySearch';
import { Compare } from './Compare';
import { Copy } from './Copy';
import { Delete } from './Delete';
import { DeleteFirst } from './DeleteFirst';
import { DeleteFrom } from './DeleteFrom';
import { DeleteLast } from './DeleteLast';
import { Disjoint } from './Disjoint';
import { Equals } from './Equals';
import { Fill } from './Fill';
import { Frequency } from './Frequency';
import { IndexOf } from './IndexOf';
import { IndexOfArray } from './IndexOfArray';
import { LastIndexOf } from './LastIndexOf';
import { LastIndexOfArray } from './LastIndexOfArray';
import { Max } from './Max';
import { Min } from './Min';
import { MisMatch } from './MisMatch';
import { Reverse } from './Reverse';
import { Rotate } from './Rotate';
import { Shuffle } from './Shuffle';
import { Sort } from './Sort';
import { SubArray } from './SubArray';
import { Insert } from './Insert';

export class ArrayFunctionRepository implements Repository<Function> {
    private static readonly repoMap: Map<string, Function> = MapUtil.ofArrayEntries(
        mapEntry(new Concatenate()),
        mapEntry(new AddFirst()),
        mapEntry(new BinarySearch()),
        mapEntry(new Compare()),
        mapEntry(new Copy()),
        mapEntry(new Delete()),
        mapEntry(new DeleteFirst()),
        mapEntry(new DeleteFrom()),
        mapEntry(new DeleteLast()),
        mapEntry(new Disjoint()),
        mapEntry(new Equals()),
        mapEntry(new Fill()),
        mapEntry(new Frequency()),
        mapEntry(new IndexOf()),
        mapEntry(new IndexOfArray()),
        mapEntry(new LastIndexOf()),
        mapEntry(new LastIndexOfArray()),
        mapEntry(new Max()),
        mapEntry(new Min()),
        mapEntry(new MisMatch()),
        mapEntry(new Reverse()),
        mapEntry(new Rotate()),
        mapEntry(new Shuffle()),
        mapEntry(new Sort()),
        mapEntry(new SubArray()),
        mapEntry(new ArrayToArrayOfObjects()),
        mapEntry(new Insert()),
    );

    private static readonly filterableNames = Array.from(
        ArrayFunctionRepository.repoMap.values(),
    ).map((e) => e.getSignature().getFullName());

    public find(namespace: string, name: string): Function | undefined {
        if (namespace != Namespaces.SYSTEM_ARRAY) {
            return undefined;
        }

        return ArrayFunctionRepository.repoMap.get(name);
    }

    public filter(name: string): string[] {
        return ArrayFunctionRepository.filterableNames.filter(
            (e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1,
        );
    }
}
