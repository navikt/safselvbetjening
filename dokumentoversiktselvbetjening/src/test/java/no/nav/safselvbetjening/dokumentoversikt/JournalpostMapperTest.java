package no.nav.safselvbetjening.dokumentoversikt;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SaksrelasjonDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantFormatCode;
import no.nav.safselvbetjening.consumer.sak.Joarksak;
import no.nav.safselvbetjening.domain.Datotype;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.RelevantDato;
import no.nav.safselvbetjening.domain.SkjermingType;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.service.Saker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Integer.parseInt;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.AKTOER_ID;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.ARKIVSAKSYSTEM_GOSYS;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.ARKIVSAKSYSTEM_PENSJON;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.ARKIVSAK_ID;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.AVSENDER_MOTTAKER_ID;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.AVS_RETUR_DATO;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.BREVKODE;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.BRUKER_ID_PERSON;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.DATO_OPPRETTET;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.DATO_OPPRETTET_LDT;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.DOKUMENT_DATO;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.DOKUMENT_INFO_ID;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.EKSPEDERT_DATO;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.FAGSAK_APPLIKASJON;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.FAGSAK_ID;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.FILSTORRELSE_1;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.FILSTORRELSE_2;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.FILTYPE;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.FILUUID_1;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.FILUUID_2;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.IDENT;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.INNHOLD;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.JOURNALPOST_ID;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.JOURNAL_DATO;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.JOURNAL_DATO_LDT;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.KANAL_REFERANSE_ID;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.KATEGORI;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.MOTTAT_DATO;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.PENSJON_SAKID;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.PENSJON_TEMA;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.SENDT_PRINT_DATO;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.TEMA;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.TITTEL;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.baseJournalpostDto;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.buildDokumentWithVarianter;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.buildJournalpostDtoMottatt;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.buildJournalpostDtoNotatType;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.createBrukerIdenter;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.createSaker;
import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.FNR;
import static no.nav.safselvbetjening.domain.Journalposttype.I;
import static no.nav.safselvbetjening.domain.Journalposttype.N;
import static no.nav.safselvbetjening.domain.Journalposttype.U;
import static no.nav.safselvbetjening.domain.Journalstatus.EKSPEDERT;
import static no.nav.safselvbetjening.domain.Journalstatus.JOURNALFOERT;
import static no.nav.safselvbetjening.domain.Journalstatus.MOTTATT;
import static no.nav.safselvbetjening.domain.Sakstype.APPLIKASJON_GENERELL_SAK;
import static no.nav.safselvbetjening.domain.Sakstype.FAGSAK;
import static no.nav.safselvbetjening.domain.Sakstype.GENERELL_SAK;
import static no.nav.safselvbetjening.domain.Variantformat.ARKIV;
import static no.nav.safselvbetjening.domain.Variantformat.SLADDET;
import static no.nav.safselvbetjening.service.Saker.FAGSYSTEM_PENSJON;
import static org.assertj.core.api.Assertions.assertThat;

class JournalpostMapperTest {
	private final JournalpostMapper journalpostMapper = new JournalpostMapper(new AvsenderMottakerMapper());

	@Test
	void shouldMapVisningsfeltWhenInngaaende() {
		Journalpost journalpost = journalpostMapper.map(buildJournalpostDtoInngaaendeType(), createSaker(), createBrukerIdenter());
		assertCommonFields(journalpost);
		assertThat(journalpost.getJournalposttype()).isEqualTo(I);
		assertThat(journalpost.getJournalstatus()).isEqualTo(JOURNALFOERT);
		assertThat(journalpost.getKanal()).isEqualTo(Kanal.NAV_NO);
		assertThat(journalpost.getEksternReferanseId()).isEqualTo(KANAL_REFERANSE_ID);
		assertThat(journalpost.getAvsender().getId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(journalpost.getAvsender().getType()).isEqualTo(FNR);
		assertThat(journalpost.getMottaker()).isNull();
		assertThat(journalpost.getRelevanteDatoer()).containsAll(List.of(
				new RelevantDato(DATO_OPPRETTET, Datotype.DATO_OPPRETTET),
				new RelevantDato(JOURNAL_DATO, Datotype.DATO_JOURNALFOERT),
				new RelevantDato(MOTTAT_DATO, Datotype.DATO_REGISTRERT)));
	}

	@Test
	void shouldMapVisningsfeltWhenUtgaaende() {
		Journalpost journalpost = journalpostMapper.map(buildJournalpostDtoUtgaaendeType(JournalStatusCode.E), createSaker(), createBrukerIdenter());
		assertCommonFields(journalpost);
		assertThat(journalpost.getJournalposttype()).isEqualTo(U);
		assertThat(journalpost.getJournalstatus()).isEqualTo(EKSPEDERT);
		assertThat(journalpost.getKanal()).isEqualTo(Kanal.SDP);
		assertThat(journalpost.getMottaker().getId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(journalpost.getMottaker().getType()).isEqualTo(FNR);
		assertThat(journalpost.getAvsender()).isNull();
		assertThat(journalpost.getRelevanteDatoer()).containsAll(List.of(
				new RelevantDato(DATO_OPPRETTET, Datotype.DATO_OPPRETTET),
				new RelevantDato(JOURNAL_DATO, Datotype.DATO_JOURNALFOERT),
				new RelevantDato(DOKUMENT_DATO, Datotype.DATO_DOKUMENT),
				new RelevantDato(AVS_RETUR_DATO, Datotype.DATO_AVS_RETUR),
				new RelevantDato(SENDT_PRINT_DATO, Datotype.DATO_SENDT_PRINT),
				new RelevantDato(EKSPEDERT_DATO, Datotype.DATO_EKSPEDERT)));
	}

	@Test
	void shouldMapVisningsfeltWhenNotat() {
		Journalpost journalpost = journalpostMapper.map(buildJournalpostDtoNotatType(JournalStatusCode.E), createSaker(), createBrukerIdenter());
		assertCommonFields(journalpost);
		assertThat(journalpost.getJournalposttype()).isEqualTo(N);
		assertThat(journalpost.getJournalstatus()).isEqualTo(EKSPEDERT);
		assertThat(journalpost.getKanal()).isEqualTo(Kanal.INGEN_DISTRIBUSJON);
		assertThat(journalpost.getMottaker()).isNull();
		assertThat(journalpost.getAvsender()).isNull();
		assertThat(journalpost.getRelevanteDatoer()).containsAll(List.of(
				new RelevantDato(DATO_OPPRETTET, Datotype.DATO_OPPRETTET),
				new RelevantDato(JOURNAL_DATO, Datotype.DATO_JOURNALFOERT),
				new RelevantDato(DOKUMENT_DATO, Datotype.DATO_DOKUMENT)));
	}

	private void assertCommonFields(final Journalpost journalpost) {
		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID.toString());
		assertThat(journalpost.getTittel()).isEqualTo(INNHOLD);
		assertThat(journalpost.getTema()).isEqualTo(Tema.FOR.name());
		assertThat(journalpost.getSak().getFagsakId()).isEqualTo(FAGSAK_ID);
		assertThat(journalpost.getSak().getFagsaksystem()).isEqualTo(FAGSAK_APPLIKASJON);
		assertThat(journalpost.getSak().getSakstype()).isEqualTo(FAGSAK);
		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo.getDokumentInfoId()).isEqualTo(DOKUMENT_INFO_ID.toString());
		assertThat(dokumentInfo.getBrevkode()).isEqualTo(BREVKODE);
		assertThat(dokumentInfo.getTittel()).isEqualTo(TITTEL);
		Dokumentvariant arkivVariant = dokumentInfo.getDokumentvarianter().get(0);
		assertThat(arkivVariant.getFiluuid()).isEqualTo(FILUUID_1);
		assertThat(arkivVariant.getFiltype()).isEqualTo(FILTYPE);
		assertThat(arkivVariant.getFilstorrelse()).isEqualTo(parseInt(FILSTORRELSE_1));
		assertThat(arkivVariant.getVariantformat()).isEqualTo(ARKIV);
		Dokumentvariant sladdetVariant = dokumentInfo.getDokumentvarianter().get(1);
		assertThat(sladdetVariant.getFiluuid()).isEqualTo(FILUUID_2);
		assertThat(sladdetVariant.getFiltype()).isEqualTo(FILTYPE);
		assertThat(sladdetVariant.getFilstorrelse()).isEqualTo(parseInt(FILSTORRELSE_2));
		assertThat(sladdetVariant.getVariantformat()).isEqualTo(SLADDET);
	}

	@Test
	void shouldMapVisningsfeltWhenMottatt() {
		Journalpost journalpost = journalpostMapper.map(buildJournalpostDtoMottatt(), createSaker(), createBrukerIdenter());
		assertThat(journalpost.getJournalposttype()).isEqualTo(I);
		assertThat(journalpost.getJournalstatus()).isEqualTo(MOTTATT);
		assertThat(journalpost.getKanal()).isEqualTo(Kanal.SKAN_IM);
		assertThat(journalpost.getRelevanteDatoer()).containsAll(List.of(new RelevantDato(MOTTAT_DATO, Datotype.DATO_REGISTRERT)));
	}

	@Test
	void shouldMapPensjonJournalpostWhenPensjonSak() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		// Tema på saken er det som skal mappes, hvis det finnes.
		journalpostDto.setFagomrade(FagomradeCode.PEN);
		journalpostDto.setSaksrelasjon(SaksrelasjonDto.builder()
				.sakId(PENSJON_SAKID)
				.fagsystem(FagsystemCode.PEN)
				.build());
		Journalpost journalpost = journalpostMapper.map(journalpostDto, createSaker(), createBrukerIdenter());
		// Tema UFO på saken
		assertThat(journalpost.getTema()).isEqualTo(PENSJON_TEMA);
		assertThat(journalpost.getSak().getFagsakId()).isEqualTo(PENSJON_SAKID);
		assertThat(journalpost.getSak().getFagsaksystem()).isEqualTo(FAGSYSTEM_PENSJON);
		assertThat(journalpost.getSak().getSakstype()).isEqualTo(FAGSAK);
		assertThat(journalpost.getJournalposttype()).isEqualTo(U);
		assertThat(journalpost.getJournalstatus()).isEqualTo(EKSPEDERT);
		assertThat(journalpost.getKanal()).isEqualTo(Kanal.SDP);
		assertThat(journalpost.getMottaker().getId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(journalpost.getMottaker().getType()).isEqualTo(FNR);
		assertThat(journalpost.getAvsender()).isNull();
		assertThat(journalpost.getRelevanteDatoer()).containsAll(List.of(
				new RelevantDato(DATO_OPPRETTET, Datotype.DATO_OPPRETTET),
				new RelevantDato(JOURNAL_DATO, Datotype.DATO_JOURNALFOERT),
				new RelevantDato(DOKUMENT_DATO, Datotype.DATO_DOKUMENT),
				new RelevantDato(AVS_RETUR_DATO, Datotype.DATO_AVS_RETUR),
				new RelevantDato(SENDT_PRINT_DATO, Datotype.DATO_SENDT_PRINT),
				new RelevantDato(EKSPEDERT_DATO, Datotype.DATO_EKSPEDERT)));
	}

	@Test
	void shouldMapSakstypeGenerellSakWhenSaksrelasjonGenerellSak() {
		Saker saker = new Saker(
				Collections.singletonList(Joarksak.builder()
						.tema(TEMA.name())
						.id(parseInt(ARKIVSAK_ID))
						.applikasjon(APPLIKASJON_GENERELL_SAK)
						.fagsakNr(null)
						.build()), new ArrayList<>());
		JournalpostDto journalpostDto = buildJournalpostDtoInngaaendeType();
		journalpostDto.setSaksrelasjon(SaksrelasjonDto.builder()
				.sakId(ARKIVSAK_ID)
				.fagsystem(FagsystemCode.FS22)
				.feilregistrert(false)
				.tema(TEMA.name())
				.applikasjon(APPLIKASJON_GENERELL_SAK)
				.fagsakNr(null)
				.build());
		Journalpost journalpost = journalpostMapper.map(journalpostDto, saker, createBrukerIdenter());
		assertThat(journalpost.getSak().getFagsakId()).isNull();
		assertThat(journalpost.getSak().getFagsaksystem()).isEqualTo(APPLIKASJON_GENERELL_SAK);
		assertThat(journalpost.getSak().getSakstype()).isEqualTo(GENERELL_SAK);
	}

	@Test
	void shouldMapTilgangsfeltWhenJournalfoert() {
		Journalpost journalpost = journalpostMapper.map(buildJournalpostDtoInngaaendeType(), createSaker(), createBrukerIdenter());
		Journalpost.TilgangJournalpost tilgangJournalpost = journalpost.getTilgang();
		assertThat(tilgangJournalpost.getTema()).isEqualTo(FagomradeCode.FOR.name());
		assertThat(tilgangJournalpost.getAvsenderMottakerId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(tilgangJournalpost.getDatoOpprettet()).isEqualTo(DATO_OPPRETTET_LDT);
		assertThat(tilgangJournalpost.getJournalfoertDato()).isEqualTo(JOURNAL_DATO_LDT);
		assertThat(tilgangJournalpost.getSkjerming()).isEqualTo(SkjermingType.POL);
		Journalpost.TilgangBruker tilgangBruker = tilgangJournalpost.getTilgangBruker();
		assertThat(tilgangBruker.getBrukerId()).isEqualTo(BRUKER_ID_PERSON);
		Journalpost.TilgangSak tilgangSak = tilgangJournalpost.getTilgangSak();
		assertThat(tilgangSak.getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(tilgangSak.getFagsystem()).isEqualTo(ARKIVSAKSYSTEM_GOSYS.name());
		assertThat(tilgangSak.getTema()).isEqualTo(Tema.FOR.name());

		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		DokumentInfo.TilgangDokument tilgangDokument = dokumentInfo.getTilgangDokument();
		assertThat(tilgangDokument.getKategori()).isEqualTo(KATEGORI.toString());

		Dokumentvariant arkivVariant = dokumentInfo.getDokumentvarianter().get(0);
		Dokumentvariant.TilgangVariant arkivTilgangVariant = arkivVariant.getTilgangVariant();
		assertThat(arkivTilgangVariant.getSkjerming()).isEqualTo(SkjermingType.POL);
		Dokumentvariant sladdetVariant = dokumentInfo.getDokumentvarianter().get(1);
		Dokumentvariant.TilgangVariant sladdetTilgangVariant = sladdetVariant.getTilgangVariant();
		assertThat(sladdetTilgangVariant.getSkjerming()).isNull();
	}

	@Test
	void shouldMapTilgangsfeltWhenMottatt() {
		Journalpost journalpost = journalpostMapper.map(buildJournalpostDtoMottatt(), createSaker(), createBrukerIdenter());
		Journalpost.TilgangJournalpost tilgangJournalpost = journalpost.getTilgang();
		assertThat(tilgangJournalpost.getDatoOpprettet()).isEqualTo(DATO_OPPRETTET_LDT);
		assertThat(tilgangJournalpost.getJournalfoertDato()).isNull();
		assertThat(tilgangJournalpost.getMottakskanal()).isEqualTo(Kanal.SKAN_IM);
		Journalpost.TilgangBruker tilgangBruker = tilgangJournalpost.getTilgangBruker();
		assertThat(tilgangBruker.getBrukerId()).isNull();
		Journalpost.TilgangSak tilgangSak = tilgangJournalpost.getTilgangSak();
		assertThat(tilgangSak.getAktoerId()).isNull();
		assertThat(tilgangSak.getFagsystem()).isNull();
		assertThat(tilgangSak.getTema()).isNull();
	}

	// Lokal utskrift - sendt ut (journalposttype U) og skannet inn (mottakskanal SKAN_*)
	@Test
	void shouldMapWhenJournalpostLokalUtskrift() {
		JournalpostDto journalpostDto = baseJournalpostDto()
				.journalstatus(JournalStatusCode.FL)
				.journalposttype(JournalpostTypeCode.U)
				.mottakskanal(MottaksKanalCode.SKAN_IM)
				.utsendingskanal(null)
				.build();

		Journalpost journalpost = journalpostMapper.map(journalpostDto, createSaker(), createBrukerIdenter());
		assertThat(journalpost.getKanal()).isEqualTo(Kanal.LOKAL_UTSKRIFT);
		Journalpost.TilgangJournalpost tilgangJournalpost = journalpost.getTilgang();
		assertThat(tilgangJournalpost.getMottakskanal()).isEqualTo(Kanal.SKAN_IM);
	}

	@Test
	void shouldMapTilgangSakWhenPensjonsak() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(JournalStatusCode.E);
		journalpostDto.setSaksrelasjon(SaksrelasjonDto.builder()
				.sakId("123")
				.fagsystem(FagsystemCode.PEN)
				.feilregistrert(false)
				.tema("PEN")
				.build());

		Journalpost journalpost = journalpostMapper.map(journalpostDto, createSaker(), createBrukerIdenter());

		Journalpost.TilgangSak tilgangSak = journalpost.getTilgang().getTilgangSak();
		assertThat(tilgangSak.getAktoerId()).isNull();
		assertThat(tilgangSak.getFoedselsnummer()).isEqualTo(IDENT);
		assertThat(tilgangSak.getFagsystem()).isEqualTo(ARKIVSAKSYSTEM_PENSJON.name());
		assertThat(tilgangSak.getTema()).isEqualTo(Tema.PEN.name());
	}

	@Test
	void shouldFilterAllExceptArkivSladdetVarianter() {
		JournalpostDto journalpostDto = baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.N)
				.dokumenter(buildDokumentWithVarianter("ARKIV", "SLADDET", "ORIGINAL", "FULLVERSJON", "PRODUKSJON", "PRODUKSJON_DLF"))
				.build();
		Journalpost journalpost = journalpostMapper.map(journalpostDto, createSaker(), createBrukerIdenter());
		assertThat(journalpost.getDokumenter().get(0).getDokumentvarianter()).hasSize(2);
		assertThat(journalpost.getDokumenter().get(0).getDokumentvarianter())
				.extracting(Dokumentvariant::getVariantformat)
				.containsExactlyInAnyOrder(ARKIV, SLADDET);
	}

	@Test
	void shouldMapFiltypePdfaToPdf() {
		JournalpostDto journalpostDto = baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.N)
				.dokumenter(Collections.singletonList(
						DokumentInfoDto.builder()
								.varianter(Collections.singletonList(VariantDto.builder()
										.variantf(VariantFormatCode.ARKIV)
										.filtype("PDFA")
										.build()))
								.build()))
				.build();
		Journalpost journalpost = journalpostMapper.map(journalpostDto, createSaker(), createBrukerIdenter());
		assertThat(journalpost.getDokumenter().get(0).getDokumentvarianter().get(0).getFiltype()).isEqualTo("PDF");
	}

	@Test
	void shouldMapNullFilstorrelseToZero() {
		JournalpostDto journalpostDto = baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.N)
				.dokumenter(Collections.singletonList(
						DokumentInfoDto.builder()
								.varianter(Collections.singletonList(VariantDto.builder()
										.variantf(VariantFormatCode.ARKIV)
										.filstorrelse(null)
										.build()))
								.build()))
				.build();
		Journalpost journalpost = journalpostMapper.map(journalpostDto, createSaker(), createBrukerIdenter());
		assertThat(journalpost.getDokumenter().get(0).getDokumentvarianter().get(0).getFilstorrelse()).isEqualTo(0);
	}
}