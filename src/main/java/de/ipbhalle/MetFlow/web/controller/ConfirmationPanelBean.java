package de.ipbhalle.MetFlow.web.controller;

import java.io.Serializable;

import org.icefaces.application.showcase.util.MessageBundleLoader;

import javax.faces.event.ActionEvent;

public class ConfirmationPanelBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private String dataIn = MessageBundleLoader.getMessage("page.panelConfirmation.dataIn");
//    private String dataOut = MessageBundleLoader.getMessage("page.panelConfirmation.dataOut");
	private String dataIn = "dataIn";
	private String dataOut = "dataOut";
	
    private boolean withConfirmation = true;

    public void save(ActionEvent event) {
        dataOut = dataIn;
    }

    public void delete(ActionEvent event) {
        dataOut = null;
    }

    public String getDataOut() {
        return dataOut;
    }

    public String getDataIn() {
        return dataIn;
    }

    public void setDataIn(String dataIn) {
        this.dataIn = dataIn;
    }

    public boolean isWithConfirmation() {
        return withConfirmation;
    }

    public void setWithConfirmation(boolean withConfirmation) {
        this.withConfirmation = withConfirmation;
    }
}
