import { Schema } from "../../../json/schema/Schema";
import { FunctionSignature } from "../../../model/FunctionSignature";
import { Event } from "../../../model/Event";
import { Parameter } from "../../../model/Parameter";
import { Namespaces } from "../../../namespaces/Namespaces";
import { MapUtil } from "../../../util/MapUtil";
import { AbstractFunction } from "../../AbstractFunction";
import { KIRuntimeException } from "../../../exception/KIRuntimeException";
import { EventResult } from "../../../model/EventResult";
import { FunctionOutput } from "../../../model/FunctionOutput";
import { FunctionExecutionParameters } from "../../../runtime/FunctionExecutionParameters";
import { isNullValue } from "../../../util/NullCheck";
import { ValidDateTimeUtil } from "../../../util/ValidDateTimeUtil";
import { Function } from "../../../../engine/function/Function";

export abstract class AbstractDateFunction extends AbstractFunction {

    private signature: FunctionSignature;

    public static readonly EVENT_RESULT_NAME: string = 'result';

    public static readonly PARAMETER_DATE_NAME: string = 'isoDate';

    public static readonly PARAMETER_FIELD_NAME: string = 'value';

    public static readonly PARAMETER_INT_NAME: string = "intValue";

	public static readonly PARAMETER_UNIT_NAME: string  = "unit";
    
    protected static readonly PARAMETER_DATE: Parameter = new Parameter(
        AbstractDateFunction.PARAMETER_DATE_NAME,
        Schema.ofString(this.PARAMETER_DATE_NAME).setRef(Namespaces.DATE + ".timeStamp")
    );

    protected static readonly PARAMETER_FIELD : Parameter = new Parameter(
        AbstractDateFunction.PARAMETER_FIELD_NAME,
        Schema.ofInteger(this.PARAMETER_FIELD_NAME)
    );

    protected static readonly PARAMETER_INT : Parameter = new Parameter(
        AbstractDateFunction.PARAMETER_INT_NAME,
        Schema.ofInteger(this.PARAMETER_INT_NAME)
    );

    protected static readonly PARAMETER_UNIT : Parameter = new Parameter(
        AbstractDateFunction.PARAMETER_UNIT_NAME,
        Schema.ofString(this.PARAMETER_UNIT_NAME)
        .setEnums(["YEAR" , "MONTH" , "DAY" , "HOUR" , "MINUTE" , "SECOND" , "MILLISECOND"])
    );

    protected static readonly EVENT_INT: Event = new Event(
        Event.OUTPUT,
        MapUtil.of(
            AbstractDateFunction.EVENT_RESULT_NAME,
            Schema.ofInteger(AbstractDateFunction.EVENT_RESULT_NAME),
        ),
    );

    protected static readonly EVENT_BOOLEAN: Event = new Event(
        Event.OUTPUT,
        MapUtil.of(
            AbstractDateFunction.EVENT_RESULT_NAME,
            Schema.ofBoolean(AbstractDateFunction.EVENT_RESULT_NAME)
        )
    );

    protected static readonly EVENT_DATE : Event = new Event(
        Event.OUTPUT,
        MapUtil.of(
            AbstractDateFunction.EVENT_RESULT_NAME,
            Schema.ofString(AbstractDateFunction.EVENT_RESULT_NAME).setRef(Namespaces.DATE + ".timeStamp")
        )
    ); 

    public getSignature(): FunctionSignature {
        return this.signature;
    }


    constructor(namespace: string, functionName: string, event: Event, ...parameter: Parameter[]) {

        super();
        const paramMap: Map<string, Parameter> = new Map();
        parameter.forEach((e) => paramMap.set(e.getParameterName(), e));

        this.signature = new FunctionSignature(functionName)
            .setNamespace(namespace)
            .setParameters(paramMap)
            .setEvents(MapUtil.of(event.getName(), event));

    }

    public static ofEntryDateAndBooleanOutput(name: string, fun: ( date : string) => boolean) : [string, Function] {
    
        return [name, new (class extends AbstractDateFunction {

            constructor(namespace: string, functionName: string, event: Event, ...parameter: Parameter[]) {
                super(namespace, functionName, event, ...parameter);
            }

            protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
                
                const date = context.getArguments()?.get(AbstractDateFunction.PARAMETER_DATE_NAME);
                
                if(isNullValue(date) || !ValidDateTimeUtil.validate(date))
                    throw new KIRuntimeException("Please provide a valid date.");

                return new FunctionOutput([EventResult.outputOf(MapUtil.of(AbstractDateFunction.EVENT_RESULT_NAME, fun(date)))]);

            }


        })(Namespaces.DATE, name, AbstractDateFunction.EVENT_BOOLEAN, AbstractDateFunction.PARAMETER_DATE)];
    }

    public static ofEntryDateAndIntegerOutput(name: string, fun: (date: string) => number) : [string, Function] {
        
        return [name, new (class extends AbstractDateFunction {
            constructor(namespace: string, functionName: string, event: Event, ...parameter: Parameter[]) {
                super(namespace, functionName, event, ...parameter);
            }

            protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {

                const date = context.getArguments()?.get(AbstractDateFunction.PARAMETER_DATE_NAME);

                if(isNullValue(date) || !ValidDateTimeUtil.validate(date))
                    throw new KIRuntimeException("Please provide a valid date object");

                return new FunctionOutput([EventResult.outputOf(MapUtil.of( AbstractDateFunction.EVENT_RESULT_NAME, fun(date)))])
            }
        })(Namespaces.DATE, name, AbstractDateFunction.EVENT_INT, AbstractDateFunction.PARAMETER_DATE)];
    }

    public static ofEntryDateWithIntegerUnitWithOutputName( functionName: string, 
        func: (date: string, amount: number, unit: string) => string): [string, Function] {
            return [functionName, new (class extends AbstractDateFunction {
                constructor(namespace: string, functionName: string, event: Event, ...parameter: Parameter[]) {
                    super(namespace, functionName, event, ...parameter);
                }

                protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {

                    const date = context.getArguments()?.get(AbstractDateFunction.PARAMETER_DATE_NAME);
    
                    if(isNullValue(date) || !ValidDateTimeUtil.validate(date))
                        throw new KIRuntimeException("Please provide a valid date object");

                    const value = context.getArguments()?.get(AbstractDateFunction.PARAMETER_INT_NAME);

                    const unit = context.getArguments()?.get(AbstractDateFunction.PARAMETER_UNIT_NAME);
    
                    return new FunctionOutput([EventResult.outputOf(MapUtil.of( AbstractDateFunction.EVENT_RESULT_NAME, func(date, value , unit)))]);

                }
            })(Namespaces.DATE, functionName, AbstractDateFunction.EVENT_DATE,
                 AbstractDateFunction.PARAMETER_DATE, AbstractDateFunction.PARAMETER_INT, AbstractDateFunction.PARAMETER_UNIT)];

          
        }

}