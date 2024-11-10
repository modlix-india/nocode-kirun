import { Parameter } from '../../../model/Parameter';
import { AbstractDateFunction } from './AbstractDateFunction';
import { Event } from '../../../model/Event';
import { Schema } from '../../../json/schema/Schema';
import { MapUtil } from '../../../util/MapUtil';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { DateTime } from 'luxon';
import { EventResult } from '../../../model/EventResult';

export class GetNames extends AbstractDateFunction {
    public static readonly EVENT_NAMES_NAME = 'names';
    public static readonly PARAMETER_UNIT_NAME = 'unit';
    public static readonly PARAMETER_LOCALE_NAME = 'locale';

    constructor() {
        super(
            'GetNames',
            new Event(
                GetNames.EVENT_NAMES_NAME,
                MapUtil.of(
                    GetNames.EVENT_NAMES_NAME,
                    Schema.ofArray(
                        GetNames.EVENT_NAMES_NAME,
                        Schema.ofString(GetNames.EVENT_NAMES_NAME),
                    ),
                ),
            ),
            new Parameter(
                GetNames.PARAMETER_UNIT_NAME,
                Schema.ofString(GetNames.PARAMETER_UNIT_NAME).setEnums([
                    'TIMEZONES',
                    'MONTHS',
                    'WEEKDAYS',
                ]),
            ),
            new Parameter(
                GetNames.PARAMETER_LOCALE_NAME,
                Schema.ofString(GetNames.PARAMETER_LOCALE_NAME).setDefaultValue('system'),
            ),
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const unit = context.getArguments()?.get(GetNames.PARAMETER_UNIT_NAME);
        const locale = context.getArguments()?.get(GetNames.PARAMETER_LOCALE_NAME);

        return new FunctionOutput([
            EventResult.outputOf(
                MapUtil.of(GetNames.EVENT_NAMES_NAME, this.getNames(unit, locale)),
            ),
        ]);
    }

    private getNames(unit: string, locale: string): string[] {
        if (unit === 'TIMEZONES') {
            return Intl.supportedValuesOf('timeZone');
        }

        if (unit === 'MONTHS') {
            return [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map((month) =>
                DateTime.now().setLocale(locale).set({ month }).toFormat('MMMM'),
            );
        }

        if (unit === 'WEEKDAYS') {
            return [1, 2, 3, 4, 5, 6, 7].map((day) =>
                DateTime.now().setLocale(locale).set({ month: 7, day }).toFormat('EEEE'),
            );
        }

        return [];
    }
}
