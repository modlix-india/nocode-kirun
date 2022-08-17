import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
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

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            .getArguments()
            .get(Frequency.PARAMETER_ARRAY_SOURCE.getParameterName());

        var find = context.getArguments().get(Frequency.PARAMETER_ANY.getParameterName());

        let from: number = context
            .getArguments()
            .get(Frequency.PARAMETER_INT_SOURCE_FROM.getParameterName());

        let length: number = context
            .getArguments()
            .get(Frequency.PARAMETER_INT_LENGTH.getParameterName());

        if (source.length == 0)
            return new this.FunctionOutput([
                EventResult.outputOf(new Map([[Frequency.EVENT_RESULT_INTEGER.getName(), 0]])),
            ]);

        let end: number = length > 0 ? length : source.length;

        if (length == -1) end = source.length - from;

        let start: number = from < 0 ? 0 : from;

        let frequency: number = 0;

        for (let i: number = start; i < end && i < source.length; i++) {
            if (
                (find.isJsonPrimitive() &&
                    source.get(i).isJsonPrimitive() &&
                    source.get(i).equals(find.getAsJsonPrimitive())) ||
                (find.isJsonArray() &&
                    source.get(i).isJsonArray() &&
                    source.get(i).equals(find.getAsJsonArray())) ||
                (find.isJsonObject() &&
                    source.get(i).isJsonObject() &&
                    source.get(i).equals(find.getAsJsonObject()))
            )
                frequency++;
        }

        return new this.FunctionOutput([
            EventResult.outputOf(new Map([[Frequency.EVENT_RESULT_INTEGER.getName(), frequency]])),
        ]);
    }
}
