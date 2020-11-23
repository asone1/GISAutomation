package autoTest;

import java.io.IOException;
import static autoTest.commonMethod.relativeSymbol;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;

import autoTest.ExcelValue.Row.Item;
import excel.Excel;
import excel.Excel.ExcelType;
import lombok.Data;
import static autoTest.scrappy.defaultPath;

public class ExcelValue {
	private List<Row> allRows;

	public class Row {
		private List<Item> items;

		public class Item {
			String column;
			String cell;

			public Item setItem(String col, String cell) {
				this.column = col;
				this.cell = cell;
				return this;
			}

			public boolean equalTo(Item item) {
				return this.column.equals(item.column);
			}

			public String toString() {
				return column + ":" + cell;
			}

			public boolean isNotEmpty() {
				return StringUtils.isNotEmpty(column) && StringUtils.isNotEmpty(cell);
			}

		}

		public void addItem(Item item) {
			if (CollectionUtils.isEmpty(items)) {
				items = new ArrayList<>();
			}
			if (item.isNotEmpty() && getItem(item.column) == null) {
				items.add(item);
			}
			System.out.println(this.getItems());
		}

		public Item getItem(String column) {
			for (Item i : items) {
				if (i.column.contains(column)) {
					return i;
				}
			}
			return null;
		}

		public List<Item> getItems() {
			return items;
		}

		public void setItems(List<Item> items) {
			this.items = items;
		}
	}

	// column index, column name
	public Map<String, Integer> excelHeaders;

	public List<String> getHeader(String containedPhrase) {
		List<String> headers = new ArrayList<>();
		for (Entry<String, Integer> map : excelHeaders.entrySet()) {
			if (map.getKey().contains(containedPhrase)) {
				headers.add(map.getKey());
			}
		}
		return headers;
	}

	public void addaRow(Row row) {
		if (allRows == null) {
			allRows = new ArrayList<>();
		}
		allRows.add(row);
	}

	public ExcelValue(String path) {
		setExcelHeaders(path);
	}

	private boolean setExcelHeaders(String path) {
		try {
			Excel headerfile = new Excel(path);
			headerfile.assignSheet(0).assignRow(0);
			excelHeaders = new HashMap();
			for (int i = 0; i < headerfile.getLastCellNum(); ++i) {
				excelHeaders.put(headerfile.assignCell(i).getCellValue().toString(), i);

			}
			System.out.println(excelHeaders);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}

	public Excel itemsToExcel(String sheetCounty) {
		
		Excel newExcel = Excel.createExcel(ExcelType.EXCEL_XLS);

		newExcel.assignSheet(sheetCounty);
		for(Entry<String, Integer> e:this.excelHeaders.entrySet()) {
			newExcel.assignRow(0);
			newExcel.assignCell(e.getValue());
			newExcel.setCellValue(e.getKey());
		}
//		int rowCount = newExcel.getLastRowNum() + 1;
		int rowCount = 1;
		for (Row row : allRows) {
			for (Item item : row.getItems()) {
				newExcel.assignRow(rowCount);
				newExcel.assignCell(excelHeaders.get(item.column));
				if (item.cell.startsWith(relativeSymbol)) {
					setLink(item.cell, newExcel, newExcel.getCurCell());
				} else {
					newExcel.setCellValue(item.cell);
				}
				System.out.println(item);
			}
			++rowCount;
		}

		return newExcel;
	}

	public void setLink(String url, Excel newExcel, Cell cell) {
		/*
		 * setPath
		 */
		Hyperlink link = newExcel.getWorkbook().getCreationHelper().createHyperlink(HyperlinkType.URL);
		link.setAddress(url);
		if (cell != null) {
			cell.setHyperlink(link);
			cell.setCellValue(url);
		}
	}
}
