import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class MisMatch extends AbstractArrayFunction {
    public constructor() {
        super(
            'MisMatch',
            [
                MisMatch.PARAMETER_ARRAY_SOURCE,
                MisMatch.PARAMETER_INT_FIND_FROM,
                MisMatch.PARAMETER_ARRAY_SECOND_SOURCE,
                MisMatch.PARAMETER_INT_SECOND_SOURCE_FROM,
                MisMatch.PARAMETER_INT_LENGTH,
            ],
            MisMatch.EVENT_RESULT_INTEGER,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let firstSource: any[] = context
            ?.getArguments()
            ?.get(MisMatch.PARAMETER_ARRAY_SOURCE.getParameterName());

        let firstFind: number = context
            ?.getArguments()
            ?.get(MisMatch.PARAMETER_INT_FIND_FROM.getParameterName());

        let secondSource: any[] = context
            ?.getArguments()
            ?.get(MisMatch.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName());

        let secondFind: number = context
            ?.getArguments()
            ?.get(MisMatch.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName());

        let length: number = context
            ?.getArguments()
            ?.get(MisMatch.PARAMETER_INT_LENGTH.getParameterName());

        // write check conditions

        let first: number = firstFind < firstSource.length && firstFind > 0 ? firstFind : 0;
        let second: number = secondFind < secondSource.length && secondFind > 0 ? secondFind : 0;

        if (first + length >= firstSource.length || second + length > secondSource.length)
            throw new KIRuntimeException(
                'The size of the array for first and second which was being requested is more than size of the given array',
            );

        let index: number = -1;

        for (let i: number = 0; i < length; i++) {
            if (firstSource[first + i] != secondSource[second + i]) {
                index = i;
                break;
            }
        }

        return new FunctionOutput([
            EventResult.outputOf(new Map([[MisMatch.EVENT_RESULT_INTEGER.getName(), index]])),
        ]);
    }
}
