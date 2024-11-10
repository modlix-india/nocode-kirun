import { DateTime } from 'luxon';
import { Schema } from '../../../json/schema/Schema';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractDateFunction } from './AbstractDateFunction';

export class FromNow extends AbstractDateFunction {
    public static readonly PARAMETER_FROM_NAME = 'from';
    public static readonly PARAMETER_FROM = new Parameter(
        FromNow.PARAMETER_FROM_NAME,
        Schema.ofRef(Namespaces.DATE + '.Timestamp').setDefaultValue(''),
    );
    public static readonly PARAMETER_LOCALE_NAME = 'locale';
    public static readonly PARAMETER_LOCALE = new Parameter(
        FromNow.PARAMETER_LOCALE_NAME,
        Schema.ofString(FromNow.PARAMETER_LOCALE_NAME).setDefaultValue('system'),
    );

    public static readonly PARAMETER_FORMAT_NAME = 'format';
    public static readonly PARAMETER_FORMAT = new Parameter(
        FromNow.PARAMETER_FORMAT_NAME,
        Schema.ofString(FromNow.PARAMETER_FORMAT_NAME)
            .setEnums(['LONG', 'SHORT', 'NARROW'])
            .setDefaultValue('LONG'),
    );

    public static readonly PARAMETER_ROUND_NAME = 'round';
    public static readonly PARAMETER_ROUND = new Parameter(
        FromNow.PARAMETER_ROUND_NAME,
        Schema.ofBoolean(FromNow.PARAMETER_ROUND_NAME).setDefaultValue(true),
    );

    public constructor() {
        super(
            'FromNow',
            AbstractDateFunction.EVENT_STRING,
            AbstractDateFunction.PARAMETER_TIMESTAMP,
            FromNow.PARAMETER_FORMAT,
            FromNow.PARAMETER_FROM,
            AbstractDateFunction.PARAMETER_VARIABLE_UNIT,
            FromNow.PARAMETER_ROUND,
            FromNow.PARAMETER_LOCALE,
        );
    }

    public internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const from1 = context.getArguments()?.get(FromNow.PARAMETER_FROM_NAME);
        const fromDate = from1 === '' ? DateTime.now() : DateTime.fromISO(from1);
        const given = context.getArguments()?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME);
        const givenDate = DateTime.fromISO(given);

        const units = context.getArguments()?.get(FromNow.PARAMETER_UNIT_NAME);
        const format = context.getArguments()?.get(FromNow.PARAMETER_FORMAT_NAME);
        const round = context.getArguments()?.get(FromNow.PARAMETER_ROUND_NAME);
        const locale = context.getArguments()?.get(FromNow.PARAMETER_LOCALE_NAME);

        const options: any = { base: fromDate, style: format?.toLowerCase(), round, locale };
        if (units?.length > 0) {
            options.unit = units.map((e: string) => e.toLowerCase());
        }

        return Promise.resolve(
            new FunctionOutput([
                EventResult.outputOf(
                    MapUtil.of(
                        AbstractDateFunction.EVENT_RESULT_NAME,
                        givenDate.toRelative(options) ?? 'Unknown',
                    ),
                ),
            ]),
        );
    }
}
