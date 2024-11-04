package no.nav.safselvbetjening.tilgang;

import java.util.Objects;

abstract class JustA<T> {
	protected final T value;

	JustA(T value) {
		if (value == null) {
			throw new IllegalArgumentException("Value must not be null");
		}
		this.value = value;
	}

	public T get() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (this.getClass().isInstance(o)) {
			return ((JustA<?>) o).value.equals(value);
		} else {
			return false;
		}
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(value);
	}
}
