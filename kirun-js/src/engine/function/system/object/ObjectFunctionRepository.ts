import { Namespaces } from '../../../namespaces/Namespaces';
import { Repository } from '../../../Repository';
import { AbstractFunction } from '../../AbstractFunction';
import { Function } from '../../Function';
import { ObjectConvert } from './ObjectConvert';
import { ObjectDeleteKey } from './ObjectDeleteKey';
import { ObjectEntries } from './ObjectEntries';
import { ObjectKeys } from './ObjectKeys';
import { ObjectPutValue } from './ObjectPutValue';
import { ObjectValues } from './ObjectValues';

export class ObjectFunctionRepository implements Repository<Function> {
    private readonly functionObjectsIndex: { [key: string]: AbstractFunction };
    private readonly filterableNames: string[];

    public constructor() {
        this.functionObjectsIndex = {
            ObjectValues: new ObjectValues(),
            ObjectKeys: new ObjectKeys(),
            ObjectEntries: new ObjectEntries(),
            ObjectDeleteKey: new ObjectDeleteKey(),
            ObjectPutValue: new ObjectPutValue(),
            ObjectConvert: new ObjectConvert(),
        };
        this.filterableNames = Object.values(this.functionObjectsIndex).map((e) =>
            e.getSignature().getFullName(),
        );
    }

    public async find(namespace: string, name: string): Promise<Function | undefined> {
        if (namespace != Namespaces.SYSTEM_OBJECT) return Promise.resolve(undefined);

        return Promise.resolve(this.functionObjectsIndex[name]);
    }

    public async filter(name: string): Promise<string[]> {
        return Promise.resolve(
            this.filterableNames.filter((e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1),
        );
    }
}
