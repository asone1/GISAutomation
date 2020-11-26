package autoTest;

import static autoTest.Jurdical.*;
import static autoTest.scrappy.*;
import static autoTest.seleniumCommon.*;
import static autoTest.GovEmap.*;
import static autoTest.AddJurdicalLinkToExcel.*;
import java.awt.AWTException;
import java.awt.Robot;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import excel.Excel;
import excel.Excel.ExcelType;

public class test {
	static String defaultFilePath = "/花蓮";

	public static void screenshot(WebDriver driver) throws Exception {
		// TODO Auto-generated method stub

		Thread.sleep(5000);
		Robot robot = null;
		try {
			robot = new Robot();
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String idName = "test1117";
		String urls[] = new String[4];
		urls[0] = defaultPath + defaultFilePath + "/" + idName + "0.png";
		takeSnapShot(driver, urls[0]);
		for (int i = 0; i < 5; i++) {
//					robot.keyPress(KeyEvent.VK_CONTROL);
//					robot.keyPress(KeyEvent.VK_SUBTRACT);
//					robot.keyRelease(KeyEvent.VK_SUBTRACT);
//					robot.keyRelease(KeyEvent.VK_CONTROL);
		}
		Thread.sleep(1500);
		urls[1] = defaultPath + defaultFilePath + "/" + idName + "0_S.png";
		takeSnapShot(driver, urls[1]);

		mustDo(driver, By.id("qt-ctrl-quickSwitch"));

		Thread.sleep(2000);
		urls[2] = defaultPath + defaultFilePath + "/" + idName + "1_S.png";
		takeSnapShot(driver, urls[2]);
		for (int i = 0; i < 5; i++) {
			WebElement html = driver.findElement(By.tagName("html"));
			html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
		}
		Thread.sleep(1500);
		urls[3] = defaultPath + defaultFilePath + "/" + idName + "1.png";
		takeSnapShot(driver, urls[3]);

		Excel newExcel = Excel.createExcel(ExcelType.EXCEL_XLS);
		int idx = 0;
		for (String url : urls) {
			Hyperlink link = newExcel.getWorkbook().getCreationHelper().createHyperlink(HyperlinkType.URL);
			link.setAddress(url.replace(defaultPath, "./"));
			Cell cell = newExcel.assignSheet(0).assignRow(0).assignCell(idx++).getCurCell();
			if (cell != null) {
				cell.setHyperlink(link);
				cell.setCellValue(url);
			}

		}
		try {
			FileOutputStream out = new FileOutputStream(new File(defaultPath + "/fileT.xls"));
			newExcel.getWorkbook().write(out);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver.quit();

	}

	

	public static void main(String[] args) throws Exception {
		Excel excel = Excel.loadExcel(defaultPath + "/" + "法拍地查詢資料結果.xls");
		excel.assignSheet(0);
		AddJurdicalLink(excel, fileName);
		
//		for (int index = 1; index < excel.getCurSheetRowCnt(); ++index) {
//			String address = excel.assignRow(index).getCell(0).getAbsoluteStringCellValue();
//			if (StringUtils.isNotBlank(address) && address.equals("108_022682_80")) {
////				String county = excel.assignRow(index).getCell(4).getAbsoluteStringCellValue();
//				System.out.println("外面"+ excel.assignRow(index).getCell(0).getCellStyle().getFillForegroundColor());
//				
//			}
//
//			
//		}

	}
}
