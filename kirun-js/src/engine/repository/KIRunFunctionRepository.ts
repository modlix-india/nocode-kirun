import { Function } from '../function/Function';
import { ArrayFunctionRepository } from '../function/system/array/ArrayFunctionRepository';
import { Create } from '../function/system/context/Create';
import { Get } from '../function/system/context/Get';
import { SetFunction } from '../function/system/context/SetFunction';
import { GenerateEvent } from '../function/system/GenerateEvent';
import { If } from '../function/system/If';
import { CountLoop } from '../function/system/loop/CountLoop';
import { RangeLoop } from '../function/system/loop/RangeLoop';
import { MathFunctionRepository } from '../function/system/math/MathFunctionRepository';
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
    [Namespaces.SYSTEM_LOOP, new Map([mapEntry(new RangeLoop()), mapEntry(new CountLoop())])],
    [
        Namespaces.SYSTEM,
        new Map([mapEntry(new If()), mapEntry(new GenerateEvent()), mapEntry(new Print())]),
    ],
]);

export class KIRunFunctionRepository extends HybridRepository<Function> {
    public constructor() {
        super(
            {
                find(namespace: string, name: string): Function | undefined {
                    return map.get(namespace)?.get(name);
                },
            },
            new MathFunctionRepository(),
            new StringFunctionRepository(),
            new ArrayFunctionRepository(),
        );
    }
}
