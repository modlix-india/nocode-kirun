export class ExecutionException extends Error {
    private cause?: Error;

    constructor(message: string, err?: Error) {
        super(message);
        this.cause = err;
    }

    public getCause(): Error | undefined {
        return this.cause;
    }
}
