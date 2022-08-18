import { AdditionalPropertiesType } from '../json/schema/object/AdditionalPropertiesType';
import { Schema } from '../json/schema/Schema';
import { Namespaces } from '../namespaces/Namespaces';
import { Event } from './Event';
import { FunctionSignature } from './FunctionSignature';
import { Parameter } from './Parameter';
import { Statement } from './Statement';
import { StatementGroup } from './StatementGroup';

export class FunctionDefinition extends FunctionSignature {
    private static readonly SCHEMA_NAME1: string = 'FunctionDefinition';
    public static readonly SCHEMA: Schema = new Schema()
        .setNamespace(Namespaces.SYSTEM)
        .setName(FunctionDefinition.SCHEMA_NAME1)
        .setProperties(
            new Map([
                ['name', Schema.ofString('name')],
                ['namespace', Schema.ofString('namespace')],
                ['parameters', Schema.ofArray('parameters', Parameter.SCHEMA)],
                [
                    'events',
                    Schema.ofObject('events').setAdditionalProperties(
                        new AdditionalPropertiesType().setSchemaValue(Event.SCHEMA),
                    ),
                ],
                [
                    'parts',
                    Schema.ofObject('parts').setAdditionalProperties(
                        new AdditionalPropertiesType().setSchemaValue(FunctionSignature.SCHEMA),
                    ),
                ],
                [
                    'steps',
                    Schema.ofObject('steps').setAdditionalProperties(
                        new AdditionalPropertiesType().setSchemaValue(Statement.SCHEMA),
                    ),
                ],
            ]),
        );
    private version: number = 1;
    private steps?: Map<string, Statement>;
    private stepGroups?: Map<string, StatementGroup>;

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
}
