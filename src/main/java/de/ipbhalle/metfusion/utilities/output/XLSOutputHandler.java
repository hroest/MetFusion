/**
 * created by Michael Gerlich, Feb 24, 2012 - 2:30:23 PM
 */ 

package de.ipbhalle.metfusion.utilities.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

//import jxl.CellView;
//import jxl.Workbook;
//import jxl.WorkbookSettings;
//import jxl.format.Colour;
//import jxl.format.UnderlineStyle;
//import jxl.write.Label;
//import jxl.write.Number;
//import jxl.write.WritableCell;
//import jxl.write.WritableCellFormat;
//import jxl.write.WritableFont;
//import jxl.write.WritableSheet;
//import jxl.write.WritableWorkbook;
//import jxl.write.WriteException;
//import jxl.write.biff.RowsExceededException;

import de.ipbhalle.enumerations.AvailableParameters;
import de.ipbhalle.metfusion.web.controller.ResultExtGroupBean;
import de.ipbhalle.metfusion.wrapper.ColorNode;
import de.ipbhalle.metfusion.wrapper.ColorcodedMatrix;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;

public class XLSOutputHandler implements IOutputHandler {

	private String filename;
	
//	private WritableWorkbook workbook;
	private Workbook workbook;
//	private WritableCellFormat timesBoldUnderline;
//	private WritableCellFormat times;
	int sheetCounter = 0;
	
	// font for caption
//	WritableFont arial12font = new WritableFont(WritableFont.ARIAL, 12);
//	WritableCellFormat arial12format = new WritableCellFormat(arial12font);
	
	// font for text
//	WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
//	WritableCellFormat arial10format = new WritableCellFormat(arial10font);
//	WritableCellFormat arial10matrix = new WritableCellFormat(arial10font);
	
	private final String DEFAULT_ENDING = ".xls";
	private final String GZIP_ENDING = ".gz";
	
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
		this.workbook = new HSSFWorkbook();
//		try {
//			this.workbook = setupWorkbook();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
		
		this.workbook = new HSSFWorkbook();
//		try {
//			this.workbook = setupWorkbook();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
//	private WritableWorkbook setupWorkbook() throws IOException {
//		WorkbookSettings wbSettings = new WorkbookSettings();
//		wbSettings.setLocale(new Locale("en", "EN"));
//		
//		WritableWorkbook workbook = Workbook.createWorkbook(new File(filename), wbSettings);
//		
//		try {
//			arial12font.setBoldStyle(WritableFont.BOLD);
//		} catch (WriteException e) {
//			e.printStackTrace();
//		}
//		
//		return workbook;
//	}
	
//	public void finishWorkbook(boolean compress) throws IOException, WriteException {
	public void finishWorkbook(boolean compress) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(filename);
	    workbook.write(fileOut);
	    fileOut.close();
//		workbook.write();
//		workbook.close();
		
		if(compress) {
			InputStream is = new FileInputStream(filename);
			OutputStream os = new GZIPOutputStream(new FileOutputStream(filename + GZIP_ENDING));
			
			byte[] buffer = new byte[ 8192 ]; 
			 
		    for ( int length; (length = is.read(buffer)) != -1; ) 
		        os.write( buffer, 0, length ); 
		    
		    os.close();
		    is.close();
		    
		    File toDelete = new File(filename);
		    if(!toDelete.delete())
		    	System.err.println("Error deleting file [" + filename + "]. It remains both compressed and uncompressed!");
		}
	}
	
//	private void createLabel(WritableSheet sheet) throws WriteException {
//		// Lets create a times font
//		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
//		// Define the cell format
//		times = new WritableCellFormat(times10pt);
//		// Lets automatically wrap the cells
//		times.setWrap(true);
//
//		// Create create a bold font with unterlines
//		WritableFont times10ptBoldUnderline = new WritableFont(
//				WritableFont.TIMES, 10, WritableFont.BOLD, false,
//				UnderlineStyle.SINGLE);
//		timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
//		// Lets automatically wrap the cells
//		timesBoldUnderline.setWrap(true);
//
//		CellView cv = new CellView();
//		cv.setFormat(times);
//		cv.setFormat(timesBoldUnderline);
//		cv.setAutosize(true);
//
//		// Write a few headers
//		addCaption(sheet, 0, 0, "Header 1");
//		addCaption(sheet, 1, 0, "This is another header");
//
//	}
//	
//	private void addCaption(WritableSheet sheet, int column, int row, String s)
//			throws RowsExceededException, WriteException {
//		Label label;
//		label = new Label(column, row, s, timesBoldUnderline);
//		sheet.addCell(label);
//	}
//
//	private void addNumber(WritableSheet sheet, int column, int row,
//			Integer integer) throws WriteException, RowsExceededException {
//		Number number;
//		number = new Number(column, row, integer, times);
//		sheet.addCell(number);
//	}
//
//	private void addLabel(WritableSheet sheet, int column, int row, String s)
//			throws WriteException, RowsExceededException {
//		Label label;
//		label = new Label(column, row, s, times);
//		sheet.addCell(label);
//	}
	
	public boolean writeSettings(Map<AvailableParameters, Object> settings) {
		boolean success = false;
//		workbook.createSheet(this.settings, sheetCounter);
		Sheet sheet = workbook.createSheet(this.settings);
//		WritableSheet sheet = workbook.getSheet(sheetCounter);
		int currentRow = 1;	// start in second row because first row is reserver for header
		int currentCol = 0;
		
		Row headerRow = sheet.createRow(0);
		Cell cell = headerRow.createCell(0);
		cell.setCellValue("Setting");
		cell = headerRow.createCell(1);
		cell.setCellValue("Value");
//		WritableCell headerSetting = new Label(0, 0, "Setting", arial12format);
//		WritableCell headerValue = new Label(1, 0, "Value", arial12format);
//		try
//		{
//			sheet.addCell(headerSetting);
//			sheet.addCell(headerValue);
//		} catch (WriteException e) {
//			System.out.println("Could not write Excel sheet headers!");
//			e.printStackTrace();
//			return success;
//		}
		
		//Set<AvailableParameters> keys = settings.keySet();
		AvailableParameters[] keys = AvailableParameters.values();
		Arrays.sort(keys);
		for (AvailableParameters ap : keys) {
			currentCol = 0;
			Object o = settings.get(ap);
			if(o == null)		// skip empty settings
				continue;
			
			Row row = sheet.createRow(currentRow);
			Cell setting = row.createCell(currentCol);
			setting.setCellValue(ap.toString());
			// output is text
//			WritableCell cellSetting = new Label(currentCol, currentRow, ap.toString(), arial10format);
//			WritableCell cellValue = null;
			currentCol++;
			Cell value = row.createCell(currentCol);
			if(o instanceof String) {
				String s = (String) o;
//				cellValue = new Label(currentCol, currentRow, s, arial10format);
				value.setCellValue(s);
			}
			else if (o instanceof String[]) {
				String[] s = (String[]) o;
				String temp = "";
				for (int i = 0; i < s.length; i++) {
					temp += s[i] + ",";
				}
				temp = temp.substring(0, temp.length()-1);	// remove trailing ,
//				cellValue = new Label(currentCol, currentRow, temp, arial10format);
				value.setCellValue(temp);
			}
			else if (o instanceof Double) {
//				cellValue = new Number(currentCol, currentRow, (Double) o, arial10format);
				value.setCellValue((Double) o);
			}
			else if (o instanceof Integer) {
//				cellValue = new Number(currentCol, currentRow, (Integer) o, arial10format);
				value.setCellValue((Integer) o);
			}
			else if (o instanceof Boolean) {
//				cellValue = new Label(currentCol, currentRow, ((Boolean) o).toString(), arial10format);
				value.setCellValue(((Boolean) o).toString());
			}
			else {	// default to String
//				cellValue = new Label(currentCol, currentRow, o.toString(), arial10format);
				value.setCellValue(o.toString());
			}
			
//			try
//			{
//				sheet.addCell(cellSetting);
//				sheet.addCell(cellValue);
//			} catch (WriteException e) {
//				System.out.println("Could not write excel cell");
//				e.printStackTrace();
//			}
			
			currentCol++;
			currentRow++;
		}
		success = true;
		
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		
		return success;
	}
	
//	private boolean writeSheet(List<?> data, WritableSheet sheet) {
	private boolean writeSheet(List<?> data, Sheet sheet) {
		int currentRow = 1;	// start in second row because first row is reserver for header
		int currentCol = 0;
		
		for (Object object : data) {
			currentCol = 0;
			
			if(object instanceof ResultExtGroupBean) {
				ResultExtGroupBean result = (ResultExtGroupBean) object;
				
			}
			else if(object instanceof ResultExt) {
				ResultExt result = (ResultExt) object;
				
				Row headerRow = sheet.createRow(0);
				headerRow.createCell(0).setCellValue("Rank");
				headerRow.createCell(1).setCellValue("ID");
				headerRow.createCell(2).setCellValue("Compound Name");
				headerRow.createCell(3).setCellValue("Original Score");
				headerRow.createCell(4).setCellValue("MetFusion Score");
				headerRow.createCell(5).setCellValue("Structure");
				headerRow.createCell(6).setCellValue("Molecular Formula");
				headerRow.createCell(7).setCellValue("InChIKey");
//				WritableCell headerRank = new Label(0, 0, "Rank", arial12format);
//				WritableCell headerID = new Label(1, 0, "ID", arial12format);
//				WritableCell headerName = new Label(2, 0, "Compound Name", arial12format);
//				WritableCell headerOrigScore = new Label(3, 0, "Original Score", arial12format);
//				WritableCell headerNewScore = new Label(4, 0, "MetFusion Score", arial12format);
//				WritableCell headerStructure = new Label(5, 0, "Structure", arial12format);
//				WritableCell headerFormula = new Label(6, 0, "Molecular Formula", arial12format);
//				try
//				{
//					sheet.addCell(headerRank);
//					sheet.addCell(headerID);
//					sheet.addCell(headerName);
//					sheet.addCell(headerOrigScore);
//					sheet.addCell(headerNewScore);
//					sheet.addCell(headerStructure);
//					sheet.addCell(headerFormula);
//				} catch (WriteException e) {
//					System.out.println("Could not write Excel sheet headers!");
//					e.printStackTrace();
//					return Boolean.FALSE;
//				}
				
				Row row = sheet.createRow(currentRow);
				row.createCell(currentCol).setCellValue(result.getTiedRank());
				currentCol++;
				row.createCell(currentCol).setCellValue(result.getId());
				currentCol++;
				row.createCell(currentCol).setCellValue(result.getName());
				currentCol++;
				row.createCell(currentCol).setCellValue(result.getScoreShort());
				currentCol++;
				row.createCell(currentCol).setCellValue(result.getResultScore());
				currentCol++;
				row.createCell(currentCol).setCellValue(result.getSmiles());
				currentCol++;
				IMolecularFormula iformula = MolecularFormulaManipulator.getMolecularFormula(result.getMol());
				String formula = MolecularFormulaManipulator.getString(iformula, true);
				row.createCell(currentCol).setCellValue(formula);
				currentCol++;
				row.createCell(currentCol).setCellValue(result.getInchikey());
				currentCol++;
				
				// output is text
//				WritableCell cellRank = new Number(currentCol, currentRow, result.getTiedRank(), arial10format);
//				currentCol++;
//				WritableCell cellID = new Label(currentCol, currentRow, result.getId(), arial10format);
//				currentCol++;
//				WritableCell cellName = new Label(currentCol, currentRow, result.getName(), arial10format);
//				currentCol++;
//				WritableCell cellOrigScore = new Number(currentCol, currentRow, result.getScoreShort(), arial10format);
//				currentCol++;
//				WritableCell cellNewScore = new Number(currentCol, currentRow, result.getResultScore(), arial10format);
//				currentCol++;
//				WritableCell cellSmiles = new Label(currentCol, currentRow, result.getSmiles(), arial10format);
//				currentCol++;
//				IMolecularFormula iformula = MolecularFormulaManipulator.getMolecularFormula(result.getMol());
//				String formula = MolecularFormulaManipulator.getString(iformula, true);
//				WritableCell cellFormula = new Label(currentCol, currentRow, formula, arial10format);
//				currentCol++;
				
//				try
//				{
//					sheet.addCell(cellRank);
//					sheet.addCell(cellID);
//					sheet.addCell(cellName);
//					sheet.addCell(cellOrigScore);
//					sheet.addCell(cellNewScore);
//					sheet.addCell(cellSmiles);
//					sheet.addCell(cellFormula);
//				} catch (WriteException e) {
//					System.out.println("Could not write excel cell");
//					e.printStackTrace();
//				}
			}
			else if(object instanceof Result) {
				Result result = (Result) object;
				
				Row headerRow = sheet.createRow(0);
				headerRow.createCell(0).setCellValue("Rank");
				headerRow.createCell(1).setCellValue("ID");
				headerRow.createCell(2).setCellValue("Compound Name");
				headerRow.createCell(3).setCellValue("Original Score");
				headerRow.createCell(4).setCellValue("Structure");
				headerRow.createCell(5).setCellValue("Molecular Formula");
				headerRow.createCell(6).setCellValue("InChIKey");
				
//				WritableCell headerRank = new Label(0, 0, "Rank", arial12format);
//				WritableCell headerID = new Label(1, 0, "ID", arial12format);
//				WritableCell headerName = new Label(2, 0, "Compound Name", arial12format);
//				WritableCell headerOrigScore = new Label(3, 0, "Original Score", arial12format);
//				WritableCell headerStructure = new Label(4, 0, "Structure", arial12format);
//				WritableCell headerFormula = new Label(5, 0, "Molecular Formula", arial12format);
//				try
//				{
//					sheet.addCell(headerRank);
//					sheet.addCell(headerID);
//					sheet.addCell(headerName);
//					sheet.addCell(headerOrigScore);
//					sheet.addCell(headerStructure);
//					sheet.addCell(headerFormula);
//				} catch (WriteException e) {
//					System.out.println("Could not write Excel sheet headers!");
//					e.printStackTrace();
//				}
				
				Row row = sheet.createRow(currentRow);
				row.createCell(currentCol).setCellValue(result.getTiedRank());
				currentCol++;
				row.createCell(currentCol).setCellValue(result.getId());
				currentCol++;
				row.createCell(currentCol).setCellValue(result.getName());
				currentCol++;
				row.createCell(currentCol).setCellValue(result.getScoreShort());
				currentCol++;
				row.createCell(currentCol).setCellValue(result.getSmiles());
				currentCol++;
				IMolecularFormula iformula = MolecularFormulaManipulator.getMolecularFormula(result.getMol());
				String formula = MolecularFormulaManipulator.getString(iformula, true);
				row.createCell(currentCol).setCellValue(formula);
				currentCol++;
				row.createCell(currentCol).setCellValue(result.getInchikey());
				currentCol++;
				// output is text
//				WritableCell cellRank = new Number(currentCol, currentRow, result.getTiedRank(), arial10format);
//				currentCol++;
//				WritableCell cellID = new Label(currentCol, currentRow, result.getId(), arial10format);
//				currentCol++;
//				WritableCell cellName = new Label(currentCol, currentRow, result.getName(), arial10format);
//				currentCol++;
//				WritableCell cellOrigScore = new Number(currentCol, currentRow, result.getScoreShort(), arial10format);
//				currentCol++;
//				WritableCell cellSmiles = new Label(currentCol, currentRow, result.getSmiles(), arial10format);
//				currentCol++;
//				IMolecularFormula iformula = MolecularFormulaManipulator.getMolecularFormula(result.getMol());
//				String formula = MolecularFormulaManipulator.getString(iformula, true);
//				WritableCell cellFormula = new Label(currentCol, currentRow, formula, arial10format);
//				currentCol++;
//				
//				try
//				{
//					sheet.addCell(cellRank);
//					sheet.addCell(cellID);
//					sheet.addCell(cellName);
//					sheet.addCell(cellOrigScore);
//					sheet.addCell(cellSmiles);
//					sheet.addCell(cellFormula);
//				} catch (WriteException e) {
//					System.out.println("Could not write excel cell");
//					e.printStackTrace();
//				}
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
		
//		workbook.createSheet(METFUSION, sheetCounter);
		Sheet sheet = workbook.createSheet(METFUSION);
//		WritableSheet sheet = workbook.getSheet(sheetCounter);
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
		
		
//		workbook.createSheet(METFUSION, sheetCounter);
		Sheet sheet = workbook.createSheet(METFUSION);
//		WritableSheet sheet = workbook.getSheet(sheetCounter);
		boolean success = writeSheet(newlyRanked, sheet);
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		
//		workbook.createSheet(databaseName, sheetCounter);
		sheet = workbook.createSheet(databaseName);
//		sheet = workbook.getSheet(sheetCounter);
		success = writeSheet(originalDatabase, sheet);
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		
//		workbook.createSheet(fragmenterName, sheetCounter);
		sheet = workbook.createSheet(fragmenterName);
//		sheet = workbook.getSheet(sheetCounter);
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
//		workbook.createSheet(name, sheetCounter);
//		WritableSheet sheet = workbook.getSheet(sheetCounter);
		Sheet sheet = workbook.createSheet(name);
		ColorNode[][] colorMatrix = matrix.getColorMatrix();
		boolean success = false;
		List<Result> rowNames = matrix.getCandidates();
		List<Result> colNames = matrix.getPrimaries();
		int colStart = 1;
		CellStyle headerStyle = workbook.createCellStyle();
		Font headerFont = workbook.createFont();
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setFontName("Arial");
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerStyle.setFont(headerFont);
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
//		WritableCellFormat header = new WritableCellFormat(arial12font);
		
		// predefined colored cell styles
		CellStyle styleRed = workbook.createCellStyle();
		styleRed.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleRed.setFillForegroundColor(IndexedColors.RED.getIndex());
		CellStyle styleDarkRed = workbook.createCellStyle();
		styleDarkRed.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleDarkRed.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
		CellStyle styleOrange = workbook.createCellStyle();
		styleOrange.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleOrange.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
		CellStyle styleLightOrange = workbook.createCellStyle();
		styleLightOrange.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleLightOrange.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
		CellStyle styleLightYellow = workbook.createCellStyle();
		styleLightYellow.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleLightYellow.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		CellStyle styleYellow = workbook.createCellStyle();
		styleYellow.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleYellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		CellStyle styleLime = workbook.createCellStyle();
		styleLime.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleLime.setFillForegroundColor(IndexedColors.LIME.getIndex());
		CellStyle styleBrightGreen = workbook.createCellStyle();
		styleBrightGreen.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleBrightGreen.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
		CellStyle styleGreen = workbook.createCellStyle();
		styleGreen.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleGreen.setFillForegroundColor(IndexedColors.GREEN.getIndex());
		
		CellRangeAddress dimMatrix = new CellRangeAddress(1, colorMatrix.length, colStart, colorMatrix[1].length);
		 // Create a row and put some cells in it. Rows are 0 based.
	    Row headerRow = sheet.createRow(0);
		for (Result result : colNames) {
			// Create a cell and put a date value in it.  The first cell is not styled
		    // as a date.
		    Cell cell = headerRow.createCell(colStart);
		    cell.setCellValue(result.getId());
		    cell.setCellStyle(headerStyle);
//			WritableCell cell = new Label(colStart, 0, result.getId(), header);
//			try {
//				sheet.addCell(cell);
//			} catch (RowsExceededException e) {
//				System.err.println("Exceeding rows while writing header columns!");
//			} catch (WriteException e) {
//				System.err.println("Error while writing header to sheet!");
//			}
			colStart++;
		}
		for (int i = 0; i < colorMatrix.length; i++) {
			Result row = rowNames.get(i);
			Row currentRow = sheet.createRow(i+1);
			Cell cell = currentRow.createCell(0);
		    cell.setCellValue(row.getId());
		    cell.setCellStyle(headerStyle);
//			WritableCell cell = new Label(0, i+1, row.getId(), header);
//			try {
//				sheet.addCell(cell);
//			} catch (RowsExceededException e) {
//				System.err.println("Exceeding rows while writing header rows!");
//			} catch (WriteException e) {
//				System.err.println("Error while writing rowname to sheet!");
//			}
			
			for (int j = 0; j < colorMatrix[i].length; j++) {
				ColorNode entry = colorMatrix[i][j];
				double value = entry.getValue();
//				short idxColor = IndexedColors.GREEN.getIndex();
				cellStyle = styleGreen;
//				Colour color = Colour.GREEN;
//				WritableCellFormat format = new WritableCellFormat(arial10font);
				if(value == 0)
//					color = Colour.DARK_RED;
//					idxColor = IndexedColors.DARK_RED.getIndex();
					cellStyle = styleDarkRed;
				else if(value < 0.2)
//					color = Colour.RED;
//					idxColor = IndexedColors.RED.getIndex();
					cellStyle = styleRed;
				else if(value < 0.4)
//					color = Colour.ORANGE;
//					idxColor = IndexedColors.ORANGE.getIndex();
					cellStyle = styleOrange;
				else if(value < 0.5)
//					color = Colour.LIGHT_ORANGE;
//					idxColor = IndexedColors.LIGHT_ORANGE.getIndex();
					cellStyle = styleLightOrange;
				else if(value < 0.6)
//					color = Colour.VERY_LIGHT_YELLOW;
//					idxColor = IndexedColors.LIGHT_YELLOW.getIndex();
					cellStyle = styleLightYellow;
				else if(value < 0.7)
//					color = Colour.YELLOW;
//					idxColor = IndexedColors.YELLOW.getIndex();
					cellStyle = styleYellow;
				else if(value < 0.8)
//					color = Colour.LIME;
//					idxColor = IndexedColors.LIME.getIndex();
					cellStyle = styleLime;
				else if(value < 0.95)
//					color = Colour.BRIGHT_GREEN;
//					idxColor = IndexedColors.BRIGHT_GREEN.getIndex();
					cellStyle = styleBrightGreen;
				else if(value == 1)
//					color = Colour.GREEN;
//					idxColor = IndexedColors.GREEN.getIndex();
					cellStyle = styleGreen;
				
				//cellStyle.setFillForegroundColor(idxColor);
				cell = currentRow.createCell(j+1);
				cell.setCellValue(value);
				cell.setCellStyle(cellStyle);
				
//				try {
//					format.setBackground(color);
//				} catch (WriteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				WritableCell tanimoto = new Number(j+1, i+1, value, format);
//				
//				try {
//					sheet.addCell(tanimoto);
//				} catch (RowsExceededException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (WriteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
			}
		}
		
//		SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
//		// value == 0
//		ConditionalFormattingRule rule0 = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "0");
//		PatternFormatting fill0 = rule0.createPatternFormatting();
//		fill0.setFillBackgroundColor(IndexedColors.DARK_RED.getIndex());
//		fill0.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
//		// value < 0.2
//		//ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(ComparisonOperator.LT, "0.2");
//		ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "0", "0.2");
//		PatternFormatting fill1 = rule1.createPatternFormatting();
//		fill1.setFillBackgroundColor(IndexedColors.RED.getIndex());
//		fill1.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
//		// value < 0.4
//		//ConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule(ComparisonOperator.LT, "0.4");
//		ConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "0.2", "0.4");
//		PatternFormatting fill2 = rule2.createPatternFormatting();
//		fill2.setFillBackgroundColor(IndexedColors.ORANGE.getIndex());
//		fill2.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
//		// value < 0.5
//		//ConditionalFormattingRule rule3 = sheetCF.createConditionalFormattingRule(ComparisonOperator.LT, "0.5");
//		ConditionalFormattingRule rule3 = sheetCF.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "0.4", "0.5");
//		PatternFormatting fill3 = rule3.createPatternFormatting();
//		fill3.setFillBackgroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
//		fill3.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
//		// value < 0.6
//		//ConditionalFormattingRule rule4 = sheetCF.createConditionalFormattingRule(ComparisonOperator.LT, "0.6");
//		ConditionalFormattingRule rule4 = sheetCF.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "0.5", "0.6");
//		PatternFormatting fill4 = rule4.createPatternFormatting();
//		fill4.setFillBackgroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
//		fill4.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
//		// value < 0.7
//		//ConditionalFormattingRule rule5 = sheetCF.createConditionalFormattingRule(ComparisonOperator.LT, "0.7");
//		ConditionalFormattingRule rule5 = sheetCF.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "0.6", "0.7");
//		PatternFormatting fill5 = rule5.createPatternFormatting();
//		fill5.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
//		fill5.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
//		// value < 0.8
//		//ConditionalFormattingRule rule6 = sheetCF.createConditionalFormattingRule(ComparisonOperator.LT, "0.8");
//		ConditionalFormattingRule rule6 = sheetCF.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "0.7", "0.8");
//		PatternFormatting fill6 = rule6.createPatternFormatting();
//		fill6.setFillBackgroundColor(IndexedColors.LIME.getIndex());
//		fill6.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
//		// value < 0.95
//		//ConditionalFormattingRule rule7 = sheetCF.createConditionalFormattingRule(ComparisonOperator.LT, "0.95");
//		ConditionalFormattingRule rule7 = sheetCF.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "0.8", "0.95");
//		PatternFormatting fill7 = rule7.createPatternFormatting();
//		fill7.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
//		fill7.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
//		// value == 1
//		//ConditionalFormattingRule rule8 = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "1");
//		ConditionalFormattingRule rule8 = sheetCF.createConditionalFormattingRule(ComparisonOperator.GT, "0.95");
//		PatternFormatting fill8 = rule8.createPatternFormatting();
//		fill8.setFillBackgroundColor(IndexedColors.GREEN.getIndex());
//		fill8.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
//
//		CellRangeAddress[] regions = {dimMatrix};
//		ConditionalFormattingRule[] rules = {rule0, rule1, rule2, rule3, rule4, rule5, rule6, rule7, rule8};
//		sheetCF.addConditionalFormatting(regions, rules);
//		
////		sheetCF.addConditionalFormatting(regions, rule8);
////		sheetCF.addConditionalFormatting(regions, rule7);
////		sheetCF.addConditionalFormatting(regions, rule6);
////		sheetCF.addConditionalFormatting(regions, rule5);
////		sheetCF.addConditionalFormatting(regions, rule4);
////		sheetCF.addConditionalFormatting(regions, rule3);
////		sheetCF.addConditionalFormatting(regions, rule2);
////		sheetCF.addConditionalFormatting(regions, rule1);
////		sheetCF.addConditionalFormatting(regions, rule0);
		
		
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
