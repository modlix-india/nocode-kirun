import { Schema } from '../json/schema/Schema';
import { Event } from '../model/Event';
import { FunctionOutput } from '../model/FunctionOutput';
import { FunctionSignature } from '../model/FunctionSignature';
import { FunctionExecutionParameters } from '../runtime/FunctionExecutionParameters';

export interface Function {
    getSignature(): FunctionSignature;

    getProbableEventSignature(probableParameters: Map<string, Schema[]>): Map<string, Event>;

    execute(context: FunctionExecutionParameters): FunctionOutput;
}
