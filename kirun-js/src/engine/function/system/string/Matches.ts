import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractFunction } from '../../AbstractFunction';

export class Matches extends AbstractFunction {
    protected static PARAMETER_REGEX_NAME: string = 'regex';

    protected static PARAMETER_STRING_NAME: string = 'string';

    protected static EVENT_RESULT_NAME: string = 'result';

    private readonly signature: FunctionSignature = new FunctionSignature('Matches')
        .setNamespace(Namespaces.STRING)
        .setParameters(
            MapUtil.ofEntries(
                MapUtil.entry(
                    ...Parameter.ofEntry(
                        Matches.PARAMETER_REGEX_NAME,
                        Schema.ofString(Matches.PARAMETER_REGEX_NAME),
                    ),
                ),
                MapUtil.entry(
                    ...Parameter.ofEntry(
                        Matches.PARAMETER_STRING_NAME,
                        Schema.ofString(Matches.PARAMETER_STRING_NAME),
                    ),
                ),
            ),
        )
        .setEvents(
            MapUtil.ofEntries(
                MapUtil.entry(
                    ...Event.outputEventMapEntry(
                        new Map([
                            [
                                Matches.EVENT_RESULT_NAME,
                                Schema.ofBoolean(Matches.EVENT_RESULT_NAME),
                            ],
                        ]),
                    ),
                ),
            ),
        );

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let regexPat: string = context.getArguments()?.get(Matches.PARAMETER_REGEX_NAME)!;

        let inputString: string = context.getArguments()?.get(Matches.PARAMETER_STRING_NAME)!;

        return new FunctionOutput([
            EventResult.outputOf(
                new Map([[Matches.EVENT_RESULT_NAME, !!inputString.match(regexPat)?.length]]),
            ),
        ]);
    }
}
