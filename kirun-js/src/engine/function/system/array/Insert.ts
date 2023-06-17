import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { isNullValue } from '../../../util/NullCheck';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class Insert extends AbstractArrayFunction {
    public constructor() {
        super(
            'Insert',
            [Insert.PARAMETER_ARRAY_SOURCE, Insert.PARAMETER_INT_OFFSET, Insert.PARAMETER_ANY],
            Insert.EVENT_RESULT_ARRAY,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context
            ?.getArguments()
            ?.get(Insert.PARAMETER_ARRAY_SOURCE.getParameterName());

        let offset: number = context
            ?.getArguments()
            ?.get(Insert.PARAMETER_INT_OFFSET.getParameterName());

        var output = context?.getArguments()?.get(Insert.PARAMETER_ANY.getParameterName());

        if (isNullValue(output) || isNullValue(offset) || offset > source.length)
            throw new KIRuntimeException('Please valid resouces to insert at the correct location');

        source = [...source];

        if (source.length == 0) {
            if (offset == 0) source.push(output);
            return new FunctionOutput([
                EventResult.outputOf(new Map([[AbstractArrayFunction.EVENT_RESULT_NAME, source]])),
            ]);
        }

        source.push(output);
        let len: number = source.length - 1;
        offset++; // to insert at that point

        while (len >= offset) {
            let temp: any = source[len - 1];
            source[len - 1] = source[len];
            source[len--] = temp;
        }

        return new FunctionOutput([
            EventResult.outputOf(new Map([[AbstractArrayFunction.EVENT_RESULT_NAME, source]])),
        ]);
    }
}
