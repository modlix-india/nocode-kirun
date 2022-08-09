import { AbstractFunction, Namespaces, Schema } from '../../../..';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { StringBuilder } from '../../../util/string/StringBuilder';

export class ReplaceAtGivenPosition extends AbstractFunction {
    protected static readonly PARAMETER_STRING_NAME: string = 'string';

    protected static readonly PARAMETER_AT_START_NAME: string = 'startPosition';

    protected static readonly PARAMETER_AT_LENGTH_NAME: string = 'lengthPosition';

    protected static readonly PARAMETER_REPLACE_STRING_NAME: string = 'replaceString';

    protected static readonly EVENT_RESULT_NAME: string = 'result';

    protected static PARAMETER_STRING: Parameter = new Parameter()
        .setParameterName(ReplaceAtGivenPosition.PARAMETER_STRING_NAME)
        .setSchema(Schema.ofString(ReplaceAtGivenPosition.PARAMETER_STRING_NAME));

    protected static PARAMETER_AT_START: Parameter = new Parameter()
        .setParameterName(ReplaceAtGivenPosition.PARAMETER_AT_START_NAME)
        .setSchema(Schema.ofInteger(ReplaceAtGivenPosition.PARAMETER_AT_START_NAME));

    protected static PARAMETER_AT_LENGTH: Parameter = new Parameter()
        .setParameterName(ReplaceAtGivenPosition.PARAMETER_AT_LENGTH_NAME)
        .setSchema(Schema.ofInteger(ReplaceAtGivenPosition.PARAMETER_AT_LENGTH_NAME));

    protected static PARAMETER_REPLACE_STRING: Parameter = new Parameter()
        .setParameterName(ReplaceAtGivenPosition.PARAMETER_REPLACE_STRING_NAME)
        .setSchema(Schema.ofString(ReplaceAtGivenPosition.PARAMETER_REPLACE_STRING_NAME));

    protected static EVENT_STRING: Event = new Event()
        .setName(Event.OUTPUT)
        .setParameters(
            new Map([
                [
                    ReplaceAtGivenPosition.EVENT_RESULT_NAME,
                    Schema.ofString(ReplaceAtGivenPosition.EVENT_RESULT_NAME),
                ],
            ]),
        );

    private signature: FunctionSignature = new FunctionSignature()
        .setName('ReplaceAtGivenPosition')
        .setNamespace(Namespaces.STRING)
        .setParameters(
            new Map([
                [
                    ReplaceAtGivenPosition.PARAMETER_STRING.getParameterName(),
                    ReplaceAtGivenPosition.PARAMETER_STRING,
                ],
                [
                    ReplaceAtGivenPosition.PARAMETER_AT_START.getParameterName(),
                    ReplaceAtGivenPosition.PARAMETER_AT_START,
                ],
                [
                    ReplaceAtGivenPosition.PARAMETER_AT_LENGTH.getParameterName(),
                    ReplaceAtGivenPosition.PARAMETER_AT_LENGTH,
                ],
                [
                    ReplaceAtGivenPosition.PARAMETER_REPLACE_STRING.getParameterName(),
                    ReplaceAtGivenPosition.PARAMETER_REPLACE_STRING,
                ],
            ]),
        )
        .setEvents(
            new Map([
                [
                    ReplaceAtGivenPosition.EVENT_STRING.getName(),
                    ReplaceAtGivenPosition.EVENT_STRING,
                ],
            ]),
        );

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let inputString: string = context
            .getArguments()
            .get(ReplaceAtGivenPosition.PARAMETER_STRING_NAME);
        let startPosition: number = context
            .getArguments()
            .get(ReplaceAtGivenPosition.PARAMETER_AT_START_NAME);
        let length: number = context
            .getArguments()
            .get(ReplaceAtGivenPosition.PARAMETER_AT_LENGTH_NAME);
        let replaceString: string = context
            .getArguments()
            .get(ReplaceAtGivenPosition.PARAMETER_REPLACE_STRING_NAME);
        let inputStringLength: number = inputString.length;

        if (startPosition < length) {
            let outputString: string = '';
            outputString += inputString.substring(0, startPosition);
            outputString += replaceString;
            outputString += inputString.substring(startPosition + length);
        }

        return new FunctionOutput([
            EventResult.outputOf(
                new Map([[ReplaceAtGivenPosition.EVENT_RESULT_NAME, inputString]]),
            ),
        ]);
    }
}
