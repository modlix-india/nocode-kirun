export interface UIError {
    message: string;
}

export function isUIError(error: unknown): error is UIError {
    return (
        typeof error === 'object' &&
        error !== null &&
        'message' in error &&
        typeof (error as Record<string, unknown>).message === 'string'
    );
}

export function toUIError(maybeError: unknown): UIError {
    if (isUIError(maybeError)) return maybeError;

    try {
        return new Error(JSON.stringify(maybeError));
    } catch {
        return new Error(String(maybeError));
    }
}
