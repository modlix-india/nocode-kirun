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
]);

export class KIRunSchemaRepository implements Repository<Schema> {
    public find(namespace: string, name: string): Schema | undefined {
        if (Namespaces.SYSTEM != namespace) return undefined;

        return map.get(name);
    }
}
