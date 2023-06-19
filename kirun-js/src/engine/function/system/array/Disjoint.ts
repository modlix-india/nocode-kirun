import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class Disjoint extends AbstractArrayFunction {
    public constructor() {
        super(
            'Disjoint',
            [
                Disjoint.PARAMETER_ARRAY_SOURCE,
                Disjoint.PARAMETER_INT_SOURCE_FROM,
                Disjoint.PARAMETER_ARRAY_SECOND_SOURCE,
                Disjoint.PARAMETER_INT_SECOND_SOURCE_FROM,
                Disjoint.PARAMETER_INT_LENGTH,
            ],
            Disjoint.EVENT_RESULT_ARRAY,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let firstSource: any[] = context
            ?.getArguments()
            ?.get(Disjoint.PARAMETER_ARRAY_SOURCE.getParameterName());

        let first: number = context
            ?.getArguments()
            ?.get(Disjoint.PARAMETER_INT_SOURCE_FROM.getParameterName());

        let secondSource: any[] = context
            ?.getArguments()
            ?.get(Disjoint.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName());

        let second: number = context
            ?.getArguments()
            ?.get(Disjoint.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName());

        let len: number = context
            ?.getArguments()
            ?.get(Disjoint.PARAMETER_INT_LENGTH.getParameterName());

        if (len == -1)
            len =
                firstSource.length <= secondSource.length
                    ? firstSource.length - first
                    : secondSource.length - second;

        if (
            len > firstSource.length ||
            len > secondSource.length ||
            first + len > firstSource.length ||
            second + len > secondSource.length
        )
            throw new KIRuntimeException(
                'The length which was being requested is more than than the size either source array or second source array',
            );

        let set1: Set<any> = new Set<any>();
        let set2: Set<any> = new Set<any>();

        for (let i: number = 0; i < len; i++) set1.add(firstSource[i + first]);

        for (let i: number = 0; i < len; i++) set2.add(secondSource[i + second]);

        let set3: Set<any> = new Set<any>();

        set1.forEach((element) => {
            if (set2.has(element)) set2.delete(element);
            else set3.add(element);
        });

        set2.forEach((element) => {
            if (!set1.has(element)) set3.add(element);
        });

        return new FunctionOutput([
            EventResult.outputOf(new Map([[Disjoint.EVENT_RESULT_NAME, [...set3]]])),
        ]);
    }
}
