import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { PrimitiveUtil } from '../../../util/primitive/PrimitiveUtil';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class IndexOfArray extends AbstractArrayFunction {
    public constructor() {
        super(
            'IndexOfArray',
            [
                IndexOfArray.PARAMETER_ARRAY_SOURCE,
                IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE,
                IndexOfArray.PARAMETER_INT_FIND_FROM,
            ],
            IndexOfArray.EVENT_RESULT_INTEGER,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context
            ?.getArguments()
            ?.get(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName());

        let secondSource: any[] = context
            ?.getArguments()
            ?.get(IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName());

        let from: number = context
            ?.getArguments()
            ?.get(IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName());

        if (source.length == 0 || secondSource.length == 0)
            return new FunctionOutput([
                EventResult.outputOf(new Map([[IndexOfArray.EVENT_RESULT_INTEGER.getName(), -1]])),
            ]);

        if (from < 0 || from > source.length || source.length < secondSource.length)
            throw new KIRuntimeException(
                'Given from second source is more than the size of the source array',
            );

        let secondSourceSize: number = secondSource.length;
        let index: number = -1;

        for (let i: number = from; i < source.length; i++) {
            let j: number = 0;
            if (PrimitiveUtil.compare(source[i], secondSource[j]) == 0) {
                while (j < secondSourceSize) {
                    if (PrimitiveUtil.compare(source[i + j], secondSource[j]) != 0) {
                        break;
                    }
                    j++;
                }
                if (j == secondSourceSize) {
                    index = i;
                    break;
                }
            }
        }

        return new FunctionOutput([
            EventResult.outputOf(new Map([[IndexOfArray.EVENT_RESULT_INTEGER.getName(), index]])),
        ]);
    }
}
