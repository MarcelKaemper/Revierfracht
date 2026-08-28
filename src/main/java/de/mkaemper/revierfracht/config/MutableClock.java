package de.mkaemper.revierfracht.config;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * A {@link Clock} whose instant can be advanced or set explicitly. Not wired as a
 * bean by default to drive scenario time without the domain code being aware of it.
 */
public final class MutableClock extends Clock {

	private final ZoneId zone;
	private Instant instant;

	public MutableClock(Instant instant, ZoneId zone) {
		this.instant = instant;
		this.zone = zone;
	}

	public void advance(Duration duration) {
		instant = instant.plus(duration);
	}

	public void setInstant(Instant instant) {
		this.instant = instant;
	}

	@Override
	public Instant instant() {
		return instant;
	}

	@Override
	public ZoneId getZone() {
		return zone;
	}

	@Override
	public Clock withZone(ZoneId zone) {
		return new MutableClock(instant, zone);
	}

}
