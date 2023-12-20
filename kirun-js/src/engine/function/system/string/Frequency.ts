import { Schema } from "../../../json/schema/Schema";
import { EventResult } from "../../../model/EventResult";
import { FunctionOutput } from "../../../model/FunctionOutput";
import { Event } from '../../../model/Event';
import { FunctionSignature } from "../../../model/FunctionSignature";
import { Parameter } from "../../../model/Parameter";
import { Namespaces } from "../../../namespaces/Namespaces";
import { FunctionExecutionParameters } from "../../../runtime/FunctionExecutionParameters";
import { AbstractFunction } from "../../AbstractFunction";


export class Frequency extends AbstractFunction {
    public static readonly PARAMETER_STRING_NAME: string = 'string';

    public static readonly PARAMETER_SEARCH_STRING_NAME: string = 'searchString';

    protected readonly EVENT_RESULT_NAME: string = 'result';

    protected readonly PARAMETER_STRING: Parameter = new Parameter(
        Frequency.PARAMETER_STRING_NAME,
        Schema.ofString(Frequency.PARAMETER_STRING_NAME),
    );

    protected readonly PARAMETER_SEARCH_STRING_NAME: Parameter = new Parameter(
        Frequency.PARAMETER_SEARCH_STRING_NAME,
        Schema.ofString(Frequency.PARAMETER_SEARCH_STRING_NAME),
    );

    protected readonly EVENT_INT: Event = new Event(
        Event.OUTPUT,
        new Map([[this.EVENT_RESULT_NAME, Schema.ofInteger(this.EVENT_RESULT_NAME)]]),
    );

    private signature: FunctionSignature = new FunctionSignature('Frequency')
        .setNamespace(Namespaces.STRING)
        .setParameters(
            new Map([
                [this.PARAMETER_STRING.getParameterName(), this.PARAMETER_STRING],
                [this.PARAMETER_SEARCH_STRING_NAME.getParameterName(), this.PARAMETER_SEARCH_STRING_NAME],
            ]),
        )
        .setEvents(
            new Map([
                Event.outputEventMapEntry(new Map([[this.EVENT_RESULT_NAME, Schema.ofInteger(this.EVENT_RESULT_NAME)]])),
            ]),
            
        );

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let inputString: string = context?.getArguments()?.get(Frequency.PARAMETER_STRING_NAME);
        let searchString: string = context?.getArguments()?.get(Frequency.PARAMETER_SEARCH_STRING_NAME);

        const frequency = this.stringFunction(inputString, searchString);

        return new FunctionOutput([
            EventResult.outputOf(new Map([[this.EVENT_RESULT_NAME, frequency]])),
        ]);
    }

    private stringFunction(input: string, search: string): number {
        const inputLength: number = input.length;
        const searchLength: number = search.length;
        let frequency: number = 0;

        if (searchLength < 1) {
            return 0;
        }

        for (let i = 0; i < inputLength - searchLength + 1; i++) {
            if (input.charAt(i) !== search.charAt(0)) {
                continue;
            }

            let flag: boolean = true;
            for (let j = 1; j < searchLength; j++) {
                if (input.charAt(i + j) !== search.charAt(j)) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                frequency++;
            }
        }

        return frequency;
    }
}
