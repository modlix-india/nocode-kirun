import { Function } from '../function/Function';
import { ArrayFunctionRepository } from '../function/system/array/ArrayFunctionRepository';
import { Join } from '../function/system/array/Join';
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
import { DateFunctionRepository } from '../function/system/date/DateFunctionRepository';
import { Wait } from '../function/system/Wait';
import { Make } from '../function/system/Make';
import { HybridRepository } from '../HybridRepository';
import { Namespaces } from '../namespaces/Namespaces';
import mapEntry from '../util/mapEntry';
import { Repository } from '../Repository';
import { ValidateSchema } from '../function/system/ValidateSchema';
import { JSONParse } from '../function/system/json/JSONParse';
import { JSONStringify } from '../function/system/json/JSONStringify';

class SystemFunctionRepository implements Repository<Function> {
    private readonly map: Map<string, Map<string, Function>>;
    private readonly filterableNames: string[];

    public constructor() {
        this.map = new Map([
            [
                Namespaces.SYSTEM_JSON,
                new Map([mapEntry(new JSONParse()), mapEntry(new JSONStringify())]),
            ],
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
                new Map([
                    mapEntry(new If()),
                    mapEntry(new GenerateEvent()),
                    mapEntry(new Print()),
                    mapEntry(new Wait()),
                    mapEntry(new Join()),
                    mapEntry(new ValidateSchema()),
                    mapEntry(new Make()),
                ]),
            ],
        ]);

        this.filterableNames = Array.from(this.map.values())
            .flatMap((e) => Array.from(e.values()))
            .map((e) => e.getSignature().getFullName());
    }

    async find(namespace: string, name: string): Promise<Function | undefined> {
        return this.map.get(namespace)?.get(name);
    }

    async filter(name: string): Promise<string[]> {
        return Array.from(this.filterableNames).filter(
            (e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1,
        );
    }
}

export class KIRunFunctionRepository extends HybridRepository<Function> {
    public constructor() {
        super(
            new SystemFunctionRepository(),
            new MathFunctionRepository(),
            new StringFunctionRepository(),
            new ArrayFunctionRepository(),
            new ObjectFunctionRepository(),
            new DateFunctionRepository(),
        );
    }
}
