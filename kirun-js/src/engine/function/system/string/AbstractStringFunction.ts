import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Function } from '../../Function';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractFunction } from '../../AbstractFunction';

export abstract class AbstractStringFunction extends AbstractFunction {
    public static readonly PARAMETER_STRING_NAME: string = 'string';

    public static readonly PARAMETER_SEARCH_STRING_NAME: string = 'searchString';

    public static readonly PARAMETER_SECOND_STRING_NAME: string = 'secondString';

    public static readonly PARAMETER_THIRD_STRING_NAME: string = 'thirdString';

    public static readonly PARAMETER_INDEX_NAME: string = 'index';

    public static readonly PARAMETER_SECOND_INDEX_NAME: string = 'secondIndex';

    public static readonly EVENT_RESULT_NAME: string = 'result';

    protected static readonly PARAMETER_STRING: Parameter =new Parameter(AbstractStringFunction.PARAMETER_STRING_NAME,Schema.ofString(AbstractStringFunction.PARAMETER_STRING_NAME));

    protected static readonly PARAMETER_SECOND_STRING: Parameter =new Parameter(AbstractStringFunction.PARAMETER_SECOND_STRING_NAME,Schema.ofString(AbstractStringFunction.PARAMETER_SECOND_STRING_NAME));

    protected static readonly PARAMETER_THIRD_STRING: Parameter =new Parameter(AbstractStringFunction.PARAMETER_THIRD_STRING_NAME,Schema.ofString(AbstractStringFunction.PARAMETER_THIRD_STRING_NAME));

    protected static readonly PARAMETER_INDEX: Parameter =new Parameter(AbstractStringFunction.PARAMETER_INDEX_NAME,Schema.ofInteger(AbstractStringFunction.PARAMETER_INDEX_NAME));

    protected static readonly PARAMETER_SECOND_INDEX: Parameter =new Parameter(AbstractStringFunction.PARAMETER_SECOND_INDEX_NAME,Schema.ofInteger(AbstractStringFunction.PARAMETER_SECOND_INDEX_NAME));

    protected static readonly PARAMETER_SEARCH_STRING: Parameter =new Parameter(AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME,Schema.ofString(AbstractStringFunction.PARAMETER_STRING_NAME));

    protected static readonly EVENT_STRING: Event =new Event(Event.OUTPUT,
            MapUtil.of(
                AbstractStringFunction.EVENT_RESULT_NAME,
                Schema.ofString(AbstractStringFunction.EVENT_RESULT_NAME),
            ),
        );

    protected static readonly EVENT_BOOLEAN: Event =new Event(Event.OUTPUT,
            MapUtil.of(
                AbstractStringFunction.EVENT_RESULT_NAME,
                Schema.ofBoolean(AbstractStringFunction.EVENT_RESULT_NAME),
            ),
        );

    protected static readonly EVENT_INT: Event =new Event(Event.OUTPUT,
            MapUtil.of(
                AbstractStringFunction.EVENT_RESULT_NAME,
                Schema.ofInteger(AbstractStringFunction.EVENT_RESULT_NAME),
            ),
        );

    protected static readonly EVENT_ARRAY: Event =new Event(Event.OUTPUT,
            MapUtil.of(
                AbstractStringFunction.EVENT_RESULT_NAME,
                Schema.ofArray(AbstractStringFunction.EVENT_RESULT_NAME),
            ),
        );

    private signature: FunctionSignature;

    constructor(namespace: string, functionName: string, event: Event, ...parameter: Parameter[]) {
        super();
        const paramMap: Map<string, Parameter> = new Map();
        parameter.forEach((e) => paramMap.set(e.getParameterName(), e));

        this.signature = new FunctionSignature(functionName)
            .setNamespace(namespace)
            .setParameters(paramMap)
            .setEvents(MapUtil.of(event.getName(), event));
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    public static ofEntryAsStringBooleanOutput(
        name: string,
        fun: (a: string, b: string) => boolean,
    ): [string, Function] {
        return [
            name,
            new (class extends AbstractStringFunction {
                constructor(
                    namespace: string,
                    functionName: string,
                    event: Event,
                    ...parameter: Parameter[]
                ) {
                    super(namespace, functionName, event, ...parameter);
                }

                protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
                    let s: string = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_STRING_NAME);
                    let ss: string = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME);

                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(AbstractStringFunction.EVENT_RESULT_NAME, fun(s, ss)),
                        ),
                    ]);
                }
            })(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_BOOLEAN,
                AbstractStringFunction.PARAMETER_STRING,
                AbstractStringFunction.PARAMETER_SEARCH_STRING,
            ),
        ];
    }

    public static ofEntryAsStringAndIntegerStringOutput(
        name: string,
        fun: (a: string, b: number) => string,
    ): [string, Function] {
        return [
            name,
            new (class extends AbstractStringFunction {
                constructor(
                    namespace: string,
                    functionName: string,
                    event: Event,
                    ...parameter: Parameter[]
                ) {
                    super(namespace, functionName, event, ...parameter);
                }

                protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
                    let s: string = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_STRING_NAME);
                    let count: number = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_INDEX_NAME);

                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(AbstractStringFunction.EVENT_RESULT_NAME, fun(s, count)),
                        ),
                    ]);
                }
            })(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_STRING,
                AbstractStringFunction.PARAMETER_STRING,
                AbstractStringFunction.PARAMETER_INDEX,
            ),
        ];
    }

    public static ofEntryAsStringIntegerOutput(
        name: string,
        fun: (a: string, b: string) => number,
    ): [string, Function] {
        return [
            name,
            new (class extends AbstractStringFunction {
                constructor(
                    namespace: string,
                    functionName: string,
                    event: Event,
                    ...parameter: Parameter[]
                ) {
                    super(namespace, functionName, event, ...parameter);
                }

                protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
                    let s1: string = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_STRING_NAME);
                    let s2: string = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME);

                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(AbstractStringFunction.EVENT_RESULT_NAME, fun(s1, s2)),
                        ),
                    ]);
                }
            })(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_INT,
                AbstractStringFunction.PARAMETER_STRING,
                AbstractStringFunction.PARAMETER_SEARCH_STRING,
            ),
        ];
    }

    public static ofEntryString(name: string, fun: (a: string) => string): [string, Function] {
        return [
            name,
            new (class extends AbstractStringFunction {
                constructor(
                    namespace: string,
                    functionName: string,
                    event: Event,
                    ...parameter: Parameter[]
                ) {
                    super(namespace, functionName, event, ...parameter);
                }

                protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
                    let s: string = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_STRING_NAME);

                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(AbstractStringFunction.EVENT_RESULT_NAME, fun(s)),
                        ),
                    ]);
                }
            })(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_STRING,
                AbstractStringFunction.PARAMETER_STRING,
            ),
        ];
    }
    public static ofEntryStringBooleanOutput(
        name: string,
        fun: (a: string) => boolean,
    ): [string, Function] {
        return [
            name,
            new (class extends AbstractStringFunction {
                constructor(
                    namespace: string,
                    functionName: string,
                    event: Event,
                    ...parameter: Parameter[]
                ) {
                    super(namespace, functionName, event, ...parameter);
                }

                protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
                    let s: string = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_STRING_NAME);

                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(AbstractStringFunction.EVENT_RESULT_NAME, fun(s)),
                        ),
                    ]);
                }
            })(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_BOOLEAN,
                AbstractStringFunction.PARAMETER_STRING,
            ),
        ];
    }

    public static ofEntryAsStringStringIntegerIntegerOutput(
        name: string,
        fun: (a: string, b: string, c: number) => number,
    ): [string, Function] {
        return [
            name,
            new (class extends AbstractStringFunction {
                constructor(
                    namespace: string,
                    functionName: string,
                    event: Event,
                    ...parameter: Parameter[]
                ) {
                    super(namespace, functionName, event, ...parameter);
                }

                protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
                    let s: string = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_STRING_NAME);
                    let ss: string = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME);

                    let ind: number = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_INDEX_NAME);

                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(AbstractStringFunction.EVENT_RESULT_NAME, fun(s, ss, ind)),
                        ),
                    ]);
                }
            })(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_INT,
                AbstractStringFunction.PARAMETER_STRING,
                AbstractStringFunction.PARAMETER_SEARCH_STRING,
                AbstractStringFunction.PARAMETER_INDEX,
            ),
        ];
    }

    public static ofEntryAsStringIntegerIntegerStringOutput(
        name: string,
        fun: (a: string, b: number, c: number) => string,
    ): [string, Function] {
        return [
            name,
            new (class extends AbstractStringFunction {
                constructor(
                    namespace: string,
                    functionName: string,
                    event: Event,
                    ...parameter: Parameter[]
                ) {
                    super(namespace, functionName, event, ...parameter);
                }

                protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
                    let s: string = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_STRING_NAME);
                    let ind1: number = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_INDEX_NAME);

                    let ind2: number = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_SECOND_INDEX_NAME);

                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(
                                AbstractStringFunction.EVENT_RESULT_NAME,
                                fun(s, ind1, ind2),
                            ),
                        ),
                    ]);
                }
            })(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_INT,
                AbstractStringFunction.PARAMETER_STRING,
                AbstractStringFunction.PARAMETER_INDEX,
                AbstractStringFunction.PARAMETER_SECOND_INDEX,
            ),
        ];
    }

    public static ofEntryAsStringStringStringStringOutput(
        name: string,
        fun: (a: string, b: string, c: string) => string,
    ): [string, Function] {
        return [
            name,
            new (class extends AbstractStringFunction {
                constructor(
                    namespace: string,
                    functionName: string,
                    event: Event,
                    ...parameter: Parameter[]
                ) {
                    super(namespace, functionName, event, ...parameter);
                }

                protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
                    let s1: string = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_STRING_NAME);
                    let s2: string = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_SECOND_STRING_NAME);
                    let s3: string = context
                        ?.getArguments()
                        ?.get(AbstractStringFunction.PARAMETER_THIRD_STRING_NAME);

                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(AbstractStringFunction.EVENT_RESULT_NAME, fun(s1, s2, s3)),
                        ),
                    ]);
                }
            })(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_STRING,
                AbstractStringFunction.PARAMETER_STRING,
                AbstractStringFunction.PARAMETER_SECOND_STRING,
                AbstractStringFunction.PARAMETER_THIRD_STRING,
            ),
        ];
    }
}
