import { Event } from './Event';

export class EventResult {
    private name: string;
    private result: Map<string, any>;

    constructor(name: string, result: Map<string, any>) {
        this.name = name;
        this.result = result;
    }

    public getName(): string {
        return this.name;
    }
    public setName(name: string): EventResult {
        this.name = name;
        return this;
    }
    public getResult(): Map<string, any> {
        return this.result;
    }
    public setResult(result: Map<string, any>): EventResult {
        this.result = result;
        return this;
    }

    public static outputOf(result: Map<string, any>): EventResult {
        return EventResult.of(Event.OUTPUT, result);
    }

    public static of(eventName: string, result: Map<string, any>): EventResult {
        return new EventResult(eventName, result);
    }
}
