package no.nav.safselvbetjening.journalpost;

import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.RelevantDato;
import no.nav.safselvbetjening.domain.SkjermingType;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode.E;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode.M;
import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.FNR;
import static no.nav.safselvbetjening.domain.Datotype.DATO_AVS_RETUR;
import static no.nav.safselvbetjening.domain.Datotype.DATO_DOKUMENT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_EKSPEDERT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_JOURNALFOERT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_OPPRETTET;
import static no.nav.safselvbetjening.domain.Datotype.DATO_REGISTRERT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_SENDT_PRINT;
import static no.nav.safselvbetjening.domain.Innsyn.BRUK_STANDARDREGLER;
import static no.nav.safselvbetjening.domain.Journalposttype.I;
import static no.nav.safselvbetjening.domain.Journalposttype.U;
import static no.nav.safselvbetjening.domain.Journalstatus.EKSPEDERT;
import static no.nav.safselvbetjening.domain.Journalstatus.MOTTATT;
import static no.nav.safselvbetjening.domain.Kanal.NAV_NO;
import static no.nav.safselvbetjening.domain.Kanal.SDP;
import static no.nav.safselvbetjening.domain.Sakstype.FAGSAK;
import static no.nav.safselvbetjening.domain.SkjermingType.FEIL;
import static no.nav.safselvbetjening.domain.Tema.HJE;
import static no.nav.safselvbetjening.domain.Variantformat.ARKIV;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostMapper.FILTYPE_PDF;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.APPLIKASJON;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_DOKUMENT;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_EKSPEDERT;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_JOURNALFOERT;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_MOTTATT;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_OPPRETTET;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_RETUR;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.ARKIVJOURNALPOST_DATO_SENDT_PRINT;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.ARKIVSAKSYSTEM_GOSYS;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.ARKIVSAKSYSTEM_PENSJON;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.ARKIVSAK_AKTOER_ID;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_ID;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_NAVN;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.BRUKER_IDENT;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.FAGSAKNR;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.HOVEDDOKUMENT_BREVKODE;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.HOVEDDOKUMENT_DOKUMENT_INFO_ID;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.HOVEDDOKUMENT_FIL_STOERRELSE;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.HOVEDDOKUMENT_FIL_UUID;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.HOVEDDOKUMENT_TITTEL;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.INNHOLD;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.JOURNALPOST_ID;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.KANAL_REFERANSE_ID;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.KATEGORI_FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.PENSJON_FAGSAKID;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.TEMA;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.TEMA_PENSJON_ALDERSPENSJON;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.TEMA_PENSJON_UFORETRYGD;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.VEDLEGG_BREVKODE;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.VEDLEGG_DOKUMENT_INFO_ID;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.VEDLEGG_FIL_STOERRELSE;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.VEDLEGG_FIL_UUID;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.VEDLEGG_TITTEL;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.createBrukerIdenter;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.inngaaendeArkivJournalpost;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.pensjonArkivJournalpost;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.utgaaendeArkivJournalpost;
import static no.nav.safselvbetjening.service.Saker.FAGSYSTEM_PENSJON;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_GDPR;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_PARTSINNSYN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class ArkivJournalpostMapperTest {

	private final ArkivJournalpostMapper mapper = new ArkivJournalpostMapper(new ArkivAvsenderMottakerMapper(), new UtledTilgangService(safSelvbetjeningProperties()));

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
		assertHoveddokument(hoveddokument);
		DokumentInfo vedlegg = journalpost.getDokumenter().get(1);
		assertVedlegg(vedlegg);

		// tilgang mapping
		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(M.name());
		assertThat(tilgang.getTema()).isEqualTo(HJE.name());
		assertThat(tilgang.getMottakskanal()).isEqualTo(NAV_NO);
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
		assertHoveddokument(hoveddokument);
		DokumentInfo vedlegg = journalpost.getDokumenter().get(1);
		assertVedlegg(vedlegg);

		// tilgang mapping
		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(E.name());
		assertThat(tilgang.getTema()).isEqualTo(HJE.name());
		assertThat(tilgang.getMottakskanal()).isNull();
		assertGsakJournalpostTilgang(tilgang);
	}

	@Test
	void skalMappePensjonJournalpost() {
		Journalpost journalpost = mapper.map(pensjonArkivJournalpost(),
				createBrukerIdenter(),
				Optional.of(new Pensjonsak(PENSJON_FAGSAKID.toString(), TEMA_PENSJON_UFORETRYGD)));

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
		assertHoveddokument(hoveddokument);
		DokumentInfo vedlegg = journalpost.getDokumenter().get(1);
		assertVedlegg(vedlegg);

		// tilgang mapping
		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(M.name());
		assertThat(tilgang.getTema()).isEqualTo(TEMA_PENSJON_ALDERSPENSJON);
		assertThat(tilgang.getMottakskanal()).isEqualTo(NAV_NO);
		assertPensjonJournalpostTilgang(tilgang);
	}

	private static void assertHoveddokument(DokumentInfo hoveddokument) {
		assertThat(hoveddokument.getDokumentInfoId()).isEqualTo(String.valueOf(HOVEDDOKUMENT_DOKUMENT_INFO_ID));
		assertThat(hoveddokument.getBrevkode()).isEqualTo(HOVEDDOKUMENT_BREVKODE);
		assertThat(hoveddokument.getTittel()).isEqualTo(HOVEDDOKUMENT_TITTEL);
		assertThat(hoveddokument.getSensitivtPselv()).isTrue();

		assertHoveddokumentVarianter(hoveddokument.getDokumentvarianter());

		// tilgang mapping
		assertThat(hoveddokument.isHoveddokument()).isTrue();
		DokumentInfo.TilgangDokument tilgangDokument = hoveddokument.getTilgangDokument();
		assertThat(tilgangDokument.getKategori()).isEqualTo(KATEGORI_FORVALTNINGSNOTAT);
		assertThat(tilgangDokument.isKassert()).isFalse();
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
						List.of(DENY_REASON_PARTSINNSYN, DENY_REASON_GDPR, DENY_REASON_GDPR),
						false));

		assertThat(dokumentvarianter)
				.extracting("tilgangVariant")
				.containsExactly(Dokumentvariant.TilgangVariant.builder().skjerming(FEIL).build());
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
						List.of(DENY_REASON_PARTSINNSYN, DENY_REASON_GDPR, DENY_REASON_GDPR),
						false));
	}

	private static void assertGsakJournalpostTilgang(Journalpost.TilgangJournalpost tilgang) {
		assertThat(tilgang.getSkjerming()).isEqualTo(SkjermingType.POL);
		assertThat(tilgang.getAvsenderMottakerId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(tilgang.getInnsyn()).isEqualTo(BRUK_STANDARDREGLER);
		assertThat(tilgang.getDatoOpprettet()).isEqualTo(ARKIVJOURNALPOST_DATO_OPPRETTET.toLocalDateTime());
		assertThat(tilgang.getJournalfoertDato()).isEqualTo(ARKIVJOURNALPOST_DATO_JOURNALFOERT.toLocalDateTime());

		Journalpost.TilgangBruker tilgangBruker = tilgang.getTilgangBruker();
		assertThat(tilgangBruker.getBrukerId()).isEqualTo(BRUKER_IDENT);

		Journalpost.TilgangSak tilgangSak = tilgang.getTilgangSak();
		assertThat(tilgangSak.getAktoerId()).isEqualTo(ARKIVSAK_AKTOER_ID);
		assertThat(tilgangSak.getFoedselsnummer()).isEqualTo(BRUKER_IDENT);
		assertThat(tilgangSak.getFagsystem()).isEqualTo(ARKIVSAKSYSTEM_GOSYS);
		assertThat(tilgangSak.getTema()).isEqualTo(TEMA);
		assertThat(tilgangSak.isFeilregistrert()).isTrue();
	}

	private static void assertPensjonJournalpostTilgang(Journalpost.TilgangJournalpost tilgang) {
		assertThat(tilgang.getSkjerming()).isEqualTo(SkjermingType.POL);
		assertThat(tilgang.getAvsenderMottakerId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(tilgang.getInnsyn()).isEqualTo(BRUK_STANDARDREGLER);
		assertThat(tilgang.getDatoOpprettet()).isEqualTo(ARKIVJOURNALPOST_DATO_OPPRETTET.toLocalDateTime());
		assertThat(tilgang.getJournalfoertDato()).isEqualTo(ARKIVJOURNALPOST_DATO_JOURNALFOERT.toLocalDateTime());

		Journalpost.TilgangBruker tilgangBruker = tilgang.getTilgangBruker();
		assertThat(tilgangBruker.getBrukerId()).isEqualTo(BRUKER_IDENT);

		Journalpost.TilgangSak tilgangSak = tilgang.getTilgangSak();
		assertThat(tilgangSak.getAktoerId()).isNull();
		assertThat(tilgangSak.getFoedselsnummer()).isEqualTo(BRUKER_IDENT);
		assertThat(tilgangSak.getFagsystem()).isEqualTo(ARKIVSAKSYSTEM_PENSJON);
		assertThat(tilgangSak.getTema()).isEqualTo(TEMA_PENSJON_UFORETRYGD);
		assertThat(tilgangSak.isFeilregistrert()).isTrue();
	}

	static SafSelvbetjeningProperties safSelvbetjeningProperties() {
		SafSelvbetjeningProperties safSelvbetjeningProperties = new SafSelvbetjeningProperties();
		safSelvbetjeningProperties.setTidligstInnsynDato(LocalDate.of(2016, 6, 4));
		return safSelvbetjeningProperties;
	}
}