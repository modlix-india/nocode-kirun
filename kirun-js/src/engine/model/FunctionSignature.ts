import { AdditionalPropertiesType } from '../json/schema/object/AdditionalPropertiesType';
import { Schema } from '../json/schema/Schema';
import { Namespaces } from '../namespaces/Namespaces';
import { Event } from './Event';
import { Parameter } from './Parameter';

export class FunctionSignature {
    private static readonly SCHEMA_NAME: string = 'FunctionSignature';
    public static readonly SCHEMA: Schema = new Schema()
        .setNamespace(Namespaces.SYSTEM)
        .setName(FunctionSignature.SCHEMA_NAME)
        .setProperties(
            new Map([
                ['name', Schema.ofString('name')],
                ['namespace', Schema.ofString('namespace')],
                [
                    'parameters',
                    Schema.ofObject('parameters').setAdditionalProperties(
                        new AdditionalPropertiesType().setSchemaValue(Parameter.SCHEMA),
                    ),
                ],
                [
                    'events',
                    Schema.ofObject('events').setAdditionalProperties(
                        new AdditionalPropertiesType().setSchemaValue(Event.SCHEMA),
                    ),
                ],
            ]),
        );
    private namespace: string;
    private name: string;
    private parameters: Map<string, Parameter> = new Map();
    private events: Map<string, Event> = new Map();

    public getNamespace(): string {
        return this.namespace;
    }
    public setNamespace(namespace: string): FunctionSignature {
        this.namespace = namespace;
        return this;
    }
    public getName(): string {
        return this.name;
    }
    public setName(name: string): FunctionSignature {
        this.name = name;
        return this;
    }
    public getParameters(): Map<string, Parameter> {
        return this.parameters;
    }
    public setParameters(parameters: Map<string, Parameter>): FunctionSignature {
        this.parameters = parameters;
        return this;
    }
    public getEvents(): Map<string, Event> {
        return this.events;
    }
    public setEvents(events: Map<string, Event>): FunctionSignature {
        this.events = events;
        return this;
    }
}
