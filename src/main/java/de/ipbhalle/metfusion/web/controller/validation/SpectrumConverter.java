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
		
		StringBuilder sb = new StringBuilder();
		String[] split = peaklist.split(linebreak);
		boolean oneLine = split.length == 1 ? true : false;
		if(oneLine) {	// only one line detected, maybe multiple whitespace separated peaks
			String[] temp = split[0].trim().split(DEFAULT_WHITESPACE);
			if(temp.length % 2 == 0) {
				for (int i = 0; i < temp.length-1; i += 2) {	// skip each 2nd entry
					sb.append(temp[i]).append(DEFAULT_WHITESPACE).append(temp[i+1]).append(linebreak);
				}
			}
			else System.err.println("Probably invalid spectrum format!");
		}
		else {	// more than one line with peaks
			for (int i = 0; i < split.length; i++) {
				split[i] = split[i].trim();
				String[] temp = split[i].split(DEFAULT_WHITESPACE);
				if(temp.length == 1) {	// only mz information -> append default intensity
					sb.append(temp[0]).append(DEFAULT_WHITESPACE).append(DEFAULT_INT).append(linebreak);
				}
				else if(temp.length == 3) {	// mz abs.int. and rel.int information -> remove absolute intensity
					sb.append(temp[0]).append(DEFAULT_WHITESPACE).append(temp[2]).append(linebreak);
				}
				else sb.append(split[i]).append(linebreak);
			}
		}
			
		String s = sb.toString().trim();
		return s;
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
