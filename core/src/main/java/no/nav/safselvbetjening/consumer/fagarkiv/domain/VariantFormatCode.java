package no.nav.safselvbetjening.consumer.fagarkiv.domain;

import no.nav.safselvbetjening.domain.Variantformat;

public enum VariantFormatCode {
	/**
	 * Produksjonsformat
	 */
	PRODUKSJON(null),
	/**
	 * Arkivformat
	 */
	ARKIV(Variantformat.ARKIV),
	/**
	 * SkanningMetadata
	 */
	SKANNING_META(null),
	/**
	 * BrevbestillingData
	 */
	BREVBESTILLING(null),
	/**
	 * Originalformat
	 */
	ORIGINAL(null),
	/**
	 * Sladdetformat
	 */
	SLADDET(Variantformat.SLADDET),
	/**
	 * Produksjonsformat DLF
	 */
	PRODUKSJON_DLF(null),
	/**
	 * versjon med infotekster
	 */
	FULLVERSJON(null);

	private final Variantformat safVariantformat;

	VariantFormatCode(Variantformat safVariantformat) {
		this.safVariantformat = safVariantformat;
	}

	public Variantformat getSafVariantformat() {
		return safVariantformat;
	}

	public static Variantformat toSafVariantformat(VariantFormatCode joarkVariantFormatCode) {
		return joarkVariantFormatCode == null ? null : joarkVariantFormatCode.getSafVariantformat();
	}

}
