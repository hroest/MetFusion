/**
 * created by Michael Gerlich, Nov 17, 2010 - 1:38:29 PM
 */ 

package de.ipbhalle.metfusion.web.controller.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import de.ipbhalle.metfusion.web.controller.MetFusionBean;

@FacesConverter("de.ipbhalle.MetFlow.web.controller.validation.SpectrumConverter")
public class SpectrumConverter implements Converter {

	private static final String linebreak = System.getProperty("line.separator");
	
	// the possible peaklist formats
	/** mz only, nominal or exact */
	private static final String PEAKLIST_PATTERN_MZ_ONLY = "([0-9]+([\\.|,][0-9]+)?(\n|\r)?)+"; 
	//"([0-9]+([\\.|,][0-9]+)?(\\s)?)+";
	
	/** mz and int */
	private static final String PEAKLIST_PATTERN_MZ_INT = "(\\d+\\.?\\d+\\s*?)+"; 
		//"([0-9]+([\\.|,][0-9]+)?(\\s)+[0-9]+([\\.|,][0-9]+)?(\n|\r)?)+";	
	//"([0-9]+([\\.|,][0-9]+)?[0-9]+([\\.|,][0-9]+)?(\\s)?)+";
	
	/** mz, intensity and rel.intensity */
	private static final String PEAKLIST_PATTERN_MZ_REL_INT = 
		"([0-9]+([\\.|,][0-9]+)?(\\s)+[0-9]+([\\.|,][0-9]+)?(\\s)+[0-9]+([\\.|,][0-9]+)?(\n|\r)?)+";
	
	/** unseparated list of int-values for mz and int */
	private static final String PEAKLIST_PATTERN_GOLM = "([0-9]+(\\.[0-9]+)?\\s[0-9]+(\\s)?)+";
	
	// ([0-9]+([\.|,][0-9]+)?(\s[0-9]+([\.|,][0-9]+)?)?(\s)?)+
	
	// the regex patterns used for each possible format
	private Pattern pattern_MZ_ONLY;
	private Pattern pattern_MZ_INT;
	private Pattern pattern_MZ_REL_INT;
	private Pattern pattern_GOLM;
	
	// the matcher objects used for each pattern
	private Matcher matcher_MZ_ONLY;
	private Matcher matcher_MZ_INT;
	private Matcher matcher_MZ_REL_INT;
	private Matcher matcher_GOLM;
	
	/** default intenstiy value used for spectrum seach */
	private static final int DEFAULT_INT = 500;
	/** default whitespace character to separate mz from intensity values */
	private static final String DEFAULT_WHITESPACE = " ";
	
	public SpectrumConverter() {
		pattern_MZ_ONLY = Pattern.compile(PEAKLIST_PATTERN_MZ_ONLY);
		pattern_MZ_INT = Pattern.compile(PEAKLIST_PATTERN_MZ_INT);
		pattern_MZ_REL_INT = Pattern.compile(PEAKLIST_PATTERN_MZ_REL_INT);
		pattern_GOLM = Pattern.compile(PEAKLIST_PATTERN_GOLM);
	}
	
	/**
	 * converts the given string value into an Object.
	 */
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		String peaklist = value.toString().trim();
		peaklist = peaklist.replaceAll(",", ".");
		
		if(peaklist.contains(";"))
			peaklist = peaklist.replaceAll(";", "\n");
		// one peak per line
		
		//System.out.println("peaklist -> \n" + peaklist);
		//StringBuilder sb = new StringBuilder();
		peaklist = peaklist.replaceAll("[ \t\\x0B\f\r]+", DEFAULT_WHITESPACE);
		
		return peaklist;
	}
	
	/**
	 * converts the given string value into an Object.
	 */
//	@Override
//	public Object getAsObject(FacesContext context, UIComponent component, String value) {
//		String peaklist = value.toString().trim();
//		peaklist = peaklist.replaceAll(",", ".");
//		System.out.println("peaklist -> \n" + peaklist);
//		
//		// initialize matchers
//		matcher_MZ_ONLY = pattern_MZ_ONLY.matcher(peaklist);
//		matcher_MZ_INT = pattern_MZ_INT.matcher(peaklist);
//		matcher_MZ_REL_INT = pattern_MZ_REL_INT.matcher(peaklist);
//		matcher_GOLM = pattern_GOLM.matcher(peaklist);
//		
//		StringBuilder sb = new StringBuilder();
//		if(matcher_MZ_ONLY.matches()) {
//			// add a default intensity value to each mz value
//			String[] split = peaklist.split(linebreak);
//			for (int i = 0; i < split.length; i++) {
//				sb.append(split[i].trim()).append(DEFAULT_WHITESPACE).append(DEFAULT_INT).append(linebreak);
//			}
//			System.out.println("matcher mz only -> \n" + sb.toString());
//			//return sb.toString();
//		}
//		else if(matcher_MZ_INT.matches()) {
//			// reduce whitespace between mz and int to the default whitespace
//			peaklist = peaklist.trim();
//			String[] split = peaklist.split(linebreak);
//			for (int i = 0; i < split.length; i++) {
//				//System.out.println("split["+i+"] = " + split[i]);
//				//System.out.println(split[i].replaceAll("\\s+", DEFAULT_WHITESPACE));
//				split[i] = split[i].replaceAll("\\s+", DEFAULT_WHITESPACE);
//				//sb.append(split[i].replaceAll("\\s+", DEFAULT_WHITESPACE)).append(linebreak);
//				String[] line = split[i].split(DEFAULT_WHITESPACE);
//				if(line.length == 2) {
//					if(line[1].contains("."))	// check if intensity has decimal place
//						line[1] = line[1].substring(0, line[1].indexOf("."));
//					//System.out.println("line -> " + line[0] + "  " + line[1]);
//				}
//				sb.append(line[0]).append(DEFAULT_WHITESPACE).append(line[1]).append(linebreak);
//			}
//			System.out.println("matcher_mz_int");
//		}
//		else if(matcher_MZ_REL_INT.matches()) {
//			peaklist = peaklist.trim();
//			String[] split = peaklist.split(linebreak);
//			for (int i = 0; i < split.length; i++) {
//				System.out.println("split["+i+"] = " + split[i]);
//				System.out.println(split[i].replaceAll("\\s+", DEFAULT_WHITESPACE));
//				split[i] = split[i].replaceAll("\\s+", DEFAULT_WHITESPACE);
//				String[] line = split[i].split(DEFAULT_WHITESPACE);
//				if(line.length == 3) {
//					if(line[2].contains("."))	// check if intensity has decimal place
//						line[2] = line[1].substring(0, line[2].indexOf("."));
//					System.out.println("line -> " + line[0] + "  " + line[1] + "  " + line[2]);
//				}
//				sb.append(line[0]).append(DEFAULT_WHITESPACE).append(line[2]).append(linebreak);
//			}
//			System.out.println("matcher_mz_rel_int");
//		}
//		
//		// if no pattern matches, throw error message
//		if(!matcher_MZ_ONLY.matches() && !matcher_MZ_INT.matches() && !matcher_MZ_REL_INT.matches() && !matcher_GOLM.matches()) {
//			FacesMessage msg = new FacesMessage("Spectrum Conversion error.", 
//						"Invalid Spectrum format.");
//			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
//			throw new ConverterException(msg);
//
//		}
//		
////		FacesContext fc = FacesContext.getCurrentInstance();
////		ELResolver el = fc.getApplication().getELResolver();
////        ELContext elc = fc.getELContext();
////		MetFusionBean app = (MetFusionBean) el.getValue(elc, null, "appBean");
////		app.setInputSpectrum(sb.toString());
//		
//		return sb.toString();
//	}

	/**
	 * converts the given object into a String.
	 */
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if(value == null)
			return "";
		//System.out.println("getAsString -> " + value.toString());
		return value.toString();
	}

}
