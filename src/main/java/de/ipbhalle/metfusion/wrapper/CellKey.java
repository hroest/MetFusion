/**
 * created by Michael Gerlich on May 31, 2010
 * last modified May 31, 2010 - 1:32:07 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.wrapper;

/**
 * Utility class used to keep track of the cells in a table.
 */
public class CellKey {
	private final Object row;
    private final Object column;

    /**
     * @param row
     * @param column
     */
    public CellKey(Object row, Object column) {
        this.row = row;
        this.column = column;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof CellKey) {
            CellKey other = (CellKey) obj;
            return other.row.equals(row) && other.column.equals(column);
        }
        return super.equals(obj);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (12345 + row.hashCode()) * (67890 + column.hashCode());
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return row.toString() + "," + column.toString();
    }

}
