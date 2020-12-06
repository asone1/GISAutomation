package autoTest;

import static CommonAPI.PDFHandler.readPdfContent;
import static OnlineLandSearch.Jurdical.searchById;
import static OnlineLandSearch.Jurdical.startJurdical;
import static autoTest.scrappy.defaultPath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.openqa.selenium.WebDriver;

import excel.Excel;

public class AddJurdicalLinkToExcel {
//	static String fileName = "台南一般結果.xls";

	static String label = "原住民";

	public static void AddJurdicalLink(Excel excel, String fileName) throws InterruptedException, IOException {
		// TODO Auto-generated method stub
//		Excel excel = Excel.loadExcel(defaultPath + "/" + fileName);
		excel.assignSheet(0);
		WebDriver driver = startJurdical();
		for (int rowCount = 1; rowCount <= excel.getLastRowNum(); ++rowCount) {
			driver.get("https://aomp.judicial.gov.tw/abbs/wkw/WHD2A00.jsp");
			String landId = excel.assignRow(rowCount).getCell(0).getStringCellValue().split("_")[1];
//			boolean isLand = excel.assignRow(rowCount).getCell(1).getStringCellValue().contains("特別")? true:false;
			boolean landType = excel.assignRow(rowCount).getCell(1).getStringCellValue().contains("特別") ? true : false;
			String url = searchById(driver, landId, excel.assignRow(rowCount).getCell(3).getAbsoluteStringCellValue(),
					landType);
			if (StringUtils.isNotBlank(url)) {
				Cell firstCell = excel.assignRow(rowCount).assignCell(0).getCurCell();
				Hyperlink link = excel.getWorkbook().getCreationHelper().createHyperlink(HyperlinkType.URL);
				link.setAddress(url);
				if (firstCell != null) {
					firstCell.setHyperlink(link);

					String pdfContent= readPdfContent(url);
					if (pdfContent.contains(label)) {

					}

				}
			}
		}
		driver.quit();
		try {
			FileOutputStream out = new FileOutputStream(
					new File(defaultPath + "/" + fileName.replace(".xls", "") + "新增連結結果.xls"));
			excel.getWorkbook().write(out);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
