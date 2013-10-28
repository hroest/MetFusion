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
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.enumerations.AvailableParameters;
import de.ipbhalle.metfusion.web.controller.ResultExtGroupBean;
import de.ipbhalle.metfusion.wrapper.ColorNode;
import de.ipbhalle.metfusion.wrapper.ColorcodedMatrix;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;

public class XLSOutputHandler implements IOutputHandler {

	private String filename;
	
	private Workbook workbook;
	int sheetCounter = 0;
	
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
		this.workbook = new XSSFWorkbook();
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
		
		this.workbook = new XSSFWorkbook();
	}
	
	public void finishWorkbook(boolean compress) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(filename);
	    workbook.write(fileOut);
	    fileOut.close();
		
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
	
	public boolean writeSettings(Map<AvailableParameters, Object> settings) {
		boolean success = false;
		Sheet sheet = workbook.createSheet(this.settings);
		int currentRow = 1;	// start in second row because first row is reserver for header
		int currentCol = 0;
		
		Row headerRow = sheet.createRow(0);
		Cell cell = headerRow.createCell(0);
		cell.setCellValue("Setting");
		cell = headerRow.createCell(1);
		cell.setCellValue("Value");
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
			currentCol++;
			Cell value = row.createCell(currentCol);
			if(o instanceof String) {
				String s = (String) o;
				value.setCellValue(s);
			}
			else if (o instanceof String[]) {
				String[] s = (String[]) o;
				String temp = "";
				for (int i = 0; i < s.length; i++) {
					temp += s[i] + ",";
				}
				temp = temp.substring(0, temp.length()-1);	// remove trailing ,
				value.setCellValue(temp);
			}
			else if (o instanceof Double) {
				value.setCellValue((Double) o);
			}
			else if (o instanceof Integer) {
				value.setCellValue((Integer) o);
			}
			else if (o instanceof Boolean) {
				value.setCellValue(((Boolean) o).toString());
			}
			else {	// default to String
				value.setCellValue(o.toString());
			}
			
			currentCol++;
			currentRow++;
		}
		success = true;
		
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		
		return success;
	}
	
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
				
				// output is text
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
				
				// output is text
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
				
			}
			else {
				
			}
			currentRow++;
		}
		
		return true;
	}
	
	@Override
	public boolean writeRerankedResults(List<ResultExt> results) {
		Sheet sheet = workbook.createSheet(METFUSION);
		boolean success = writeSheet(results, sheet);
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		
		return true;
	}

	@Override
	public boolean writeAllResults(List<Result> originalFragmenter, List<Result> originalDatabase, 
					List<ResultExt> newlyRanked, List<ResultExtGroupBean> cluster) {
		Sheet sheet = workbook.createSheet(METFUSION);
		boolean success = writeSheet(newlyRanked, sheet);
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		
		sheet = workbook.createSheet(databaseName);
		success = writeSheet(originalDatabase, sheet);
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		
		sheet = workbook.createSheet(fragmenterName);
		success = writeSheet(originalFragmenter, sheet);
		if(success)		// increment sheetCounter only when current sheet was processed successfully
			sheetCounter++;
		
		// TODO cluster results
		
		return true;
	}

	private boolean writeMatrix(ColorcodedMatrix matrix, String name) {
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
		
		 // Create a row and put some cells in it. Rows are 0 based.
	    Row headerRow = sheet.createRow(0);
		for (Result result : colNames) {
		    Cell cell = headerRow.createCell(colStart);
		    cell.setCellValue(result.getId());
		    cell.setCellStyle(headerStyle);
			colStart++;
		}
		for (int i = 0; i < colorMatrix.length; i++) {
			Result row = rowNames.get(i);
			Row currentRow = sheet.createRow(i+1);
			Cell cell = currentRow.createCell(0);
		    cell.setCellValue(row.getId());
		    cell.setCellStyle(headerStyle);
			
			for (int j = 0; j < colorMatrix[i].length; j++) {
				ColorNode entry = colorMatrix[i][j];
				double value = entry.getValue();
				cellStyle = styleGreen;
				if(value == 0)
					cellStyle = styleDarkRed;
				else if(value < 0.2)
					cellStyle = styleRed;
				else if(value < 0.4)
					cellStyle = styleOrange;
				else if(value < 0.5)
					cellStyle = styleLightOrange;
				else if(value < 0.6)
					cellStyle = styleLightYellow;
				else if(value < 0.7)
					cellStyle = styleYellow;
				else if(value < 0.8)
					cellStyle = styleLime;
				else if(value < 0.95)
					cellStyle = styleBrightGreen;
				else if(value == 1)
					cellStyle = styleGreen;
				
				cell = currentRow.createCell(j+1);
				cell.setCellValue(value);
				cell.setCellStyle(cellStyle);
				
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
