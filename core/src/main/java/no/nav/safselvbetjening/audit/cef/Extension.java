package no.nav.safselvbetjening.audit.cef;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Clock;
import java.time.Instant;

import static java.lang.String.format;
import static no.nav.safselvbetjening.MDCUtils.getCallId;
import static no.nav.safselvbetjening.MDCUtils.getConsumerId;

@Getter
@AllArgsConstructor
@SuperBuilder
public abstract class Extension {
	Clock clock;
	/**
	 * Identifies the destination user by ID.
	 */
	String destinationUserId;
	/**
	 * Identifies the source user by ID.
	 */
	String sourceUserId;
	/**
	 * The typical values are "Administrator", "User", and "Guest". This identifies the destination user’s privileges.
	 */
	String sourceUserPrivileges;
	/**
	 * Action taken by the device.
	 */
	String deviceAction;

	protected abstract String getDeviceCustomStringsCef();

	@Override
	public String toString() {
		return format("end=%s sproc=%s spid=%s suid=%s spriv=%s duid=%s act=%s ", Instant.now(clock).toEpochMilli(), getConsumerId(), getCallId(), getSourceUserId(), getSourceUserPrivileges(),
				getDestinationUserId(), getDeviceAction()) + getDeviceCustomStringsCef();
	}
}