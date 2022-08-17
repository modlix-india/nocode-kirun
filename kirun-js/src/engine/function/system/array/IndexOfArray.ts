import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
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

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            .getArguments()
            .get(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName());

        let secondSource: any[] = context
            .getArguments()
            .get(IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName());

        let from: number = context
            .getArguments()
            .get(IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName());

        let len: number = from >= source.length ? 0 : from;

        let secondSourceSize: number = secondSource.length;
        let index: number = -1;

        for (let i: number = len; i < source.length; i++) {
            let j: number = 0;
            if (
                typeof source[i] != null &&
                typeof source[i] != undefined &&
                typeof secondSource[j] != null &&
                typeof secondSource[j] != undefined &&
                source[i] == secondSource[j]
            ) {
                while (j < secondSourceSize) {
                    if (
                        typeof source[i] == null ||
                        typeof source[i] == undefined ||
                        typeof secondSource[j] == null ||
                        typeof secondSource[j] == undefined ||
                        source[i + j] != secondSource[j]
                    ) {
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
