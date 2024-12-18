import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

export class TrimTo extends AbstractFunction {
    public static readonly PARAMETER_STRING_NAME: string = 'string';

    public static readonly PARAMETER_LENGTH_NAME: string = 'length';

    public static readonly EVENT_RESULT_NAME: string = 'result';

    protected static readonly PARAMETER_STRING: Parameter = new Parameter(
        TrimTo.PARAMETER_STRING_NAME,
        Schema.ofString(TrimTo.PARAMETER_STRING_NAME),
    );

    protected static readonly PARAMETER_LENGTH: Parameter = new Parameter(
        TrimTo.PARAMETER_LENGTH_NAME,
        Schema.ofInteger(TrimTo.PARAMETER_LENGTH_NAME),
    );

    protected static readonly EVENT_STRING: Event = new Event(
        Event.OUTPUT,
        new Map([[TrimTo.EVENT_RESULT_NAME, Schema.ofString(TrimTo.EVENT_RESULT_NAME)]]),
    );

    private signature: FunctionSignature = new FunctionSignature('TrimTo')
        .setNamespace(Namespaces.STRING)
        .setParameters(
            new Map([
                [TrimTo.PARAMETER_STRING.getParameterName(), TrimTo.PARAMETER_STRING],
                [TrimTo.PARAMETER_LENGTH.getParameterName(), TrimTo.PARAMETER_LENGTH],
            ]),
        )
        .setEvents(new Map([[TrimTo.EVENT_STRING.getName(), TrimTo.EVENT_STRING]]));

    private readonly signature = this.signature;
    public getSignature(): FunctionSignature {
        return this.signature;
    }

    public constructor() {
        super();
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let inputString: string = context.getArguments()?.get(TrimTo.PARAMETER_STRING_NAME);
        let length: number = context.getArguments()?.get(TrimTo.PARAMETER_LENGTH_NAME);

        return new FunctionOutput([
            EventResult.outputOf(
                new Map([[TrimTo.EVENT_RESULT_NAME, inputString.substring(0, length)]]),
            ),
        ]);
    }
}
