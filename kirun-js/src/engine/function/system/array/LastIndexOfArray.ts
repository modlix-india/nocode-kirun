import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
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
            .getArguments()
            .get(LastIndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName());

        let secondSource: any[] = context
            .getArguments()
            .get(LastIndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName());

        let from: any = context
            .getArguments()
            .get(LastIndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName());

        if (secondSource.length == 0)
            return new FunctionOutput([
                EventResult.outputOf(
                    new Map([[LastIndexOfArray.EVENT_RESULT_ARRAY.getName(), -1]]),
                ),
            ]);

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
                }
            }
        }

        return new FunctionOutput([
            EventResult.outputOf(new Map([[LastIndexOfArray.EVENT_RESULT_ARRAY.getName(), index]])),
        ]);
    }
}
