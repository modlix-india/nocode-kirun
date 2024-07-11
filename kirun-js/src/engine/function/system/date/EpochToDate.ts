import { Schema } from "../../../json/schema/Schema";
import { FunctionOutput } from "../../../model/FunctionOutput";
import { FunctionSignature } from "../../../model/FunctionSignature";
import { Event } from '../../../model/Event';
import { Parameter } from "../../../model/Parameter";
import { Namespaces } from "../../../namespaces/Namespaces";
import { FunctionExecutionParameters } from "../../../runtime/FunctionExecutionParameters";
import { AbstractFunction } from "../../AbstractFunction";
import { isNullValue } from "../../../util/NullCheck";
import { KIRuntimeException } from "../../../exception/KIRuntimeException";
import { EventResult } from "../../../model/EventResult";


const VALUE = 'epoch';
const OUTPUT = 'date';
const  ERROR_MSG: string = "Please provide a valid value for epoch.";

const SIGNATURE = new FunctionSignature('EpochToDate')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([
            [
                VALUE,
                new Parameter(
                    VALUE,
                    new Schema().setAnyOf([
                        Schema.ofInteger(VALUE),
                        Schema.ofLong(VALUE),
                        Schema.ofString(VALUE),
                    ]),
                ),
            ],
        ]),
    )
    .setEvents(
        new Map([
            Event.outputEventMapEntry(
                new Map([[OUTPUT, Schema.ofRef(`${Namespaces.DATE}.timeStamp`)]]),
            ),
        ]),
    );

export class EpochToDate extends AbstractFunction{

    public getSignature(): FunctionSignature {
      return SIGNATURE;
    }
    
    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {

       var epoch : any =  context.getArguments()?.get(VALUE);

       if(isNullValue(epoch))
        throw new KIRuntimeException(ERROR_MSG);

       if(typeof epoch === 'boolean')
            throw new KIRuntimeException(ERROR_MSG);

       if(typeof epoch === 'string')
            epoch  = parseInt(epoch)

       if(isNaN(epoch))
        throw new KIRuntimeException(ERROR_MSG);

       epoch = epoch > 999999999999 ? epoch : epoch * 1000;

      
       return new FunctionOutput([
           EventResult.outputOf(new Map([
            [OUTPUT, new Date(epoch).toISOString()]
            ]))
       ]);

    }
    
}