import { AdditionalPropertiesType } from '../json/schema/object/AdditionalPropertiesType';
import { Schema } from '../json/schema/Schema';
import { SchemaType } from '../json/schema/type/SchemaType';
import { TypeUtil } from '../json/schema/type/TypeUtil';
import { SchemaReferenceException } from '../json/schema/validator/exception/SchemaReferenceException';
import { Namespaces } from '../namespaces/Namespaces';

export class Event {
    public static readonly OUTPUT: string = 'output';
    public static readonly ERROR: string = 'error';
    public static readonly ITERATION: string = 'iteration';
    public static readonly TRUE: string = 'true';
    public static readonly FALSE: string = 'false';
    public static readonly SCHEMA_NAME: string = 'Event';
    public static readonly SCHEMA: Schema = new Schema()
        .setNamespace(Namespaces.SYSTEM)
        .setName(Event.SCHEMA_NAME)
        .setType(TypeUtil.of(SchemaType.OBJECT))
        .setProperties(
            new Map([
                ['name', Schema.ofString('name')],
                [
                    'parameters',
                    Schema.ofObject('parameter').setAdditionalProperties(
                        new AdditionalPropertiesType().setSchemaValue(Schema.SCHEMA),
                    ),
                ],
            ]),
        );
    private name: string;
    private parameters: Map<string, Schema>;

    constructor(name: string, parameters: Map<string, Schema>) {
        this.name = name;
        this.parameters = parameters;
    }

    public getName(): string {
        return this.name;
    }
    public setName(name: string): Event {
        this.name = name;
        return this;
    }
    public getParameters(): Map<string, Schema> {
        return this.parameters;
    }
    public setParameters(parameters: Map<string, Schema>): Event {
        this.parameters = parameters;
        return this;
    }

    public static outputEventMapEntry(parameters: Map<string, Schema>): [string, Event] {
        return Event.eventMapEntry(Event.OUTPUT, parameters);
    }

    public static eventMapEntry(
        eventName: string,
        parameters: Map<string, Schema>,
    ): [string, Event] {
        return [eventName, new Event(eventName, parameters)];
    }

    public static from(json: any): Event {
        return new Event(
            json.name,
            new Map(
                Object.entries(json.parameters ?? {}).map((e: any) => {
                    const eventSchema = Schema.from(e[1]);
                    if (!eventSchema)
                        throw new SchemaReferenceException('', 'Event expects a schema');
                    return [e[0], eventSchema];
                }),
            ),
        );
    }
}
