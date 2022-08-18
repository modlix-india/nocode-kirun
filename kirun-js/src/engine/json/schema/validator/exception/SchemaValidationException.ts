export class SchemaValidationException extends Error {
    private schemaPath: string;
    private cause?: Error;
    constructor(
        schemaPath: string,
        message: string,
        sve: SchemaValidationException[] = [],
        err?: Error,
    ) {
        super(message + (sve ? sve.map((e) => e.message).reduce((a, c) => a + '\n' + c, '') : ''));
        this.schemaPath = schemaPath;
        this.cause = err;
    }

    public getSchemaPath(): string {
        return this.schemaPath;
    }

    public getCause(): Error | undefined {
        return this.cause;
    }
}
