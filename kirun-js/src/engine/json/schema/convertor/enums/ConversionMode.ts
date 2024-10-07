export enum ConversionMode {
    STRICT = "STRICT",
    LENIENT = "LENIENT",
    USE_DEFAULT = "USE_DEFAULT",
    SKIP = "SKIP"
}

export function genericValueOf(mode: string): ConversionMode {
    return ConversionMode[mode.toUpperCase() as keyof typeof ConversionMode];
}

export function getConversionModes(): string[] {
    return Object.values(ConversionMode);
}