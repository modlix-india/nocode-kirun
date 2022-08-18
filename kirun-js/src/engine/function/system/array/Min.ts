import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';
import { PrimitiveUtil } from '../../../util/primitive/PrimitiveUtil';
import { isNullValue } from '../../../util/NullCheck';

export class Min extends AbstractArrayFunction {
    public constructor() {
        super('Min', [Min.PARAMETER_ARRAY_SOURCE_PRIMITIVE], Min.EVENT_RESULT_ANY);
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            .getArguments()
            .get(Min.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName());

        if (source.length == 0) throw new KIRuntimeException('Search source array cannot be empty');

        let min: any = undefined;
        for (let i: number = 0; i < source.length; i++) {
            if (isNullValue(source[i])) continue;
            if (min === undefined || PrimitiveUtil.compareFunction(source[i], min) < 0)
                min = source[i];
        }

        return new FunctionOutput([
            EventResult.outputOf(new Map([[Min.EVENT_RESULT_ANY.getName(), min]])),
        ]);
    }
}
