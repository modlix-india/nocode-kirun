import { SchemaType } from '../../../json/schema/type/SchemaType';
import { Namespaces } from '../../../namespaces/Namespaces';
import { Repository } from '../../../Repository';
import { AbstractFunction } from '../../AbstractFunction';
import { Function } from '../../Function';
import { ObjectDeleteKey } from './ObjectDeleteKey';
import { ObjectEntries } from './ObjectEntries';
import { ObjectKeys } from './ObjectKeys';
import { ObjectPutValue } from './ObjectPutValue';
import { ObjectValues } from './ObjectValues';

const functionObjectsIndex: { [key: string]: AbstractFunction } = {
    ObjectValues: new ObjectValues(),
    ObjectKeys: new ObjectKeys(),
    ObjectEntries: new ObjectEntries(),
    ObjectDeleteKey: new ObjectDeleteKey(),
    ObjectPutValue: new ObjectPutValue(),
};

const filterableNames = Object.values(functionObjectsIndex).map((e) =>
    e.getSignature().getFullName(),
);

export class ObjectFunctionRepository implements Repository<Function> {
    public async find(namespace: string, name: string): Promise<Function | undefined> {
        if (namespace != Namespaces.SYSTEM_OBJECT) return Promise.resolve(undefined);

        return Promise.resolve(functionObjectsIndex[name]);
    }

    public async filter(name: string): Promise<string[]> {
        return Promise.resolve(
            filterableNames.filter((e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1),
        );
    }
}
