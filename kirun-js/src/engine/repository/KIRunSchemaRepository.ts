import { AdditionalType, Schema } from '../json/schema/Schema';
import { Parameter } from '../model/Parameter';
import { Namespaces } from '../namespaces/Namespaces';
import { Repository } from '../Repository';
import { MapUtil } from '../util/MapUtil';

const map: Map<string, Schema> = new Map([
    ['any', Schema.ofAny('any').setNamespace(Namespaces.SYSTEM)],
    ['boolean', Schema.ofBoolean('boolean').setNamespace(Namespaces.SYSTEM)],
    ['double', Schema.ofDouble('double').setNamespace(Namespaces.SYSTEM)],
    ['float', Schema.ofFloat('float').setNamespace(Namespaces.SYSTEM)],
    ['integer', Schema.ofInteger('integer').setNamespace(Namespaces.SYSTEM)],
    ['long', Schema.ofLong('long').setNamespace(Namespaces.SYSTEM)],
    ['number', Schema.ofNumber('number').setNamespace(Namespaces.SYSTEM)],
    ['string', Schema.ofString('string').setNamespace(Namespaces.SYSTEM)],
    ['Timestamp', Schema.ofString('Timestamp').setNamespace(Namespaces.DATE)],
    [
        'Timeunit',
        Schema.ofString('Timeunit')
            .setNamespace(Namespaces.DATE)
            .setEnums([
                'YEARS',
                'QUARTERS',
                'MONTHS',
                'WEEKS',
                'DAYS',
                'HOURS',
                'MINUTES',
                'SECONDS',
                'MILLISECONDS',
            ]),
    ],
    [
        'Duration',
        Schema.ofObject('Duration')
            .setNamespace(Namespaces.DATE)
            .setProperties(
                MapUtil.ofArrayEntries(
                    ['years', Schema.ofNumber('years')],
                    ['quarters', Schema.ofNumber('quarters')],
                    ['months', Schema.ofNumber('months')],
                    ['weeks', Schema.ofNumber('weeks')],
                    ['days', Schema.ofNumber('days')],
                    ['hours', Schema.ofNumber('hours')],
                    ['minutes', Schema.ofNumber('minutes')],
                    ['seconds', Schema.ofNumber('seconds')],
                    ['milliseconds', Schema.ofNumber('milliseconds')],
                ),
            )
            .setAdditionalItems(AdditionalType.from(false)!),
    ],
    [
        'TimeObject',
        Schema.ofObject('TimeObject')
            .setNamespace(Namespaces.DATE)
            .setProperties(
                MapUtil.ofArrayEntries(
                    ['year', Schema.ofNumber('year')],
                    ['month', Schema.ofNumber('month')],
                    ['day', Schema.ofNumber('day')],
                    ['hour', Schema.ofNumber('hour')],
                    ['minute', Schema.ofNumber('minute')],
                    ['second', Schema.ofNumber('second')],
                    ['millisecond', Schema.ofNumber('millisecond')],
                ),
            )
            .setAdditionalItems(AdditionalType.from(false)!),
    ],
    [Parameter.EXPRESSION.getName()!, Parameter.EXPRESSION],
    [Schema.NULL.getName()!, Schema.NULL],
    [Schema.SCHEMA.getName()!, Schema.SCHEMA],
]);

const filterableNames = Array.from(map.values()).map((e) => e.getFullName());

export class KIRunSchemaRepository implements Repository<Schema> {
    public async find(namespace: string, name: string): Promise<Schema | undefined> {
        if (Namespaces.SYSTEM != namespace && Namespaces.DATE != namespace)
            return Promise.resolve(undefined);

        return Promise.resolve(map.get(name));
    }

    public async filter(name: string): Promise<string[]> {
        return Promise.resolve(
            filterableNames.filter((e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1),
        );
    }
}
