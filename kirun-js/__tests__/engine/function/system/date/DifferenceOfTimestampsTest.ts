import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../../src';
import { DifferenceOfTimestamps } from '../../../../../src/engine/function/system/date/DifferenceOfTimestamps';

const differenceOfTimestamps: DifferenceOfTimestamps = new DifferenceOfTimestamps();

const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository()
);


describe('testing DifferenceOfTimestamps for invalid dates', () => {
    

    test('invalid date one', async () => {

        fep.setArguments(new Map([['isoDateOne', '2029-05-95T06:04:18.073Z'], ['isoDateTwo', '2029-05-05T06:04:18.073Z']]));

        await expect( () => differenceOfTimestamps.execute(fep)).rejects.toThrow();
    })

    test('invalid date one', async () => {

        fep.setArguments(new Map([['isoDateOne', '2029-05-05T06:04:18.073Z']]));

        await expect( () => differenceOfTimestamps.execute(fep)).rejects.toThrow();
    })
})

describe('testing DifferenceOfTimestamps for valid dates', () => {

    test('valid one', async () => {

        fep.setArguments(new Map([['isoDateOne', '2024-09-13T23:52:34.633-05:30'], ['isoDateTwo', '2024-09-13T23:52:34.633Z']]));

        expect((await differenceOfTimestamps.execute(fep)).allResults()[0].getResult().get('result')).toBe(-330);
    })

    test('valid two', async () => {

        fep.setArguments(new Map([['isoDateOne', '2024-09-13T23:52:34.633-05:30'], ['isoDateTwo', '2024-09-12T23:52:34.633Z']]));

        expect((await differenceOfTimestamps.execute(fep)).allResults()[0].getResult().get('result')).toBe(-1770);
    })

    test('valid three', async () => {

        fep.setArguments(new Map([['isoDateOne', '2023-09-12T23:52:34.633Z'], ['isoDateTwo', '2024-09-13T23:52:34.633-05:30']]));

        expect((await differenceOfTimestamps.execute(fep)).allResults()[0].getResult().get('result')).toBe(528810);
    })
})
