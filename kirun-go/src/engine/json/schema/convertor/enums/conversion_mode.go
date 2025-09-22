package enums

// ConversionMode represents the mode for schema conversion
type ConversionMode string

const (
	// ConversionModeStrict represents strict conversion mode
	ConversionModeStrict ConversionMode = "STRICT"

	// ConversionModeLoose represents loose conversion mode
	ConversionModeLoose ConversionMode = "LOOSE"

	// ConversionModeAuto represents automatic conversion mode
	ConversionModeAuto ConversionMode = "AUTO"
)
