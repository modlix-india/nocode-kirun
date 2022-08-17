import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class SubArray extends AbstractArrayFunction {
    public constructor() {
        super(
            'SubArray',
            [
                SubArray.PARAMETER_ARRAY_SOURCE,
                SubArray.PARAMETER_INT_FIND_FROM,
                SubArray.PARAMETER_INT_LENGTH,
            ],
            SubArray.EVENT_RESULT_EMPTY,
        );
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            .getArguments()
            .get(SubArray.PARAMETER_ARRAY_SOURCE.getParameterName());

        let start: number = context
            .getArguments()
            .get(SubArray.PARAMETER_INT_FIND_FROM.getParameterName());

        let len: number = context
            .getArguments()
            .get(SubArray.PARAMETER_INT_LENGTH.getParameterName());

        if (len == -1) len = source.length - start;

        if (len <= 0) return new FunctionOutput([EventResult.outputOf(new Map([]))]);

        if (!(start >= 0 && start < source.length) || start + len > source.length)
            throw new KIRuntimeException(
                'Given find from point is more than the source size array or the Requested length for the subarray was more than the source size',
            );

        while (start != 0) {
            source.shift();
            start--;
        }

        while (source.length > len) source.pop();

        return new FunctionOutput([EventResult.outputOf(new Map([]))]);
    }
}
