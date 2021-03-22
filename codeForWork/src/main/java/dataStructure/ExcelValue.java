package dataStructure;

import static CommonAPI.ChineseAddressHandler.relativeSymbol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;

import dataStructure.ExcelValue.Row.Item;
import excel.Excel;
import excel.Excel.ExcelType;

public class ExcelValue {

	public ExcelValue() {
	}

	private List<Row> allRows;

	public class Row {
		private List<Item> items;

		public class Item {
			private String column;
			private String value;

			public String getValue() {
				return value;
			}

			public String getColumn() {
				return column;
			}

			public void setColumn(String col) {
				this.column = col.trim();
			}

			public void setValue(String val) {
				this.value = val.trim();
			}

			public Item setItem(String col, String cell) {
				setColumn(col);
				setValue(cell);
				return this;
			}

			public String getItemValue() {
				if (StringUtils.isNotBlank(this.getValue())) {
					return this.getValue();
				}
				return "";
			}

			public boolean equalTo(Item item) {
				return this.column.equals(item.column);
			}

			public String toString() {
				return column + ":" + value;
			}

			public boolean isNotEmpty() {
				return StringUtils.isNotEmpty(column) && StringUtils.isNotEmpty(value);
			}

		}

		public Item addNewItem(String col, String cell) {
			Item item = new Item();
//			System.out.println(col +"_"+ cell);
			item.setColumn(col);
			item.setValue(cell);
			this.addItem(item);
			return  item;
		}

		public void addItem(Item item) {
			if (CollectionUtils.isEmpty(items)) {
				items = new ArrayList<>();
			}
//			if (item.isNotEmpty() && getItem(item.column) == null) {
			items.add(item);
//			}
//			System.out.println(this.getItems());
		}

		public Item getItem(String column) {
			for (Item i : items) {
				if (i.column.contains(column)) {
					return i;
				}
			}
			return new Item();
		}

		public List<Item> getItems() {
			return items;
		}

		public void setItems(List<Item> items) {
			this.items = items;
		}
	}

	// column index, column name
	public OneToOneMap<String, Integer> excelHeaders;

	public OneToOneMap<String, Integer> getExcelHeaders() {
		if (excelHeaders == null)
			excelHeaders = new OneToOneMap<>();
		return excelHeaders;
	}

	public List<String> getHeader(String containedPhrase) {
		List<String> headers = new ArrayList<>();
		for (Entry<String, Integer> map : excelHeaders.entrySet()) {
			if (map.getKey().trim().contains(containedPhrase)) {
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

	public List<Row> getAllRows() {
		if (allRows == null) {
			allRows = new ArrayList<>();
		}
		return allRows;
	}

	public ExcelValue(String path) {
		setExcelHeaders(path);
	}

	private boolean setExcelHeaders(String path) {
		try {
			Excel headerfile = new Excel(path);
			headerfile.assignSheet(0).assignRow(0);
			excelHeaders = new OneToOneMap<>();
			for (int i = 0; i < headerfile.getLastCellNum(); ++i) {
				excelHeaders.put(headerfile.assignCell(i).getCellValue().toString(), i);

			}
//			System.out.println(excelHeaders);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}

//	public List<List<Object>> rowToStringArr(Row row) {
//
//		List<Object> lists = new ArrayList<>();
//		for (Item item : row.getItems()) {
//			try {
//				lists.add(item.getItemValue());
//			} catch (Exception e) {
//				System.out.println("!!!!");
//				System.out.println(item.getColumn());
//				System.out.println(item.getItemValue());
//				e.printStackTrace();
//			}
//		}
//
//		List<List<Object>> values = new ArrayList<>();
//		values.add(lists);
//		return values;
//	}
	public List<List<Object>> rowToStringArr(Row row) {

		List<Object> lists = new ArrayList<>();
		for(int index=0;index<this.excelHeaders.size();++index) {
			Item item =row.getItem(this.excelHeaders.getKey(index));
			if(item!=null) {
				lists.add(item.getItemValue());
			}else {
				lists.add("");
			}
		}
		List<List<Object>> values = new ArrayList<>();
		values.add(lists);
		return values;
	}
	
	public static List<List<Object>> headerToStringArr(OneToOneMap<String, Integer> map) {
		List<Object> lists = new ArrayList<>();
		for(int index=0;index<map.size();++index) {
			lists.add(map.getKey(index));
		}

		List<List<Object>> values = new ArrayList<>();
		values.add(lists);
		return values;
	}

	public Excel itemsToExcel(String sheetCounty) {

		Excel newExcel = Excel.createExcel(ExcelType.EXCEL_XLS);

		newExcel.assignSheet(sheetCounty);
		for (Entry<String, Integer> e : this.excelHeaders.entrySet()) {
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
				if (item.value.startsWith(relativeSymbol)) {
					setLink(item.value, newExcel, newExcel.getCurCell());
				} else {
					newExcel.setCellValue(item.value);
				}
//				System.out.println(item);
			}
			++rowCount;
		}

		return newExcel;
	}

	public void print() {
		for (Row row : this.getAllRows()) {
			for (Item item : row.getItems()) {
				System.out.println(item);
			}
		}
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
