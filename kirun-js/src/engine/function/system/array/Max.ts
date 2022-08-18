import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';
import { PrimitiveUtil } from '../../../util/primitive/PrimitiveUtil';

export class Max extends AbstractArrayFunction {
    public constructor() {
        super('Max', [Max.PARAMETER_ARRAY_SOURCE_PRIMITIVE], Max.EVENT_RESULT_ANY);
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            .getArguments()
            .get(Max.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName());

        if (source.length == 0) throw new KIRuntimeException('Search source array cannot be empty');

        let max: any = source[0];
        for (let i: number = 1; i < source.length; i++) {
            let y: any = source[i];
            if (PrimitiveUtil.compareFunction(max, y) >= 0) continue;
            max = y;
        }

        return new FunctionOutput([
            EventResult.outputOf(new Map([[Max.EVENT_RESULT_ANY.getName(), max]])),
        ]);
    }
}
