import { KIRuntimeException } from "../../../exception/KIRuntimeException";
import { Schema } from "../../../json/schema/Schema";
import { Event } from '../../../model/Event';
import { EventResult } from "../../../model/EventResult";
import { FunctionOutput } from "../../../model/FunctionOutput";
import { FunctionSignature } from "../../../model/FunctionSignature";
import { Parameter } from "../../../model/Parameter";
import { Namespaces } from "../../../namespaces/Namespaces";
import { FunctionExecutionParameters } from "../../../runtime/FunctionExecutionParameters";
import { MapUtil } from "../../../util/MapUtil";
import { isNullValue } from "../../../util/NullCheck";
import { ValidDateTimeUtil } from "../../../util/ValidDateTimeUtil";
import { AbstractFunction } from "../../AbstractFunction";


const VALUE : string = "isoDate";

const OUTPUT : string = "output";

const SIGNATURE = new FunctionSignature('IsValidISODate')
.setNamespace(Namespaces.DATE)
.setParameters(MapUtil.of(VALUE, Parameter.of(VALUE, Schema.ofString(VALUE))))
.setEvents(MapUtil.of(OUTPUT , new Event(OUTPUT , MapUtil.of(OUTPUT, Schema.ofBoolean(OUTPUT)))));

export class IsValidISODate extends AbstractFunction{

    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        
        var date = context.getArguments()?.get(VALUE);

        console.log(date)

        if(isNullValue(date))
            throw new KIRuntimeException("Please provide a valid date object");

        return new FunctionOutput([ EventResult.of(OUTPUT , MapUtil.of( OUTPUT , ValidDateTimeUtil.validate(date)))]);
        
    }
    

}