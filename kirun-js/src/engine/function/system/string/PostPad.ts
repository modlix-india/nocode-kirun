import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

export class PostPad extends AbstractFunction {
    protected static readonly PARAMETER_STRING_NAME: string = 'string';

    protected static readonly PARAMETER_POSTPAD_STRING_NAME: string = 'postpadString';

    protected static readonly PARAMETER_LENGTH_NAME: string = 'length';

    protected static readonly EVENT_RESULT_NAME: string = 'result';

    protected static PARAMETER_STRING: Parameter = new Parameter(
        PostPad.PARAMETER_STRING_NAME,
        Schema.ofString(PostPad.PARAMETER_STRING_NAME),
    );

    protected static PARAMETER_POSTPAD_STRING: Parameter = new Parameter(
        PostPad.PARAMETER_POSTPAD_STRING_NAME,
        Schema.ofString(PostPad.PARAMETER_POSTPAD_STRING_NAME),
    );

    protected static PARAMETER_LENGTH: Parameter = new Parameter(
        PostPad.PARAMETER_LENGTH_NAME,
        Schema.ofInteger(PostPad.PARAMETER_LENGTH_NAME),
    );

    protected static EVENT_STRING: Event = new Event(
        Event.OUTPUT,
        new Map([[PostPad.EVENT_RESULT_NAME, Schema.ofString(PostPad.EVENT_RESULT_NAME)]]),
    );

    private signature: FunctionSignature = new FunctionSignature('PostPad')
        .setNamespace(Namespaces.STRING)
        .setParameters(
            new Map([
                [PostPad.PARAMETER_STRING.getParameterName(), PostPad.PARAMETER_STRING],
                [
                    PostPad.PARAMETER_POSTPAD_STRING.getParameterName(),
                    PostPad.PARAMETER_POSTPAD_STRING,
                ],
                [PostPad.PARAMETER_LENGTH.getParameterName(), PostPad.PARAMETER_LENGTH],
            ]),
        )
        .setEvents(new Map([[PostPad.EVENT_STRING.getName(), PostPad.EVENT_STRING]]));

    public constructor() {
        super();
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let inputString: string = context.getArguments()?.get(PostPad.PARAMETER_STRING_NAME);
        let postpadString: string = context
            ?.getArguments()
            ?.get(PostPad.PARAMETER_POSTPAD_STRING_NAME);

        let length: number = context.getArguments()?.get(PostPad.PARAMETER_LENGTH_NAME);
        let outputString: string = '';
        let prepadStringLength: number = postpadString.length;

        outputString += inputString;

        while (prepadStringLength <= length) {
            outputString += postpadString;
            prepadStringLength += postpadString.length;
        }

        if (outputString.length - inputString.length < length) {
            outputString += postpadString.substring(
                0,
                length - (outputString.length - inputString.length),
            );
        }

        return new FunctionOutput([
            EventResult.outputOf(new Map([[PostPad.EVENT_RESULT_NAME, outputString.toString()]])),
        ]);
    }
}
