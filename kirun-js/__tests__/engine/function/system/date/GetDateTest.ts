import {
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    MapUtil,
    Namespaces,
} from '../../../../../src';
import { DateFunctionRepository } from '../../../../../src/engine/function/system/date/DateFunctionRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

// const getDate: GetDate = new GetDate();

const dateFunctionRepo = new DateFunctionRepository();

test('testing GetDateFunction', async () => {
    let getDate = await dateFunctionRepo.find(Namespaces.DATE, 'getDate');
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    if (!getDate) {
        throw new Error('Function not available');
    }

    fep.setArguments(new Map([['isodate', '0224-08-29T11:45:38.939Z']]));

    expect((await getDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(29);

    fep.setArguments(new Map([['isodate', '2024-02-30T11:45:38.939Z']]));

    expect(async () =>
        (await getDate?.execute(fep))?.allResults()[0]?.getResult()?.get('date'),
    ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '2023-02-29T11:45:38.939Z']]));

    expect(async () =>
        (await getDate?.execute(fep))?.allResults()[0]?.getResult()?.get('date'),
    ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '2024-02-20T14:48:20.000Z']]));

    expect((await getDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(20);

    fep.setArguments(new Map([['isodate', '122024-02-04T14:10:30.700+00:00']]));

    expect(async () =>
        (await getDate?.execute(fep))?.allResults()[0].getResult().get('date'),
    ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '+202023-04-29T14:10:30.700+00:00']]));

    expect((await getDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(29);

    fep.setArguments(new Map([['isodate', '+202023-04-31T07:35:17.000Z']]));

    expect(async () =>
        (await getDate?.execute(fep))?.allResults()[0].getResult().get('date'),
    ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '2023-02-35T07:35:17.000Z']]));

    expect(async () =>
        (await getDate?.execute(fep))?.allResults()[0].getResult().get('date'),
    ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '2024+02+20T14:48:20.000Z']]));

    expect(async () =>
        (await getDate?.execute(fep))?.allResults()[0].getResult().get('date'),
    ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '2024-02-29T14:48:20.000Z']]));

    expect((await getDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(29);

    fep.setArguments(new Map([['isodate', '2024-02-30T14:48:20.000Z']]));

    expect(async () =>
        (await getDate?.execute(fep))?.allResults()[0].getResult().get('date'),
    ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '-852024-02-30T14:48:20.000Z']]));

    expect(async () =>
        (await getDate?.execute(fep))?.allResults()[0].getResult().get('date'),
    ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '4-02-30T14:48:20.000Z']]));

    expect(async () =>
        (await getDate?.execute(fep))?.allResults()[0].getResult().get('date'),
    ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '0000-13-02T14:48:20.000Z']]));

    expect(async () =>
        (await getDate?.execute(fep))?.allResults()[0].getResult().get('date'),
    ).rejects.toThrowError('Invalid ISO 8601 Date format.');

    fep.setArguments(new Map([['isodate', '0015-02-29T14:48:20.335Z']]));

    expect((await getDate.execute(fep)).allResults()[0].getResult().get('date')).toBe(29);
});
