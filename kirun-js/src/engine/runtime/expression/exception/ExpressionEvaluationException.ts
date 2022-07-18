import { StringFormatter } from '../../../util/string/StringFormatter';

export class ExpressionEvaluationException extends Error {
    constructor(expression: string, message: string, err?: Error) {
        super(StringFormatter.format('$ : $', expression, message));

        if (Error.captureStackTrace) {
            Error.captureStackTrace(err ?? this, ExpressionEvaluationException);
        }
    }
}
