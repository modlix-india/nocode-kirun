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
import { Function } from '../../Function';

export abstract class AbstractDateFunction extends AbstractFunction {
    private readonly signature: FunctionSignature;

    public static readonly PARAMETER_TIMESTAMP_NAME: string = 'isoTimeStamp';

    public static readonly PARAMETER_TIMESTAMP_NAME_ONE: string = 'isoTimeStamp1';
    public static readonly PARAMETER_TIMESTAMP_NAME_TWO: string = 'isoTimeStamp2';

    public static readonly PARAMETER_UNIT_NAME: string = 'unit';

    public static readonly PARAMETER_NUMBER_NAME: string = 'number';

    public static readonly PARAMETER_TIMESTAMP: Parameter = new Parameter(
        AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
        Schema.ofRef(Namespaces.DATE + '.Timestamp'),
    );

    public static readonly PARAMETER_TIMESTAMP_ONE: Parameter = new Parameter(
        AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_ONE,
        Schema.ofRef(Namespaces.DATE + '.Timestamp'),
    );

    public static readonly PARAMETER_TIMESTAMP_TWO: Parameter = new Parameter(
        AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_TWO,
        Schema.ofRef(Namespaces.DATE + '.Timestamp'),
    );

    public static readonly PARAMETER_VARIABLE_UNIT: Parameter = new Parameter(
        AbstractDateFunction.PARAMETER_UNIT_NAME,
        Schema.ofRef(Namespaces.DATE + '.Timeunit'),
    ).setVariableArgument(true);

    public static readonly PARAMETER_UNIT: Parameter = new Parameter(
        AbstractDateFunction.PARAMETER_UNIT_NAME,
        Schema.ofRef(Namespaces.DATE + '.Timeunit'),
    );

    public static readonly PARAMETER_NUMBER: Parameter = new Parameter(
        AbstractDateFunction.PARAMETER_NUMBER_NAME,
        Schema.ofInteger(AbstractDateFunction.PARAMETER_NUMBER_NAME),
    );

    public static readonly EVENT_RESULT_NAME: string = 'result';
    public static readonly EVENT_TIMESTAMP_NAME: string = 'isoTimeStamp';

    public static readonly EVENT_INT: Event = new Event(
        Event.OUTPUT,
        MapUtil.of(
            AbstractDateFunction.EVENT_RESULT_NAME,
            Schema.ofInteger(AbstractDateFunction.EVENT_RESULT_NAME),
        ),
    );

    public static readonly EVENT_STRING: Event = new Event(
        Event.OUTPUT,
        MapUtil.of(
            AbstractDateFunction.EVENT_RESULT_NAME,
            Schema.ofString(AbstractDateFunction.EVENT_RESULT_NAME),
        ),
    );

    public static readonly EVENT_LONG: Event = new Event(
        Event.OUTPUT,
        MapUtil.of(
            AbstractDateFunction.EVENT_RESULT_NAME,
            Schema.ofLong(AbstractDateFunction.EVENT_RESULT_NAME),
        ),
    );

    protected static readonly EVENT_BOOLEAN: Event = new Event(
        Event.OUTPUT,
        MapUtil.of(
            AbstractDateFunction.EVENT_RESULT_NAME,
            Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME),
        ),
    );

    public static readonly EVENT_TIMESTAMP: Event = new Event(
        Event.OUTPUT,
        MapUtil.of(
            AbstractDateFunction.EVENT_TIMESTAMP_NAME,
            Schema.ofRef(Namespaces.DATE + '.Timestamp'),
        ),
    );

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    constructor(functionName: string, event: Event, ...parameter: Parameter[]) {
        super();

        this.signature = new FunctionSignature(functionName)
            .setNamespace(Namespaces.DATE)
            .setEvents(MapUtil.of(event.getName(), event));

        if (!parameter?.length) return;

        const paramMap: Map<string, Parameter> = new Map();
        parameter.forEach((e) => paramMap.set(e.getParameterName(), e));
        this.signature.setParameters(paramMap);
    }

    public static ofEntryTimestampAndIntegerOutput(
        name: string,
        fun: (isoTimestamp: string) => number,
    ): [string, Function] {
        return [
            name,
            new (class extends AbstractDateFunction {
                protected async internalExecute(
                    context: FunctionExecutionParameters,
                ): Promise<FunctionOutput> {
                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(
                                AbstractDateFunction.EVENT_RESULT_NAME,
                                fun(
                                    context
                                        .getArguments()
                                        ?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME),
                                ),
                            ),
                        ),
                    ]);
                }
            })(name, AbstractDateFunction.EVENT_INT, AbstractDateFunction.PARAMETER_TIMESTAMP),
        ];
    }

    public static ofEntryTimestampAndBooleanOutput(
        name: string,
        fun: (isoTimestamp: string) => boolean,
    ): [string, Function] {
        return [
            name,
            new (class extends AbstractDateFunction {
                protected async internalExecute(
                    context: FunctionExecutionParameters,
                ): Promise<FunctionOutput> {
                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(
                                AbstractDateFunction.EVENT_RESULT_NAME,
                                fun(
                                    context
                                        .getArguments()
                                        ?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME),
                                ),
                            ),
                        ),
                    ]);
                }
            })(name, AbstractDateFunction.EVENT_BOOLEAN, AbstractDateFunction.PARAMETER_TIMESTAMP),
        ];
    }

    public static ofEntryTimestampAndStringOutput(
        name: string,
        fun: (isoTimestamp: string) => string,
    ): [string, Function] {
        return [
            name,
            new (class extends AbstractDateFunction {
                protected async internalExecute(
                    context: FunctionExecutionParameters,
                ): Promise<FunctionOutput> {
                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(
                                AbstractDateFunction.EVENT_RESULT_NAME,
                                fun(
                                    context
                                        .getArguments()
                                        ?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME),
                                ),
                            ),
                        ),
                    ]);
                }
            })(name, AbstractDateFunction.EVENT_STRING, AbstractDateFunction.PARAMETER_TIMESTAMP),
        ];
    }

    public static ofEntryTimestampIntegerAndTimestampOutput(
        name: string,
        fun: (isoTimestamp: string, integer: number) => string,
    ): [string, Function] {
        return [
            name,
            new (class extends AbstractDateFunction {
                protected async internalExecute(
                    context: FunctionExecutionParameters,
                ): Promise<FunctionOutput> {
                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(
                                AbstractDateFunction.EVENT_RESULT_NAME,
                                fun(
                                    context
                                        .getArguments()
                                        ?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME),
                                    context
                                        .getArguments()
                                        ?.get(AbstractDateFunction.PARAMETER_NUMBER_NAME),
                                ),
                            ),
                        ),
                    ]);
                }
            })(
                name,
                AbstractDateFunction.EVENT_TIMESTAMP,
                AbstractDateFunction.PARAMETER_TIMESTAMP,
                AbstractDateFunction.PARAMETER_NUMBER,
            ),
        ];
    }

    public static ofEntryTimestampTimestampAndTOutput<T>(
        name: string,
        event: Event,
        fun: (ts1: string, ts2: string, ...parameters: any[]) => T,
        ...parameters: Parameter[]
    ): [string, Function] {
        return [
            name,
            new (class extends AbstractDateFunction {
                protected async internalExecute(
                    context: FunctionExecutionParameters,
                ): Promise<FunctionOutput> {
                    const args = [];

                    if (parameters?.length) {
                        args.push(
                            ...parameters.map((e) =>
                                context.getArguments()?.get(e.getParameterName()),
                            ),
                        );
                    }

                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(
                                AbstractDateFunction.EVENT_RESULT_NAME,
                                fun(
                                    context
                                        .getArguments()
                                        ?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_ONE),
                                    context
                                        .getArguments()
                                        ?.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_TWO),
                                    args,
                                ),
                            ),
                        ),
                    ]);
                }
            })(
                name,
                event,
                AbstractDateFunction.PARAMETER_TIMESTAMP_ONE,
                AbstractDateFunction.PARAMETER_TIMESTAMP_TWO,
                ...parameters,
            ),
        ];
    }
}
