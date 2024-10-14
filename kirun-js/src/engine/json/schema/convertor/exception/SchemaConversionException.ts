import { ConversionMode } from '../enums/ConversionMode';

export class SchemaConversionException extends Error {
    private readonly schemaPath: string;
    private readonly source?: string | null;
    private readonly mode?: ConversionMode | null;
    cause?: Error;

    constructor(
        schemaPath: string,
        source: string,
        message: string,
        mode?: ConversionMode,
        sce: SchemaConversionException[] = [],
        err?: Error,
    ) {
        super(message + (sce ? sce.map((e) => e.message).reduce((a, c) => a + '\n' + c, '') : ''));
        this.schemaPath = schemaPath;
        this.source = source ?? null;
        this.mode = mode ?? null;
        this.cause = err;
    }

    public getSchemaPath(): string {
        return this.schemaPath;
    }

    public getSource(): string | null {
        return this.source ?? null;
    }

    public getMode(): ConversionMode | null {
        return this.mode ?? null;
    }

    public getCause(): Error | undefined {
        return this.cause;
    }
}
