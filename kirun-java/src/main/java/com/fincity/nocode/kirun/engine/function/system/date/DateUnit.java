package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;

public enum DateUnit implements TemporalUnit {

    MILLISECONDS("Milliseconds", Duration.ofNanos(1000_000)),
    SECONDS("Seconds", Duration.ofSeconds(1)),
    MINUTES("Minutes", Duration.ofSeconds(60)),
    HOURS("Hours", Duration.ofSeconds(3600)),
    DAYS("Days", Duration.ofSeconds(86400)),
    WEEKS("Weeks", Duration.ofSeconds(7 * 86400L)),
    MONTHS("Months", Duration.ofSeconds(30 * 86400L)),
    QUARTERS("Quarters", Duration.ofSeconds(3 * 30 * 86400L)),
    YEARS("Years", Duration.ofSeconds(365 * 86400L));

    private final String name;
    private final Duration duration;

    private DateUnit(String name, Duration estimatedDuration) {
        this.name = name;
        this.duration = estimatedDuration;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public boolean isDurationEstimated() {
        return this.compareTo(DAYS) >= 0;
    }

    @Override
    public boolean isDateBased() {
        return this.compareTo(DAYS) >= 0;
    }

    @Override
    public boolean isTimeBased() {
        return this.compareTo(DAYS) < 0;
    }

    @Override
    public boolean isSupportedBy(Temporal temporal) {
        return temporal.isSupported(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Temporal> R addTo(R temporal, long amount) {
        return (R) temporal.plus(amount, this);
    }

    @Override
    public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
        return temporal1Inclusive.until(temporal2Exclusive, this);
    }

    @Override
    public String toString() {
        return name;
    }

}
