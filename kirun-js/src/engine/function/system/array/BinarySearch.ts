import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { PrimitiveUtil } from '../../../util/primitive/PrimitiveUtil';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class BinarySearch extends AbstractArrayFunction {
    public constructor() {
        super(
            'BinarySearch',
            [
                BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE,
                BinarySearch.PARAMETER_INT_SOURCE_FROM,
                BinarySearch.PARAMETER_FIND_PRIMITIVE,
                BinarySearch.PARAMETER_INT_LENGTH,
            ],
            BinarySearch.EVENT_INDEX,
        );
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            ?.getArguments()
            ?.get(BinarySearch.PARAMETER_ARRAY_SOURCE.getParameterName());

        let start: number = context
            ?.getArguments()
            ?.get(BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName());

        let find: any = context
            ?.getArguments()
            ?.get(BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName());

        let end: number = context
            ?.getArguments()
            ?.get(BinarySearch.PARAMETER_INT_LENGTH.getParameterName());

        if (source.length == 0 || start < 0 || start > source.length)
            throw new KIRuntimeException('Search source array cannot be empty');

        if (end == -1) end = source.length - start;

        end = start + end;

        if (end > source.length)
            throw new KIRuntimeException(
                'End point for array cannot be more than the size of the source array',
            );

        let index: number = -1;

        while (start <= end) {
            let mid: number = Math.floor((start + end) / 2);
            if (PrimitiveUtil.compare(source[mid], find) == 0) {
                index = mid;
                break;
            } else if (PrimitiveUtil.compare(source[mid], find) > 0) end = mid - 1;
            else start = mid + 1;
        }

        return new FunctionOutput([
            EventResult.outputOf(new Map([[BinarySearch.EVENT_INDEX.getName(), index]])),
        ]);
    }
}
