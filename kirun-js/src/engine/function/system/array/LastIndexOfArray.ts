import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { PrimitiveUtil } from '../../../util/primitive/PrimitiveUtil';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class LastIndexOfArray extends AbstractArrayFunction {
    public constructor() {
        super(
            'LastIndexOfArray',
            [
                LastIndexOfArray.PARAMETER_ARRAY_SOURCE,
                LastIndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE,
                LastIndexOfArray.PARAMETER_INT_FIND_FROM,
            ],
            LastIndexOfArray.EVENT_RESULT_INTEGER,
        );
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            ?.getArguments()
            ?.get(LastIndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName());

        let secondSource: any[] = context
            ?.getArguments()
            ?.get(LastIndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName());

        let from: any = context
            ?.getArguments()
            ?.get(LastIndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName());

        if (source.length == 0)
            return new FunctionOutput([
                EventResult.outputOf(
                    new Map([[LastIndexOfArray.EVENT_RESULT_ARRAY.getName(), -1]]),
                ),
            ]);

        if (from < 0 || from > source.length || secondSource.length > source.length)
            throw new KIRuntimeException(
                'Given from index is more than the size of the source array',
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
                }
            }
        }

        return new FunctionOutput([
            EventResult.outputOf(
                new Map([[LastIndexOfArray.EVENT_RESULT_INTEGER.getName(), index]]),
            ),
        ]);
    }
}
