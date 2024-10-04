import { KIRuntimeException } from "../../../exception/KIRuntimeException";
import { ArraySchemaType } from "../../../json/schema/array/ArraySchemaType";
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


const VALUE:string = "isoDate";

const OUTPUT:string = "result";

const SIGNATURE = new FunctionSignature('GetTimeAsArray')
    .setNamespace(Namespaces.DATE)
    .setParameters(MapUtil.of(VALUE, Parameter.of(VALUE, Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT , Schema.ofArray(OUTPUT).setItems(ArraySchemaType.of(Schema.ofNumber("number")))]]))]))


export class GetTimeAsArray extends AbstractFunction{
    
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }
    
    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
         
        var inputDate = context.getArguments()?.get(VALUE);

        if(isNullValue(inputDate) || !ValidDateTimeUtil.validate(inputDate))
            throw new KIRuntimeException("Please provide a valid date object");

        const date = new Date(inputDate);

        const outputArray = [ date.getUTCFullYear(), date.getUTCMonth() + 1, date.getUTCDate()
            , date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds(), date.getUTCMilliseconds()];
        
        return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT , outputArray]]))]);
    }

}