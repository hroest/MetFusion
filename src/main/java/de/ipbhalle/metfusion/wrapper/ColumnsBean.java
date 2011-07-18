/**
 * created by Michael Gerlich on May 31, 2010
 * last modified May 31, 2010 - 1:33:07 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

@ManagedBean
public class ColumnsBean {
	
	private DataModel columnDataModel;
    private DataModel rowDataModel;
    private Map cellMap = new HashMap();

    public static final int NUMBER_OF_ITEMS = 24;
    // Starting point to generate the alphabet
    public static final int ASCII_STARTING_POINT = 64;
    // default column and row values
    private int columns = 4;
    // possible columns values selected from ice:selectOneMenu.
    private SelectItem[] columnsItems = new SelectItem[]{new SelectItem(new Integer(3)),
            new SelectItem(new Integer(4)),
            new SelectItem(new Integer(6)),
            new SelectItem(new Integer(8))};
    private int rows = 6;
    private int intValue = 0;

    public ColumnsBean() {
        generateDataModels();
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public DataModel getRowDataModel() {
        return rowDataModel;
    }

    public DataModel getColumnDataModel() {
        return columnDataModel;
    }

    public SelectItem[] getColumnsItems() {
        return columnsItems;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public void setColumnsItems(SelectItem[] columnsItems) {
        this.columnsItems = columnsItems;
    }

    /**
     * Called from the ice:dataTable.  This method uses the columnDataModel and
     * rowDataModel with the CellKey utility class to display the correct cell
     * value.
     *
     * @return data which should be displayed for the given model state.
     */
    public String getCellValue() {
        if (rowDataModel.isRowAvailable() && columnDataModel.isRowAvailable()) {
            // get the index of the row and column for this cell
            String row = (String) rowDataModel.getRowData();
            int currentRow = Integer.parseInt(row);
            Object column = columnDataModel.getRowData();
            int currentColumn = ((ArrayList) columnDataModel.getWrappedData()).indexOf(column);
            // return the element at this location
            Object key = new CellKey(row, column);
            if (!cellMap.containsKey(key)) {
                cellMap.put(key, getCharacter(currentRow, currentColumn));
            }
            return (String) cellMap.get(key);
        }
        return null;
    }

    /**
     * Retrieves a character from the alphabet for display in the UI.
     *
     * @param currentRow  The currentRow  index required to
     *                retrieve the proper letter for a particular table cell.
     * @param currentColumn  The currentColumn index required to
     *                retrieve the proper letter for a particular table cell.
     *
     * @return character at the specified cell. 
     */
    private String getCharacter(int currentRow, int currentColumn) {
        String toReturn = "";
        intValue = (currentRow + (currentColumn * rows));
        if (intValue <= NUMBER_OF_ITEMS) {
            toReturn = getCharacter(intValue);
        }
        return toReturn;
    }

    /**
     * Retrieves a character from the alphabet.
     *
     * @param characterInt The index of the character in the alphabet.
     */
    private String getCharacter(int characterInt) {
        characterInt += ASCII_STARTING_POINT;
        return String.valueOf((char) characterInt);
    }

    /**
     * Updates the table model data.
     *
     * @param event Event fired from the ice:selectOneMenu component which
     *              specifies whether the column count has changed.
     */
    public void updateTableColumns(ValueChangeEvent event) {
        if (event != null && event.getNewValue() != null &&
                event.getNewValue() instanceof Integer) {
            columns = ((Integer) event.getNewValue()).intValue();
        }
        intValue = 0;
        cellMap.clear();
        generateDataModels();
    }

    /**
     * Method called when the dataTable parameters have changed, in order to
     * generate a new rowDataModel and columnDataModel.
     */
    private void generateDataModels() {
        // Generate rowDataModel
        List rowList = new ArrayList();
        calculateRows();
        for (int i = 1; i <= rows; i++) {
            rowList.add(String.valueOf(i));
        }
        if (rowDataModel == null) {
            rowDataModel = new ListDataModel(rowList);
        } else {
            rowDataModel.setWrappedData(rowList);
        }
        rowDataModel = new ListDataModel(rowList);

        // Generate columnDataModel
        List columnList = new ArrayList();
        boolean reset = true;
        StringBuffer headerString = new StringBuffer();
        for (int i = 1; i <= NUMBER_OF_ITEMS; i++) {
            if (reset) {
                headerString.append(getCharacter(i));
                headerString.append(" to ");
                reset = false;
            }
            if (i % rows == 0 || i == NUMBER_OF_ITEMS) {
                headerString.append(getCharacter(i));
                columnList.add(headerString.toString());
                headerString = new StringBuffer();
                reset = true;
            }
        }
        if (columnDataModel == null) {
            columnDataModel = new ListDataModel(columnList);
        } else {
            columnDataModel.setWrappedData(columnList);
        }
    }

    /**
     * Utility method that calculates the number of rows required for a specified
     * number of columns.
     */
    private void calculateRows() {
        // calculate the number of rows.
        rows = NUMBER_OF_ITEMS / columns;
        // make an extra row if there is a modulus
        if ((NUMBER_OF_ITEMS % columns) != 0) {
            rows += 1;
        }
    }
}
