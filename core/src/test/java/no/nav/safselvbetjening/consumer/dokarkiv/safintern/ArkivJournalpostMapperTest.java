package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.RelevantDato;
import no.nav.safselvbetjening.tilgang.TilgangBruker;
import no.nav.safselvbetjening.tilgang.TilgangDokument;
import no.nav.safselvbetjening.tilgang.TilgangFagsystem;
import no.nav.safselvbetjening.tilgang.TilgangJournalpost;
import no.nav.safselvbetjening.tilgang.TilgangJournalstatus;
import no.nav.safselvbetjening.tilgang.TilgangSak;
import no.nav.safselvbetjening.tilgang.TilgangSkjermingType;
import no.nav.safselvbetjening.tilgang.TilgangVariant;
import no.nav.safselvbetjening.tilgang.TilgangVariantFormat;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostMapper.FAGSYSTEM_PENSJON;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostMapper.FILTYPE_PDF;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.APPLIKASJON;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_DOKUMENT;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_EKSPEDERT;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_JOURNALFOERT;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_MOTTATT;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_OPPRETTET;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_RETUR;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_SENDT_PRINT;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.ARKIVSAK_AKTOER_ID;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_ID;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_NAVN;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.BRUKER_IDENT;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.FAGSAKNR;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.HOVEDDOKUMENT_BREVKODE;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.HOVEDDOKUMENT_DOKUMENT_INFO_ID;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.HOVEDDOKUMENT_FIL_STOERRELSE;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.HOVEDDOKUMENT_FIL_UUID;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.HOVEDDOKUMENT_TITTEL;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.INNHOLD;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.JOURNALPOST_ID;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.KANAL_REFERANSE_ID;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.KATEGORI_FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.PENSJON_FAGSAKID;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.TEMA;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.TEMA_PENSJON_ALDERSPENSJON;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.TEMA_PENSJON_UFORETRYGD;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.VEDLEGG_BREVKODE;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.VEDLEGG_DOKUMENT_INFO_ID;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.VEDLEGG_FIL_STOERRELSE;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.VEDLEGG_FIL_UUID;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.VEDLEGG_TITTEL;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.createBrukerIdenter;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.inngaaendeArkivJournalpost;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.pensjonArkivJournalpost;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.utgaaendeArkivJournalpost;
import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.FNR;
import static no.nav.safselvbetjening.domain.Datotype.DATO_AVS_RETUR;
import static no.nav.safselvbetjening.domain.Datotype.DATO_DOKUMENT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_EKSPEDERT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_JOURNALFOERT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_OPPRETTET;
import static no.nav.safselvbetjening.domain.Datotype.DATO_REGISTRERT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_SENDT_PRINT;
import static no.nav.safselvbetjening.domain.Journalposttype.I;
import static no.nav.safselvbetjening.domain.Journalposttype.U;
import static no.nav.safselvbetjening.domain.Journalstatus.EKSPEDERT;
import static no.nav.safselvbetjening.domain.Journalstatus.MOTTATT;
import static no.nav.safselvbetjening.domain.Kanal.NAV_NO;
import static no.nav.safselvbetjening.domain.Kanal.SDP;
import static no.nav.safselvbetjening.domain.Sakstype.FAGSAK;
import static no.nav.safselvbetjening.domain.Tema.HJE;
import static no.nav.safselvbetjening.domain.Variantformat.ARKIV;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_ANNEN_PART;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_GDPR;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.BRUK_STANDARDREGLER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class ArkivJournalpostMapperTest {

	private final ArkivJournalpostMapper mapper = new ArkivJournalpostMapper(new ArkivAvsenderMottakerMapper(), new UtledTilgangService(safSelvbetjeningProperties().getTidligstInnsynDato()));

	@Test
	void skalMappeInngaaendeJournalpost() {
		Journalpost journalpost = mapper.map(inngaaendeArkivJournalpost(), createBrukerIdenter(), Optional.empty());

		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(journalpost.getJournalposttype()).isEqualTo(I);
		assertThat(journalpost.getJournalstatus()).isEqualTo(MOTTATT);
		assertThat(journalpost.getKanal()).isEqualTo(NAV_NO);
		assertThat(journalpost.getTema()).isEqualTo(TEMA);
		assertThat(journalpost.getTittel()).isEqualTo(INNHOLD);
		assertThat(journalpost.getEksternReferanseId()).isEqualTo(KANAL_REFERANSE_ID);
		assertThat(journalpost.getSak().getFagsakId()).isEqualTo(FAGSAKNR);
		assertThat(journalpost.getSak().getFagsaksystem()).isEqualTo(APPLIKASJON);
		assertThat(journalpost.getSak().getSakstype()).isEqualTo(FAGSAK);
		assertThat(journalpost.getAvsender().getId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(journalpost.getAvsender().getType()).isEqualTo(FNR);
		assertThat(journalpost.getAvsender().getNavn()).isEqualTo(AVSENDER_MOTTAKER_NAVN);
		assertThat(journalpost.getRelevanteDatoer())
				.hasSize(4)
				.contains(
						new RelevantDato(ARKIVJOURNALPOST_DATO_OPPRETTET, DATO_OPPRETTET),
						new RelevantDato(ARKIVJOURNALPOST_DATO_JOURNALFOERT, DATO_JOURNALFOERT),
						new RelevantDato(ARKIVJOURNALPOST_DATO_DOKUMENT, DATO_DOKUMENT),
						new RelevantDato(ARKIVJOURNALPOST_DATO_MOTTATT, DATO_REGISTRERT)
				);

		DokumentInfo hoveddokument = journalpost.getDokumenter().get(0);
		assertHoveddokument(hoveddokument, journalpost);
		DokumentInfo vedlegg = journalpost.getDokumenter().get(1);
		assertVedlegg(vedlegg);

		// tilgang mapping
		TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(TilgangJournalstatus.MOTTATT);
		assertThat(tilgang.getTema()).isEqualTo(HJE.name());
		assertThat(tilgang.getMottakskanal()).isEqualTo(NAV_NO.name());
		assertGsakJournalpostTilgang(tilgang);
	}

	@Test
	void skapMappeUtgaaendeJournalpost() {
		Journalpost journalpost = mapper.map(utgaaendeArkivJournalpost(), createBrukerIdenter(), Optional.empty());

		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(journalpost.getJournalposttype()).isEqualTo(U);
		assertThat(journalpost.getJournalstatus()).isEqualTo(EKSPEDERT);
		assertThat(journalpost.getKanal()).isEqualTo(SDP);
		assertThat(journalpost.getTema()).isEqualTo(TEMA);
		assertThat(journalpost.getTittel()).isEqualTo(INNHOLD);
		assertThat(journalpost.getEksternReferanseId()).isEqualTo(KANAL_REFERANSE_ID);
		assertThat(journalpost.getSak().getFagsakId()).isEqualTo(FAGSAKNR);
		assertThat(journalpost.getSak().getFagsaksystem()).isEqualTo(APPLIKASJON);
		assertThat(journalpost.getSak().getSakstype()).isEqualTo(FAGSAK);
		assertThat(journalpost.getMottaker().getId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(journalpost.getMottaker().getNavn()).isEqualTo(AVSENDER_MOTTAKER_NAVN);
		assertThat(journalpost.getMottaker().getType()).isEqualTo(FNR);
		assertThat(journalpost.getRelevanteDatoer())
				.hasSize(6)
				.contains(
						new RelevantDato(ARKIVJOURNALPOST_DATO_OPPRETTET, DATO_OPPRETTET),
						new RelevantDato(ARKIVJOURNALPOST_DATO_JOURNALFOERT, DATO_JOURNALFOERT),
						new RelevantDato(ARKIVJOURNALPOST_DATO_DOKUMENT, DATO_DOKUMENT),
						new RelevantDato(ARKIVJOURNALPOST_DATO_RETUR, DATO_AVS_RETUR),
						new RelevantDato(ARKIVJOURNALPOST_DATO_SENDT_PRINT, DATO_SENDT_PRINT),
						new RelevantDato(ARKIVJOURNALPOST_DATO_EKSPEDERT, DATO_EKSPEDERT)
				);

		DokumentInfo hoveddokument = journalpost.getDokumenter().get(0);
		assertHoveddokument(hoveddokument, journalpost);
		DokumentInfo vedlegg = journalpost.getDokumenter().get(1);
		assertVedlegg(vedlegg);

		// tilgang mapping
		TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(TilgangJournalstatus.EKSPEDERT);
		assertThat(tilgang.getTema()).isEqualTo(HJE.name());
		assertThat(tilgang.getMottakskanal()).isNull();
		assertGsakJournalpostTilgang(tilgang);
	}

	@Test
	void skalMappePensjonJournalpost() {
		Journalpost journalpost = mapper.map(pensjonArkivJournalpost(),
				createBrukerIdenter(),
				Optional.of(new Pensjonsak(PENSJON_FAGSAKID, TEMA_PENSJON_UFORETRYGD)));

		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(journalpost.getJournalposttype()).isEqualTo(I);
		assertThat(journalpost.getJournalstatus()).isEqualTo(MOTTATT);
		assertThat(journalpost.getKanal()).isEqualTo(NAV_NO);
		assertThat(journalpost.getTema()).isEqualTo(TEMA_PENSJON_UFORETRYGD);
		assertThat(journalpost.getTittel()).isEqualTo(INNHOLD);
		assertThat(journalpost.getEksternReferanseId()).isEqualTo(KANAL_REFERANSE_ID);
		assertThat(journalpost.getSak().getFagsakId()).isEqualTo(PENSJON_FAGSAKID.toString());
		assertThat(journalpost.getSak().getFagsaksystem()).isEqualTo(FAGSYSTEM_PENSJON);
		assertThat(journalpost.getSak().getSakstype()).isEqualTo(FAGSAK);
		assertThat(journalpost.getAvsender().getId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(journalpost.getAvsender().getType()).isEqualTo(FNR);
		assertThat(journalpost.getAvsender().getNavn()).isEqualTo(AVSENDER_MOTTAKER_NAVN);
		assertThat(journalpost.getRelevanteDatoer())
				.hasSize(4)
				.contains(
						new RelevantDato(ARKIVJOURNALPOST_DATO_OPPRETTET, DATO_OPPRETTET),
						new RelevantDato(ARKIVJOURNALPOST_DATO_JOURNALFOERT, DATO_JOURNALFOERT),
						new RelevantDato(ARKIVJOURNALPOST_DATO_DOKUMENT, DATO_DOKUMENT),
						new RelevantDato(ARKIVJOURNALPOST_DATO_MOTTATT, DATO_REGISTRERT)
				);

		DokumentInfo hoveddokument = journalpost.getDokumenter().get(0);
		assertHoveddokument(hoveddokument, journalpost);
		DokumentInfo vedlegg = journalpost.getDokumenter().get(1);
		assertVedlegg(vedlegg);

		// tilgang mapping
		TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(TilgangJournalstatus.MOTTATT);
		assertThat(tilgang.getTema()).isEqualTo(TEMA_PENSJON_ALDERSPENSJON);
		assertThat(tilgang.getMottakskanal()).isEqualTo(NAV_NO.name());
		assertPensjonJournalpostTilgang(tilgang);
	}

	private static void assertHoveddokument(DokumentInfo hoveddokument, Journalpost journalpost) {
		assertThat(hoveddokument.getDokumentInfoId()).isEqualTo(String.valueOf(HOVEDDOKUMENT_DOKUMENT_INFO_ID));
		assertThat(hoveddokument.getBrevkode()).isEqualTo(HOVEDDOKUMENT_BREVKODE);
		assertThat(hoveddokument.getTittel()).isEqualTo(HOVEDDOKUMENT_TITTEL);
		assertThat(hoveddokument.getSensitivtPselv()).isTrue();

		assertHoveddokumentVarianter(hoveddokument.getDokumentvarianter());

		// tilgang mapping
		assertThat(hoveddokument.isHoveddokument()).isTrue();
		TilgangDokument tilgangDokument = journalpost.getTilgang().getDokumenter().getFirst();
		assertThat(tilgangDokument.kategori()).isEqualTo(KATEGORI_FORVALTNINGSNOTAT);
		assertThat(tilgangDokument.kassert()).isFalse();
		assertTilgangsVarianter(tilgangDokument.dokumentvarianter());
	}

	private static void assertHoveddokumentVarianter(List<Dokumentvariant> dokumentvarianter) {
		assertThat(dokumentvarianter)
				.extracting(Dokumentvariant::getVariantformat,
						Dokumentvariant::getFiluuid,
						Dokumentvariant::getFiltype,
						Dokumentvariant::getFilstorrelse,
						Dokumentvariant::getCode,
						Dokumentvariant::isBrukerHarTilgang)
				.hasSize(1)
				.containsExactly(tuple(ARKIV,
						HOVEDDOKUMENT_FIL_UUID,
						FILTYPE_PDF,
						parseInt(HOVEDDOKUMENT_FIL_STOERRELSE),
						List.of(DENY_REASON_ANNEN_PART.reason, DENY_REASON_GDPR.reason, DENY_REASON_GDPR.reason),
						false));
	}

	private static void assertTilgangsVarianter(List<TilgangVariant> dokumentvarianter) {
		assertThat(dokumentvarianter).hasSize(1);

		assertThat(dokumentvarianter)
				.containsExactly(TilgangVariant.builder()
						.skjerming(TilgangSkjermingType.FEIL)
						.variantformat(TilgangVariantFormat.ARKIV)
						.build());
	}

	private static void assertVedlegg(DokumentInfo vedlegg) {
		assertThat(vedlegg.getDokumentInfoId()).isEqualTo(String.valueOf(VEDLEGG_DOKUMENT_INFO_ID));
		assertThat(vedlegg.getBrevkode()).isEqualTo(VEDLEGG_BREVKODE);
		assertThat(vedlegg.getTittel()).isEqualTo(VEDLEGG_TITTEL);
		assertThat(vedlegg.getSensitivtPselv()).isNull();

		assertVedleggVarianter(vedlegg.getDokumentvarianter());
	}

	private static void assertVedleggVarianter(List<Dokumentvariant> dokumentvarianter) {
		assertThat(dokumentvarianter)
				.extracting(Dokumentvariant::getVariantformat,
						Dokumentvariant::getFiluuid,
						Dokumentvariant::getFiltype,
						Dokumentvariant::getFilstorrelse,
						Dokumentvariant::getCode,
						Dokumentvariant::isBrukerHarTilgang)
				.hasSize(1)
				.containsExactly(tuple(ARKIV,
						VEDLEGG_FIL_UUID,
						FILTYPE_PDF,
						parseInt(VEDLEGG_FIL_STOERRELSE),
						List.of(DENY_REASON_ANNEN_PART.reason, DENY_REASON_GDPR.reason, DENY_REASON_GDPR.reason),
						false));
	}

	private static void assertGsakJournalpostTilgang(TilgangJournalpost tilgang) {
		assertThat(tilgang.getSkjerming()).isEqualTo(TilgangSkjermingType.POL);
		assertThat(tilgang.getAvsenderMottakerId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(tilgang.getInnsyn()).isEqualTo(BRUK_STANDARDREGLER);
		assertThat(tilgang.getDatoOpprettet()).isEqualTo(ARKIVJOURNALPOST_DATO_OPPRETTET.toLocalDateTime());
		assertThat(tilgang.getJournalfoertDato()).isEqualTo(ARKIVJOURNALPOST_DATO_JOURNALFOERT.toLocalDateTime());

		TilgangBruker tilgangBruker = tilgang.getTilgangBruker();
		assertThat(tilgangBruker.getBrukerId()).isEqualTo(BRUKER_IDENT);

		TilgangSak tilgangSak = tilgang.getTilgangSak();
		assertThat(tilgangSak.aktoerId()).isEqualTo(ARKIVSAK_AKTOER_ID);
		assertThat(tilgangSak.foedselsnummer()).isEqualTo(BRUKER_IDENT);
		assertThat(tilgangSak.fagsystem()).isEqualTo(TilgangFagsystem.FS22);
		assertThat(tilgangSak.tema()).isEqualTo(TEMA);
		assertThat(tilgangSak.feilregistrert()).isTrue();
	}

	private static void assertPensjonJournalpostTilgang(TilgangJournalpost tilgang) {
		assertThat(tilgang.getSkjerming()).isEqualTo(TilgangSkjermingType.POL);
		assertThat(tilgang.getAvsenderMottakerId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(tilgang.getInnsyn()).isEqualTo(BRUK_STANDARDREGLER);
		assertThat(tilgang.getDatoOpprettet()).isEqualTo(ARKIVJOURNALPOST_DATO_OPPRETTET.toLocalDateTime());
		assertThat(tilgang.getJournalfoertDato()).isEqualTo(ARKIVJOURNALPOST_DATO_JOURNALFOERT.toLocalDateTime());

		TilgangBruker tilgangBruker = tilgang.getTilgangBruker();
		assertThat(tilgangBruker.getBrukerId()).isEqualTo(BRUKER_IDENT);

		TilgangSak tilgangSak = tilgang.getTilgangSak();
		assertThat(tilgangSak.aktoerId()).isNull();
		assertThat(tilgangSak.foedselsnummer()).isEqualTo(BRUKER_IDENT);
		assertThat(tilgangSak.fagsystem()).isEqualTo(TilgangFagsystem.PEN);
		assertThat(tilgangSak.tema()).isEqualTo(TEMA_PENSJON_UFORETRYGD);
		assertThat(tilgangSak.feilregistrert()).isTrue();
	}

	static SafSelvbetjeningProperties safSelvbetjeningProperties() {
		SafSelvbetjeningProperties safSelvbetjeningProperties = new SafSelvbetjeningProperties();
		safSelvbetjeningProperties.setTidligstInnsynDato(LocalDate.of(2016, 6, 4));
		return safSelvbetjeningProperties;
	}
}