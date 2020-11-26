package excel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Excel {

	Workbook curWb;
	Sheet curSheet;
	Row curRow;
	Cell curCell;
	Font font;
	HSSFColor slxColor;
	XSSFColor xlsxColor;
	CellStyle cellStyle;
	ExcelType excelType;

	public Sheet getSheet() {
		return curSheet;
	}
	
	public Row getCurRow() {
		return curRow;
	}

	public enum ExcelType {

		EXCEL_XLS(".xls"), EXCEL_XLSX(".xlsx");

		private String value;

		private ExcelType(String type) {
			this.value = type;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String type) {
			this.value = type;
		}
	}

	private Excel() {
	}

	public Excel(String fileName) throws IOException {
		this(new FileInputStream(new File(fileName)), fileName);
	}

	private Excel(InputStream in, String fileName) {
		try {
			if (fileName.toString().toLowerCase().endsWith(ExcelType.EXCEL_XLS.getValue())) {// Excel 2003
				curWb = new HSSFWorkbook(in);
				this.excelType = ExcelType.EXCEL_XLS;
			} else if (fileName.toString().toLowerCase().endsWith(ExcelType.EXCEL_XLSX.getValue())) {// Excel 2007/2010
				curWb = new XSSFWorkbook(in);
				this.excelType = ExcelType.EXCEL_XLSX;
			}
		} catch (IOException e) {

		}
	}

	private Excel(ExcelType type, byte[] bytes) {
		try {
			this.excelType = type;
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			switch (type) {
			case EXCEL_XLS:
				curWb = new HSSFWorkbook(in);
				break;
			case EXCEL_XLSX:
				curWb = new XSSFWorkbook(in);
				break;
			default:
				break;
			}
		} catch (IOException e) {

		}
	}

	private Excel(ExcelType type) {
		this.excelType = type;
		switch (type) {
		case EXCEL_XLS:
			curWb = new HSSFWorkbook();
			break;
		case EXCEL_XLSX:
			curWb = new XSSFWorkbook();
			break;
		default:
			break;
		}
	}

	public String getExtName() {
		return excelType.value;
	}

	public ExcelType getExcelType() {
		return excelType;
	}

	public static Excel loadExcel(String uri) {
		return loadExcel(new File(uri));
	}

	public static Excel loadExcel(File file) {
		try {
			return new Excel(new FileInputStream(file), file.getName());
		} catch (FileNotFoundException e) {

		}
		return null;
	}

	public static Excel loadExcel(InputStream is, String name) {
		return new Excel(is, name);
	}

	public static Excel loadExcel(ExcelType type, byte[] bytes) {
		return new Excel(type, bytes);
	}

	public static Excel loadExcel(byte[] bytes) {
		return new Excel(ExcelType.EXCEL_XLSX, bytes);
	}

	public static Excel createExcel(ExcelType type) {
		return new Excel(type);
	}

	/*
	 * sheet operation
	 */
	/**
	 * If the name is null,
	 * 
	 * @param sheetname The name to set for the sheet.
	 * @return New sheet will be created.
	 */
	public Excel createSheet(String sheetname) {

		if (StringUtils.isNotEmpty(sheetname)) {
			curSheet = curWb.createSheet(sheetname);
			return this;
		}
		curSheet = curWb.createSheet();
		return this;
	}

	public Excel assignSheet(int index) {
		if (curSheet == null) {
			curSheet = curWb.createSheet();
		}
		curSheet = curWb.getSheetAt(index);
		
		return this;
	}

	public Excel assignSheet(String name) {
		curSheet = curWb.getSheet(name);
		if (curSheet == null) {
			curSheet = curWb.createSheet(name);
		}

		return this;
	}

	public String getSheetName(int index) {
		return curWb.getSheetName(index);
	}

	@SuppressWarnings("unchecked")
	public List<Sheet> getSheets() {
		Iterator<Sheet> i = curWb.iterator();
		List<Sheet> sheetList = new ArrayList<>();
		while (i.hasNext()) {
			sheetList.add(i.next());
		}
		return sheetList;
	}

	public int getSheetSize() {
		return this.getSheets().size();
	}

	/**
	 * @see org.apache.poi.ss.usermodel.Workbook.getNumberOfSheets
	 * @return
	 */
	public int getNumberOfSheets() {
		return curWb.getNumberOfSheets();
	}

	public Workbook getWorkbook() {
		return curWb;
	}

	/*
	 * row operation
	 */
	public Excel assignRow(int idx) {
		if (curSheet == null) {
			assignSheet(curWb.getNumberOfSheets() - 1);
		}
		this.curRow = curSheet.getRow(idx);
		if (curRow == null) {
			this.curRow = curSheet.createRow(idx);
		}
		this.curCell = null;
		return this;
	}

	public void removeSheet(int index) {
		int sheetCount = curWb.getNumberOfSheets();
		if (index < sheetCount) {
			curWb.removeSheetAt(index);
		}
	}

	public void removeRow(int index) {
		int lastRowNum = curSheet.getLastRowNum();
		if (index >= 0 && index < lastRowNum)
			curSheet.shiftRows(index + 1, lastRowNum, -1);
		// 将行号为rowIndex+1一直到行号为lastRowNum的单元格全部上移一行，以便删除rowIndex行
		if (index == lastRowNum) {
			Row removingRow = curSheet.getRow(index);
			if (removingRow != null)
				curSheet.removeRow(removingRow);
		}
	}

	public int getLastRowNum() {
		if (curSheet == null) {
			return 0;
		}
		return curSheet.getLastRowNum();
	}

	public boolean checkRowIsBlank(int idx) throws Exception {
		if (curSheet == null) {
			throw new Exception("current sheet is null!");
		}
		this.curRow = curSheet.getRow(idx);
		if (curRow != null) {
			for (int i = curRow.getFirstCellNum(); i < curRow.getLastCellNum(); i++) {
				Cell cell = curRow.getCell(i, MissingCellPolicy.RETURN_NULL_AND_BLANK);
				if (cell != null) {
					if (StringUtils.isNotBlank(cell.toString())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/*
	 * cell operation
	 */
	public Excel getCell(int idx) {
		if (curCell == null) {
			this.assignCell(idx);
			if (curCell == null)
			this.curCell = curRow.createCell(idx);
		}
		this.curCell = this.curRow.getCell(idx);
		
		return this;
	}

	public CellStyle getCellStyle() {
		return this.curCell.getCellStyle();
	}

	public void setCellStyle(CellStyle style) {
		this.curCell.setCellStyle(style);
	}
	
	public Cell getCurCell() {
			return this.curCell;
	
	}

	public void setAutoFilter(int firstRow, int lastRow, int firstCol, int lastCol) {
		this.curSheet.setAutoFilter(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
	}

	/**
	 * 
	 * @param index
	 * @return true if the Cell is empty or null
	 */
	public boolean checkCellIsNull(int index) {
		if (curSheet != null && curRow != null) {
			return curRow.getCell(index) == null;
		}
		return false;
	}

	public Excel assignCell(int index) {
		return assignCell(index, false);
	}

	public Excel assignCell(int index, boolean toCreate) {
		if (curRow != null) {
			curCell = curRow.getCell(index,
					toCreate ? MissingCellPolicy.CREATE_NULL_AS_BLANK : MissingCellPolicy.RETURN_NULL_AND_BLANK);
		}
		if(curCell == null) {
			curCell= curRow.createCell(index);
		}
		return this;
	}

	public int getLastCellNum() {
		if (curRow == null) {
			return 0;
		}
		return curRow.getLastCellNum();
	}

//    public Excel setCellValue(Object context, CellType type) {
//        this.curCell.setCellType(type);
//
//        switch (type) {
//        case NUMERIC:
//            this.curCell.setCellValue((double) context);
//            break;
//        case ERROR:
//
//            break;
//        case BOOLEAN:
//            this.curCell.setCellValue((boolean) context);
//            break;
//        default:
//            this.curCell.setCellValue("" + context);
//            break;
//        }
//        return this;
//    }

	public Excel setCellValue(double dou) {
		this.curCell.setCellValue(dou);
		return this;
	}

	public Excel setCellValue(String str) {
		this.curCell.setCellValue(str);
		return this;
	}

	public Excel setCellValue(Date date) {
		this.curCell.setCellValue(date);
		return this;
	}

	public Object getCellValue() {
		if (curCell != null) {
			switch (this.curCell.getCellType()) {
			case NUMERIC:
				if (HSSFDateUtil.isCellDateFormatted(this.curCell)) {
					return this.curCell.getDateCellValue();
				} else {
					String value = this.curCell.getNumericCellValue() + "";
					return new BigDecimal(value).toPlainString();
				}

			case STRING:
				return this.curCell.getStringCellValue();
			case FORMULA:
				switch (this.curCell.getCachedFormulaResultType()) {
				case NUMERIC:
					return this.curCell.getNumericCellValue();
				case STRING:
					return this.curCell.getRichStringCellValue();
				default:
					return this.curCell.getCellFormula();
				}
			case BOOLEAN:
				return this.curCell.getBooleanCellValue();
			case ERROR:
				return this.curCell.getErrorCellValue();
			case BLANK:
//                return this.curCell.getCellComment();
				return "";
			default:
				break;
			}
		}
		return "";
	}

	public static Object getCellValue(Cell cell) {
		if (cell != null) {
			switch (cell.getCellType()) {
			case NUMERIC:
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					return cell.getDateCellValue();
				} else {
					return cell.getNumericCellValue();
				}

			case STRING:
				return cell.getStringCellValue();
			case FORMULA:
				switch (cell.getCachedFormulaResultType()) {
				case NUMERIC:
					return cell.getNumericCellValue();
				case STRING:
					return cell.getRichStringCellValue();
				default:
					return cell.getCellFormula();
				}
			case BOOLEAN:
				return cell.getBooleanCellValue();
			case ERROR:
				return cell.getErrorCellValue();
			case BLANK:
//                return this.curCell.getCellComment();
				return "";
			default:
				break;
			}
		}
		return "";
	}

	public BigDecimal getNumericCellValue() {
		String _val = getStringCellValue();
		return StringUtils.isEmpty(_val) ? new BigDecimal(0) : new BigDecimal(_val);
	}

	public String getAbsoluteStringCellValue() {
		if (curCell != null) {
			switch (this.excelType.name()) {
			case "EXCEL_XLS":
				curCell.setCellType(CellType.STRING);
				break;
			case "EXCEL_XLSX":
				curCell.setCellType(CellType.STRING);
				break;
			default:
				break;
			}
			return this.curCell.getStringCellValue();
		}
		return "";
	}

	public String getStringCellValue() {
		Object _val = getCellValue();
		if (NumberUtils.isNumber("" + _val)) {
			String[] rs = String.valueOf(_val).split("\\.");
			if (rs.length > 1 && rs[1].equals("0")) {
				return rs[0];
			} else {
				return "" + _val;
			}
		} else {
			return "" + _val;
		}

	}

	public Date getDateCellValue() {
		return this.curCell.getDateCellValue();
	}

	/*
	 * cell style
	 */

	public Excel createCellStyle(String sfmt) {

		short fmt = curWb.getCreationHelper().createDataFormat().getFormat(sfmt);
		if (cellStyle == null) {
			cellStyle = curWb.createCellStyle();
		}
		cellStyle.setDataFormat(fmt);

		return this;
	}

	public Excel applyCellStyle(String sfmt) {
		if (curCell != null) {
			if (cellStyle != null) {
				short fmt = curWb.getCreationHelper().createDataFormat().getFormat(sfmt);
				cellStyle.setDataFormat(fmt);
			} else {
				cellStyle = curWb.createCellStyle();
			}
			this.curCell.setCellStyle(cellStyle);
		}
		return this;
	}

	public Excel applyCellStyle() {
		if (curCell != null && cellStyle != null) {
			this.curCell.setCellStyle(cellStyle);
		}
		return this;
	}

	/*
	 * other operation
	 */
	public byte[] toArray() {
		ByteArrayOutputStream obs = new ByteArrayOutputStream();
		try {
			curWb.write(obs);
		} catch (IOException e) {

		}
		return obs.toByteArray();
	}

	public void outputFile(String fileName) {
		try {
			String excelFileName = String.format("%s%s", fileName, getExtName());
			FileOutputStream fileOut = new FileOutputStream(excelFileName);
			curWb.write(fileOut);
			fileOut.flush();
			fileOut.close();
		} catch (IOException e) {

		}
	}

	public int getCurSheetRowCnt() {
		if (curSheet != null) {
			return curSheet.getPhysicalNumberOfRows();
		}
		System.out.println("Excel.getCurSheetRowCnt() curSheet is null");
		return 0;
	}

	public void autoSizeColumns() {
		int numberOfSheets = curWb.getNumberOfSheets();
		for (int i = 0; i < numberOfSheets; i++) {
			Sheet sheet = curWb.getSheetAt(i);
			if (sheet.getPhysicalNumberOfRows() > 0) {
				Row row = sheet.getRow(sheet.getFirstRowNum());
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					int columnIndex = cell.getColumnIndex();
					sheet.autoSizeColumn(columnIndex);
				}
			}
		}
	}

	/**
	 * 根據源Sheet樣式copy新Sheet
	 * 
	 * @param fromsheetname
	 * @param newsheetname
	 * @param targetFile
	 */
	private void copySheet(String fromsheetname, String newsheetname, String targetFile) {
		Workbook anothWb = null;
		try {
			FileInputStream fis = new FileInputStream(targetFile);
			anothWb = new HSSFWorkbook(fis);
			Sheet fromsheet = anothWb.getSheet(fromsheetname);
			if (fromsheet != null && anothWb.getSheet(newsheetname) == null) {
				Sheet newsheet = anothWb.createSheet(newsheetname);
				// 設定列印引數
//                newsheet.setMargin(HSSFSheet.TopMargin,fromsheet.getMargin(HSSFSheet.TopMargin));// 頁邊距（上）
//                newsheet.setMargin(HSSFSheet.BottomMargin,fromsheet.getMargin(HSSFSheet.BottomMargin));// 頁邊距（下）
//                newsheet.setMargin(HSSFSheet.LeftMargin,fromsheet.getMargin(HSSFSheet.LeftMargin) );// 頁邊距（左）
//                newsheet.setMargin(HSSFSheet.RightMargin,fromsheet.getMargin(HSSFSheet.RightMargin));// 頁邊距（右

//                HSSFPrintSetup ps = newsheet.getPrintSetup();
//                ps.setLandscape(false); // 列印方向，true：橫向，false：縱向(預設)
//                ps.setVResolution((short)600);
//                ps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE); //紙張型別

				File file = new File(targetFile);
				if (file.exists() && (file.renameTo(file))) {
					copyRows(anothWb, fromsheet, newsheet, fromsheet.getFirstRowNum(), fromsheet.getLastRowNum());
					FileOutputStream fileOut = new FileOutputStream(targetFile);
					anothWb.write(fileOut);
					fileOut.flush();
					fileOut.close();
				} else {
					System.out.println("檔案不存在或者正在使用,請確認...");
				}
			}
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拷貝Excel行
	 * 
	 * @param wb
	 * @param fromsheet
	 * @param newsheet
	 * @param firstrow
	 * @param lastrow
	 */
	private void copyRows(Workbook wb, Sheet fromsheet, Sheet newsheet, int firstrow, int lastrow) {
		if ((firstrow == -1) || (lastrow == -1) || lastrow < firstrow) {
			return;
		}
		// 拷貝合併的單元格
//        Region region = null;
//        for (int i = 0; i < fromsheet.getNumMergedRegions(); i++) {
//            region = fromsheet.getMergedRegionAt(i);
//            if ((region.getRowFrom() >= firstrow) && (region.getRowTo() <= lastrow)) {
//                newsheet.addMergedRegion(region);
//            }
//        }

		Row fromRow = null;
		Row newRow = null;
		Cell newCell = null;
		Cell fromCell = null;
		// 設定列寬
		for (int i = firstrow; i <= lastrow; i++) {
			fromRow = fromsheet.getRow(i);
			if (fromRow != null) {
				for (int j = fromRow.getLastCellNum(); j >= fromRow.getFirstCellNum(); j--) {
					int colnum = fromsheet.getColumnWidth((short) j);
					if (colnum > 100) {
						newsheet.setColumnWidth((short) j, (short) colnum);
					}
					if (colnum == 0) {
						newsheet.setColumnHidden((short) j, true);
					} else {
						newsheet.setColumnHidden((short) j, false);
					}
				}
				break;
			}
		}
		// 拷貝行並填充資料
		for (int i = 0; i <= lastrow; i++) {
			fromRow = fromsheet.getRow(i);
			if (fromRow == null) {
				continue;
			}
			newRow = newsheet.createRow(i - firstrow);
			newRow.setHeight(fromRow.getHeight());
			for (int j = fromRow.getFirstCellNum(); j < fromRow.getPhysicalNumberOfCells(); j++) {
				fromCell = fromRow.getCell((short) j);
				if (fromCell == null) {
					continue;
				}
				newCell = newRow.createCell((short) j);
				newCell.setCellStyle(fromCell.getCellStyle());
				CellType cType = fromCell.getCellTypeEnum();
				newCell.setCellType(cType);
				switch (cType) {
				case STRING:
					newCell.setCellValue(fromCell.getRichStringCellValue());
					break;
				case NUMERIC:
					newCell.setCellValue(fromCell.getNumericCellValue());
					break;
				case FORMULA:
					newCell.setCellFormula(fromCell.getCellFormula());
					break;
				case BOOLEAN:
					newCell.setCellValue(fromCell.getBooleanCellValue());
					break;
				case ERROR:
					newCell.setCellValue(fromCell.getErrorCellValue());
					break;
				default:
					newCell.setCellValue(fromCell.getRichStringCellValue());
					break;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static Excel turnToNoMergedCellExcel(Excel excel) {

		for (int sheetCnt = 0; sheetCnt < excel.getNumberOfSheets(); ++sheetCnt) {
			Sheet sheet = excel.assignSheet(sheetCnt).getSheet();
			for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
				CellRangeAddress region = sheet.getMergedRegion(i); // Region of merged cells
				int colIndex = region.getFirstColumn(); // number of columns merged
				int rowNum = region.getFirstRow(); // number of rows merged
				Cell firstCellInMergedArea = sheet.getRow(rowNum).getCell(colIndex);

				Object cellValue = getCellValue(firstCellInMergedArea);

				for (Row row : sheet) {
					for (Cell cell : row) {
						if (region.isInRange(cell)) {
							if (cellValue instanceof Date)
								cell.setCellValue((Date) cellValue);
							else if (cellValue instanceof Boolean)
								cell.setCellValue((Boolean) cellValue);
							else if (cellValue instanceof Double)
								cell.setCellValue((Double) cellValue);
							else
								cell.setCellValue(cellValue.toString());
						}
					}
				}
				sheet.removeMergedRegion(i);
			}
		}
		return excel;
	}

}// end of class