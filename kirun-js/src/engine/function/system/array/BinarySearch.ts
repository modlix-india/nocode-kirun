import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class BinarySearch extends AbstractArrayFunction {
    public constructor() {
        super(
            'BinarySearch',
            [
                BinarySearch.PARAMETER_ARRAY_SOURCE,
                BinarySearch.PARAMETER_INT_SOURCE_FROM,
                BinarySearch.PARAMETER_ARRAY_FIND,
                BinarySearch.PARAMETER_INT_LENGTH,
            ],
            BinarySearch.EVENT_INDEX,
        );
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            .getArguments()
            .get(BinarySearch.PARAMETER_ARRAY_SOURCE.getParameterName());

        let from: number = context
            .getArguments()
            .get(BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName());

        let find: any[] = context
            .getArguments()
            .get(BinarySearch.PARAMETER_ARRAY_FIND.getParameterName());

        let length: number = context
            .getArguments()
            .get(BinarySearch.PARAMETER_INT_LENGTH.getParameterName());

        if (source.length == 0) {
            throw new KIRuntimeException('Search source array cannot be empty');
        }

        if (find.length == 0) {
            throw new KIRuntimeException('Find array cannot be empty');
        }

        if (length == -1) length = source.length;

        if (find.length > source.length || find.length > length - from)
            throw new KIRuntimeException('Find array is larger than the source array');

        return new FunctionOutput([
            EventResult.outputOf(
                new Map([
                    [
                        BinarySearch.EVENT_INDEX_NAME,
                        bMultiSearch(source, from, length + from, find),
                    ],
                ]),
            ),
        ]);
    }
}

function bMultiSearch(src: any[], start: number, end: number, fnd: any[]): number {
    let ind: number = bSearch(src, start, end, fnd[0]);
    if (fnd.length > 1) {
        let j: number = 0;
        if (ind == -1) return ind;
        for (let i: number = ind; i < ind + fnd.length; i++) {
            if (src[i] != fnd[j++]) {
                return -1;
            }
        }
    }
    return ind;
}

function bSearch(src: any[], start: number, end: number, search: any): number {
    while (start <= end) {
        let mid: number = Math.floor((start + end) / 2);
        if (src[mid] == search) return mid;
        else if (src[mid] > search) end = mid - 1;
        else start = mid + 1;
    }

    return -1;
}
