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

export class Split extends AbstractFunction {
    protected readonly PARAMETER_STRING_NAME: string = 'string';
    protected readonly PARAMETER_SPLIT_STRING_NAME: string = 'searchString';
    protected readonly EVENT_RESULT_NAME: string = 'result';

    protected readonly PARAMETER_STRING: Parameter = new Parameter()
        .setParameterName(this.PARAMETER_STRING_NAME)
        .setSchema(Schema.ofString(this.PARAMETER_STRING_NAME));

    protected readonly PARAMETER_SPLIT_STRING: Parameter = new Parameter()
        .setParameterName(this.PARAMETER_SPLIT_STRING_NAME)
        .setSchema(Schema.ofString(this.PARAMETER_SPLIT_STRING_NAME));

    protected readonly EVENT_ARRAY: Event = new Event()
        .setName(Event.OUTPUT)
        .setParameters(MapUtil.of(this.EVENT_RESULT_NAME, Schema.ofArray(this.EVENT_RESULT_NAME)));

    public getSignature(): FunctionSignature {
        return new FunctionSignature()
            .setName('Split')
            .setNamespace(Namespaces.STRING)
            .setParameters(
                new Map([
                    [this.PARAMETER_STRING_NAME, this.PARAMETER_STRING],
                    [this.PARAMETER_SPLIT_STRING_NAME, this.PARAMETER_SPLIT_STRING],
                ]),
            )
            .setEvents(
                new Map([
                    Event.outputEventMapEntry(
                        new Map([[this.EVENT_RESULT_NAME, Schema.ofArray(this.EVENT_RESULT_NAME)]]),
                    ) as [string, Event],
                ]),
            );
    }

    public constructor() {
        super();
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let s1: string = context.getArguments()?.get(this.PARAMETER_STRING_NAME);
        let s2: string = context.getArguments()?.get(this.PARAMETER_SPLIT_STRING_NAME);

        return new FunctionOutput([
            EventResult.outputOf(MapUtil.of(this.EVENT_ARRAY.getName(), s1.split(s2))),
        ]);
    }
}
