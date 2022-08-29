import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class DeleteFrom extends AbstractArrayFunction {
    public constructor() {
        super(
            'DeleteFrom',
            [
                DeleteFrom.PARAMETER_ARRAY_SOURCE,
                DeleteFrom.PARAMETER_INT_SOURCE_FROM,
                DeleteFrom.PARAMETER_INT_LENGTH,
            ],
            DeleteFrom.EVENT_RESULT_EMPTY,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context
            ?.getArguments()
            ?.get(DeleteFrom.PARAMETER_ARRAY_SOURCE.getParameterName());

        let start: number = context
            ?.getArguments()
            ?.get(DeleteFrom.PARAMETER_INT_SOURCE_FROM.getParameterName());

        let len: number = context
            ?.getArguments()
            ?.get(DeleteFrom.PARAMETER_INT_LENGTH.getParameterName());

        if (source.length == 0) throw new KIRuntimeException('There are no elements to be deleted');

        if (start >= source.length || start < 0)
            throw new KIRuntimeException(
                'The int source for the array should be in between 0 and length of the array ',
            );

        if (len == -1) len = source.length - start;

        if (start + len > source.length)
            throw new KIRuntimeException(
                'Requested length to be deleted is more than the size of array ',
            );

        source.splice(start, len);

        return new FunctionOutput([EventResult.outputOf(new Map([]))]);
    }
}
