package CommonAPI;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateSpreadsheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

public class GoogleSheetHandler {
	/*
	 * 設定預設最大的欄位為AZ
	 */
	private static String sheetName = "工作表1";
	
	public static void main(String... arg) throws IOException, GeneralSecurityException {
	}


	static String readRange = sheetName + "!A1:AZ";

	public static String createColRange(int col) {
		String colStr = getChar(col).toUpperCase();
		return sheetName + "!" + colStr + ":" + colStr;
	}

	// 搜尋範圍為1000筆
	// col放header的index
	public static int findOneCol(Sheets service, String spreadsheetId, int col, String word) throws IOException {
		final String range = createColRange(col);
		ValueRange result = service.spreadsheets().values().get(spreadsheetId, range).execute();
		int index = 1;
		for (List<Object> oneCell : result.getValues()) {
			if (oneCell.size() > 0) {
				if (oneCell.get(0).toString().equals(word)) {
					break;
				}
			}
			++index;
		}
		return index;
//		return "!A" + index + ":Y" + index;
//		return service.spreadsheets().values().get(spreadsheetId, selectedRow).execute().getValues();

	}

	public static void clearOneCol(Sheets service, String spreadsheetId, int col) throws IOException {
	
		String colStr = getChar(col).toUpperCase();
		String range  = sheetName + "!" + colStr + "2:" + colStr;
		List<List<Object>> toWrite = new ArrayList<>();
		
		for (int idx = 0; idx < findSheetLastRow(service, spreadsheetId); ++idx) {
			List<Object> track = new ArrayList<>();
			track.add("");
			toWrite.add(track);
		}
		
		if (StringUtils.isNotEmpty(spreadsheetId)) {
			List<List<Object>> values = toWrite;
			ValueRange body = new ValueRange().setValues(values);
			UpdateValuesResponse result = service.spreadsheets().values().update(spreadsheetId, range, body).setValueInputOption("RAW").execute();

			System.out.printf("%d cells updated.", result.getUpdatedRows());
		} else {
			throw new IOException("無法建立雲端試算表");
		}

	}

	public static int findSheetLastRow(Sheets service, String spreadsheetId) throws IOException {
		ValueRange result = service.spreadsheets().values().get(spreadsheetId, readRange).execute();
		return result.getValues() != null ? result.getValues().size() : 0;
	}

	public static Sheets getSheetService() throws GeneralSecurityException, IOException {
		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

	}

	// 設定插入列，不覆蓋掉原本的質
	public static void addOneRowOnSheet(Sheets service, String spreadsheetId, int numOfRow, List<List<Object>> value)
			throws IOException, GeneralSecurityException {

//		String lastColChar = "E";
//		final String range = sheetName + "!A" + String.valueOf(lastRowNumber) + ":" + lastColChar;
		final String range = sheetName + "!A" + String.valueOf(numOfRow) + ":AZ" + String.valueOf(numOfRow);

//		spreadsheetId = createSheetFile(service, fileName).getSpreadsheetId();
		if (StringUtils.isNotEmpty(spreadsheetId)) {
			List<List<Object>> values = value;
			// Additional rows ...
//			);
			ValueRange body = new ValueRange().setValues(values);
			AppendValuesResponse result = service.spreadsheets().values().append(spreadsheetId, range, body)
					.setValueInputOption("RAW").setInsertDataOption("INSERT_ROWS").execute();

//			System.out.printf("%d cells updated.", result.getUpdates());
		} else {
			throw new IOException("無法建立雲端試算表");
		}

	}

	public static void updateOneValue(Sheets service, String spreadsheetId, int col, int row, List<List<Object>> value)
			throws IOException, GeneralSecurityException {
		String colStr = getChar(col).toUpperCase();
		final String range = sheetName + "!" + colStr + String.valueOf(row) + ":" + colStr + String.valueOf(row);
		if (StringUtils.isNotEmpty(spreadsheetId)) {
			List<List<Object>> values = value;
			ValueRange body = new ValueRange().setValues(values);
			service.spreadsheets().values().append(spreadsheetId, range, body).setValueInputOption("RAW").execute();
		} else {
			throw new IOException("無法建立雲端試算表");
		}

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
		InputStream in = GoogleSheetHandler.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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

	public static Spreadsheet createSheetFile(Sheets service, String fileName) throws IOException {
		Spreadsheet spreadsheet = new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(fileName));
		spreadsheet = service.spreadsheets().create(spreadsheet).setFields("spreadsheetId").execute();
		return spreadsheet;
	}

	public static void batchUpdate() {
//		List<List<Object>> values = Arrays.asList(
//        Arrays.asList(
//                "A","A","A","A","A"
//        )
//        // Additional rows ...
//);
//List<ValueRange> data = new ArrayList<>();
//data.add(new ValueRange()
//        .setRange(range)
//        .setValues(values));
//
//BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
//        .setValueInputOption("RAW")
//        .setData(data);
//BatchUpdateValuesResponse result =
//        service.spreadsheets().values().batchUpdate(spreadsheetId, body).execute();
//System.out.printf("%d cells updated.", result.getTotalUpdatedCells());
	}

//	public static String lastColChar = "E";
//
	public static String getChar(int rowNum) {
		int a = (int) 'a';
		// if totalNumsOfCol==1, lastColChar='A'
		return String.valueOf((char) (a + rowNum - 1));
	}

//	public GoogleSheetHandler(int lastColChar) {
//		this.lastColChar = setLastColChar(lastColChar);
//	}

	// TODO Auto-generated method stub
	private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "sheetToken/tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	private static final String CREDENTIALS_FILE_PATH = "/credentialsForSheet.json";

}
