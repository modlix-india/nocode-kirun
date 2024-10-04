import { FunctionOutput } from "../../../model/FunctionOutput";
import { FunctionSignature } from "../../../model/FunctionSignature";
import { Event } from '../../../model/Event';
import { Namespaces } from "../../../namespaces/Namespaces";
import { FunctionExecutionParameters } from "../../../runtime/FunctionExecutionParameters";
import { AbstractFunction } from "../../AbstractFunction";
import { Schema } from "../../../json/schema/Schema";
import { EventResult } from "../../../model/EventResult";


const OUTPUT = 'date';

const SIGNATURE: FunctionSignature = new FunctionSignature("GetCurrentTimeStamp")
                                        .setNamespace(Namespaces.DATE)
                                        .setParameters(new Map([]))
                                        .setEvents(new Map([
                                            Event.outputEventMapEntry(
                                                new Map([[OUTPUT, Schema.ofRef(`${Namespaces.DATE}.timeStamp`)]]),
                                            ),
                                        ]));

export class GetCurrentTimeStamp extends AbstractFunction{

    public getSignature(): FunctionSignature {

        return SIGNATURE;        
    }
    

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        
        const date : string = new Date(Date.now()).toISOString();
        
        return new FunctionOutput([EventResult.of(OUTPUT, new Map([ [OUTPUT, date]]))]);
    }
}