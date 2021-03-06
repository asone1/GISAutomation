package googleAPI;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.docs.v1.model.ParagraphElement;
import com.google.api.services.docs.v1.model.StructuralElement;
import com.google.api.services.docs.v1.model.TableCell;
import com.google.api.services.docs.v1.model.TableRow;
import com.google.api.services.docs.v1.model.TextRun;

public class ExtractText {
	private static final String APPLICATION_NAME = "Google Docs API Extract Guide";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final String DOCUMENT_ID = "1ocUKIXRexWZA1otSn0xuoOlaSyjeyTY4FOR1hbGHU0w";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(DocsScopes.DOCUMENTS_READONLY);

	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

	/**
	 * Creates an authorized Credential object.
	 *
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = ExtractText.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	/**
	 * Returns the text in the given ParagraphElement.
	 *
	 * @param element a ParagraphElement from a Google Doc
	 */

	static String readParagraphElement(ParagraphElement element) {
		TextRun run = element.getTextRun();
		if (run == null || run.getContent() == null) {
			// The TextRun can be null if there is an inline object.
			return "";
		}
		return run.getContent();
	}

	private static String readLink(ParagraphElement element) {
		TextRun run = element.getTextRun();
		if (run != null && run.getTextStyle() != null && run.getTextStyle().getLink() != null) {
			// The TextRun can be null if there is an inline object.
			return run.getTextStyle().getLink().getUrl();
		} else
			return "";
	}

	/**
	 * Recurses through a list of Structural Elements to read a document's text
	 * where text may be in nested elements.
	 *
	 * @param elements a list of Structural Elements
	 */
	private static String readStructrualElements(List<StructuralElement> elements) {
		StringBuilder sb = new StringBuilder();
		for (StructuralElement element : elements) {
			if (element.getParagraph() != null) {
				for (ParagraphElement paragraphElement : element.getParagraph().getElements()) {
					sb.append(readParagraphElement(paragraphElement));
				}
			} else if (element.getTable() != null) {
				// The text in table cells are in nested Structural Elements and tables may be
				// nested.
				for (TableRow row : element.getTable().getTableRows()) {
					for (TableCell cell : row.getTableCells()) {
						sb.append(readStructrualElements(cell.getContent()));
					}
				}
			} else if (element.getTableOfContents() != null) {
				// The text in the TOC is also in a Structural Element.
				sb.append(readStructrualElements(element.getTableOfContents().getContent()));
			}
		}
		return sb.toString();
	}

	private static HashSet<String> readEveryLink(List<StructuralElement> elements) {
		HashSet<String> everyUrl = new HashSet<String>();
		for (StructuralElement element : elements) {
			if (element.getParagraph() != null) {
				for (ParagraphElement paragraphElement : element.getParagraph().getElements()) {
					String content = readParagraphElement(paragraphElement);
					if(!readLink(paragraphElement).isEmpty()) {
						everyUrl.add(readLink(paragraphElement));
					}
					if (content.startsWith("http")) {
						everyUrl.add(content);
					}
				}
			} else if (element.getTable() != null) {
				// The text in table cells are in nested Structural Elements and tables may be
				// nested.
				for (TableRow row : element.getTable().getTableRows()) {
					for (TableCell cell : row.getTableCells()) {
						String content = readStructrualElements(cell.getContent());
						if (content.startsWith("http")) {
							everyUrl.add(content);
						}
					}
				}
			} else if (element.getTableOfContents() != null) {
				// The text in the TOC is also in a Structural Element.
				String content = readStructrualElements(element.getTableOfContents().getContent());
				if (content.startsWith("http")) {
					everyUrl.add(content);
				}
			}
		}
		return everyUrl;
	}

	public static void main(String... args) throws IOException, GeneralSecurityException {
		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Docs service = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		Document doc = service.documents().get(DOCUMENT_ID).execute();
		for(String link : readEveryLink(doc.getBody().getContent())){
			System.out.println(link);
		}
		// paragraphElement.getTextRun().getTextStyle().getLink()
	}
}
