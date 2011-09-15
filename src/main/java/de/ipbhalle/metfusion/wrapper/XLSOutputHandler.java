/**
 * created by Michael Gerlich, Jul 20, 2011 - 1:56:44 PM
 */ 

package de.ipbhalle.metfusion.wrapper;

import java.io.IOException;

import javax.faces.context.FacesContext;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import com.icesoft.faces.component.dataexporter.OutputTypeHandler;

public class XLSOutputHandler extends OutputTypeHandler {
	
	WritableSheet sheet = null;
	WritableWorkbook workbook = null;
	
	
	public XLSOutputHandler(String arg0) {
		super(arg0);
		try{
		   WorkbookSettings settings = new WorkbookSettings();
		   settings.setLocale(FacesContext.getCurrentInstance().getViewRoot().getLocale());
		   workbook = Workbook.createWorkbook(super.getFile());
		   sheet = workbook.createSheet("test", 0);
		   
		   this.mimeType = "application/vnd.ms-excel";
	   }
	   catch(IOException ioe){
		   ioe.printStackTrace();
	   }
	}
	
	public XLSOutputHandler(String path, FacesContext fc, String title) {
		   super(path);
		   try{
			   WorkbookSettings settings = new WorkbookSettings();
			   settings.setLocale(fc.getViewRoot().getLocale());
			   workbook = Workbook.createWorkbook(super.getFile());
			   sheet = workbook.createSheet(title, 0);
			   
			   this.mimeType = "application/vnd.ms-excel";
		   }
		   catch(IOException ioe){
			   ioe.printStackTrace();
		   }
	}
	
	@Override
	public void flushFile() {
		try {
			workbook.write();
			workbook.close();
		} catch (WriteException ioe) {
			ioe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public void writeCell(Object output, int col, int row) {
		WritableCell cell = null;
		if (output instanceof String) {
			String out = (String) output;
			if(out.endsWith(".gif") || out.endsWith(".png")) {
				//String path = CoreUtils.getRealPath(FacesContext.getCurrentInstance(), out);
//				File f = new File(path);
//				WritableImage wi = new WritableImage((double) col + 9, (double) row + (3*row), 1, 3, f);
//				sheet.addImage(wi);
				cell = new Label(col, row + 1, (String) output);
			}
			else 
				cell = new Label(col, row + 1, (String) output);			
		} 
		else if (output instanceof Double) {
			cell = new Number(col, row + 1, ((Double) output).doubleValue());
		}
		
		try {
			sheet.addCell(cell);
		} catch (WriteException e) {
			System.out.println("Could not write excel cell");
			e.printStackTrace();
		}
	}

	@Override
	public void writeHeaderCell(String text, int col) {
		try {
			WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
			WritableCellFormat arial10format = new WritableCellFormat(
					arial10font);
			arial10font.setBoldStyle(WritableFont.BOLD);
			Label label = new Label(col, 0, text, arial10format);
			sheet.addCell(label);
		} catch (WriteException we) {
			we.printStackTrace();
		}
	}

}
