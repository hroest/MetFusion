/**
 * created by Michael Gerlich on May 25, 2010
 * last modified May 25, 2010 - 5:05:05 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.wrapper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class ColorNode {

	private double value;
	private double valueShort;
	private String color;
	
	private static String[] gradient = {"#FF0000", "#FF1900", "#FF3300", "#FF4C00", "#FF6600", "#FF7F00", "#FF9900", "#FFB200", "#FFCC00", "#FFE500",
		"#FFFF00", "#E7FF00", "#D0FF00", "#B9FF00", "#A2FF00", "#8BFF00", "#73FF00", "#5CFF00", "#45FF00", "#2EFF00", "#17FF00", "#00FF00"};
	
	public ColorNode(double value) {
		this.value = value;
		this.valueShort = roundThreeDecimals(value);
		getColor(value);
	}
	
	public ColorNode(double value, String color) {
		this.value = value;
		this.color = color;
	}
	
	private double roundThreeDecimals(double d) {
//		DecimalFormat threeDForm = new DecimalFormat("#.###");
//		return Double.valueOf(threeDForm.format(d));
		NumberFormat f = NumberFormat.getInstance(Locale.ENGLISH);
		if (f instanceof DecimalFormat) {
			((DecimalFormat) f).applyPattern("#.###");
			return Double.valueOf(((DecimalFormat) f).format(d));
		}
		
		DecimalFormat threeDForm = new DecimalFormat("#.###");
		try {
			Double newD = Double.valueOf(threeDForm.format(d));
			return newD;
		} catch(NumberFormatException e) {
			return d;
		}
	}
	
	private void getColor(double value) {
		if(value == 0)
			this.color = gradient[0];
		else if(value > 0.0 && value <= 0.05)
			this.color = gradient[1];
		else if(value > 0.05 && value <= 0.1)
			this.color = gradient[2];
		else if(value > 0.1 && value <= 0.15)
			this.color = gradient[3];
		else if(value > 0.15 && value <= 0.2)
			this.color = gradient[4];
		else if(value > 0.2 && value <= 0.25)
			this.color = gradient[5];
		else if(value > 0.25 && value <= 0.3)
			this.color = gradient[6];
		else if(value > 0.3 && value <= 0.35)
			this.color = gradient[7];
		else if(value > 0.35 && value <= 0.4)
			this.color = gradient[8];
		else if(value > 0.4 && value <= 0.45)
			this.color = gradient[9];
		else if(value > 0.45 && value <= 0.5)
			this.color = gradient[10];
		else if(value > 0.5 && value <= 0.55)
			this.color = gradient[11];
		else if(value > 0.55 && value <= 0.6)
			this.color = gradient[12];
		else if(value > 0.6 && value <= 0.65)
			this.color = gradient[13];
		else if(value > 0.65 && value <= 0.7)
			this.color = gradient[14];
		else if(value > 0.7 && value <= 0.75)
			this.color = gradient[15];
		else if(value > 0.75 && value <= 0.8)
			this.color = gradient[16];
		else if(value > 0.8 && value <= 0.85)
			this.color = gradient[17];
		else if(value > 0.85 && value <= 0.9)
			this.color = gradient[18];
		else if(value > 0.9 && value <= 0.95)
			this.color = gradient[19];
		else if(value > 0.95 && value < 1)
			this.color = gradient[20];
		else if(value == 1.0 )
			this.color = gradient[21];
	}
	
	public void setValue(double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {
		return color;
	}

	public void setValueShort(double valueShort) {
		this.valueShort = valueShort;
	}

	public double getValueShort() {
		return valueShort;
	}
}
