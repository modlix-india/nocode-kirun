import { FromDateString } from '../../../../../src/engine/function/system/date/FromDateString';
import { ToDateString } from '../../../../../src/engine/function/system/date/ToDateString';
import { KIRunFunctionRepository } from '../../../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateString = new FromDateString();

let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository(),
);

test('test1 IsSameFunction', async () => {
    fep.setArguments(
        new Map<string, any>([
            ['date', '2023 4th 33 am'],
            ['dateFormat', 'YYYY Qth mm a'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
        new Date('2023-09-30T19:03:00.000Z'),
    );
});
