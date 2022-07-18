export class SchemaValidationException extends Error {
    private schemaPath: string;

    constructor(
        schemaPath: string,
        message: string,
        sve: SchemaValidationException[] = [],
        err?: Error,
    ) {
        super(message + (sve ? sve.map((e) => e.message).reduce((a, c) => a + '\n' + c, '') : ''));
        this.schemaPath = schemaPath;

        if (Error.captureStackTrace) {
            Error.captureStackTrace(err ?? this, SchemaValidationException);
        }
    }

    public getSchemaPath(): string {
        return this.schemaPath;
    }
}
