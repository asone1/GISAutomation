package autoTest;

import static autoTest.scrappy.*;
import static CommonAPI.seleniumCommon.*;
import static OnlineLandSearch.GovEmap.*;
import static OnlineLandSearch.Jurdical.*;
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
