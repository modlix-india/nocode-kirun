import { KIRunFunctionRepository, KIRunSchemaRepository, MapUtil } from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { GetNames } from '../../../../../src/engine/function/system/date/GetNames';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const getNames = new GetNames();

describe('GetNames', () => {
    test('should return the names of the units', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(MapUtil.of(GetNames.PARAMETER_UNIT_NAME, 'TIMEZONES'));

        const result = (await getNames.execute(fep))
            .allResults()[0]
            .getResult()
            .get(GetNames.EVENT_NAMES_NAME);

        expect(result).toEqual(Intl.supportedValuesOf('timeZone'));
    });

    test('should return the names of the months', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(MapUtil.of(GetNames.PARAMETER_UNIT_NAME, 'MONTHS'));

        const result = (await getNames.execute(fep))
            .allResults()[0]
            .getResult()
            .get(GetNames.EVENT_NAMES_NAME);

        expect(result).toEqual([
            'January',
            'February',
            'March',
            'April',
            'May',
            'June',
            'July',
            'August',
            'September',
            'October',
            'November',
            'December',
        ]);
    });

    test('should return the names of the weekdays', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(MapUtil.of(GetNames.PARAMETER_UNIT_NAME, 'WEEKDAYS'));

        const result = (await getNames.execute(fep))
            .allResults()[0]
            .getResult()
            .get(GetNames.EVENT_NAMES_NAME);

        expect(result).toEqual([
            'Monday',
            'Tuesday',
            'Wednesday',
            'Thursday',
            'Friday',
            'Saturday',
            'Sunday',
        ]);
    });

    test('should return the names of months in french', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository(),
        ).setArguments(
            MapUtil.of(
                GetNames.PARAMETER_UNIT_NAME,
                'MONTHS',
                GetNames.PARAMETER_LOCALE_NAME,
                'fr',
            ),
        );

        const result = (await getNames.execute(fep))
            .allResults()[0]
            .getResult()
            .get(GetNames.EVENT_NAMES_NAME);

        expect(result).toEqual([
            'janvier',
            'février',
            'mars',
            'avril',
            'mai',
            'juin',
            'juillet',
            'août',
            'septembre',
            'octobre',
            'novembre',
            'décembre',
        ]);
    });
});
