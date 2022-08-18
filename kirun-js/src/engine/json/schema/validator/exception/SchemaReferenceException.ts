export class SchemaReferenceException extends Error {
    private schemaPath: string;
    private cause?: Error;

    constructor(schemaPath: string, message: string, err?: Error) {
        super(schemaPath.trim() ? schemaPath + '-' + message : message);

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
