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
            .getArguments()
            .get(Delete.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName());

        let deletable: any[] = context
            .getArguments()
            .get(Delete.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName());

        if (source.length == 0 || deletable.length == 0 || deletable.length > source.length)
            throw new KIRuntimeException(
                'Expected a source or deletable for an array but not found any or the deletable size of the array is more than the source array',
            );

        let deletableSize: number = deletable.length;

        let index: number = -1;

        for (let i = 0; i < source.length; i++) {
            let j: number = 0;
            if (
                !source.get(i).isJsonNull() &&
                !deletable.get(j).isJsonNull() &&
                source.get(i).equals(deletable.get(j))
            ) {
                while (j < deletableSize) {
                    if (
                        source.get(i).isJsonNull() ||
                        deletable.get(j).isJsonNull() ||
                        !source.get(i + j).equals(deletable.get(j))
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
            for (let i = index; i <= deletableSize; i++) {
                source.remove(index);
            }
        }

        return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));
    }
}
