import { Function } from '../function/Function';
import { Create } from '../function/system/context/Create';
import { Get } from '../function/system/context/Get';
import { SetFunction } from '../function/system/context/SetFunction';
import { GenerateEvent } from '../function/system/GenerateEvent';
import { If } from '../function/system/If';
import { CountLoop } from '../function/system/loop/CountLoop';
import { RangeLoop } from '../function/system/loop/RangeLoop';
import { MathFunctionRepository } from '../function/system/math/MathFunctionRepository';
import { HybridRepository } from '../HybridRepository';
import { Namespaces } from '../namespaces/Namespaces';

function entry(fun: Function): [string, Function] {
    return [fun.getSignature().getName(), fun];
}

const map: Map<string, Map<string, Function>> = new Map([
    [
        Namespaces.SYSTEM_CTX,
        new Map([entry(new Create()), entry(new Get()), entry(new SetFunction())]),
    ],
    [Namespaces.SYSTEM_LOOP, new Map([entry(new RangeLoop()), entry(new CountLoop())])],
    [Namespaces.SYSTEM, new Map([entry(new If()), entry(new GenerateEvent())])],
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
        );
    }
}
