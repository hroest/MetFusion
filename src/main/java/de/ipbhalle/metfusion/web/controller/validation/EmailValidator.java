/**
 * created by Michael Gerlich, Nov 17, 2010 - 10:58:41 AM
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

@FacesValidator("de.ipbhalle.metfusion.web.controller.validation.EmailValidator")
public class EmailValidator implements Validator{
 
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\." +
			"[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*" +
			"(\\.[A-Za-z]{2,})$";
 
	private Pattern pattern;
	private Matcher matcher;
 
	public EmailValidator(){
		  pattern = Pattern.compile(EMAIL_PATTERN);
	}
 
	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
 
		if(value.toString().isEmpty())	// skip validation if no email provided
			return;
		
		matcher = pattern.matcher(value.toString());
		if(!matcher.matches()){
 
			FacesMessage msg = 
				new FacesMessage("E-mail validation failed.", 
						"Invalid E-mail format.");
			msg.setSeverity(FacesMessage.SEVERITY_INFO);
			throw new ValidatorException(msg);
 
		}
		//else context.renderResponse();
	}
}
