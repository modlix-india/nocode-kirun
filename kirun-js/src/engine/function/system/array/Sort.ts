import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { ObjectValueSetterExtractor } from '../../../runtime/expression/tokenextractor/ObjectValueSetterExtractor';

import { AbstractArrayFunction } from './AbstractArrayFunction';

export class Sort extends AbstractArrayFunction {
    public constructor() {
        super(
            'Sort',
            [
                Sort.PARAMETER_ARRAY_SOURCE,
                Sort.PARAMETER_INT_FIND_FROM,
                Sort.PARAMETER_INT_LENGTH,
                Sort.PARAMETER_BOOLEAN_ASCENDING,
                Sort.PARAMETER_KEY_PATH,
            ],
            Sort.EVENT_RESULT_ARRAY,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context
            ?.getArguments()
            ?.get(Sort.PARAMETER_ARRAY_SOURCE.getParameterName());

        let start: number = context
            ?.getArguments()
            ?.get(Sort.PARAMETER_INT_FIND_FROM.getParameterName());

        let len: number = context
            ?.getArguments()
            ?.get(Sort.PARAMETER_INT_LENGTH.getParameterName());

        let ascending: boolean = context
            ?.getArguments()
            ?.get(Sort.PARAMETER_BOOLEAN_ASCENDING.getParameterName());

        let keyPath: string = context
            ?.getArguments()
            ?.get(Sort.PARAMETER_KEY_PATH.getParameterName());

        if (source.length == 0)
            return new FunctionOutput([
                EventResult.outputOf(new Map([[AbstractArrayFunction.EVENT_RESULT_NAME, source]])),
            ]);

        source = [...source];
        if (len == -1) len = source.length - start;

        if (start < 0 || start >= source.length || start + len > source.length)
            throw new KIRuntimeException(
                'Given start point is more than the size of the array or not available at that point',
            );

        let slicedArray: any[] = source.slice(start, start + len + 1);

        const ove: ObjectValueSetterExtractor = new ObjectValueSetterExtractor({}, 'Data.');
        slicedArray.sort((a, b) => {
            if (typeof a === 'object' && typeof b === 'object' && keyPath.length) {
                ove.setStore({ a: a, b: b });
                const aVal = ove.getValue('Data.a.' + keyPath);
                const bVal = ove.getValue('Data.b.' + keyPath);
                return compareFunction(aVal, bVal, ascending);
            }
            return compareFunction(a, b, ascending);
        });

        source.splice(start, len, ...slicedArray);

        return new FunctionOutput([
            EventResult.outputOf(new Map([[AbstractArrayFunction.EVENT_RESULT_NAME, source]])),
        ]);
    }
}

function compareFunction(a: any, b: any, ascending: boolean): number {
    if (a === b) return 0;
    if (a === null) return 1;
    if (b === null) return -1;
    if (!ascending) return a < b ? 1 : -1;

    return a < b ? -1 : 1;
}
