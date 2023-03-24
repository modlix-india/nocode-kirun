import { AdditionalType, Schema } from '../json/schema/Schema';
import { Namespaces } from '../namespaces/Namespaces';
import { Event } from './Event';
import { FunctionSignature } from './FunctionSignature';
import { Parameter } from './Parameter';
import { Statement } from './Statement';
import { StatementGroup } from './StatementGroup';

const SCHEMA_NAME1: string = 'FunctionDefinition';
const IN_SCHEMA = new Schema()
    .setNamespace(Namespaces.SYSTEM)
    .setName(SCHEMA_NAME1)
    .setProperties(
        new Map([
            ['name', Schema.ofString('name')],
            ['namespace', Schema.ofString('namespace')],
            ['parameters', Schema.ofArray('parameters', Parameter.SCHEMA)],
            [
                'events',
                Schema.ofObject('events').setAdditionalProperties(
                    new AdditionalType().setSchemaValue(Event.SCHEMA),
                ),
            ],
            [
                'steps',
                Schema.ofObject('steps').setAdditionalProperties(
                    new AdditionalType().setSchemaValue(Statement.SCHEMA),
                ),
            ],
        ]),
    );

IN_SCHEMA.getProperties()?.set('parts', Schema.ofArray('parts', IN_SCHEMA));

export class FunctionDefinition extends FunctionSignature {
    public static readonly SCHEMA: Schema = IN_SCHEMA;
    private version: number = 1;
    private steps?: Map<string, Statement>;
    private stepGroups?: Map<string, StatementGroup>;
    private parts?: FunctionDefinition[];

    constructor(name: string) {
        super(name);
    }

    public getVersion(): number {
        return this.version;
    }
    public setVersion(version: number): FunctionDefinition {
        this.version = version;
        return this;
    }
    public getSteps(): Map<string, Statement> {
        return this.steps ?? new Map();
    }
    public setSteps(steps: Map<string, Statement>): FunctionDefinition {
        this.steps = steps;
        return this;
    }
    public getStepGroups(): Map<string, StatementGroup> | undefined {
        return this.stepGroups;
    }
    public setStepGroups(stepGroups: Map<string, StatementGroup>): FunctionDefinition {
        this.stepGroups = stepGroups;
        return this;
    }

    public getParts(): FunctionDefinition[] | undefined {
        return this.parts;
    }

    public setParts(parts: FunctionDefinition[]): FunctionDefinition {
        this.parts = parts;
        return this;
    }

    public static from(json: any): FunctionDefinition {
        if (!json) return new FunctionDefinition('unknown');
        return new FunctionDefinition(json.name)
            .setSteps(
                new Map(
                    Object.values(json.steps ?? {})
                        .filter((e) => !!e)
                        .map((e: any) => [e.statementName, Statement.from(e)]),
                ),
            )
            .setStepGroups(
                new Map(
                    Object.values(json.stepGroups ?? {})
                        .filter((e) => !!e)
                        .map((e: any) => [e.statementGroupName, StatementGroup.from(e)]),
                ),
            )
            .setParts(
                Array.from(json.parts ?? [])
                    .filter((e) => !!e)
                    .map((e: any) => FunctionDefinition.from(e)),
            )
            .setVersion(json.version ?? 1)
            .setEvents(
                new Map(
                    Object.values(json.events ?? {})
                        .filter((e) => !!e)
                        .map((e: any) => [e.name, Event.from(e)]),
                ),
            )
            .setParameters(
                new Map(
                    Object.values(json.parameters ?? {})
                        .filter((e) => !!e)
                        .map((e: any) => [e.parameterName, Parameter.from(e)]),
                ),
            ) as FunctionDefinition;
    }
}
