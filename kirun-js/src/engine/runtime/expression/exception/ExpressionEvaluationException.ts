export class ExpressionEvaluationException extends Error {
    cause?: Error;
    private readonly _expression: string;
    private readonly _msg: string;

    constructor(expression: string, message: string, err?: Error) {
        // Temporarily disable stack trace capture — this exception is used as control flow
        // in hot paths, and V8 stack trace capture is very expensive.
        const prevLimit = (Error as any).stackTraceLimit;
        (Error as any).stackTraceLimit = 0;
        super();
        (Error as any).stackTraceLimit = prevLimit;
        this._expression = expression;
        this._msg = message;
        this.cause = err;
    }

    // Lazy message formatting — only computed when actually read (e.g. logging)
    override get message(): string {
        return this._expression + ' : ' + this._msg;
    }

    public getCause(): Error | undefined {
        return this.cause;
    }
}
