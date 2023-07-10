import { StringFormatter } from '../../../util/string/StringFormatter';

export class ExpressionEvaluationException extends Error {
    cause?: Error;

    constructor(expression: string, message: string, err?: Error) {
        super(StringFormatter.format('$ : $', expression, message));

        this.cause = err;
    }

    public getCause(): Error | undefined {
        return this.cause;
    }
}
