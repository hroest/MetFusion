/**
 * created by Michael Gerlich, Nov 18, 2010 - 4:01:26 PM
 */ 

package de.ipbhalle.metfusion.web.controller.validation;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import de.ipbhalle.MassBank.MassBankLookupBean;

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
@FacesValidator("de.ipbhalle.metfusion.web.controller.validation.InstrumentValidator")
public class InstrumentValidator implements Validator {

	public InstrumentValidator() {
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		System.out.println("Instrumentvalidator");
		
		List<String> instruments = new ArrayList<String>();
		Object obj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(MassBankLookupBean.getSessionmapkeyinstruments());
		if(obj != null && (obj instanceof List)) {
			/**
			 * this cast is unchecked
			 */
			List<String> old = (List<String>) obj;
			for (String s : old) {
				System.out.println("old -> " + s);
				instruments.add(s);
			}
		}
		
		FacesMessage msg = new FacesMessage("No instruments selected.", 
		"At least one instrument is required.");
		msg.setSeverity(FacesMessage.SEVERITY_ERROR);
		
		if(value == null)
			//throw new ConverterException(msg);
			throw new ValidatorException(msg);
		
		if(value instanceof String[]) {		// value of MassBankLookupBean.selectedInstruments
			String[] temp = (String[]) value;
			for (int i = 0; i < temp.length; i++) {
				System.out.println("temp["+i+"] -> " + temp[i]);
				instruments.add(temp[i]);
			}
			System.out.println("temp# -> " + temp.length);
			//System.out.println("temp == null -> " + temp == null);
			if(temp.length == 0)
				throw new ValidatorException(msg);
		}
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MassBankLookupBean.getSessionmapkeyinstruments(), instruments);
		for (String s : instruments) {
			System.out.println("new -> " + s);
		}
	}
}
