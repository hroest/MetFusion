package de.ipbhalle.metfusion.web.controller.validation;

import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@ManagedBean
public class PeakValidatorBean implements Validator {

	public void validate(FacesContext context, UIComponent component, Object value)
			throws ValidatorException {
		
		String peaks = (String) value;
		boolean error = false;
		
		// allow 2 or 3 tab- or space-separated fields per line, each line ends with either newline or space
		Pattern pL1 = Pattern.compile("([0-9]+.?[0-9]*\\s*[0-9]+\\s*[0-9]+)+\\s*");	// korrekt, 3 fields
		String regexL1 = "([0-9]+.?[0-9]*\\s*[0-9]+\\s*[0-9]+\\s*)+\\s*";
		Pattern pL2 = Pattern.compile("([0-9]+.?[0-9]*\\s*[0-9]+)+\\s*");			// korrekt, 2 fields
		String regexL2 = "([0-9]+.?[0-9]*\\s*[0-9]+)+\\s*";
		
		// allow 2 or 3 comma-separated fields per line, each line ends with either newline or semicolon
		Pattern pL3 = Pattern.compile("([0-9]+.?[0-9]*,\\s*[0-9]+,\\s*[0-9]+;\\s*)+");
		String regexL3 = "([0-9]+.?[0-9]*,\\s*[0-9]+,\\s*[0-9]+;\\s*)+";
		Pattern pL4 = Pattern.compile("([0-9]+.?[0-9]*,\\s*[0-9]+;\\s*)+");
		String regexL4 = "([0-9]+.?[0-9]*,\\s*[0-9]+;\\s*)+";
		
		// allow 2 or 3 space-separated fields per line, each line ends with either newline or semicolon
//		Pattern pL5 = Pattern.compile("([0-9]+.?[0-9]*,?\\s[0-9]*,?\\s[0-9]*[;\\s]?)+");
//		Pattern pL6 = Pattern.compile("([0-9]+.?[0-9]*,?\\s[0-9]*;\\s?)+");
		
		// look at whole input sequence, after that look at single entries to match current regex, fail if not
		if(pL1.matcher(peaks).lookingAt() || pL2.matcher(peaks).lookingAt()) {
			String[] split = peaks.split("\n");
			if(split.length > 0) {
				for (int i = 0; i < split.length; i++) {
					if(!split[i].matches(regexL1) && !split[i].matches(regexL2)) {
						error = true;
						break;
					}
				}
			}
		}
		
		if(pL3.matcher(peaks).lookingAt() || pL4.matcher(peaks).lookingAt()) {
			String[] split = peaks.split(";");
			if(split.length > 0) {
				error = false;
				for (int i = 0; i < split.length; i++) {
					if(!split[i].matches(regexL3) && !split[i].matches(regexL4)) {
						error = true;
						break;
					}
				}
			}
		}
		
		if(error) {
			FacesMessage message = new FacesMessage();
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			message.setSummary("Peaks are not valid.");
			message.setDetail("Peaks are not valid.");
			context.addMessage("completeForm:inputPeaks", message);
			throw new ValidatorException(message);
		}
		
//		if(!pL1.matcher(peaks).matches() && !pL2.matcher(peaks).matches() && !pL3.matcher(peaks).matches()
//				&& !pL4.matcher(peaks).matches() && !pL5.matcher(peaks).matches() && !pL6.matcher(peaks).matches()) {
//			
//			FacesMessage message = new FacesMessage();
//			message.setSeverity(FacesMessage.SEVERITY_ERROR);
//			message.setSummary("Peaks are not valid.");
//			message.setDetail("Peaks are not valid.");
//			context.addMessage("completeForm:inputPeaks", message);
//			throw new ValidatorException(message);
//		}
	}
}
