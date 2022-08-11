import { InsertAtGivenPosition } from '../../../../../src/engine/function/system/string/InsertATGivenPosition';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';
import { MapEntry, MapUtil } from '../../../../../src/engine/util/MapUtil';

const reve: InsertAtGivenPosition = new InsertAtGivenPosition();

test('InsertATGivenPositions test1', () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        new Map<string, string | number>([
            [InsertAtGivenPosition.PARAMETER_STRING_NAME, ' THIScompatY IS A NOcoDE plATFNORM'],
            [InsertAtGivenPosition.PARAMETER_AT_POSITION_NAME, 6],
            [InsertAtGivenPosition.PARAMETER_INSERT_STRING_NAME, 'surendhar'],
        ]),
    );

    let padded: string = ' THIScsurendharompatY IS A NOcoDE plATFNORM';

    expect(reve.execute(fep).allResults()[0].getResult().get('result')).toBe(padded);
});

test('InsertATGivenPositions test2', () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        new Map<string, string | number>([
            [InsertAtGivenPosition.PARAMETER_STRING_NAME, ' THIScompatY IS A NOcoDE plATFNORM'],
            [InsertAtGivenPosition.PARAMETER_AT_POSITION_NAME, 6],
            [InsertAtGivenPosition.PARAMETER_INSERT_STRING_NAME, 'surendhar'],
        ]),
    );

    let padded: string = ' THIScsurendharompatY IS A NOcoDE plATFNORM';

    expect(reve.execute(fep).allResults()[0].getResult().get('result')).toBe(padded);
});

test('InsertATGivenPositions test3', () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters();

    fep.setArguments(
        new Map<string, string | number>([
            [InsertAtGivenPosition.PARAMETER_STRING_NAME, ' THIScompatY IS A NOcoDE plATFNORM'],
            [InsertAtGivenPosition.PARAMETER_AT_POSITION_NAME, 29],
            [InsertAtGivenPosition.PARAMETER_INSERT_STRING_NAME, 'surendhar'],
        ]),
    );

    let padded: string = ' THIScompatY IS A NOcoDE plATsurendharFNORM';

    expect(reve.execute(fep).allResults()[0].getResult().get('result')).toBe(padded);
});
