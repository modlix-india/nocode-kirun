import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class Add extends AbstractArrayFunction {
    public constructor() {
        super(
            'Add',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE,
            ],
            AbstractArrayFunction.EVENT_RESULT_EMPTY,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context
            ?.getArguments()
            ?.get(AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.getParameterName());

        let secondSource: any[] = context
            ?.getArguments()
            ?.get(AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName());

        source.splice(source.length, 0, ...secondSource);

        return new FunctionOutput([EventResult.outputOf(new Map([]))]);
    }
}
