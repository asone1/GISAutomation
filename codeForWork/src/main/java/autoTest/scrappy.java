package autoTest;

import static CommonAPI.ChineseAddressHandler.newLine;
import static OnlineLandSearch.GovEmap.locationToArr;
import static OnlineLandSearch.GovEmap.search;
import static OnlineLandSearch.Jurdical.AddressBuilder;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang.StringUtils;

import dataStructure.ExcelValue;
import dataStructure.ExcelValue.Row;
import dataStructure.ExcelValue.Row.Item;
import excel.Excel;

public class scrappy {

	private static ExcelValue excelValue;
	static Map<String, Integer> jurdicalHeader;
	final static String address_col_name = "地址";
	final static String unit_price_name = "萬/1坪";

//	台南一般.xls
	public final static String fileName = "法拍地查詢資料.xls";
	public final static String defaultPath = FileSystemView.getFileSystemView().getHomeDirectory().getPath();

	public static void setExcelHeader(Excel jurdicalResult) {
		for (int cellIndex = 0; cellIndex < jurdicalResult.getLastCellNum(); ++cellIndex) {
			String headerName = jurdicalResult.assignCell(cellIndex).getCellValue().toString();
			if (excelValue.excelHeaders.containsKey(headerName)) {
				jurdicalHeader.put(headerName, cellIndex);
			}
		}
	}

	public static String createLandId(Excel jurdicalResult, int rowCount, int colIdx) {
		String id = String.join("_", Arrays.asList(jurdicalResult.assignSheet(0).assignRow(rowCount).assignCell(colIdx)
				.getCellValue().toString().split("\\D+")));
		String price = jurdicalResult.assignSheet(0).assignRow(rowCount).assignCell(jurdicalHeader.get("價錢(萬)"))
				.getCellValue().toString().replace(".0", "");
		return id + (StringUtils.isBlank(price) ? "" : ("_" + price));
	}
	
	

	public static void main(String[] args) throws InterruptedException {
		jurdicalHeader = new HashMap<>();
		excelValue = new ExcelValue(defaultPath + "/excelHeader.xlsx");
		Excel jurdicalResult = Excel.loadExcel(defaultPath + "/" + fileName);
		jurdicalResult.assignSheet(0).assignRow(0);
		// set header
		setExcelHeader(jurdicalResult);
		Excel fileToSave = null;

		List<String> pics = excelValue.getHeader("pic");
		// for every row in excel
		for (int rowCount = 1; rowCount <= jurdicalResult.getLastRowNum(); ++rowCount) {
			String location[] = new String[4];
			String address = jurdicalResult.assignSheet(0).assignRow(rowCount)
					.assignCell(jurdicalHeader.get(address_col_name)).getCellValue().toString();
			Double unit_Price = null;

			// 持分為全部才搜尋
			try {
				unit_Price = Double.parseDouble(jurdicalResult.assignSheet(0).assignRow(rowCount)
						.assignCell(jurdicalHeader.get(unit_price_name)).getCellValue().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (StringUtils.isNotBlank(address) && unit_Price != null && unit_Price <= 0.9) {
				Row row = excelValue.new Row();
				// for every cell (將法拍資料放入item中)
				for (Entry<String, Integer> map : jurdicalHeader.entrySet()) {
					Item item = row.new Item();
					if (map.getKey().equals(address_col_name)) {
						String county = jurdicalResult.assignSheet(0).assignRow(rowCount).assignCell(4).getCellValue()
								.toString();
						String location_spec = jurdicalResult.assignSheet(0).assignRow(rowCount)
								.assignCell(map.getValue()).getCellValue().toString();
						item.setValue(AddressBuilder(county, location_spec));
						location = locationToArr(county, location_spec);

					} else if (map.getKey().contains("字號")) {
						item.setValue(createLandId(jurdicalResult, rowCount, map.getValue()));
					} else {
						item.setValue(jurdicalResult.assignSheet(0).assignRow(rowCount).assignCell(map.getValue())
								.getCellValue().toString().replace(newLine, ""));
					}
					item.setColumn(map.getKey());
					row.addItem(item);
				}

				// 每筆row為單位 進入國土繪測查詢
				try {
					search(location, row, pics, excelValue.excelHeaders);
				} catch (Exception e) {
					Item err = row.new Item();
					err.setColumn("查詢備註");
					err.setValue(e.toString());
					row.addItem(err);

				}
				excelValue.addaRow(row);
//				System.out.println("!!"+row.getItems());
				if (location != null && location.length > 1) {
					fileToSave = excelValue.itemsToExcel(location[0]);
				} else {
					fileToSave = excelValue.itemsToExcel("查詢結果");
				}

//				AddJurdicalLink(fileToSave, fileName);

				try {
					String filePath = defaultPath + "/" + fileName.replace(".xls", "") + "結果.xls";
					FileOutputStream out = new FileOutputStream(new File(filePath));
					fileToSave.getWorkbook().write(out);
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	

	public static Robot returnRobot() {
		Robot robot = null;
		try {
			robot = new Robot();
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return robot;
	}

	public static void zoomOut(int times) {
		Robot robot = returnRobot();
		for (int i = 0; i < times; i++) {
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_SUBTRACT);
			robot.keyRelease(KeyEvent.VK_SUBTRACT);
			robot.keyRelease(KeyEvent.VK_CONTROL);
		}
	}

	public static void zoomIn(int times) {
		Robot robot = returnRobot();
		for (int i = 0; i < times; i++) {
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_ADD);
			robot.keyRelease(KeyEvent.VK_ADD);
			robot.keyRelease(KeyEvent.VK_CONTROL);
		}
	}
}