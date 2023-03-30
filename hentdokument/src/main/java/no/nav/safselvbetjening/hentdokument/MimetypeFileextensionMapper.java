package no.nav.safselvbetjening.hentdokument;

import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper fra mimetype til filendelse for filtyper som er kjent for fagarkivet til NAV.
 */
public final class MimetypeFileextensionMapper {

	private static final Map<String, String> mimetypeFileextensions = new HashMap<>();
	private static final String NO_EXTENSION = "";

	static {
		mimetypeFileextensions.put("application/pdf", "pdf");
		mimetypeFileextensions.put("text/xml", "xml");
		mimetypeFileextensions.put("application/rtf", "rtf");
		mimetypeFileextensions.put("application/afp", "afp");
		mimetypeFileextensions.put("application/dlf", "dlf");
		mimetypeFileextensions.put("image/jpeg", "jpeg");
		mimetypeFileextensions.put("image/tiff", "tiff");
		mimetypeFileextensions.put("application/msword", "doc");
		mimetypeFileextensions.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
		mimetypeFileextensions.put("application/vnd.ms-excel", "xls");
		mimetypeFileextensions.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
		mimetypeFileextensions.put("application/json", "json");
	}

	private MimetypeFileextensionMapper() {
		// ingen instansiering
	}

	public static String toFileextension(final MediaType mimetype) {
		if (mimetype == null) {
			return NO_EXTENSION;
		}
		return toFileextension(mimetype.toString());
	}

	/**
	 * Oversetter fra mimetype til filendelse inkludert seperator "."
	 * Hvis mimetype ikke finnes, returneres en blank string.
	 *
	 * @param mimetype Mimetype. Eksempel: "application/pdf"
	 * @return Filendelse inkludert skilletegn ".". Eksempel ".pdf".
	 */
	public static String toFileextension(final String mimetype) {
		return "." + mimetypeFileextensions.getOrDefault(mimetype, NO_EXTENSION);
	}
}
