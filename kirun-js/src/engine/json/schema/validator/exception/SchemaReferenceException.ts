export class SchemaReferenceException extends Error {
    private schemaPath: string;
    constructor(schemaPath: string, message: string, err?: Error) {
        super(schemaPath.trim() ? schemaPath + '-' + message : message);

        this.schemaPath = schemaPath;

        if (Error.captureStackTrace) {
            Error.captureStackTrace(err ?? this, SchemaReferenceException);
        }
    }

    public getSchemaPath(): string {
        return this.schemaPath;
    }
}
