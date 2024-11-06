import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { PrimitiveUtil } from '../../../util/primitive/PrimitiveUtil';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class IndexOf extends AbstractArrayFunction {
    public constructor() {
        super(
            'IndexOf',
            [
                IndexOf.PARAMETER_ARRAY_SOURCE,
                IndexOf.PARAMETER_ANY_ELEMENT_OBJECT,
                IndexOf.PARAMETER_INT_FIND_FROM,
            ],
            IndexOf.EVENT_RESULT_INTEGER,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context
            ?.getArguments()
            ?.get(IndexOf.PARAMETER_ARRAY_SOURCE.getParameterName());

        let find = context
            ?.getArguments()
            ?.get(IndexOf.PARAMETER_ANY_ELEMENT_OBJECT.getParameterName());

        let len: number = context
            ?.getArguments()
            ?.get(IndexOf.PARAMETER_INT_FIND_FROM.getParameterName());

        if (source.length == 0)
            return new FunctionOutput([
                EventResult.outputOf(new Map([[IndexOf.EVENT_RESULT_NAME, -1]])),
            ]);
        if (len < 0 || len > source.length)
            throw new KIRuntimeException(
                'The size of the search index of the array is greater than the size of the array',
            );

        let index: number = -1;

        for (let i: number = len; i < source.length; i++) {
            if (PrimitiveUtil.compare(source[i], find) == 0) {
                index = i;
                break;
            }
        }

        return new FunctionOutput([
            EventResult.outputOf(new Map([[IndexOf.EVENT_RESULT_NAME, index]])),
        ]);
    }
}
