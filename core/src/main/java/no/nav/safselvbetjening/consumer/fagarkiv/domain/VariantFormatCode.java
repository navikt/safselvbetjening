package no.nav.safselvbetjening.consumer.fagarkiv.domain;

import no.nav.safselvbetjening.domain.Variantformat;

public enum VariantFormatCode {
	/**
	 * Produksjonsformat
	 */
	PRODUKSJON(Variantformat.PRODUKSJON),
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
	ORIGINAL(Variantformat.ORIGINAL),
	/**
	 * Sladdetformat
	 */
	SLADDET(Variantformat.SLADDET),
	/**
	 * Produksjonsformat DLF
	 */
	PRODUKSJON_DLF(Variantformat.PRODUKSJON_DLF),
	/**
	 * versjon med infotekster
	 */
	FULLVERSJON(Variantformat.FULLVERSJON);

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
