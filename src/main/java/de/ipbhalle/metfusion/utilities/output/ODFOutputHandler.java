/**
 * created by Michael Gerlich, Feb 16, 2012 - 4:49:49 PM
 */

package de.ipbhalle.metfusion.utilities.output;

import java.io.BufferedReader;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.doc.table.OdfTableColumn;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.element.office.OfficeSpreadsheetElement;
import org.odftoolkit.odfdom.dom.element.office.OfficeTextElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.dom.style.props.OdfParagraphProperties;
import org.odftoolkit.odfdom.dom.style.props.OdfTableColumnProperties;
import org.odftoolkit.odfdom.dom.style.props.OdfTableRowProperties;
import org.odftoolkit.odfdom.dom.style.props.OdfTextProperties;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberDateStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberTimeStyle;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeAutomaticStyles;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextHeading;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextParagraph;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextSpan;
import org.odftoolkit.odfdom.pkg.OdfFileDom;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ipbhalle.metfrag.fragmenter.Fragmenter;


public class ODFOutputHandler {

	private String inputFileName; 
	private Document inputDocument; 
	private XPath inputXPath;
	
	private String outputFileName;	
	private OdfSpreadsheetDocument outputDocument;
	
	private OdfFileDom contentDom; 							// the document object model for content.xml 
	private OdfFileDom stylesDom; 							// the document object model for styles.xml 
	private OdfOfficeAutomaticStyles contentAutoStyles;		// the office:automatic-styles element in content.xml 
	private OdfOfficeStyles stylesOfficeStyles; 			// the office:styles element in styles.xml 
	private OfficeSpreadsheetElement officeSpreadsheet;
	private OfficeTextElement officeText;					// the office:text element in the content.xml file 
	
	private String columnStyleName;
	private String rowStyleName;
	private String headingStyleName;
	private String noaaTimeStyleName; 
	private String noaaDateStyleName; 
	private String noaaTempStyleName;
	
	
	void setupOutputDocument() {
		try {
			outputDocument = OdfSpreadsheetDocument.newSpreadsheetDocument();
			contentDom = outputDocument.getContentDom();
			stylesDom = outputDocument.getStylesDom();
			contentAutoStyles = contentDom.getOrCreateAutomaticStyles();
			stylesOfficeStyles = outputDocument.getOrCreateDocumentStyles();
			officeSpreadsheet = outputDocument.getContentRoot();
		} catch (Exception e) {
			System.err.println("Unable to create output file.");
			System.err.println(e.getMessage());
			outputDocument = null;
		}
	}

	void addAutomaticStyles() { 
		OdfStyle style; 
		
		// Column style (all columns same width) 
		style = contentAutoStyles.newStyle(OdfStyleFamily.TableColumn); 
		columnStyleName = style.getStyleNameAttribute(); 
		style.setProperty(OdfTableColumnProperties.ColumnWidth, "2.5cm"); 
		
		// Row style 
		style = contentAutoStyles.newStyle(OdfStyleFamily.TableRow); 
		rowStyleName = style.getStyleNameAttribute(); 
		style.setProperty(OdfTableRowProperties.RowHeight, "0.5cm"); 
		
		// bold centered cells (for first row) 
		style = contentAutoStyles.newStyle(OdfStyleFamily.TableCell); 
		headingStyleName = style.getStyleNameAttribute(); 
		style.setProperty(OdfParagraphProperties.TextAlign, "center"); 
		setFontWeight(style, "bold");	
		
		// Create the date, time, and temperature styles and add them. 
		// The null in OdfNumberDateStyle means "use default calendar system" 
		OdfNumberDateStyle dateStyle = new OdfNumberDateStyle(contentDom, "yyyy-MM-dd", "numberDateStyle", null);
		OdfNumberTimeStyle timeStyle = new OdfNumberTimeStyle(contentDom, "hh:mm:ss", "numberTimeStyle"); 
		OdfNumberStyle numberStyle = new OdfNumberStyle(contentDom, "#0.00", "numberTemperatureStyle"); 
		
		contentAutoStyles.appendChild(dateStyle); 
		contentAutoStyles.appendChild(timeStyle); 
		contentAutoStyles.appendChild(numberStyle);
		
		// cell style for Date cells 
		style = contentAutoStyles.newStyle(OdfStyleFamily.TableCell); 
		noaaDateStyleName = style.getStyleNameAttribute(); 
		style.setStyleDataStyleNameAttribute("numberDateStyle"); 
		
		// and for time cells 
		style = contentAutoStyles.newStyle(OdfStyleFamily.TableCell); 
		noaaTimeStyleName = style.getStyleNameAttribute(); 
		style.setStyleDataStyleNameAttribute("numberTimeStyle"); 
		
		// and for the temperatures 
		style = contentAutoStyles.newStyle(OdfStyleFamily.TableCell); 
		noaaTempStyleName = style.getStyleNameAttribute(); 
		style.setStyleDataStyleNameAttribute("numberTemperatureStyle"); 
		style.setProperty(OdfParagraphProperties.TextAlign, "right");
	}
	
	void setFontWeight(OdfStyleBase style, String value)
	{
		style.setProperty(OdfTextProperties.FontWeight, value);
		style.setProperty(OdfTextProperties.FontWeightAsian, value);
		style.setProperty(OdfTextProperties.FontWeightComplex, value);
	}

	void setFontStyle(OdfStyleBase style, String value)
	{
		style.setProperty(OdfTextProperties.FontStyle, value);
		style.setProperty(OdfTextProperties.FontStyleAsian, value);
		style.setProperty(OdfTextProperties.FontStyleComplex, value);
	}

	void setFontSize(OdfStyleBase style, String value)
	{
		style.setProperty(OdfTextProperties.FontSize, value);
		style.setProperty(OdfTextProperties.FontSizeAsian, value);
		style.setProperty(OdfTextProperties.FontSizeComplex, value);
	}
	
	void cleanOutDocument() { 
		Node childNode; 
		childNode = officeText.getFirstChild(); 
		while (childNode != null) { 
			officeText.removeChild(childNode); 
			childNode = officeText.getFirstChild(); 
		} 
	}
	
	void processInputDocument() { 
		BufferedReader inReader; // for reading the file 
		String data; // holds one line of the file 
		String[] info; // holds the split-up data 
		
		OdfTable table;		
		OdfTableRow row; 
		OdfTableColumn column; 
		OdfTableCell cell; 
		
		table = OdfTable.newTable(outputDocument);	// contentDom
//		column = table.addStyledTableColumn(columnStyleName); 
//		column = table.addStyledTableColumn(columnStyleName); 
//		column = table.addStyledTableColumn(columnStyleName);
//		
//		// fill in the header row 
//		row = new OdfTableRow(contentDom); 
//		row.setTableStyleNameAttribute(rowStyleName); 
//		
//		row.appendCell(createCell(headingStyleName, "Date")); 
//		row.appendCell(createCell(headingStyleName, "Time")); 
//		row.appendCell(createCell(headingStyleName, "\u00b0C")); 
//		
//		table.appendRow(row);
	}
	
	void saveOutputDocument()
	{
		try
		{
			outputDocument.save(outputFileName);
		}
		catch (Exception e)
		{
			System.err.println("Unable to save document.");
			System.err.println(e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		URL url = Fragmenter.class.getClassLoader().getResource("bondenergies.txt");
		String file = url.getFile();
		System.out.println(file);
		System.exit(0);
		
		ODFOutputHandler handler = new ODFOutputHandler();
		handler.outputFileName = "test.odt";
		handler.setupOutputDocument();
		
		OdfTable table = OdfTable.newTable(handler.outputDocument);
		table.setTableName("testTable");
		
		OdfTableRow row; 
		OdfTableColumn column; 
		OdfTableCell cell;
		
		row = table.getRowByIndex(0);
		column = table.getColumnByIndex(0);
		cell = table.getCellByPosition("A1");
		cell.setStringValue("test123");
		handler.saveOutputDocument();
	}
}
