import { KIRuntimeException } from "../../../exception/KIRuntimeException";
import { Schema } from "../../../json/schema/Schema";
import { EventResult } from "../../../model/EventResult";
import { FunctionOutput } from "../../../model/FunctionOutput";
import { FunctionSignature } from "../../../model/FunctionSignature";
import { Parameter } from "../../../model/Parameter";
import { Namespaces } from "../../../namespaces/Namespaces";
import { FunctionExecutionParameters } from "../../../runtime/FunctionExecutionParameters";
import { ValidDateTimeUtil } from "../../../util/ValidDateTimeUtil";
import { AbstractFunction } from "../../AbstractFunction";

const DATE_ONE: string = "isoDateOne";
const DATE_TWO: string = "isoDateTwo";
const OUTPUT: string = "result";

const SIGNATURE: FunctionSignature = new FunctionSignature('DifferenceOfTimestamps')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([
            [DATE_ONE, new Parameter(DATE_ONE, Schema.ofString(DATE_ONE).setRef(Namespaces.DATE+".timeStamp"))],
            [DATE_TWO, new Parameter(DATE_TWO, Schema.ofString(DATE_TWO).setRef(Namespaces.DATE+".timeStamp"))]
        ])
    );;

export class DifferenceOfTimestamps extends AbstractFunction {

    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {

        const firstDate:string = context?.getArguments()?.get(DATE_ONE);
        const secondDate:string = context?.getArguments()?.get(DATE_TWO);

        if(!ValidDateTimeUtil.validate(firstDate) || !ValidDateTimeUtil.validate(secondDate)) 
            throw new KIRuntimeException("Please provide valid ISO date for both the given dates.");

        const fDate: Date = new Date(firstDate);
        const sDate: Date = new Date(secondDate);

        return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT, (sDate.getTime() - fDate.getTime())/60000]]))]);
    }

}