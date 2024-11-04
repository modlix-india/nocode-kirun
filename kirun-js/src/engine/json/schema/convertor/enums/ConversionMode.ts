export enum ConversionMode {
    STRICT = 'STRICT',
    LENIENT = 'LENIENT',
    USE_DEFAULT = 'USE_DEFAULT',
    SKIP = 'SKIP',
}

export const genericValueOf = (mode: string): ConversionMode =>
    ConversionMode[mode.toUpperCase() as keyof typeof ConversionMode];

export const getConversionModes = (): string[] => Object.values(ConversionMode);
