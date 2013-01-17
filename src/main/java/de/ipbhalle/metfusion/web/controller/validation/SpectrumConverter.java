/**
 * created by Michael Gerlich, Nov 17, 2010 - 1:38:29 PM
 */ 

package de.ipbhalle.metfusion.web.controller.validation;


import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;


@FacesConverter("de.ipbhalle.metfusion.web.controller.validation.SpectrumConverter")
public class SpectrumConverter implements Converter {

	private static final String linebreak = "\n";	//System.getProperty("line.separator");
	
	/** default intenstiy value used for spectrum seach */
	private static final int DEFAULT_INT = 500;
	/** default whitespace character to separate mz from intensity values */
	private static final String DEFAULT_WHITESPACE = " ";
	
	public SpectrumConverter() {	}
	
	private String normalizePeaks(String peaks) {
		double maxInt = 0d;
		String[] lines = peaks.split(linebreak);	// each m/z int pair one line
		boolean oneLine = lines.length == 1 ? true : false;
		StringBuilder sb = new StringBuilder();
		if (oneLine) { // only one line detected, maybe multiple whitespace separated peaks
			String[] temp = lines[0].trim().split(DEFAULT_WHITESPACE);
			if (temp.length % 2 == 0) {
				for (int i = 0; i < temp.length - 1; i += 2) { // skip each 2nd entry
					sb.append(temp[i]).append(DEFAULT_WHITESPACE).append(temp[i + 1]).append(linebreak);
				}
				lines = sb.toString().trim().split(linebreak);		// overwrite lines[] if spectrum information was in a single line
			} else {
				System.err.println("Probably invalid spectrum format!");
				return sb.toString().trim();
			}
		}
		
		String[] mz = new String[lines.length];
		String[] ints = new String[lines.length];
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].replaceAll("[ \t\\x0B\f\r]+", DEFAULT_WHITESPACE).trim();	// replace each white space by default one
			String[] split = lines[i].split(DEFAULT_WHITESPACE);
			if(split.length == 1) {		// only m/z, set relative intensity to 500
				mz[i] = split[0];
				ints[i] = "500";
			}
			else if(split.length == 2) {		// m/z intensity pair
				double intensity = Double.parseDouble(split[1]);
				if(intensity > maxInt)
					maxInt = intensity;
				mz[i] = split[0];
				ints[i] = split[1];
			}
			else if(split.length == 3) {// m/z intensity rel.intensity triple
				double intensity = Double.parseDouble(split[2]);
				if(intensity > maxInt)
					maxInt = intensity;
				mz[i] = split[0];
				ints[i] = split[2];
			}
			else {	// unknown split - assume length 3 split
				System.err.println("Unusual size of split!");
				mz[i] = split[0];
				ints[i] = split[2];
			}
		}
		sb = new StringBuilder();
		//calculate the rel. intensity
		for (int i = 0; i < lines.length; i++) {
			sb.append(mz[i]).append(DEFAULT_WHITESPACE).append(Math.round(((Double.valueOf(ints[i]) / maxInt) * 999))).append("\n");
		}
		
		return sb.toString();
	}
	/**
	 * converts the given string value into an Object.
	 */
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		String peaklist = value.toString().trim();
		peaklist = peaklist.replaceAll(",", ".");
		
		if(peaklist.contains(";"))
			peaklist = peaklist.replaceAll(";", linebreak);
		// one peak per line
		
		peaklist = peaklist.replaceAll("[ \t\\x0B\f\r]+", DEFAULT_WHITESPACE);
		return normalizePeaks(peaklist);
	}

	/**
	 * converts the given object into a String.
	 */
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if(value == null)
			return "";
		return value.toString();
	}

}
