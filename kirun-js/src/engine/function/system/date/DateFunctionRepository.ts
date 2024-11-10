import { Namespaces } from '../../../namespaces/Namespaces';
import { Event } from '../../../model/Event';
import { Repository } from '../../../Repository';
import { MapUtil } from '../../../util/MapUtil';
import { Function } from '../../Function';
import { AbstractDateFunction } from './AbstractDateFunction';
import { AddSubtractTime } from './AddSubtractTime';
import { getDateTime } from './common';
import { EpochToTimestamp } from './EpochToTimestamp';
import { TimestampToEpoch } from './TimestampToEpoch';
import { ToDateString } from './ToDateString';
import { Schema } from '../../../json/schema/Schema';
import { DateTimeUnit, DurationUnits } from 'luxon';
import { SetTimeZone } from './SetTimeZone';
import { IsBetween } from './IsBetween';
import { LastFirstOf } from './LastFirstOf';
import { TimeAs } from './TimeAs';
import { StartEndOf } from './StartEndOf';
import { GetNames } from './GetNames';
import { IsValidISODate } from './IsValidISODate';
import { FromNow } from './FromNow';
import { FromDateString } from './FromDateString';
import { GetCurrentTimestamp } from './GetCurrentTimestamp';

export class DateFunctionRepository implements Repository<Function> {
    private static readonly repoMap: Map<string, Function> = MapUtil.ofArrayEntries(
        ['EpochSecondsToTimestamp', new EpochToTimestamp('EpochSecondsToTimestamp', true)],
        [
            'EpochMillisecondsToTimestamp',
            new EpochToTimestamp('EpochMillisecondsToTimestamp', false),
        ],
        AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
            'GetDay',
            (isoTimestamp: string) => getDateTime(isoTimestamp).day,
        ),
        AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
            'GetDayOfWeek',
            (isoTimestamp: string) => getDateTime(isoTimestamp).weekday,
        ),
        AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
            'GetMonth',
            (isoTimestamp: string) => getDateTime(isoTimestamp).month,
        ),
        AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
            'GetYear',
            (isoTimestamp: string) => getDateTime(isoTimestamp).year,
        ),
        AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
            'GetHours',
            (isoTimestamp: string) => getDateTime(isoTimestamp).hour,
        ),
        AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
            'GetMinutes',
            (isoTimestamp: string) => getDateTime(isoTimestamp).minute,
        ),
        AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
            'GetSeconds',
            (isoTimestamp: string) => getDateTime(isoTimestamp).second,
        ),
        AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
            'GetMilliseconds',
            (isoTimestamp: string) => getDateTime(isoTimestamp).millisecond,
        ),
        AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
            'GetDaysInMonth',
            (isoTimestamp: string) => getDateTime(isoTimestamp).daysInMonth!,
        ),
        AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
            'GetDaysInYear',
            (isoTimestamp: string) => getDateTime(isoTimestamp).daysInYear!,
        ),
        ['TimestampToEpochSeconds', new TimestampToEpoch('TimestampToEpochSeconds', true)],
        [
            'TimestampToEpochMilliseconds',
            new TimestampToEpoch('TimestampToEpochMilliseconds', false),
        ],
        AbstractDateFunction.ofEntryTimestampAndStringOutput(
            'GetTimeZoneName',
            (isoTimestamp: string) => getDateTime(isoTimestamp).zoneName!,
        ),
        AbstractDateFunction.ofEntryTimestampAndStringOutput(
            'GetTimeZoneOffsetLong',
            (isoTimestamp: string) => getDateTime(isoTimestamp).offsetNameLong!,
        ),
        AbstractDateFunction.ofEntryTimestampAndStringOutput(
            'GetTimeZoneOffsetShort',
            (isoTimestamp: string) => getDateTime(isoTimestamp).offsetNameShort!,
        ),
        AbstractDateFunction.ofEntryTimestampAndIntegerOutput(
            'GetTimeZoneOffset',
            (isoTimestamp: string) => getDateTime(isoTimestamp).offset,
        ),
        ['ToDateString', new ToDateString()],
        ['AddTime', new AddSubtractTime(true)],
        ['SubtractTime', new AddSubtractTime(false)],
        ['GetCurrentTimestamp', new GetCurrentTimestamp()],

        AbstractDateFunction.ofEntryTimestampTimestampAndTOutput<any>(
            'Difference',
            new Event(
                Event.OUTPUT,
                MapUtil.of(
                    AbstractDateFunction.EVENT_RESULT_NAME,
                    Schema.ofRef(`${Namespaces.DATE}.Duration`),
                ),
            ),
            (ts1: string, ts2: string, extraParams: any[]) => {
                const dt1 = getDateTime(ts1);
                const dt2 = getDateTime(ts2);
                let units: DurationUnits | undefined = undefined;
                if (extraParams?.[0]?.length) {
                    units = extraParams[0]
                        ?.filter((e: any) => !!e)
                        .map((e: string) => e.toLowerCase() as DateTimeUnit);
                }
                const duration = dt1.diff(dt2, units);
                return duration.toObject();
            },
            AbstractDateFunction.PARAMETER_VARIABLE_UNIT,
        ),

        AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
            'SetDay',
            (isoTimestamp: string, day: number) => getDateTime(isoTimestamp).set({ day }).toISO()!,
        ),

        AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
            'SetMonth',
            (isoTimestamp: string, month: number) =>
                getDateTime(isoTimestamp).set({ month }).toISO()!,
        ),

        AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
            'SetYear',
            (isoTimestamp: string, year: number) =>
                getDateTime(isoTimestamp).set({ year }).toISO()!,
        ),

        AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
            'SetHours',
            (isoTimestamp: string, hour: number) =>
                getDateTime(isoTimestamp).set({ hour }).toISO()!,
        ),

        AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
            'SetMinutes',
            (isoTimestamp: string, minute: number) =>
                getDateTime(isoTimestamp).set({ minute }).toISO()!,
        ),

        AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
            'SetSeconds',
            (isoTimestamp: string, second: number) =>
                getDateTime(isoTimestamp).set({ second }).toISO()!,
        ),

        AbstractDateFunction.ofEntryTimestampIntegerAndTimestampOutput(
            'SetMilliseconds',
            (isoTimestamp: string, millisecond: number) =>
                getDateTime(isoTimestamp).set({ millisecond }).toISO()!,
        ),

        ['SetTimeZone', new SetTimeZone()],

        AbstractDateFunction.ofEntryTimestampTimestampAndTOutput<boolean>(
            'IsBefore',
            new Event(
                Event.OUTPUT,
                MapUtil.of(
                    AbstractDateFunction.EVENT_RESULT_NAME,
                    Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME),
                ),
            ),
            (t1: string, t2: string) => getDateTime(t1) < getDateTime(t2),
        ),

        AbstractDateFunction.ofEntryTimestampTimestampAndTOutput<boolean>(
            'IsAfter',
            new Event(
                Event.OUTPUT,
                MapUtil.of(
                    AbstractDateFunction.EVENT_RESULT_NAME,
                    Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME),
                ),
            ),
            (t1: string, t2: string) => getDateTime(t1) > getDateTime(t2),
        ),

        AbstractDateFunction.ofEntryTimestampTimestampAndTOutput<boolean>(
            'IsSame',
            new Event(
                Event.OUTPUT,
                MapUtil.of(
                    AbstractDateFunction.EVENT_RESULT_NAME,
                    Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME),
                ),
            ),
            (t1: string, t2: string) => getDateTime(t1) === getDateTime(t2),
        ),

        AbstractDateFunction.ofEntryTimestampTimestampAndTOutput<boolean>(
            'IsSameOrBefore',
            new Event(
                Event.OUTPUT,
                MapUtil.of(
                    AbstractDateFunction.EVENT_RESULT_NAME,
                    Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME),
                ),
            ),
            (t1: string, t2: string) => getDateTime(t1) <= getDateTime(t2),
        ),

        AbstractDateFunction.ofEntryTimestampTimestampAndTOutput<boolean>(
            'IsSameOrAfter',
            new Event(
                Event.OUTPUT,
                MapUtil.of(
                    AbstractDateFunction.EVENT_RESULT_NAME,
                    Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME),
                ),
            ),
            (t1: string, t2: string) => getDateTime(t1) >= getDateTime(t2),
        ),

        AbstractDateFunction.ofEntryTimestampAndBooleanOutput(
            'IsInLeapYear',
            (isoTimestamp: string) => getDateTime(isoTimestamp).isInLeapYear,
        ),

        AbstractDateFunction.ofEntryTimestampAndBooleanOutput(
            'IsInDST',
            (isoTimestamp: string) => getDateTime(isoTimestamp).isInDST,
        ),

        ['IsBetween', new IsBetween()],
        ['LastOf', new LastFirstOf(true)],
        ['FirstOf', new LastFirstOf(false)],
        ['StartOf', new StartEndOf(true)],
        ['EndOf', new StartEndOf(false)],
        ['TimeAsObject', new TimeAs(false)],
        ['TimeAsArray', new TimeAs(true)],
        ['GetNames', new GetNames()],
        ['IsValidISODate', new IsValidISODate()],
        ['FromNow', new FromNow()],
        ['FromDateString', new FromDateString()],
    );

    private static readonly filterableNames = Array.from(
        DateFunctionRepository.repoMap.values(),
    ).map((e) => e.getSignature().getFullName());

    find(namespace: string, name: string): Promise<Function | undefined> {
        if (namespace != Namespaces.DATE) {
            return Promise.resolve(undefined);
        }
        return Promise.resolve(DateFunctionRepository.repoMap.get(name));
    }
    filter(name: string): Promise<string[]> {
        return Promise.resolve(
            DateFunctionRepository.filterableNames.filter(
                (e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1,
            ),
        );
    }
}
