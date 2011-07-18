/**
 * created by Michael Gerlich, Nov 18, 2010 - 4:01:26 PM
 */ 

package de.ipbhalle.metfusion.web.controller.validation;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * Validator class for selected instruments in JSF page.
 * Checks for selected instruments passed by a String[].
 * If the corresponding component in JSF has the attribute
 * <b>required</b> set to true, this validator is not called
 * if no instrument is selected at all. In this case, the 
 * <b>required</b> attribute precedes this Validator and
 * a JSF standard error message is displayed.
 * 
 * @author mgerlich
 *
 */
@FacesValidator("de.ipbhalle.MetFlow.web.controller.validation.InstrumentValidator")
public class InstrumentValidator implements Validator {

	public InstrumentValidator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		System.out.println("Instrumentvalidator");
		FacesMessage msg = new FacesMessage("No instruments selected.", 
		"At least one instrument is required.");
		msg.setSeverity(FacesMessage.SEVERITY_ERROR);
		
		if(value == null)
			throw new ConverterException(msg);
		
		if(value instanceof String[]) {		// value of MassBankLookupBean.selectedInstruments
			String[] temp = (String[]) value;
			for (int i = 0; i < temp.length; i++) {
				System.out.println("temp["+i+"] -> " + temp[i]);
			}
			System.out.println("temp# -> " + temp.length);
			//System.out.println("temp == null -> " + temp == null);
			if(temp.length == 0)
				throw new ValidatorException(msg);
		}
	}
}
