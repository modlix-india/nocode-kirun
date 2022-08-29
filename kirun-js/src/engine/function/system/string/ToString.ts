import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

export class ToString extends AbstractFunction {
    protected readonly PARAMETER_INPUT_ANYTYPE_NAME: string = 'anytype';

    protected readonly EVENT_RESULT_NAME: string = 'result';

    protected readonly PARAMETER_INPUT_ANYTYPE: Parameter = new Parameter(
        this.PARAMETER_INPUT_ANYTYPE_NAME,
        Schema.ofAny(this.PARAMETER_INPUT_ANYTYPE_NAME),
    );

    protected readonly EVENT_STRING: Event = new Event(
        Event.OUTPUT,
        new Map([[this.EVENT_RESULT_NAME, Schema.ofString(this.EVENT_RESULT_NAME)]]),
    );

    public getSignature(): FunctionSignature {
        return new FunctionSignature('ToString')
            .setNamespace(Namespaces.STRING)
            .setParameters(
                new Map([
                    [this.PARAMETER_INPUT_ANYTYPE.getParameterName(), this.PARAMETER_INPUT_ANYTYPE],
                ]),
            )
            .setEvents(new Map([[this.EVENT_STRING.getName(), this.EVENT_STRING]]));
    }

    public constructor() {
        super();
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let input: any = context.getArguments()?.get(this.PARAMETER_INPUT_ANYTYPE_NAME);
        let output: string = input + '';

        return new FunctionOutput([
            EventResult.outputOf(new Map([[this.EVENT_RESULT_NAME, output]])),
        ]);
    }
}
