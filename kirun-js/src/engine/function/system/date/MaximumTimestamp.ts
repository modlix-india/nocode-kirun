import { Schema } from "../../../json/schema/Schema";
import { FunctionOutput } from "../../../model/FunctionOutput";
import { FunctionSignature } from "../../../model/FunctionSignature";
import { Parameter } from "../../../model/Parameter";
import { Namespaces } from "../../../namespaces/Namespaces";
import { FunctionExecutionParameters } from "../../../runtime/FunctionExecutionParameters";
import { AbstractFunction } from "../../AbstractFunction";
import { Event } from "../../../model/Event";
import { KIRuntimeException } from "../../../exception/KIRuntimeException";
import { ValidDateTimeUtil } from "../../../util/ValidDateTimeUtil";
import { EventResult } from "../../../model/EventResult";

const VALUE = "isoDates";

const OUTPUT = "result";

const ERROR_MESSAGE = "Please provide a valid date";



const SIGNATURE : FunctionSignature = new FunctionSignature('MaximumTimestamp')
    .setNamespace(Namespaces.DATE)
    .setParameters(new Map([[VALUE, Parameter.of(VALUE, Schema.ofString(VALUE).setRef(Namespaces.DATE + ".timeStamp")).setVariableArgument(true)]]))
    .setEvents(new Map([[OUTPUT, new Event(OUTPUT, new Map([[OUTPUT, Schema.ofString(OUTPUT).setRef(Namespaces.DATE + ".timeStamp")]]))]]));

export class MaximumTimestamp extends AbstractFunction {

    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        
        const dates = context?.getArguments()?.get(VALUE);

        const size = dates.length;

        if(size === 0){
            throw new KIRuntimeException(ERROR_MESSAGE)
        }

        else if (size == 1) {

            const firstDate: string = dates[0];

            if (!ValidDateTimeUtil.validate(firstDate))

                throw new KIRuntimeException(ERROR_MESSAGE);

            return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT, firstDate]]))]);
        }

        let maxIndex: number = 0;
        let max : number = new Date(dates[0]).getTime();

        for(let i=1;i<size;i++){

            const date: string = dates[i];

            if(!ValidDateTimeUtil.validate(date))
                throw new KIRuntimeException(ERROR_MESSAGE);
            
            const current: number = new Date(date).getTime();

            if(current > max){
                max = current;
                maxIndex = i;
            }
        }

        return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT, dates[maxIndex]]]))]);
    }
}