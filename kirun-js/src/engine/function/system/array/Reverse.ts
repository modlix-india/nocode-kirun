import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class Reverse extends AbstractArrayFunction {
    public constructor() {
        super(
            'Reverse',
            [
                Reverse.PARAMETER_ARRAY_SOURCE,
                Reverse.PARAMETER_INT_SOURCE_FROM,
                Reverse.PARAMETER_INT_LENGTH,
            ],
            Reverse.EVENT_RESULT_EMPTY,
        );
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            .getArguments()
            .get(Reverse.PARAMETER_ARRAY_SOURCE.getParameterName());

        let st: number = context
            .getArguments()
            .get(Reverse.PARAMETER_INT_SOURCE_FROM.getParameterName());

        let ed: number = context
            .getArguments()
            .get(Reverse.PARAMETER_INT_LENGTH.getParameterName());

        if (source.length == 0 || ed > source.length - 1 || st < 0)
            return new FunctionOutput([EventResult.outputOf(new Map([]))]);

        if (ed == -1) ed = source.length - st;
        ed--;

        while (st < ed) {
            let first: any = source[st];
            let last: any = source[ed];
            source[st++] = last;
            source[ed--] = first;
        }

        return new FunctionOutput([EventResult.outputOf(new Map([]))]);
    }
}
