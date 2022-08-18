import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class Delete extends AbstractArrayFunction {
    public constructor() {
        super(
            'Delete',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE,
                AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE,
            ],
            AbstractArrayFunction.EVENT_RESULT_EMPTY,
        );
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            ?.getArguments()
            ?.get(Delete.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName());

        let deletable: any[] = context
            ?.getArguments()
            ?.get(Delete.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName());

        if (source.length == 0 || deletable.length == 0 || deletable.length > source.length)
            throw new KIRuntimeException(
                'Expected a source or deletable for an array but not found any or the deletable size of the array is more than the source array',
            );

        let deletableSize: number = deletable.length;

        let index: number = -1;

        for (let i = 0; i < source.length; i++) {
            let j: number = 0;
            if (source[i] !== null && deletable[j] !== null && source[i] == deletable[j]) {
                while (j < deletableSize) {
                    if (
                        source[i] == null ||
                        deletable[j] == null ||
                        source[i + j] != deletable[j]
                    ) {
                        break;
                    }
                    j++;
                }
                if (j == deletableSize) {
                    index = i;
                    break;
                }
            }
        }

        if (index != -1) {
            source.splice(index, deletableSize);
        }

        return new FunctionOutput([EventResult.outputOf(new Map([]))]);
    }
}
