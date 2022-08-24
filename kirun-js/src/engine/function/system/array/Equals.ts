import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractArrayFunction } from './AbstractArrayFunction';
import { Compare } from './Compare';

export class Equals extends AbstractArrayFunction {
    public constructor() {
        super(
            'Equals',
            [
                Equals.PARAMETER_ARRAY_SOURCE,
                Equals.PARAMETER_INT_SOURCE_FROM,
                Equals.PARAMETER_ARRAY_FIND,
                Equals.PARAMETER_INT_FIND_FROM,
                Equals.PARAMETER_INT_LENGTH,
            ],
            Equals.EVENT_RESULT_BOOLEAN,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let compare: Compare = new Compare();

        let fo: FunctionOutput = await compare.execute(context);

        let resultMap: Map<string, any> = fo.allResults()[0].getResult();

        let v: number = resultMap.get(Equals.EVENT_RESULT_NAME);

        return new FunctionOutput([
            EventResult.outputOf(MapUtil.of(Equals.EVENT_RESULT_NAME, v == 0)),
        ]);
    }
}
