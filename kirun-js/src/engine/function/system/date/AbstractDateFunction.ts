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
    
    protected static readonly PARAMETER_DATE: Parameter = new Parameter(
        AbstractDateFunction.PARAMETER_DATE_NAME,
        Schema.ofString(this.PARAMETER_DATE_NAME).setRef(Namespaces.DATE + ".timeStamp")
    );

    protected static readonly PARAMETER_FIELD : Parameter = new Parameter(
        AbstractDateFunction.PARAMETER_FIELD_NAME,
        Schema.ofInteger(this.PARAMETER_FIELD_NAME)
    );

    protected static readonly EVENT_INT: Event = new Event(
        Event.OUTPUT,
        MapUtil.of(
            AbstractDateFunction.EVENT_RESULT_NAME,
            Schema.ofInteger(AbstractDateFunction.EVENT_RESULT_NAME),
        ),
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


        })(Namespaces.DATE, name, AbstractDateFunction.EVENT_INT, AbstractDateFunction.PARAMETER_DATE)];
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

}
