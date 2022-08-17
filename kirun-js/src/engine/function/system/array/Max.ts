import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';
import { Sort } from './Sort';

export class Max extends AbstractArrayFunction {
    public constructor() {
        super('Max', [Max.PARAMETER_ARRAY_SOURCE], Max.EVENT_RESULT_ANY);
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            .getArguments()
            .get(Max.PARAMETER_ARRAY_SOURCE.getParameterName());

        if (source.length == 0) throw new KIRuntimeException('Search source array cannot be empty');

        source.sort((a, b) => sortingNullValues(a, b));

        let len: number = source.length - 1;

        while (len >= 0) {
            if (source[len] != null) break;
            len--;
        }

        let slicedSource: any[] = source.slice(0, len + 1);

        slicedSource.sort();

        return new FunctionOutput([
            EventResult.outputOf(
                new Map([[Max.EVENT_RESULT_ANY.getName(), slicedSource[slicedSource.length - 1]]]),
            ),
        ]);
    }
}

function sortingNullValues(a: any, b: any): number {
    if (a === b) return 0;
    if (a === null) return 1;
    if (b === null) return -1;

    return a < b ? -1 : 1;
}
