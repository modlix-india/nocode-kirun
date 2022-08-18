import { EventResult } from './EventResult';

export interface FunctionOutputGenerator {
    next(): EventResult | undefined;
}
