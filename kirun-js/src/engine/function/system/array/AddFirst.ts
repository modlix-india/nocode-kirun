import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class AddFirst extends AbstractArrayFunction {
    public constructor() {
        super(
            'AddFirst',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_ANY_NOT_NULL,
            ],
            AbstractArrayFunction.EVENT_RESULT_EMPTY,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context
            ?.getArguments()
            ?.get(AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.getParameterName());

        let input: any = context
            ?.getArguments()
            ?.get(AbstractArrayFunction.PARAMETER_ANY_NOT_NULL.getParameterName());

        if (source.length == 0) {
            source.push(input);
            return new FunctionOutput([EventResult.outputOf(new Map([]))]);
        }

        source.push(input);

        let len: number = source.length - 1;

        while (len > 0) {
            let temp: any = source[len - 1];
            source[len - 1] = source[len];
            source[len--] = temp;
        }

        return new FunctionOutput([EventResult.outputOf(new Map([]))]);
    }
}
