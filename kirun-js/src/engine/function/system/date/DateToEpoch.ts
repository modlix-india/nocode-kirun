import { Schema } from "../../../json/schema/Schema";
import { FunctionOutput } from "../../../model/FunctionOutput";
import { FunctionSignature } from "../../../model/FunctionSignature";
import { Event } from '../../../model/Event';
import { Parameter } from "../../../model/Parameter";
import { Namespaces } from "../../../namespaces/Namespaces";
import { FunctionExecutionParameters } from "../../../runtime/FunctionExecutionParameters";
import { MapUtil } from "../../../util/MapUtil";
import { AbstractFunction } from "../../AbstractFunction";
import { isNullValue } from "../../../util/NullCheck";
import { KIRuntimeException } from "../../../exception/KIRuntimeException";
import { EventResult } from "../../../model/EventResult";
import { ValidDateTimeUtil } from "../../../util/ValidDateTimeUtil";

const OUTPUT : string = "result";

const VALUE : string = "isoDate";

export class DateToEpoch extends AbstractFunction{

    public getSignature(): FunctionSignature {

        return  new FunctionSignature('DateToEpoch')
        .setNamespace(Namespaces.DATE)
        .setParameters(MapUtil.of(VALUE, Parameter.of(VALUE,Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
        .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofLong(OUTPUT)]]))]));
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {

        var date = context.getArguments()?.get(VALUE);

        if(isNullValue(date) || !ValidDateTimeUtil.validate(date))
            throw new KIRuntimeException("Please provide a valid date object");

        const epochMillis = new Date(date).getTime();
        return new FunctionOutput([ EventResult.of(OUTPUT , MapUtil.of( OUTPUT  , epochMillis))]);
    }
}