import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../../src';
import { FromDateString } from '../../../../../src/engine/function/system/date/FromDateString';
import { ToDateString } from '../../../../../src/engine/function/system/date/ToDateString';

const fds: FromDateString = new FromDateString();

const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository(),
);

describe('test case for from date string', () => {
    test('simple started test', () => {});
});
