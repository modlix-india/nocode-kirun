import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

export class PrePad extends AbstractFunction {
    public static readonly PARAMETER_STRING_NAME: string = 'string';

    public static readonly PARAMETER_PREPAD_STRING_NAME: string = 'prepadString';

    public static readonly PARAMETER_LENGTH_NAME: string = 'length';

    public static readonly EVENT_RESULT_NAME: string = 'result';

    protected static readonly PARAMETER_STRING: Parameter = new Parameter(
        PrePad.PARAMETER_STRING_NAME,
        Schema.ofString(PrePad.PARAMETER_STRING_NAME),
    );

    protected static readonly PARAMETER_PREPAD_STRING: Parameter = new Parameter(
        PrePad.PARAMETER_PREPAD_STRING_NAME,
        Schema.ofString(PrePad.PARAMETER_PREPAD_STRING_NAME),
    );

    protected static readonly PARAMETER_LENGTH: Parameter = new Parameter(
        PrePad.PARAMETER_LENGTH_NAME,
        Schema.ofInteger(PrePad.PARAMETER_LENGTH_NAME),
    );

    protected static readonly EVENT_STRING: Event = new Event(
        Event.OUTPUT,
        new Map([[PrePad.EVENT_RESULT_NAME, Schema.ofString(PrePad.EVENT_RESULT_NAME)]]),
    );

    private readonly signature: FunctionSignature = new FunctionSignature('PrePad')
        .setNamespace(Namespaces.STRING)
        .setParameters(
            new Map([
                [PrePad.PARAMETER_STRING.getParameterName(), PrePad.PARAMETER_STRING],
                [PrePad.PARAMETER_PREPAD_STRING.getParameterName(), PrePad.PARAMETER_PREPAD_STRING],
                [PrePad.PARAMETER_LENGTH.getParameterName(), PrePad.PARAMETER_LENGTH],
            ]),
        )
        .setEvents(new Map([[PrePad.EVENT_STRING.getName(), PrePad.EVENT_STRING]]));

    private readonly signature = this.signature;
    public getSignature(): FunctionSignature {
        return this.signature;
    }

    public constructor() {
        super();
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let inputString: string = context.getArguments()?.get(PrePad.PARAMETER_STRING_NAME);
        let prepadString: string = context.getArguments()?.get(PrePad.PARAMETER_PREPAD_STRING_NAME);
        let length: number = context.getArguments()?.get(PrePad.PARAMETER_LENGTH_NAME);
        let outputString: string = '';
        let prepadStringLength: number = prepadString.length;

        while (prepadStringLength <= length) {
            outputString += prepadString;
            prepadStringLength += prepadString.length;
        }

        if (outputString.length < length) {
            outputString += prepadString.substring(0, length - outputString.length);
        }

        outputString += inputString;
        return new FunctionOutput([
            EventResult.outputOf(new Map([[PrePad.EVENT_RESULT_NAME, outputString]])),
        ]);
    }
}
