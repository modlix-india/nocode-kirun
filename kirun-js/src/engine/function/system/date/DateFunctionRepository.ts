import { Repository } from '../../../Repository';
import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Namespaces } from '../../../namespaces/Namespaces';
import { MapUtil } from '../../../util/MapUtil';
import { Function } from '../../Function';
import { AbstractTimeFunction } from './AbstractTimeFunction';

const iso8601Pattern =
    /^(\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d+)?(Z|([+-]\d{2}:\d{2}))?$/;

export class DateFunctionRepository implements Repository<Function> {
    private static readonly repoMap: Map<String, Function> = MapUtil.ofArrayEntries(
        AbstractTimeFunction.ofEntryDateAndStringWithOutputName('getDate', 'date', (inputDate) => {
            return new Date(inputDate).getDate();
        }),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName('getDay', 'day', (inputDate) => {
            return new Date(inputDate).getUTCDay();
        }),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getFullYear',
            'year',
            (inputDate) => {
                return new Date(inputDate).getUTCFullYear();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getMonth',
            'month',
            (inputDate) => {
                return new Date(inputDate).getUTCMonth() + 1;
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getHours',
            'hours',
            (inputDate) => {
                return new Date(inputDate).getUTCHours();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getMinutes',
            'minutes',
            (inputDate) => {
                return new Date(inputDate).getUTCMinutes();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getSeconds',
            'seconds',
            (inputDate) => {
                return new Date(inputDate).getUTCSeconds();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getMilliSeconds',
            'milliSeconds',
            (inputDate) => {
                return new Date(inputDate).getUTCMilliseconds();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName('getTime', 'time', (inputDate) => {
            return new Date(inputDate).getTime();
        }),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getTimeZoneOffset',
            'timeZoneOffset',
            (inputDate) => {
                const match = inputDate.match(iso8601Pattern);
                if (match) {
                    const timezoneOffsetString = match[8];
                    if (
                        timezoneOffsetString === 'Z' ||
                        timezoneOffsetString === '+00:00' ||
                        timezoneOffsetString === '-00:00'
                    ) {
                        return 0;
                    } else if (timezoneOffsetString) {
                        const parts = timezoneOffsetString.split(':');
                        const hours = parseInt(parts[0].substring(1), 10);
                        const minutes = parseInt(parts[1], 10);
                        return parts[0][0] === '+'
                            ? -1 * (hours * 60 + minutes)
                            : 1 * (hours * 60 + minutes);
                    }
                }
                throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setDate',
            'dateValue',
            'date',
            (inputDate, value) => {
                let newDate = new Date(inputDate).setUTCDate(value);
                return new Date(newDate).getUTCDate();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputDate(
            'setTime',
            'timeValue',
            'time',
            (inputDate, value) => {
                let newDate = new Date(inputDate).setTime(value);
                return new Date(newDate).toString();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setFullYear',
            'yearValue',
            'year',
            (inputDate, value) => {
                let newDate = new Date(inputDate).setUTCFullYear(value);
                return new Date(newDate).getUTCFullYear();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setMonth',
            'monthValue',
            'month',
            (inputDate, value) => {
                let newDate = new Date(inputDate).setUTCMonth(value);
                return new Date(newDate).getUTCMonth();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setHours',
            'hoursValue',
            'hours',
            (inputDate, value) => {
                let newDate = new Date(inputDate).setUTCHours(value);
                return new Date(newDate).getUTCHours();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setMinutes',
            'minutesValue',
            'minutes',
            (inputDate, value) => {
                let newDate = new Date(inputDate).setUTCMinutes(value);
                return new Date(newDate).getUTCMinutes();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setSeconds',
            'secondsValue',
            'seconds',
            (inputDate, value) => {
                let newDate = new Date(inputDate).setUTCSeconds(value);
                return new Date(newDate).getUTCSeconds();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setMilliSeconds',
            'milliSecondsValue',
            'milliSeconds',
            (inputDate, value) => {
                let newDate = new Date(inputDate).setUTCMilliseconds(value);
                return new Date(newDate).getUTCMilliseconds();
            },
        ),
    );

    private static readonly filterableNames = Array.from(
        DateFunctionRepository.repoMap.values(),
    ).map((e) => e.getSignature().getFullName());

    public async find(namespace: string, name: string): Promise<Function | undefined> {
        if (namespace != Namespaces.DATE) {
            return Promise.resolve(undefined);
        }
        return Promise.resolve(DateFunctionRepository.repoMap.get(name));
    }

    public async filter(name: string): Promise<string[]> {
        return Promise.resolve(
            DateFunctionRepository.filterableNames.filter(
                (e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1,
            ),
        );
    }
}
