import { Schema } from '../json/schema/Schema';
import { Parameter } from '../model/Parameter';
import { Namespaces } from '../namespaces/Namespaces';
import { Repository } from '../Repository';

const map: Map<string, Schema> = new Map([
    ['any', Schema.ofAny('any').setNamespace(Namespaces.SYSTEM)],
    ['boolean', Schema.ofBoolean('boolean').setNamespace(Namespaces.SYSTEM)],
    ['double', Schema.ofDouble('double').setNamespace(Namespaces.SYSTEM)],
    ['float', Schema.ofFloat('float').setNamespace(Namespaces.SYSTEM)],
    ['integer', Schema.ofInteger('integer').setNamespace(Namespaces.SYSTEM)],
    ['long', Schema.ofLong('long').setNamespace(Namespaces.SYSTEM)],
    ['number', Schema.ofNumber('number').setNamespace(Namespaces.SYSTEM)],
    ['string', Schema.ofString('string').setNamespace(Namespaces.SYSTEM)],
    [Parameter.EXPRESSION.getName()!, Parameter.EXPRESSION],
    [Schema.NULL.getName()!, Schema.NULL],
    [Schema.SCHEMA.getName()!, Schema.SCHEMA],
]);

const filterableNames = Array.from(map.values()).map((e) => e.getFullName());

export class KIRunSchemaRepository implements Repository<Schema> {
    public find(namespace: string, name: string): Schema | undefined {
        if (Namespaces.SYSTEM != namespace) return undefined;

        return map.get(name);
    }

    public filter(name: string): string[] {
        return filterableNames.filter((e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1);
    }
}
