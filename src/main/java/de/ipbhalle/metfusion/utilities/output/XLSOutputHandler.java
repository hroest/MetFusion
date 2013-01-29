/**
 * created by Michael Gerlich, Feb 24, 2012 - 2:30:23 PM
 */ 

package de.ipbhalle.metfusion.utilities.output;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import de.ipbhalle.enumerations.AvailableParameters;
import de.ipbhalle.metfusion.web.controller.ResultExtGroupBean;
import de.ipbhalle.metfusion.wrapper.CellKey;
import de.ipbhalle.metfusion.wrapper.ColorNode;
import de.ipbhalle.metfusion.wrapper.ColorcodedMatrix;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;

public class XLSOutputHandler implements IOutputHandler {

	private String filename;
	
	private WritableWorkbook workbook;
	private WritableCellFormat timesBoldUnderline;
	private WritableCellFormat times;
	int sheetCounter = 0;
	
	// font for caption
	WritableFont arial12font = new WritableFont(WritableFont.ARIAL, 12);
	WritableCellFormat arial12format = new WritableCellFormat(arial12font);
	
	// font for text
	WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
	WritableCellFormat arial10format = new WritableCellFormat(arial10font);
	WritableCellFormat arial10matrix = new WritableCellFormat(arial10font);
	
	private final String DEFAULT_ENDING = ".xls";

	private static final String METFUSION = "MetFusion";
	private String databaseName = "MassBank";	// default database name is MassBank
	private String fragmenterName =  "MetFrag";	// default fragmener name is MetFrag
	private String settings = "settings";
	
	/**
	 * Default constructor, uses filename and does not append to this file.
	 * 
	 * @param filename - name/path of the file to write
	 */
	public XLSOutputHandler(String filename) {
		this.filename = filename.endsWith(DEFAULT_ENDING) ? filename : filename + DEFAULT_ENDING;
		try {
			this.workbook = setupWorkbook();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor, uses filename and does not append to this file.
	 * 
	 * @param filename - name/path of the file to write
	 */
	public XLSOutputHandler(String filename, String databaseName, String fragmenterName) {
		this.filename = filename.endsWith(DEFAULT_ENDING) ? filename : filename + DEFAULT_ENDING;
		this.databaseName = databaseName;
		this.fragmenterName = fragmenterName;
		
		try {
			this.workbook = setupWorkbook();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private WritableWorkbook setupWorkbook() throws IOException {
		WorkbookSettings wbSettings = new WorkbookSettings();
		wbSettings.setLocale(new Locale("en", "EN"));
		
		WritableWorkbook workbook = Workbook.createWorkbook(new File(filename), wbSettings);
		
		try {
			arial12font.setBoldStyle(WritableFont.BOLD);
		} catch (WriteException e) {
			e.printStackTrace();
		}
		
		return workbook;
	}
	
	public void finishWorkbook() throws IOException, WriteException {
		workbook.write();
		workbook.close();
	}
	
	private void createLabel(WritableSheet sheet) throws WriteException {
		// Lets create a times font
		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
		// Define the cell format
		times = new WritableCellFormat(times10pt);
		// Lets automatically wrap the cells
		times.setWrap(true);

		// Create create a bold font with unterlines
		WritableFont times10ptBoldUnderline = new WritableFont(
				WritableFont.TIMES, 10, WritableFont.BOLD, false,
				UnderlineStyle.SINGLE);
		timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
		// Lets automatically wrap the cells
		timesBoldUnderline.setWrap(true);

		CellView cv = new CellView();
		cv.setFormat(times);
		cv.setFormat(timesBoldUnderline);
		cv.setAutosize(true);

		// Write a few headers
		addCaption(sheet, 0, 0, "Header 1");
		addCaption(sheet, 1, 0, "This is another header");

	}
	
	private void addCaption(WritableSheet sheet, int column, int row, String s)
			throws RowsExceededException, WriteException {
		Label label;
		label = new Label(column, row, s, timesBoldUnderline);
		sheet.addCell(label);
	}

	private void addNumber(WritableSheet sheet, int column, int row,
			Integer integer) throws WriteException, RowsExceededException {
		Number number;
		number = new Number(column, row, integer, times);
		sheet.addCell(number);
	}

	private void addLabel(WritableSheet sheet, int column, int row, String s)
			throws WriteException, RowsExceededException {
		Label label;
		label = new Label(column, row, s, times);
		sheet.addCell(label);
	}
	
	public boolean writeSettings(Map<AvailableParameters, Object> settings) {
		boolean success = false;
		workbook.createSheet(this.settings, sheetCounter);
		WritableSheet sheet = workbook.getSheet(sheetCounter);
		int currentRow = 1;	// start in second row because first row is reserver for header
		int currentCol = 0;
		
		WritableCell headerSetting = new Label(0, 0, "Setting", arial12format);
		WritableCell headerValue = new Label(1, 0, "Value", arial12format);
		try
		{
			sheet.addCell(headerSetting);
			sheet.addCell(headerValue);
		} catch (WriteException e) {
			System.out.println("Could not write Excel sheet headers!");
			e.printStackTrace();
			return success;
		}
		
		Set<AvailableParameters> keys = settings.keySet();
		for (AvailableParameters ap : keys) {
			currentCol = 0;
			Object o = settings.get(ap);
			if(o == null)		// skip empty settings
				continue;
			
			// output is text
			WritableCell cellSetting = new Label(currentCol, currentRow, ap.toString(), arial10format);
			WritableCell cellValue = null;
			currentCol++;
			if(o instanceof String) {
				String s = (String) o;
				cellValue = new Label(currentCol, currentRow, s, arial10format);
			}
			else if (o instanceof String[]) {
				String[] s = (String[]) o;
				String temp = "";
				for (int i = 0; i < s.length; i++) {
					temp += s[i] + ",";
				}
				temp = temp.substring(0, temp.length()-1);	// remove trailing ,
				cellValue = new Label(currentCol, currentRow, temp, arial10format);
			}
			else if (o instanceof Double) {
				cellValue = new Number(currentCol, currentRow, (Double) o, arial10format);
			}
			else if (o instanceof Integer) {
				cellValue = new Number(currentCol, currentRow, (Integer) o, arial10format);
			}
			else if (o instanceof Boolean) {
				cellValue = new Label(currentCol, currentRow, ((Boolean) o).toString(), arial10format);
			}
			else {	// default to String
				cellValue = new Label(currentCol, currentRow, o.toString(), arial10format);
			}
			
			try
			{
				sheet.addCell(cellSetting);
				sheet.addCell(cellValue);
			} catch (WriteException e) {
				System.out.println("Could not write excel cell");
				e.printStackTrace();
			}
			
			currentCol++;
			currentRow++;
		}
		success = true;
		
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		
		return success;
	}
	
	private boolean writeSheet(List<?> data, WritableSheet sheet) {
		int currentRow = 1;	// start in second row because first row is reserver for header
		int currentCol = 0;
//		WritableImage wi = null;
		
		for (Object object : data) {
			currentCol = 0;
			
			if(object instanceof ResultExtGroupBean) {
				ResultExtGroupBean result = (ResultExtGroupBean) object;
				
			}
			else if(object instanceof ResultExt) {
				ResultExt result = (ResultExt) object;
				
				WritableCell headerRank = new Label(0, 0, "Rank", arial12format);
				WritableCell headerID = new Label(1, 0, "ID", arial12format);
				WritableCell headerName = new Label(2, 0, "Compound Name", arial12format);
				WritableCell headerOrigScore = new Label(3, 0, "Original Score", arial12format);
				WritableCell headerNewScore = new Label(4, 0, "MetFusion Score", arial12format);
				WritableCell headerStructure = new Label(5, 0, "Structure", arial12format);
				try
				{
					sheet.addCell(headerRank);
					sheet.addCell(headerID);
					sheet.addCell(headerName);
					sheet.addCell(headerOrigScore);
					sheet.addCell(headerNewScore);
					sheet.addCell(headerStructure);
				} catch (WriteException e) {
					System.out.println("Could not write Excel sheet headers!");
					e.printStackTrace();
					return Boolean.FALSE;
				}
				
				// output is text
				WritableCell cellRank = new Number(currentCol, currentRow, result.getTiedRank(), arial10format);
				currentCol++;
				WritableCell cellID = new Label(currentCol, currentRow, result.getId(), arial10format);
				currentCol++;
				WritableCell cellName = new Label(currentCol, currentRow, result.getName(), arial10format);
				currentCol++;
				WritableCell cellOrigScore = new Number(currentCol, currentRow, result.getScoreShort(), arial10format);
				currentCol++;
				WritableCell cellNewScore = new Number(currentCol, currentRow, result.getResultScore(), arial10format);
				currentCol++;
				WritableCell cellSmiles = new Label(currentCol, currentRow, result.getSmiles(), arial10format);
//				File temp = new File(".", result.getImagePath());
//				if(temp.exists())
//					wi = new WritableImage(currentCol, currentRow, 1, 3, temp);
				
				try
				{
					sheet.addCell(cellRank);
					sheet.addCell(cellID);
					sheet.addCell(cellName);
					sheet.addCell(cellOrigScore);
					sheet.addCell(cellNewScore);
					sheet.addCell(cellSmiles);
//					sheet.addImage(wi);
				} catch (WriteException e) {
					System.out.println("Could not write excel cell");
					e.printStackTrace();
				}
			}
			else if(object instanceof Result) {
				Result result = (Result) object;
				
				WritableCell headerRank = new Label(0, 0, "Rank", arial12format);
				WritableCell headerID = new Label(1, 0, "ID", arial12format);
				WritableCell headerName = new Label(2, 0, "Compound Name", arial12format);
				WritableCell headerOrigScore = new Label(3, 0, "Original Score", arial12format);
				WritableCell headerStructure = new Label(4, 0, "Structure", arial12format);
				try
				{
					sheet.addCell(headerRank);
					sheet.addCell(headerID);
					sheet.addCell(headerName);
					sheet.addCell(headerOrigScore);
					sheet.addCell(headerStructure);
				} catch (WriteException e) {
					System.out.println("Could not write Excel sheet headers!");
					e.printStackTrace();
				}
				// output is text
				WritableCell cellRank = new Number(currentCol, currentRow, result.getTiedRank(), arial10format);
				currentCol++;
				WritableCell cellID = new Label(currentCol, currentRow, result.getId(), arial10format);
				currentCol++;
				WritableCell cellName = new Label(currentCol, currentRow, result.getName(), arial10format);
				currentCol++;
				WritableCell cellOrigScore = new Number(currentCol, currentRow, result.getScoreShort(), arial10format);
				currentCol++;
				WritableCell cellSmiles = new Label(currentCol, currentRow, result.getSmiles(), arial10format);
//				File temp = new File(".", result.getImagePath());
//				if(temp.exists())
//					wi = new WritableImage(currentCol, currentRow, 1, 3, temp);
				
				try
				{
					sheet.addCell(cellRank);
					sheet.addCell(cellID);
					sheet.addCell(cellName);
					sheet.addCell(cellOrigScore);
					sheet.addCell(cellSmiles);
//					sheet.addImage(wi);
				} catch (WriteException e) {
					System.out.println("Could not write excel cell");
					e.printStackTrace();
				}
			}
			else {
				
			}
			currentRow++;
		}
		
		return true;
	}
	
	@Override
	public boolean writeRerankedResults(List<ResultExt> results) {
//		try {
//			this.workbook = setupWorkbook();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
		
		workbook.createSheet(METFUSION, sheetCounter);
		WritableSheet sheet = workbook.getSheet(sheetCounter);
		boolean success = writeSheet(results, sheet);
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		
//		try {
//			finishWorkbook();
//		} catch (WriteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
		
		return true;
	}

	@Override
	public boolean writeAllResults(List<Result> originalFragmenter, List<Result> originalDatabase, 
					List<ResultExt> newlyRanked, List<ResultExtGroupBean> cluster) {
//		try {
//			this.workbook = setupWorkbook();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
		
		
		workbook.createSheet(METFUSION, sheetCounter);
		WritableSheet sheet = workbook.getSheet(sheetCounter);
		boolean success = writeSheet(newlyRanked, sheet);
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		
		workbook.createSheet(databaseName, sheetCounter);
		sheet = workbook.getSheet(sheetCounter);
		success = writeSheet(originalDatabase, sheet);
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		
		workbook.createSheet(fragmenterName, sheetCounter);
		sheet = workbook.getSheet(sheetCounter);
		success = writeSheet(originalFragmenter, sheet);
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		
		// TODO cluster results
		
//		try {
//			finishWorkbook();
//		} catch (WriteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
		
		return true;
	}

	private boolean writeMatrix(ColorcodedMatrix matrix, String name) {
		workbook.createSheet(name, sheetCounter);
		WritableSheet sheet = workbook.getSheet(sheetCounter);
		ColorNode[][] colorMatrix = matrix.getColorMatrix();
		boolean success = false;
		List<Result> rowNames = matrix.getCandidates();
		List<Result> colNames = matrix.getPrimaries();
		int colStart = 1;
		WritableCellFormat header = new WritableCellFormat(arial12font);
		for (Result result : colNames) {
			WritableCell cell = new Label(colStart, 0, result.getId(), header);
			try {
				sheet.addCell(cell);
			} catch (RowsExceededException e) {
				System.err.println("Exceeding rows while writing header columns!");
			} catch (WriteException e) {
				System.err.println("Error while writing header to sheet!");
			}
			colStart++;
		}
		for (int i = 0; i < colorMatrix.length; i++) {
			Result row = rowNames.get(i);
			WritableCell cell = new Label(0, i+1, row.getId(), header);
			try {
				sheet.addCell(cell);
			} catch (RowsExceededException e) {
				System.err.println("Exceeding rows while writing header rows!");
			} catch (WriteException e) {
				System.err.println("Error while writing rowname to sheet!");
			}
			
			for (int j = 0; j < colorMatrix[i].length; j++) {
				ColorNode entry = colorMatrix[i][j];
				double value = entry.getValue();
				Colour color = Colour.GREEN;
				WritableCellFormat format = new WritableCellFormat(arial10font);
				if(value == 0)
					color = Colour.DARK_RED;
				else if(value < 0.2)
					color = Colour.RED;
				else if(value < 0.4)
					color = Colour.ORANGE;
				else if(value < 0.5)
					color = Colour.LIGHT_ORANGE;
				else if(value < 0.6)
					color = Colour.VERY_LIGHT_YELLOW;
				else if(value < 0.7)
					color = Colour.YELLOW;
				else if(value < 0.8)
					color = Colour.LIME;
				else if(value < 0.95)
					color = Colour.BRIGHT_GREEN;
				else if(value == 1)
					color = Colour.GREEN;
				
				try {
					format.setBackground(color);
				} catch (WriteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				WritableCell tanimoto = new Number(j+1, i+1, value, format);
				
				try {
					sheet.addCell(tanimoto);
				} catch (RowsExceededException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WriteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		success = true;
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		return success;
	}
	
	public boolean writeOriginalMatrix(ColorcodedMatrix matrix, String name) {
		return writeMatrix(matrix, name);
	}
	
	public boolean writeModifiedMatrix(ColorcodedMatrix matrix, String name) {
		return writeMatrix(matrix, name);
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setFragmenterName(String fragmenterName) {
		this.fragmenterName = fragmenterName;
	}

	public String getFragmenterName() {
		return fragmenterName;
	}
}
