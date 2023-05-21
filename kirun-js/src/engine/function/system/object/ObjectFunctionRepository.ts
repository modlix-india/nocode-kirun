import { SchemaType } from '../../../json/schema/type/SchemaType';
import { Namespaces } from '../../../namespaces/Namespaces';
import { Repository } from '../../../Repository';
import { AbstractFunction } from '../../AbstractFunction';
import { Function } from '../../Function';
import { ObjectDeleteKey } from './ObjectDeleteKey';
import { ObjectEntries } from './ObjectEntries';
import { ObjectKeys } from './ObjectKeys';
import { ObjectValues } from './ObjectValues';

const functionObjectsIndex: { [key: string]: AbstractFunction } = {
    ObjectValues: new ObjectValues(),
    ObjectKeys: new ObjectKeys(),
    ObjectEntries: new ObjectEntries(),
    ObjectDeleteKey: new ObjectDeleteKey(),
};

const filterableNames = Object.values(functionObjectsIndex).map((e) =>
    e.getSignature().getFullName(),
);

export class ObjectFunctionRepository implements Repository<Function> {
    find(namespace: string, name: string): Function | undefined {
        if (namespace != Namespaces.SYSTEM_OBJECT) return undefined;

        return functionObjectsIndex[name];
    }

    public filter(name: string): string[] {
        return filterableNames.filter((e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1);
    }
}
