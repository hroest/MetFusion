/**
 * created by Michael Gerlich, Nov 17, 2010 - 10:57:30 AM
 */ 

package de.ipbhalle.metfusion.web.controller.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("de.ipbhalle.MetFlow.web.controller.validation.SpectrumValidator")
public class SpectrumValidator implements Validator {

	private static final String PEAKLIST_PATTERN_2COL = "([0-9]+(\\.[0-9]+)?\\s[0-9]+(\\s)?)+"; 
		//"([0-9]+(\\.{1}[0-9]+)?\\s[0-9]+\\s)+";	//"([0-9]+.?[0-9]*\\s*[0-9]+\\s*[0-9]+)+\\s*";
	private static final String PEAKLIST_PATTERN_3COL = "([0-9]+(\\.[0-9]+)?\\s[0-9]+\\s[0-9]+)+";
	
	private Pattern pattern_2col;
	private Pattern pattern_3col;
	private Matcher matcher;
	private Matcher matcher2;
	
	/** default whitespace character to separate mz from intensity values */
	private static final String DEFAULT_WHITESPACE = " ";
	
	public SpectrumValidator() {
		//pattern_2col = Pattern.compile(PEAKLIST_PATTERN_2COL);
		//pattern_3col = Pattern.compile(PEAKLIST_PATTERN_3COL);
	}
	
	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		String spectrum = value.toString().trim();
		System.out.println("SpectrumValidator -> validating \n" + spectrum);
		//matcher = pattern_3col.matcher(spectrum);
		//matcher2 = pattern_2col.matcher(spectrum);
		
		//if(!matcher.matches() && !matcher2.matches()) {
		//	System.out.println("matcher -> " + matcher.matches() + "  matcher2 -> " + matcher2.matches());
			
		spectrum = spectrum.replaceAll(",", ".");
		spectrum = spectrum.replaceAll("[ \t\\x0B\f\r]+", DEFAULT_WHITESPACE);
		
		Matcher m = Pattern.compile("[a-zA-Z]+").matcher(spectrum);	// [0-9\\.,]+		[a-zA-Z]+
		if(m.find()){
			System.err.println("spectrum doesnt match!");
			//FacesContext fc = FacesContext.getCurrentInstance();
			
			FacesMessage msg = 
				new FacesMessage("Spectrum validation failed.", 
						"Invalid Spectrum format.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			//fc.addMessage("inputForm:spec", msg);
			throw new ValidatorException(msg);
 
		}
		System.out.println("spectrum valid!");
	}

}
