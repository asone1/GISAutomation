package autoTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import static autoTest.scrappy.defaultPath;
import static autoTest.scrappy.fileName;
public class separateDataFromExcel {
	public static void main(String args[]) throws IOException {

		try {
//obtaining input bytes from a file  
			FileInputStream fis = new FileInputStream(new File(defaultPath + "\\" + fileName.replace("查詢", "查詢前")));

//creating workbook instance that refers to .xls file  
			HSSFWorkbook wb = new HSSFWorkbook(fis);
//creating a Sheet object to retrieve the object  
			HSSFSheet sheet = wb.getSheetAt(0);

// 大loop，第一排開始 (橫排)
			for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				if (row != null) {
// 每排的第五格 是我們要的資料，存入cell object
					Cell cell = row.getCell(5);

					if (cell != null) {
// Found column and there is value in the cell.
// 取出第五格cell object的 string value
						String FifthCell = cell.getStringCellValue();
// maybe the length of the array will append
// 第五格的string中，若遇到空格，就split，存到下一個string array
						String arr[] = FifthCell.split("\n");
// 要把值放入表格後方還未有資料的cell，

// 把欲更動的資料存在最後一欄 (lastColumn )
						short lastColumn = row.getLastCellNum();
// 計算arr資料數
						int arrIndex = 0;
// 目前的欄位 = 最後一欄 +新增的arr資料數
						int currentColumn = lastColumn + arrIndex;

//小數點兩位
						DecimalFormat df = new DecimalFormat("#.##");
//四捨五入
						df.setRoundingMode(RoundingMode.CEILING);
//土地除與總價格得到一坪的價錢，把土地大小跟價格放入cell後計算 ，所以把此參數放在外面
						double land = 0;
						double price = 0;
//新增的欄位名稱
						String header[] = { "地址", "坪數", "價錢(萬)", "萬/1坪" };
						for (arrIndex = 0; arrIndex < arr.length; arrIndex++) {
// At the last column in each row, add string array to newcell

							Cell newCell = row.createCell(currentColumn + arrIndex);

//加上新增的欄位名稱
							if (rowIndex == 0) {
								for (int i = 0; i < 4; i++) {
									Cell head = row.createCell(currentColumn + i);
									head.setCellValue(header[i]);
								}

							} else {
								if (FifthCell.contains("全部")) {

									switch (arrIndex) {
									case 0:
										String address = arr[arrIndex];
										newCell.setCellValue(address);
										break;
									case 1:
										String LandArr[] = arr[arrIndex].split("坪");
										String Land = LandArr[0];
										land = Integer.parseInt(Land);
										newCell.setCellValue(land);
										break;
									case 2:
										String Price = arr[arrIndex].substring(11).replace("元", "").replace(",", "")
												.trim();
										price = Integer.parseInt(Price) / 10000;
//萬為單位
										newCell.setCellValue(price);
										break;
									}

								}
							}

						}
//把欄位調整到適當大小
						sheet.autoSizeColumn(currentColumn);
						if (land != 0) {
							Cell calculate = row.createCell(currentColumn + arrIndex);

							calculate.setCellValue(df.format(price / land));
						}
					}
				}
			}

			System.out.println("OK");

			FileOutputStream outputStream = new FileOutputStream(new File(defaultPath +  "\\" + fileName));

			wb.write(outputStream);
			outputStream.close();
			fis.close();
			wb.close();
		} catch (IOException | EncryptedDocumentException ex) {
			ex.printStackTrace();
		}

	}

}
