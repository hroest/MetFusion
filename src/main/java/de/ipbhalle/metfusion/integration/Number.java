/**
 * created by Michael Gerlich on Apr 8, 2010
 * last modified Apr 8, 2010 - 2:49:56 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.integration;

public class Number implements Comparable<Number> {

	private int rank;
    private double number;

    
    public Number(double number) {
    	this(number, -1);
	}
    
    public Number(double number, int rank){
        this.number = number;
        this.rank = rank;
    }
    
    @Override
	public int compareTo(Number o) {
    	Number n = (Number)o;
        if(rank < n.getRank()){
            return 1;
        }else if(rank > n.getRank()){
            return -1;
        }else{
            return 0;
        }
	}

	@Override
	public String toString() {
		return this.number + ",   " + this.rank;
	}

	public int getRank() {
		return rank;
	}

	public double getNumber() {
		return number;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public void setNumber(double number) {
		this.number = number;
	}
}
