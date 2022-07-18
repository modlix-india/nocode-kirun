export class KIRuntimeException extends Error {
    constructor(message: string, err?: Error) {
        super(message);

        if (Error.captureStackTrace) {
            Error.captureStackTrace(err ?? this, KIRuntimeException);
        }
    }
}
