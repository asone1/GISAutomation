package CommonAPI;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import static OnlineLandSearch.findLandOnline.*;
public class GoogleDriverHandler {

	
	/*
	 * create folders according to its county (if folder doesn't exist), save it as
	 * anyone-can-view, and return its link
	 */
	public static Drive getService() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
	}
	
	public static String savePicToGoogle(Drive service,String county, String picName) throws IOException, GeneralSecurityException {
//		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//		Drive service= new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//				.setApplicationName(APPLICATION_NAME).build();
		
		String folderId = isFolderNameExist(service, county);
		if (StringUtils.isBlank(folderId)) {
			folderId = createFolder(service, county).getId();
		}

		File pic = uploadFile(service, picName, tempShotPath, folderId);
		setViewerToAnyonePermission(service, pic.getId());
		// https://drive.google.com/file/d/1WiZQ9yrnkQyFSP8ssctmM5cWHpkdOV2I/view?usp=sharing
		return "https://drive.google.com/file/d/" + pic.getId() + "/view?usp=sharing";
	}

	

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = GoogleDriverHandler.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static File uploadFile(Drive service, String picName, String sourcePathOfUploaded, String parentFileId)
			throws IOException {
		File fileMetadata = new File();
		fileMetadata.setName(picName + ".jpg");
		fileMetadata.setParents(Collections.singletonList(parentFileId));
//		java.io.File filePath = new java.io.File("C:\\Users\\Acer\\Desktop\\山崩.png");
		java.io.File filePath = new java.io.File(sourcePathOfUploaded);
		FileContent mediaContent = new FileContent("image/jpeg", filePath);
		return service.files().create(fileMetadata, mediaContent).setFields("id, parents").execute();
	}

	public static File createFolder(Drive service, String folderName) throws IOException {
		File fileMetadata = new File();
		fileMetadata.setName(folderName);
		fileMetadata.setMimeType("application/vnd.google-apps.folder");
		File file = service.files().create(fileMetadata).setFields("id").execute();
		return file;
	}

	public static String isFolderNameExist(Drive service, String folderName) throws IOException {
		String folderId = "";
		FileList result = service.files().list().setPageSize(30).setFields("nextPageToken, files(id, name)").execute();
		List<File> files = result.getFiles();
		if (files != null && !files.isEmpty()) {
			for (File file : files) {
				if (file.getName().equals(folderName)) {
					folderId = file.getId();
					return folderId;
				}
			}
		}
		return folderId;
	}

	public static void setViewerToAnyonePermission(Drive service, String fileId) throws IOException {
		BatchRequest batch = service.batch();
		JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
			@Override
			public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
				// Handle error
				System.err.println(e.getMessage());
			}

			@Override
			public void onSuccess(Permission permission, HttpHeaders responseHeaders) throws IOException {
//				System.out.println("Permission ID: " + permission.getId());
			}
		};
		Permission viewerPermission = new Permission().setType("anyone").setRole("reader");
		service.permissions().create(fileId, viewerPermission).setFields("id").queue(batch, callback);
		batch.execute();
	}

	private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "driveToken/tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

}
