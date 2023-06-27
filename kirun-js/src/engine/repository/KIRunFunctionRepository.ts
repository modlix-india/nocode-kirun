import { Function } from '../function/Function';
import { ArrayFunctionRepository } from '../function/system/array/ArrayFunctionRepository';
import { Create } from '../function/system/context/Create';
import { Get } from '../function/system/context/Get';
import { SetFunction } from '../function/system/context/SetFunction';
import { GenerateEvent } from '../function/system/GenerateEvent';
import { If } from '../function/system/If';
import { Break } from '../function/system/loop/Break';
import { CountLoop } from '../function/system/loop/CountLoop';
import { ForEachLoop } from '../function/system/loop/ForEachLoop';
import { RangeLoop } from '../function/system/loop/RangeLoop';
import { MathFunctionRepository } from '../function/system/math/MathFunctionRepository';
import { ObjectFunctionRepository } from '../function/system/object/ObjectFunctionRepository';
import { Print } from '../function/system/Print';
import { StringFunctionRepository } from '../function/system/string/StringFunctionRepository';
import { HybridRepository } from '../HybridRepository';
import { Namespaces } from '../namespaces/Namespaces';
import mapEntry from '../util/mapEntry';

const map: Map<string, Map<string, Function>> = new Map([
    [
        Namespaces.SYSTEM_CTX,
        new Map([mapEntry(new Create()), mapEntry(new Get()), mapEntry(new SetFunction())]),
    ],
    [
        Namespaces.SYSTEM_LOOP,
        new Map([
            mapEntry(new RangeLoop()),
            mapEntry(new CountLoop()),
            mapEntry(new Break()),
            mapEntry(new ForEachLoop()),
        ]),
    ],
    [
        Namespaces.SYSTEM,
        new Map([mapEntry(new If()), mapEntry(new GenerateEvent()), mapEntry(new Print())]),
    ],
]);

const filterableNames = Array.from(map.values())
    .flatMap((e) => Array.from(e.values()))
    .map((e) => e.getSignature().getFullName());

export class KIRunFunctionRepository extends HybridRepository<Function> {
    public constructor() {
        super(
            {
                async find(namespace: string, name: string): Promise<Function | undefined> {
                    return map.get(namespace)?.get(name);
                },

                async filter(name: string): Promise<string[]> {
                    return Array.from(filterableNames).filter(
                        (e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1,
                    );
                },
            },
            new MathFunctionRepository(),
            new StringFunctionRepository(),
            new ArrayFunctionRepository(),
            new ObjectFunctionRepository(),
        );
    }
}
