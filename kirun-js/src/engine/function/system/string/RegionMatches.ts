import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

export class RegionMatches extends AbstractFunction {
    public static readonly PARAMETER_STRING_NAME: string = 'string';

    public static readonly PARAMETER_BOOLEAN_NAME: string = 'boolean';

    public static readonly PARAMETER_FIRST_OFFSET_NAME: string = 'firstOffset';

    public static readonly PARAMETER_OTHER_STRING_NAME: string = 'otherString';

    public static readonly PARAMETER_SECOND_OFFSET_NAME: string = 'secondOffset';

    public static readonly PARAMETER_INTEGER_NAME: string = 'length';

    public static readonly EVENT_RESULT_NAME: string = 'result';

    public static PARAMETER_STRING: Parameter = new Parameter(
        RegionMatches.PARAMETER_STRING_NAME,
        Schema.ofString(RegionMatches.PARAMETER_STRING_NAME),
    );

    protected static PARAMETER_OTHER_STRING: Parameter = new Parameter(
        RegionMatches.PARAMETER_OTHER_STRING_NAME,
        Schema.ofString(RegionMatches.PARAMETER_OTHER_STRING_NAME),
    );

    protected static PARAMETER_FIRST_OFFSET: Parameter = new Parameter(
        RegionMatches.PARAMETER_FIRST_OFFSET_NAME,
        Schema.ofInteger(RegionMatches.PARAMETER_FIRST_OFFSET_NAME),
    );

    protected static PARAMETER_SECOND_OFFSET: Parameter = new Parameter(
        RegionMatches.PARAMETER_SECOND_OFFSET_NAME,
        Schema.ofInteger(RegionMatches.PARAMETER_SECOND_OFFSET_NAME),
    );

    protected static PARAMETER_INTEGER: Parameter = new Parameter(
        RegionMatches.PARAMETER_INTEGER_NAME,
        Schema.ofInteger(RegionMatches.PARAMETER_INTEGER_NAME),
    );

    protected static PARAMETER_BOOLEAN: Parameter = new Parameter(
        RegionMatches.PARAMETER_BOOLEAN_NAME,
        Schema.ofBoolean(RegionMatches.PARAMETER_BOOLEAN_NAME),
    );

    protected static EVENT_BOOLEAN: Event = new Event(
        Event.OUTPUT,
        new Map([
            [RegionMatches.EVENT_RESULT_NAME, Schema.ofBoolean(RegionMatches.EVENT_RESULT_NAME)],
        ]),
    );

    private signature: FunctionSignature = new FunctionSignature('RegionMatches')
        .setNamespace(Namespaces.STRING)
        .setParameters(
            new Map([
                [RegionMatches.PARAMETER_STRING.getParameterName(), RegionMatches.PARAMETER_STRING],
                [
                    RegionMatches.PARAMETER_BOOLEAN.getParameterName(),
                    RegionMatches.PARAMETER_BOOLEAN,
                ],
                [
                    RegionMatches.PARAMETER_FIRST_OFFSET.getParameterName(),
                    RegionMatches.PARAMETER_FIRST_OFFSET,
                ],
                [
                    RegionMatches.PARAMETER_OTHER_STRING.getParameterName(),
                    RegionMatches.PARAMETER_OTHER_STRING,
                ],
                [
                    RegionMatches.PARAMETER_SECOND_OFFSET.getParameterName(),
                    RegionMatches.PARAMETER_SECOND_OFFSET,
                ],
                [
                    RegionMatches.PARAMETER_INTEGER.getParameterName(),
                    RegionMatches.PARAMETER_INTEGER,
                ],
            ]),
        )
        .setEvents(new Map([[RegionMatches.EVENT_BOOLEAN.getName(), RegionMatches.EVENT_BOOLEAN]]));

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    public constructor() {
        super();
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let inputString: string = context.getArguments()?.get(RegionMatches.PARAMETER_STRING_NAME);
        let ignoreCase: boolean = context.getArguments()?.get(RegionMatches.PARAMETER_BOOLEAN_NAME);
        let toffSet: number = context
            .getArguments()
            ?.get(RegionMatches.PARAMETER_FIRST_OFFSET_NAME);
        let otherString: string = context
            ?.getArguments()
            ?.get(RegionMatches.PARAMETER_OTHER_STRING_NAME);
        let oOffSet: number = context
            ?.getArguments()
            ?.get(RegionMatches.PARAMETER_SECOND_OFFSET_NAME);
        let length: number = context.getArguments()?.get(RegionMatches.PARAMETER_INTEGER_NAME);

        let matches: boolean = false;

        if (
            toffSet < 0 ||
            oOffSet < 0 ||
            toffSet + length > inputString.length ||
            oOffSet + length > otherString.length
        )
            matches = false;
        else if (ignoreCase) {
            inputString = inputString.substring(toffSet, toffSet + length).toUpperCase();
            let s2: string = otherString.substring(oOffSet, oOffSet + length).toUpperCase();
            matches = inputString == s2;
        } else {
            inputString = inputString.substring(toffSet, toffSet + length);
            let s2: string = otherString.substring(oOffSet, length);
            matches = inputString == s2;
        }

        return new FunctionOutput([
            EventResult.outputOf(new Map([[RegionMatches.EVENT_RESULT_NAME, matches]])),
        ]);
    }
}
