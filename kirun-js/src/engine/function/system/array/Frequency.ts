import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { PrimitiveUtil } from '../../../util/primitive/PrimitiveUtil';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class Frequency extends AbstractArrayFunction {
    FunctionOutput: any;

    public constructor() {
        super(
            'Frequency',
            [
                Frequency.PARAMETER_ARRAY_SOURCE,
                Frequency.PARAMETER_ANY,
                Frequency.PARAMETER_INT_SOURCE_FROM,
                Frequency.PARAMETER_INT_LENGTH,
            ],
            Frequency.EVENT_RESULT_INTEGER,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context
            ?.getArguments()
            ?.get(Frequency.PARAMETER_ARRAY_SOURCE.getParameterName());

        let find: any = context?.getArguments()?.get(Frequency.PARAMETER_ANY.getParameterName());

        let start: number = context
            ?.getArguments()
            ?.get(Frequency.PARAMETER_INT_SOURCE_FROM.getParameterName());

        let length: number = context
            ?.getArguments()
            ?.get(Frequency.PARAMETER_INT_LENGTH.getParameterName());

        if (source.length == 0)
            return new FunctionOutput([
                EventResult.outputOf(new Map([[Frequency.EVENT_RESULT_NAME, 0]])),
            ]);

        if (start > source.length)
            throw new KIRuntimeException('Given start point is more than the size of source');

        let end: number = start + length;

        if (length == -1) end = source.length - start;

        if (end > source.length)
            throw new KIRuntimeException('Given length is more than the size of source');

        let frequency: number = 0;

        for (let i: number = start; i < end && i < source.length; i++) {
            if (PrimitiveUtil.compare(source[i], find) == 0) frequency++;
        }

        return new FunctionOutput([
            EventResult.outputOf(new Map([[Frequency.EVENT_RESULT_NAME, frequency]])),
        ]);
    }
}
