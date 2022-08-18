import { Schema } from '../../../json/schema/Schema';
import { SchemaType } from '../../../json/schema/type/SchemaType';
import { TypeUtil } from '../../../json/schema/type/TypeUtil';
import { Event } from '../../../model/Event';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractFunction } from '../../AbstractFunction';

export abstract class AbstractArrayFunction extends AbstractFunction {
    public static readonly EVENT_INDEX_NAME: string = 'index';
    public static readonly EVENT_RESULT_NAME: string = 'result';

    public static readonly EVENT_INDEX: Event = new Event()
        .setName(Event.OUTPUT)
        .setParameters(
            MapUtil.of(
                AbstractArrayFunction.EVENT_INDEX_NAME,
                Schema.ofInteger(AbstractArrayFunction.EVENT_INDEX_NAME),
            ),
        );

    public static readonly EVENT_RESULT_INTEGER: Event = new Event()
        .setName(Event.OUTPUT)
        .setParameters(
            MapUtil.of(
                AbstractArrayFunction.EVENT_RESULT_NAME,
                Schema.ofInteger(AbstractArrayFunction.EVENT_RESULT_NAME),
            ),
        );

    public static readonly EVENT_RESULT_BOOLEAN: Event = new Event()
        .setName(Event.OUTPUT)
        .setParameters(
            MapUtil.of(
                AbstractArrayFunction.EVENT_RESULT_NAME,
                Schema.ofBoolean(AbstractArrayFunction.EVENT_RESULT_NAME),
            ),
        );

    public static readonly EVENT_RESULT_ARRAY: Event = new Event()
        .setName(Event.OUTPUT)
        .setParameters(
            MapUtil.of(
                AbstractArrayFunction.EVENT_RESULT_NAME,
                Schema.ofArray(
                    AbstractArrayFunction.EVENT_RESULT_NAME,
                    Schema.ofAny(AbstractArrayFunction.EVENT_RESULT_NAME),
                ),
            ),
        );

    public static readonly EVENT_RESULT_EMPTY: Event = new Event()
        .setName(Event.OUTPUT)
        .setParameters(MapUtil.of());

    public static readonly EVENT_RESULT_ANY: Event = new Event()
        .setName(Event.OUTPUT)
        .setParameters(MapUtil.of(this.EVENT_RESULT_NAME, Schema.ofAny(this.EVENT_RESULT_NAME)));

    public static readonly PARAMETER_INT_LENGTH: Parameter = Parameter.of(
        'length',
        Schema.ofInteger('length').setDefaultValue(-1),
    );
    public static readonly PARAMETER_ARRAY_FIND: Parameter = Parameter.of(
        'find',
        Schema.ofArray('eachFind', Schema.ofAny('eachFind')),
    );
    public static readonly PARAMETER_INT_SOURCE_FROM: Parameter = Parameter.of(
        'srcFrom',
        Schema.ofInteger('srcFrom').setDefaultValue(0).setMinimum(0),
    );
    public static readonly PARAMETER_INT_SECOND_SOURCE_FROM: Parameter = Parameter.of(
        'secondSrcFrom',
        Schema.ofInteger('secondSrcFrom').setDefaultValue(0),
    );
    public static readonly PARAMETER_INT_FIND_FROM: Parameter = Parameter.of(
        'findFrom',
        Schema.ofInteger('findFrom').setDefaultValue(0),
    );

    public static readonly PARAMETER_INT_OFFSET: Parameter = Parameter.of(
        'offset',
        Schema.ofInteger('offset').setDefaultValue(0),
    );

    public static readonly PARAMETER_ROTATE_LENGTH: Parameter = Parameter.of(
        'rotateLength',
        Schema.ofInteger('rotateLength').setDefaultValue(1).setMinimum(1),
    );

    public static readonly PARAMETER_BOOLEAN_ASCENDING: Parameter = Parameter.of(
        'ascending',
        Schema.ofBoolean('ascending').setDefaultValue(false),
    );

    public static readonly PARAMETER_ARRAY_SOURCE: Parameter = Parameter.of(
        'source',
        Schema.ofArray('eachSource', Schema.ofAny('eachSource')),
    );

    public static readonly PARAMETER_ARRAY_SECOND_SOURCE: Parameter = Parameter.of(
        'secondSource',
        Schema.ofArray('eachSecondSource', Schema.ofAny('eachSecondSource')),
    );

    public static readonly PARAMETER_ARRAY_SOURCE_PRIMITIVE: Parameter = Parameter.of(
        'source',
        Schema.ofArray(
            'eachSource',
            new Schema()
                .setName('eachSource')
                .setType(
                    TypeUtil.of(
                        SchemaType.STRING,
                        SchemaType.NULL,
                        SchemaType.INTEGER,
                        SchemaType.FLOAT,
                        SchemaType.DOUBLE,
                        SchemaType.LONG,
                    ),
                ),
        ),
    );

    public static readonly PARAMETER_BOOLEAN_DEEP_COPY: Parameter = Parameter.of(
        'deepCopy',
        Schema.ofBoolean('deepCopy').setDefaultValue(true),
    );
    public static readonly PARAMETER_ANY: Parameter = Parameter.of(
        'element',
        Schema.ofAny('element'),
    );

    public static readonly PARAMETER_ANY_NOT_NULL: Parameter = Parameter.of(
        'elementObject',
        Schema.ofAnyNotNull('elementObject'),
    );

    public static readonly PARAMETER_ARRAY_RESULT: Parameter = Parameter.of(
        AbstractArrayFunction.EVENT_RESULT_NAME,
        Schema.ofArray('eachResult', Schema.ofAny('eachResult')),
    );

    private signature: FunctionSignature;

    protected constructor(functionName: string, parameters: Parameter[], event: Event) {
        super();

        const paramMap: Map<string, Parameter> = new Map();
        for (const param of parameters) paramMap.set(param.getParameterName(), param);

        this.signature = new FunctionSignature()
            .setNamespace(Namespaces.SYSTEM_ARRAY)
            .setName(functionName)
            .setParameters(paramMap)
            .setEvents(MapUtil.of(event.getName(), event));
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }
}
