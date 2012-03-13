/**
 * created by Michael Gerlich, Oct 18, 2011 - 12:40:16 PM
 */ 

package de.ipbhalle.metfusion.web.controller.validation;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * Validator class for file uploads.
 * 
 * @author mgerlich
 *
 */
@FacesValidator("de.ipbhalle.metfusion.web.controller.validation.UploadValidator")
public class UploadValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component, Object value)
			throws ValidatorException {
		System.out.println("value -> " + value.getClass());
		
		FacesMessage msg = new FacesMessage("No valid SDF file.", 
		"Please specify a valid SDF file for upload.");
		msg.setSeverity(FacesMessage.SEVERITY_ERROR);
		throw new ValidatorException(msg);
	}

}
