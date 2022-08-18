import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

export class InsertAtGivenPosition extends AbstractFunction {
    public static readonly PARAMETER_STRING_NAME: string = 'string';

    public static readonly PARAMETER_AT_POSITION_NAME: string = 'position';

    public static readonly PARAMETER_INSERT_STRING_NAME: string = 'insertString';

    protected readonly EVENT_RESULT_NAME: string = 'result';

    protected readonly PARAMETER_STRING: Parameter = new Parameter()
        .setParameterName(InsertAtGivenPosition.PARAMETER_STRING_NAME)
        .setSchema(Schema.ofString(InsertAtGivenPosition.PARAMETER_STRING_NAME));

    protected readonly PARAMETER_AT_POSITION: Parameter = new Parameter()
        .setParameterName(InsertAtGivenPosition.PARAMETER_AT_POSITION_NAME)
        .setSchema(Schema.ofInteger(InsertAtGivenPosition.PARAMETER_AT_POSITION_NAME));

    protected readonly PARAMETER_INSERT_STRING: Parameter = new Parameter()
        .setParameterName(InsertAtGivenPosition.PARAMETER_INSERT_STRING_NAME)
        .setSchema(Schema.ofString(InsertAtGivenPosition.PARAMETER_INSERT_STRING_NAME));

    protected readonly EVENT_STRING: Event = new Event()
        .setName(Event.OUTPUT)
        .setParameters(
            new Map([[this.EVENT_RESULT_NAME, Schema.ofString(this.EVENT_RESULT_NAME)]]),
        );

    private signature: FunctionSignature = new FunctionSignature()
        .setName('InsertAtGivenPosition')
        .setNamespace(Namespaces.STRING)
        .setParameters(
            new Map([
                [this.PARAMETER_STRING.getParameterName(), this.PARAMETER_STRING],
                [this.PARAMETER_AT_POSITION.getParameterName(), this.PARAMETER_AT_POSITION],
                [this.PARAMETER_INSERT_STRING.getParameterName(), this.PARAMETER_INSERT_STRING],
            ]),
        )
        .setEvents(
            new Map([
                Event.outputEventMapEntry(
                    new Map([[this.EVENT_RESULT_NAME, Schema.ofString(this.EVENT_RESULT_NAME)]]),
                ),
            ]),
        );

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let inputString: string = context
            ?.getArguments()
            ?.get(InsertAtGivenPosition.PARAMETER_STRING_NAME);
        let at: number = context
            ?.getArguments()
            ?.get(InsertAtGivenPosition.PARAMETER_AT_POSITION_NAME);
        let insertString: string = context
            ?.getArguments()
            ?.get(InsertAtGivenPosition.PARAMETER_INSERT_STRING_NAME);

        let outputString: string = '';

        outputString += inputString.substring(0, at);
        outputString += insertString;
        outputString += inputString.substring(at);

        return new FunctionOutput([
            EventResult.outputOf(new Map([[this.EVENT_RESULT_NAME, outputString]])),
        ]);
    }
}
