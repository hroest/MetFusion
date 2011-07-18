/**
 * created by Michael Gerlich on May 25, 2010
 * last modified May 25, 2010 - 5:04:41 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.wrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.commons.math.linear.RealMatrix;


public class ColorcodedMatrix implements Runnable{
	
	private RealMatrix origMatrix;
	private ColorNode[][] colorMatrix;
	
	// implementing columns
	//private DataModel<String> columnDataModel;
	private DataModel<Result> columnDataModel;
    private DataModel<String> rowDataModel;
    private Map<Object, Object> cellMap = new HashMap<Object, Object>();
    
    private int columns = 4;
    private int rows = 6;
    
    private List<Result> primaries;
    private List<Result> candidates;
	//
	
	public ColorcodedMatrix(RealMatrix matrix) {
		this.origMatrix = matrix;
		this.colorMatrix = createColorMatrix(matrix);
		
		this.setColumns(matrix.getColumnDimension());
		this.setRows(matrix.getRowDimension());
		
		System.out.println("matrix columns -> " + matrix.getColumnDimension() + "\trows -> " + matrix.getRowDimension());
		generateDataModels();
	}
	
	public ColorcodedMatrix(RealMatrix matrix, List<Result> primaries, List<Result> candidates) {
		this.origMatrix = matrix;
		this.colorMatrix = createColorMatrix(matrix);
		
		this.setColumns(matrix.getColumnDimension());
		this.setRows(matrix.getRowDimension());
		
		this.setPrimaries(primaries);
		this.setCandidates(candidates);
		
		generateDataModels();
	}
	
	private ColorNode[][] createColorMatrix(RealMatrix matrix) {
		int rows = matrix.getRowDimension();
		int cols = matrix.getColumnDimension();
		
		/**
		 * TODO: check runtime!
		 */
		ColorNode[][] colMat = new ColorNode[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				colMat[i][j] = new ColorNode(matrix.getEntry(i, j));
			}
		}
		
		return colMat;
	}
	
	public boolean writeColorMatrix(File f) {
		
		return true;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	/**
     * Method called when the dataTable parameters have changed, in order to
     * generate a new rowDataModel and columnDataModel.
     */
    private void generateDataModels() {
    	System.out.println("start generating datamodel for ColorMatrix");
    	
        // Generate rowDataModel
        List<String> rowList = new ArrayList<String>();
        for (int i = 0; i < rows; i++) {
            rowList.add(String.valueOf(i));
        }
        if (rowDataModel == null) {
            rowDataModel = new ListDataModel<String>(rowList);
        } else {
            rowDataModel.setWrappedData(rowList);
        }
        rowDataModel = new ListDataModel<String>(rowList);

        // Generate columnDataModel
        List<Result> columnList = new ArrayList<Result>();
        for (int i = 0; i < columns; i++) {
        	// use compound names as column header
        	//columnList.add(primaries.get(i).getName());
        	
        	// use record ID as column header
        	//columnList.add(primaries.get(i).getId());
        	
        	// use record URL as column header
        	//columnList.add(primaries.get(i).getUrl());
        	
        	columnList.add(primaries.get(i));	// use Result for Column model -> allows access to fields like url and id
        }
        if (columnDataModel == null) {
            //columnDataModel = new ListDataModel<String>(columnList);
        	columnDataModel = new ListDataModel<Result>(columnList);
        } else {
            columnDataModel.setWrappedData(columnList);
        }
        
        System.out.println("finished generating datamodel for ColorMatrix");
    }
    
    /**
     * Called from the ice:dataTable.  This method uses the columnDataModel and
     * rowDataModel with the CellKey utility class to display the correct cell
     * value.
     *
     * @return data which should be displayed for the given model state.
     */   
    public ColorNode getCellValue() {
    	if (rowDataModel.isRowAvailable() && columnDataModel.isRowAvailable()) {
    		// get the index of the row and column for this cell
            String row = (String) rowDataModel.getRowData();
            int currentRow = Integer.parseInt(row);
            Object column = columnDataModel.getRowData();
            int currentColumn = ((ArrayList) columnDataModel.getWrappedData()).indexOf(column);
            // return the element at this location
            Object key = new CellKey(row, column);
            if (!cellMap.containsKey(key)) {
                cellMap.put(key, colorMatrix[currentRow][currentColumn]);
            }
            return (ColorNode) cellMap.get(key);
    	}
    	return null;
    }
    
	public void setColorMatrix(ColorNode[][] colorMatrix) {
		this.colorMatrix = colorMatrix;
	}

	public ColorNode[][] getColorMatrix() {
		return colorMatrix;
	}

	public RealMatrix getOrigMatrix() {
		return origMatrix;
	}

	public void setOrigMatrix(RealMatrix origMatrix) {
		this.origMatrix = origMatrix;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getRows() {
		return rows;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public int getColumns() {
		return columns;
	}

	public void setCellMap(Map cellMap) {
		this.cellMap = cellMap;
	}

	public Map getCellMap() {
		return cellMap;
	}

	public DataModel getColumnDataModel() {
		return columnDataModel;
	}

	public DataModel getRowDataModel() {
		return rowDataModel;
	}

	public void setColumnDataModel(DataModel columnDataModel) {
		this.columnDataModel = columnDataModel;
	}

	public void setRowDataModel(DataModel rowDataModel) {
		this.rowDataModel = rowDataModel;
	}

	public void setPrimaries(List<Result> primaries) {
		this.primaries = primaries;
	}

	public List<Result> getPrimaries() {
		return primaries;
	}

	public void setCandidates(List<Result> candidates) {
		this.candidates = candidates;
	}

	public List<Result> getCandidates() {
		return candidates;
	}

}
