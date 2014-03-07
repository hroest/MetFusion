/**
 * created by Michael Gerlich, Mar 7, 2014 - 10:11:16 AM

 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package de.ipbhalle.metfusion.web.controller.validation;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.icesoft.faces.context.effects.JavascriptContext;

@FacesValidator("de.ipbhalle.metfusion.web.controller.validation.NumberValidator")
public class NumberValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		// value should only be cast to either Integer or Double
		if(value instanceof Integer)
			;
		else if(value instanceof Double) {
			Double d = (Double) value;
			if(d.isNaN())
				catchError(context);
			else if(d.isInfinite())
				catchError(context);
			else if(d < 0)
				catchError(context);
		}
		else {
			catchError(context);
		}
	}

	private void catchError(FacesContext context) {
		FacesMessage msg = new FacesMessage("Number validation failed.", "Invalid number entered.");
		JavascriptContext.addJavascriptCall(context, "document.getElementById(\"command\").firstChild.disabled = true;");
		msg.setSeverity(FacesMessage.SEVERITY_ERROR);
		throw new ValidatorException(msg);
	}
}
