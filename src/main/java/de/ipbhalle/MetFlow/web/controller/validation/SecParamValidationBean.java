package de.ipbhalle.MetFlow.web.controller.validation;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class SecParamValidationBean implements Validator{

	public SecParamValidationBean() {
		// TODO Auto-generated constructor stub
	}
	
	public void validate(FacesContext arg0, UIComponent arg1, Object arg2)
			throws ValidatorException {
		// TODO Auto-generated method stub
		System.out.println("validation method called!");
		if(arg2 instanceof java.lang.Boolean) {
			boolean content = (java.lang.Boolean) arg2;
			System.out.println("content = " + content);
		}
		if(arg2 instanceof String) {
			String content = (String) arg2;
			System.out.println("content = " + content);
		}	
	}

}
